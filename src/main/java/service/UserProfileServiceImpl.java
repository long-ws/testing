package service;

import beans.User;
import beans.user.UserProfile;
import config.security.SecurityConfig;
import constants.system.SystemKeys;
import dao.common.GenderDAO;
import dao.common.GenderDAOImpl;
import dao.common.LanguageDAO;
import dao.common.LanguageDAOImpl;
import dto.user.UserProfileRequest;
import dto.user.UserProfileResponse;
import exception.BusinessException;
import mapper.user.UserProfileMapper;
import mapper.user.UserProfileResponseMapper;
import repository.UserRepository;
import repository.UserRepositoryImpl;
import service.user.UserGenderService;
import service.user.UserLanguageService;
import utils.DbTransaction;
import utils.TransactionCallback;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserProfileServiceImpl implements UserProfileService {

	private final UserRepository userRepository;
	private final UserProfileMapper userProfileMapper;
	private final UserProfileResponseMapper userProfileResponseMapper;
	private final UserGenderService genderResolver;
	private final UserLanguageService languageResolver;

	private static final String ERR_DB_UPDATE = "Cập nhật thông tin hồ sơ thất bại. Vui lòng thử lại sau.";
	private static final String ERR_DB_FETCH = "Tải dữ liệu hồ sơ cá nhân thất bại. Vui lòng thử lại sau.";

	public UserProfileServiceImpl() {
		this(new UserRepositoryImpl(), new UserProfileMapper(), new UserProfileResponseMapper(), new LanguageDAOImpl(), new GenderDAOImpl());
	}

	public UserProfileServiceImpl(UserRepository userRepository, UserProfileMapper userProfileMapper, UserProfileResponseMapper userProfileResponseMapper, LanguageDAO languageDAO,
			GenderDAO genderDAO) {
		this.userRepository = userRepository;
		this.userProfileMapper = userProfileMapper;
		this.userProfileResponseMapper = userProfileResponseMapper;
		this.genderResolver = new UserGenderService(genderDAO);
		this.languageResolver = new UserLanguageService(languageDAO);
	}

	@Override
	public UserProfileResponse getUserProfile(long userId) throws BusinessException {
		User user = getById(userId);
		return (user != null) ? userProfileResponseMapper.toUserProfileResponse(user) : null;
	}

	@Override
	public UserProfileResponse updateUserProfile(long userId, UserProfileRequest request) throws BusinessException {
		if (SecurityConfig.isSystemGhostUserId(userId)) {
			throw new BusinessException(SystemKeys.ERROR_GLOBAL, "Không thể cập nhật tài khoản hệ thống.");
		}

		try {
			return DbTransaction.run(new TransactionCallback<UserProfileResponse>() {
				@Override
				public UserProfileResponse doInTransaction(Connection conn) throws SQLException, BusinessException {
					Map<String, String> errors = new HashMap<>();

					Optional<User> existingOpt = userRepository.findById(conn, userId);
					if (!existingOpt.isPresent()) {
						errors.put(SystemKeys.ERROR_GLOBAL, "Hồ sơ tài khoản không tồn tại.");
						throw new BusinessException(errors);
					}

					User existing = existingOpt.get();
					UserProfile profile = userProfileMapper.toUserProfile(request);
					profile.setUserId(existing.getId());

					User user = new User();
					user.setUserAccount(existing.getUserAccount());
					user.setProfile(profile);
					user.setLocalAuth(existing.getLocalAuth());
					user.setOauthAuths(existing.getOauthAuths());
					user.setRole(existing.getRole());
					user.setUserEmail(existing.getUserEmail());

					genderResolver.applyGender(conn, user, existing);
					languageResolver.applyLanguage(conn, user, existing);
					userRepository.update(conn, user);

					return userProfileResponseMapper.toUserProfileResponse(user);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException(ERR_DB_UPDATE);
		}
	}

	@Override
	public User getById(long id) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<User>() {
				@Override
				public User doInTransaction(Connection conn) throws SQLException {
					return userRepository.findById(conn, id).orElse(null);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException(ERR_DB_FETCH);
		}
	}
}
