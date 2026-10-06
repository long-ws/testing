package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import beans.common.Gender;
import beans.common.Language;
import beans.user.UserAccount;
import beans.user.UserEmail;
import beans.user.UserLocalAuth;
import beans.user.UserOAuthAuth;
import beans.user.UserProfile;
import constants.RequestParamConstants;
import constants.system.SystemKeys;
import dao.common.GenderDAO;
import dao.common.GenderDAOImpl;
import dao.common.LanguageDAO;
import dao.common.LanguageDAOImpl;
import domain.user.UserDefaults;
import domain.user.UserIds;
import dto.user.LocalUserRegistrationRequest;
import dto.user.OAuthUserRegistrationRequest;
import dto.user.UserProfileRequest;
import exception.BusinessException;
import mapper.user.UserAccountMapper;
import mapper.user.UserEmailMapper;
import mapper.user.UserLocalAuthMapper;
import mapper.user.UserOAuthAuthMapper;
import mapper.user.UserProfileMapper;
import policy.security.AuthenticationPolicy;
import repository.UserRepository;
import repository.UserRepositoryImpl;
import utils.BCryptPasswordEncoder;
import utils.DbTransaction;
import utils.PasswordEncoder;
import utils.TransactionCallback;

public class UserRegistrationServiceImpl implements UserRegistrationService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserAccountMapper userAccountMapper;
	private final UserEmailMapper userEmailMapper;
	private final UserLocalAuthMapper userLocalAuthMapper;
	private final UserOAuthAuthMapper userOAuthAuthMapper;
	private final UserProfileMapper userProfileMapper;
	private final GenderDAO genderDAO;
	private final LanguageDAO languageDAO;

	private static final String ERR_DB_REGISTRATION = "Hệ thống không thể thực hiện đăng ký tài khoản lúc này. Vui lòng thử lại sau.";
	private static final String ERR_DB_OAUTH = "Kết nối xác thực tài khoản liên kết thất bại. Vui lòng thử lại.";

	public UserRegistrationServiceImpl() {
		this(
				new UserRepositoryImpl(),
				new BCryptPasswordEncoder(),
				new UserAccountMapper(),
				new UserEmailMapper(),
				new UserLocalAuthMapper(),
				new UserOAuthAuthMapper(),
				new UserProfileMapper(),
				new GenderDAOImpl(),
				new LanguageDAOImpl());
	}

	public UserRegistrationServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
			UserAccountMapper userAccountMapper, UserEmailMapper userEmailMapper,
			UserLocalAuthMapper userLocalAuthMapper, UserOAuthAuthMapper userOAuthAuthMapper,
			UserProfileMapper userProfileMapper, GenderDAO genderDAO, LanguageDAO languageDAO) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.userAccountMapper = userAccountMapper;
		this.userEmailMapper = userEmailMapper;
		this.userLocalAuthMapper = userLocalAuthMapper;
		this.userOAuthAuthMapper = userOAuthAuthMapper;
		this.userProfileMapper = userProfileMapper;
		this.genderDAO = genderDAO;
		this.languageDAO = languageDAO;
	}

	@Override
	public long registerLocalUser(LocalUserRegistrationRequest request) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<Long>() {
				@Override
				public Long doInTransaction(Connection conn) throws SQLException, BusinessException {
					return registerLocalUser(conn, request);
				}
			});
		} catch (BusinessException e) {
			throw e;
		} catch (SQLException e) {
			throw new BusinessException(ERR_DB_REGISTRATION);
		}
	}

	@Override
	public long registerLocalUser(Connection conn, LocalUserRegistrationRequest request) throws BusinessException, SQLException {
		Map<String, String> errors = new HashMap<>();

		if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
			if (userRepository.existUserByUsername(conn, request.getUsername())) {
				errors.put(RequestParamConstants.User.USERNAME, "Tên đăng nhập này đã được đăng ký sử dụng.");
			}
		}
		if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
			if (userRepository.existUserByEmail(conn, request.getEmail())) {
				errors.put(RequestParamConstants.User.EMAIL, "Địa chỉ email này đã tồn tại trên hệ thống.");
			}
		}

		if (!errors.isEmpty()) {
			throw new BusinessException(errors);
		}

		UserAccount account = userAccountMapper.toUserAccount();
		// Set business defaults for account
		account.setStatusId(UserIds.Status.ACTIVE);
		account.setTokenVersion(AuthenticationPolicy.TOKEN_VERSION_INITIAL);
		
		UserProfile profile = createProfile(conn, request.getFullname(), null);
		UserEmail userEmail = userEmailMapper.toUserEmail(request.getEmail());
		// Set business default for email
		userEmail.setVerifiedAt(null);
		
		String passwordHash = passwordEncoder.encode(request.getPassword());
		UserLocalAuth localAuth = userLocalAuthMapper.toUserLocalAuth(request, passwordHash);
		// Set business default for local auth
		localAuth.setFailedAttempts(0);

		try {
			return userRepository.createLocalUser(conn, account, profile, localAuth, userEmail);
		} catch (SQLException e) {
			handleDuplicateException(e);
			throw e;
		}
	}

	@Override
	public long registerOAuthUser(OAuthUserRegistrationRequest request) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<Long>() {
				@Override
				public Long doInTransaction(Connection conn) throws SQLException, BusinessException {
					return registerOAuthUser(conn, request);
				}
			});
		} catch (BusinessException e) {
			throw e;
		} catch (SQLException e) {
			throw new BusinessException(ERR_DB_OAUTH);
		}
	}

	@Override
	public long registerOAuthUser(Connection conn, OAuthUserRegistrationRequest request) throws BusinessException, SQLException {
		Map<String, String> errors = new HashMap<>();

		if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
			if (userRepository.existUserByEmail(conn, request.getEmail())) {
				errors.put(RequestParamConstants.User.EMAIL, "Tài khoản liên kết thất bại: Email này đã được đăng ký trên hệ thống.");
			}
		}

		if (!errors.isEmpty()) {
			throw new BusinessException(errors);
		}

		UserAccount account = userAccountMapper.toUserAccount();
		// Set business defaults for account
		account.setStatusId(UserIds.Status.ACTIVE);
		account.setTokenVersion(AuthenticationPolicy.TOKEN_VERSION_INITIAL);
		
		UserProfile profile = createProfile(conn, request.getFullname(), request.getAvatarUrl());
		UserEmail userEmail = userEmailMapper.toUserEmail(request.getEmail());
		// Set business default for email (verified since it's OAuth)
		userEmail.setVerifiedAt(new java.sql.Timestamp(System.currentTimeMillis()));
		
		UserOAuthAuth oauthAuth = userOAuthAuthMapper.toUserOAuthAuth(request);

		try {
			return userRepository.createOAuthUser(conn, account, profile, oauthAuth, userEmail);
		} catch (SQLException e) {
			handleDuplicateException(e);
			throw e;
		}
	}

	private UserProfile createProfile(Connection conn, String fullname, String avatarUrl) throws SQLException {
		String defaultGenderCode = UserDefaults.DEFAULT_GENDER_CODE;
		String defaultLanguageCode = UserDefaults.DEFAULT_LANGUAGE_CODE;

		UserProfileRequest profileRequest = new UserProfileRequest.Builder()
			.fullname(fullname)
			.avatarUrl(avatarUrl)
			.genderCode(defaultGenderCode)
			.preferredLanguageCode(defaultLanguageCode)
			.build();

		UserProfile profile = userProfileMapper.toUserProfile(profileRequest);
		
		if (profile.getGender() != null && profile.getGender().getCode() != null) {
			Optional<Gender> genderOpt = genderDAO.findByCode(conn, profile.getGender().getCode());
			if (genderOpt.isPresent()) {
				profile.setGender(genderOpt.get());
			}
		}
		
		if (profile.getPreferredLanguage() != null && profile.getPreferredLanguage().getCode() != null) {
			Optional<Language> languageOpt = languageDAO.findByCode(conn, profile.getPreferredLanguage().getCode());
			if (languageOpt.isPresent()) {
				profile.setPreferredLanguage(languageOpt.get());
			}
		}

		return profile;
	}

	private void handleDuplicateException(SQLException e) throws BusinessException {
		String message = (e.getMessage() != null) ? e.getMessage().toLowerCase() : "";

		if (message.contains("duplicate") || message.contains("unique")) {
			Map<String, String> errors = new HashMap<>();

			if (message.contains("email")) {
				errors.put(RequestParamConstants.User.EMAIL, "Địa chỉ email đã tồn tại.");
			} else if (message.contains("username")) {
				errors.put(RequestParamConstants.User.USERNAME, "Tên đăng nhập đã tồn tại.");
			} else {
				errors.put(SystemKeys.ERROR_GLOBAL, "Dữ liệu bạn nhập đã tồn tại trong hệ thống.");
			}
			throw new BusinessException(errors);
		}

		throw new BusinessException("Đã có lỗi xảy ra trong quá trình đăng ký người dùng mới");
	}
}
