package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import beans.User;
import beans.common.Role;
import beans.user.UserAccount;
import beans.user.UserEmail;
import beans.user.UserLocalAuth;
import beans.user.UserProfile;
import config.security.SecurityConfig;
import constants.RequestParamConstants;
import constants.system.SystemKeys;
import dao.common.GenderDAO;
import dao.common.GenderDAOImpl;
import dao.common.LanguageDAO;
import dao.common.LanguageDAOImpl;
import dao.common.RoleDAO;
import dao.common.RoleDAOImpl;
import domain.user.UserDefaults;
import dto.user.UserCreateRequest;
import dto.user.UserDetailResponse;
import dto.user.UserManageResponse;
import dto.user.UserUpdateRequest;
import exception.BusinessException;
import mapper.user.UserAccountMapper;
import mapper.user.UserDetailResponseMapper;
import mapper.user.UserEmailMapper;
import mapper.user.UserManageResponseMapper;
import mapper.user.UserProfileMapper;
import repository.UserRepository;
import repository.UserRepositoryImpl;
import service.user.UserGenderService;
import service.user.UserLanguageService;
import utils.BCryptPasswordEncoder;
import utils.DBConnection;
import utils.DbTransaction;
import utils.PasswordEncoder;
import utils.TransactionCallback;

public class UserManagementServiceImpl implements UserManagementService {

	private final UserRepository userRepository;
	private final UserAccountMapper userAccountMapper;
	private final UserEmailMapper userEmailMapper;
	private final UserProfileMapper userProfileMapper;
	private final UserDetailResponseMapper userDetailResponseMapper;
	private final UserManageResponseMapper userManageResponseMapper;
	private final UserGenderService genderResolver;
	private final UserLanguageService languageResolver;
	private final PasswordEncoder passwordEncoder;

	public UserManagementServiceImpl() {
		this(new UserRepositoryImpl(), new UserAccountMapper(), new UserEmailMapper(), new UserProfileMapper(), new UserDetailResponseMapper(), new UserManageResponseMapper(), new LanguageDAOImpl(),
				new GenderDAOImpl(), new BCryptPasswordEncoder());
	}

	public UserManagementServiceImpl(UserRepository userRepository) {
		this(userRepository, new UserAccountMapper(), new UserEmailMapper(), new UserProfileMapper(), new UserDetailResponseMapper(), new UserManageResponseMapper(), new LanguageDAOImpl(),
				new GenderDAOImpl(), new BCryptPasswordEncoder());
	}

	public UserManagementServiceImpl(UserRepository userRepository, UserAccountMapper userAccountMapper, UserEmailMapper userEmailMapper, UserProfileMapper userProfileMapper,
			UserDetailResponseMapper userDetailResponseMapper, UserManageResponseMapper userManageResponseMapper) {
		this(userRepository, userAccountMapper, userEmailMapper, userProfileMapper, userDetailResponseMapper, userManageResponseMapper, new LanguageDAOImpl(), new GenderDAOImpl(),
				new BCryptPasswordEncoder());
	}

	public UserManagementServiceImpl(UserRepository userRepository, UserAccountMapper userAccountMapper, UserEmailMapper userEmailMapper, UserProfileMapper userProfileMapper,
			UserDetailResponseMapper userDetailResponseMapper, UserManageResponseMapper userManageResponseMapper, LanguageDAO languageDAO, GenderDAO genderDAO) {
		this(userRepository, userAccountMapper, userEmailMapper, userProfileMapper, userDetailResponseMapper, userManageResponseMapper, languageDAO, genderDAO, new BCryptPasswordEncoder());
	}

	public UserManagementServiceImpl(UserRepository userRepository, UserAccountMapper userAccountMapper, UserEmailMapper userEmailMapper, UserProfileMapper userProfileMapper,
			UserDetailResponseMapper userDetailResponseMapper, UserManageResponseMapper userManageResponseMapper, LanguageDAO languageDAO, GenderDAO genderDAO, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.userAccountMapper = userAccountMapper;
		this.userEmailMapper = userEmailMapper;
		this.userProfileMapper = userProfileMapper;
		this.userDetailResponseMapper = userDetailResponseMapper;
		this.userManageResponseMapper = userManageResponseMapper;
		this.genderResolver = new UserGenderService(genderDAO);
		this.languageResolver = new UserLanguageService(languageDAO);
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserDetailResponse createUser(UserCreateRequest dto) throws BusinessException {
		Map<String, String> errors = new HashMap<>();

		try (Connection readConn = DBConnection.getConnection()) {
			if (SecurityConfig.isSuperAdminUsername(dto.getUsername())) {
				errors.put(RequestParamConstants.User.USERNAME, "Tên đăng nhập này được bảo vệ bởi hệ thống.");
			}

			if (dto.getRole() != null && dto.getRole().getCode() != null && !dto.getRole().getCode().isBlank()) {
				RoleDAO roleDAO = new RoleDAOImpl();
				Optional<Role> roleOpt = roleDAO.findByCode(readConn, dto.getRole().getCode().trim());
				if (roleOpt.isEmpty()) {
					errors.put(RequestParamConstants.User.ROLE, "Vai trò không tồn tại.");
				} else if (roleOpt.get().isSystem()) {
					errors.put(RequestParamConstants.User.ROLE, "Không được phép gán vai trò hệ thống.");
				}
			}

			if (userRepository.existUserByUsername(readConn, dto.getUsername())) {
				errors.put(RequestParamConstants.User.USERNAME, "Tên đăng nhập đã tồn tại!");
			}
			if (userRepository.existUserByEmail(readConn, dto.getEmail())) {
				errors.put(RequestParamConstants.User.EMAIL, "Email đã được sử dụng!");
			}
		} catch (SQLException e) {
			throw new BusinessException("Lỗi hệ thống khi kiểm tra tài khoản: " + e.getMessage());
		}

		if (!errors.isEmpty()) {
			throw new BusinessException(errors);
		}

		User user = new User();
		UserAccount account = userAccountMapper.toUserAccount();
		account.setStatusId(domain.user.UserIds.Status.ACTIVE);
		account.setTokenVersion(policy.security.AuthenticationPolicy.TOKEN_VERSION_INITIAL);
		user.setUserAccount(account);

		UserLocalAuth local = new UserLocalAuth();
		local.setUsername(dto.getUsername());
		String passwordHash = dto.getPassword() != null ? passwordEncoder.encode(dto.getPassword()) : null;
		local.setPasswordHash(passwordHash);
		local.setFailedAttempts(0);
		user.setLocalAuth(local);

		Role role = dto.getRole() != null ? dto.getRole() : new Role();
		if (role.getCode() == null) {
			role.setCode(UserDefaults.DEFAULT_ROLE_CODE);
		}
		user.setRole(role);

		user.setProfile(userProfileMapper.toUserProfile(dto));
		UserEmail userEmail = userEmailMapper.toUserEmail(dto.getEmail());
		userEmail.setVerifiedAt(null);
		user.setUserEmail(userEmail);

		try {
			long id = DbTransaction.run(new TransactionCallback<Long>() {
				@Override
				public Long doInTransaction(Connection conn) throws SQLException {
					genderResolver.applyGender(conn, user, null);
					languageResolver.applyLanguage(conn, user, null);
					return userRepository.insert(conn, user);
				}
			});

			return getUserById(id);
		} catch (SQLException e) {
			throw new BusinessException("Lỗi ghi dữ liệu vào Database: " + e.getMessage());
		}
	}

	@Override
	public UserDetailResponse updateUser(UserUpdateRequest dto) throws BusinessException {
		Map<String, String> errors = new HashMap<>();

		if (dto.getId() == null) {
			errors.put(RequestParamConstants.ID, "Yêu cầu user id");
		}

		if (dto.getId() != null && SecurityConfig.isSystemGhostUserId(dto.getId())) {
			errors.put(SystemKeys.ERROR_GLOBAL, "Không thể cập nhật tài khoản hệ thống.");
		}

		if (!errors.isEmpty()) {
			throw new BusinessException(errors);
		}

		User existing;
		try (Connection readConn = DBConnection.getConnection()) {
			Optional<User> emailOwnerOptional = userRepository.findByEmail(readConn, dto.getEmail());
			if (emailOwnerOptional.isPresent()) {
				User emailOwner = emailOwnerOptional.get();
				if (emailOwner.getId() != dto.getId()) {
					errors.put(RequestParamConstants.User.EMAIL, "Email đã được sử dụng!");
				}
			}

			Optional<User> existingOptional = userRepository.findById(readConn, dto.getId());
			if (!existingOptional.isPresent()) {
				errors.put(SystemKeys.ERROR_GLOBAL, "Người dùng không tồn tại trên hệ thống");
			} else {
				User user = existingOptional.get();
				if (user.getRole() != null && user.getRole().isSystem()) {
					errors.put(SystemKeys.ERROR_GLOBAL, "Không thể cập nhật người dùng có vai trò hệ thống");
				}
			}

			if (!errors.isEmpty()) {
				throw new BusinessException(errors);
			}

			existing = existingOptional.get();

			if (SecurityConfig.isSuperAdminUsername(dto.getUsername()) && !SecurityConfig.isSuperAdminUsername(existing.getUsername())) {
				errors.put(RequestParamConstants.User.USERNAME, "Tên đăng nhập này được bảo vệ bởi hệ thống.");
				throw new BusinessException(errors);
			}

			if (dto.getRole() != null && dto.getRole().getCode() != null && !dto.getRole().getCode().isBlank()) {
				RoleDAO roleDAO = new RoleDAOImpl();
				Optional<beans.common.Role> roleOpt = roleDAO.findByCode(readConn, dto.getRole().getCode().trim());
				if (roleOpt.isEmpty()) {
					errors.put(RequestParamConstants.User.ROLE, "Vai trò không tồn tại.");
					throw new BusinessException(errors);
				}
				if (roleOpt.get().isSystem()) {
					errors.put(RequestParamConstants.User.ROLE, "Không được phép gán vai trò hệ thống.");
					throw new BusinessException(errors);
				}
			}
		} catch (SQLException e) {
			throw new BusinessException("Lỗi hệ thống khi đọc dữ liệu: " + e.getMessage());
		}

		User user = new User();
		user.setUserAccount(existing.getUserAccount());

		UserLocalAuth local = new UserLocalAuth();
		local.setUserId(existing.getId());
		local.setUsername(existing.getUsername());
		String newPasswordHash = null;
		if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
			newPasswordHash = passwordEncoder.encode(dto.getPassword());
		}
		if (newPasswordHash != null) {
			local.setPasswordHash(newPasswordHash);
		} else {
			local.setPasswordHash(existing.getPasswordHash());
		}
		user.setLocalAuth(local);

		UserEmail userEmail = existing.getUserEmail() != null ? existing.getUserEmail() : new UserEmail();
		userEmail.setEmail(dto.getEmail());
		user.setUserEmail(userEmail);

		if (dto.getRole() != null) {
			user.setRole(dto.getRole());
		} else {
			user.setRole(existing.getRole());
		}

		UserProfile profile = userProfileMapper.toUserProfile(dto);
		if (profile != null) {
			profile.setUserId(dto.getId());
		}
		user.setProfile(profile);

		try {
			DbTransaction.runVoid(new TransactionCallback<Void>() {
				@Override
				public Void doInTransaction(Connection writeConn) throws SQLException {
					genderResolver.applyGender(writeConn, user, existing);
					languageResolver.applyLanguage(writeConn, user, existing);
					userRepository.update(writeConn, user);
					return null;
				}
			});

			return getUserById(dto.getId());
		} catch (SQLException e) {
			throw new BusinessException("Lỗi cập nhật dữ liệu vào Database: " + e.getMessage());
		}
	}

	@Override
	public boolean deleteUsers(List<Long> ids) throws BusinessException {
		if (ids == null || ids.isEmpty()) {
			return false;
		}

		try (Connection readConn = DBConnection.getConnection()) {
			for (Long id : ids) {
				if (id != null && SecurityConfig.isSystemGhostUserId(id)) {
					throw new BusinessException("Không thể xóa tài khoản hệ thống.");
				}
				Optional<User> userOptional = userRepository.findById(readConn, id);
				if (userOptional.isPresent()) {
					User user = userOptional.get();
					if (user.getRole() != null && user.getRole().isSystem()) {
						throw new BusinessException("Không thể xóa người dùng có vai trò hệ thống");
					}
				}
			}
		} catch (SQLException e) {
			throw new BusinessException("Lỗi hệ thống khi kiểm tra người dùng: " + e.getMessage());
		}

		try {
			return DbTransaction.run(new TransactionCallback<Boolean>() {
				@Override
				public Boolean doInTransaction(Connection conn) throws SQLException {
					return userRepository.delete(conn, ids);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Lỗi database: " + e.getMessage());
		}
	}

	@Override
	public UserDetailResponse getUserById(long id) {
		if (SecurityConfig.isSystemGhostUserId(id)) {
			return null;
		}
		try (Connection conn = DBConnection.getConnection()) {
			Optional<User> userOptional = userRepository.findById(conn, id);
			if (userOptional.isPresent()) {
				User user = userOptional.get();
				return userDetailResponseMapper.toUserDetailResponse(user);
			}
			return null;
		} catch (SQLException e) {
			throw new BusinessException("Lỗi database: " + e.getMessage());
		}
	}

	@Override
	public User getById(long id) {
		if (SecurityConfig.isSystemGhostUserId(id)) {
			return null;
		}
		try (Connection conn = DBConnection.getConnection()) {
			Optional<User> userOptional = userRepository.findById(conn, id);
			if (userOptional.isPresent()) {
				return userOptional.get();
			}
			return null;
		} catch (SQLException e) {
			throw new BusinessException("Lỗi database: " + e.getMessage());
		}
	}

	@Override
	public List<UserManageResponse> getUsers() {
		try (Connection conn = DBConnection.getConnection()) {
			List<User> users = userRepository.findAllUser(conn);
			List<UserManageResponse> dtos = new ArrayList<>();
			for (User user : users) {
				dtos.add(userManageResponseMapper.toUserManageResponse(user));
			}
			return dtos;
		} catch (SQLException e) {
			throw new BusinessException("Lỗi database: " + e.getMessage());
		}
	}

	@Override
	public long countUsers() {
		try (Connection conn = DBConnection.getConnection()) {
			return userRepository.count(conn);
		} catch (SQLException e) {
			throw new BusinessException("Lỗi database: " + e.getMessage());
		}
	}

	@Override
	public boolean isUsernameExists(String username) {
		try (Connection conn = DBConnection.getConnection()) {
			return userRepository.existUserByUsername(conn, username);
		} catch (SQLException e) {
			throw new BusinessException("Lỗi database: " + e.getMessage());
		}
	}

	@Override
	public boolean isUsernameExists(String username, long excludeId) {
		try (Connection conn = DBConnection.getConnection()) {
			return userRepository.existUserByUsername(conn, username, excludeId);
		} catch (SQLException e) {
			throw new BusinessException("Lỗi database: " + e.getMessage());
		}
	}

	@Override
	public boolean isEmailExists(String email) {
		try (Connection conn = DBConnection.getConnection()) {
			return userRepository.existUserByEmail(conn, email);
		} catch (SQLException e) {
			throw new BusinessException("Lỗi database: " + e.getMessage());
		}
	}

	@Override
	public boolean isEmailExists(String email, long excludeId) {
		try (Connection conn = DBConnection.getConnection()) {
			return userRepository.existUserByEmail(conn, email, excludeId);
		} catch (SQLException e) {
			throw new BusinessException("Lỗi database: " + e.getMessage());
		}
	}

	@Override
	public User getUserEntityByUsername(String username) {
		try (Connection conn = DBConnection.getConnection()) {
			Optional<User> userOptional = userRepository.findByUsername(conn, username);
			if (userOptional.isPresent()) {
				return userOptional.get();
			}
			return null;
		} catch (SQLException e) {
			throw new BusinessException("Lỗi database: " + e.getMessage());
		}
	}

	@Override
	public UserCreateRequest toCreateRequest(UserDetailResponse detail) {
		return userDetailResponseMapper.toUserCreateRequest(detail);
	}

	@Override
	public UserUpdateRequest toUpdateRequest(UserDetailResponse detail) {
		return userDetailResponseMapper.toUserUpdateRequest(detail);
	}
}
