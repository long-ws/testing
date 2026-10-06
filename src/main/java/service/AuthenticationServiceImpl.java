package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import beans.User;
import beans.common.Permission;
import beans.common.Role;
import constants.PermissionConstants;
import config.security.SecurityConfig;
import constants.auth.AuthConstants;
import constants.system.SystemKeys;
import dto.user.AdminAuthenticationResult;
import dto.user.AdminSigninRequest;
import dto.user.ChangePasswordRequest;
import dto.user.SigninRequest;
import dto.user.UserAuthenticationResult;
import dto.user.UserCreateRequest;
import dto.user.UserDetailResponse;
import exception.BusinessException;
import repository.UserRepository;
import repository.UserRepositoryImpl;
import utils.BCryptPasswordEncoder;
import utils.DbTransaction;
import utils.PasswordEncoder;
import utils.TransactionCallback;

public class AuthenticationServiceImpl implements AuthenticationService {

	private final UserRepository userRepository;
	private final UserManagementService userManagementService;
	private final PasswordEncoder passwordEncoder;
	private final PasswordService passwordService;
	private final AuthorizationService authorizationService;

	private static final String ERR_AUTH_SYSTEM = "Hệ thống xác thực đang bận. Vui lòng thử lại sau.";
	private static final String ERR_PASSWORD_CHANGE = "Quá trình đổi mật khẩu gặp sự cố. Vui lòng kiểm tra lại.";

	public AuthenticationServiceImpl() {
		UserRepository repository = new UserRepositoryImpl();
		PasswordEncoder encoder = new BCryptPasswordEncoder();

		this.userRepository = repository;
		this.userManagementService = new UserManagementServiceImpl(repository);
		this.passwordEncoder = encoder;
		this.passwordService = new PasswordServiceImpl(repository, encoder);
		this.authorizationService = new AuthorizationServiceImpl();
	}

	public AuthenticationServiceImpl(UserRepository userRepository, UserManagementService userManagementService, PasswordEncoder passwordEncoder, PasswordService passwordService, AuthorizationService authorizationService) {
		this.userRepository = userRepository;
		this.userManagementService = userManagementService;
		this.passwordEncoder = passwordEncoder;
		this.passwordService = passwordService;
		this.authorizationService = authorizationService;
	}

	@Override
	public UserDetailResponse signupUser(UserCreateRequest dto) throws BusinessException {
		return userManagementService.createUser(dto);
	}

	@Override
	public void changePassword(long userId, ChangePasswordRequest request) throws BusinessException {
		try {
			DbTransaction.runVoid(new TransactionCallback<Void>() {
				@Override
				public Void doInTransaction(Connection conn) throws SQLException {
					passwordService.changePassword(conn, userId, request);
					userRepository.incrementTokenVersion(conn, userId);
					return null;
				}
			});
		} catch (SQLException e) {
			throw new BusinessException(ERR_PASSWORD_CHANGE);
		}
	}

	@Override
	public boolean incrementTokenVersion(long userId) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<Boolean>() {
				@Override
				public Boolean doInTransaction(Connection conn) throws SQLException {
					return userRepository.incrementTokenVersion(conn, userId);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Không thể cập nhật trạng thái phiên làm việc lúc này.");
		}
	}

	@Override
	public int getTokenVersion(long userId) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<Integer>() {
				@Override
				public Integer doInTransaction(Connection conn) throws SQLException {
					return userRepository.getTokenVersion(conn, userId);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Không thể đối soát mã phiên làm việc.");
		}
	}

	@Override
	public User authenticate(SigninRequest request) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<User>() {
				@Override
				public User doInTransaction(Connection conn) throws SQLException, BusinessException {
					return checkUserAndPass(conn, request.getUsername(), request.getPassword());
				}
			});
		} catch (SQLException e) {
			throw new BusinessException(ERR_AUTH_SYSTEM);
		}
	}

	@Override
	public User authenticateAdmin(AdminSigninRequest request) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<User>() {
				@Override
				public User doInTransaction(Connection conn) throws SQLException, BusinessException {
					User user = checkUserAndPass(conn, request.getUsername(), request.getPassword());

					if (isSuperAdmin(user)) {
						return user;
					}

					ensureAdminPortalAccess(conn, user);
					return user;
				}
			});
		} catch (SQLException e) {
			throw new BusinessException(ERR_AUTH_SYSTEM);
		}
	}

	@Override
	public UserAuthenticationResult authenticateWithResult(SigninRequest request) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<UserAuthenticationResult>() {
				@Override
				public UserAuthenticationResult doInTransaction(Connection conn) throws SQLException, BusinessException {
					User user = checkUserAndPass(conn, request.getUsername(), request.getPassword());
					boolean isSuperAdmin = SecurityConfig.isSuperAdminUsername(user.getUsername());

					return new UserAuthenticationResult.Builder()
							.user(user)
							.isSuperAdmin(isSuperAdmin)
							.build();
				}
			});
		} catch (SQLException e) {
			throw new BusinessException(ERR_AUTH_SYSTEM);
		}
	}

	@Override
	public AdminAuthenticationResult authenticateAdminWithResult(AdminSigninRequest request) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<AdminAuthenticationResult>() {
				@Override
				public AdminAuthenticationResult doInTransaction(Connection conn) throws SQLException, BusinessException {
					User user = checkUserAndPass(conn, request.getUsername(), request.getPassword());

					if (isSuperAdmin(user)) {
						return buildSuperAdminAuthenticationResult(user);
					}

					ensureAdminPortalAccess(conn, user);
					Set<String> permissionCodes = getPermissionCodes(conn, user.getId());

					return new AdminAuthenticationResult.Builder()
							.user(user)
							.isSuperAdmin(false)
							.permissionCodes(permissionCodes)
							.build();
				}
			});
		} catch (SQLException e) {
			throw new BusinessException(ERR_AUTH_SYSTEM);
		}
	}

	private User checkUserAndPass(Connection conn, String username, String rawPassword) throws BusinessException, SQLException {
		if (SecurityConfig.isSuperAdminUsername(username)) {
			String passwordHash = SecurityConfig.SUPER_ADMIN_PASSWORD_BCRYPT;
			if (passwordHash == null || passwordHash.isBlank()) {
				passwordHash = AuthConstants.DUMMY_BCRYPT;
			}

			boolean isPasswordValid = passwordEncoder.matches(rawPassword, passwordHash);
			if (!isPasswordValid) {
				throw invalidCredentialsException();
			}

			return createSuperAdminUser();
		}

		Optional<User> userOptional = userRepository.findByUsername(conn, username);
		boolean existUser = userOptional.isPresent();

		String passwordHash = existUser ? userOptional.get().getPasswordHash() : AuthConstants.DUMMY_BCRYPT;
		if (passwordHash == null) {
			passwordHash = AuthConstants.DUMMY_BCRYPT;
		}

		boolean isPasswordValid = passwordEncoder.matches(rawPassword, passwordHash);

		if (!existUser || !isPasswordValid) {
			throw invalidCredentialsException();
		}

		return userOptional.get();
	}

	private boolean isSuperAdmin(User user) {
		return SecurityConfig.isSuperAdminUserId(user.getId())
				&& SecurityConfig.isSuperAdminUsername(user.getUsername());
	}

	private void ensureAdminPortalAccess(Connection conn, User user) throws SQLException, BusinessException {
		if (!authorizationService.hasAnyPermission(conn, user.getId(), PermissionConstants.ADMIN_PORTAL_ACCESS_PERMISSIONS)) {
			throw adminAccessDeniedException();
		}
	}

	private Set<String> getPermissionCodes(Connection conn, long userId) throws SQLException, BusinessException {
		Set<String> permissionCodes = new HashSet<>();

		for (Permission permission : authorizationService.getPermissionsByUserId(conn, userId)) {
			if (permission != null && permission.getCode() != null && !permission.getCode().isBlank()) {
				permissionCodes.add(permission.getCode());
			}
		}

		return permissionCodes;
	}

	private AdminAuthenticationResult buildSuperAdminAuthenticationResult(User user) {
		return new AdminAuthenticationResult.Builder()
				.user(user)
				.isSuperAdmin(true)
				.permissionCodes(new HashSet<>())
				.build();
	}

	private User createSuperAdminUser() {
		User user = new User();
		user.setId(SecurityConfig.SUPER_ADMIN_USER_ID);
		user.setUsername(SecurityConfig.SUPER_ADMIN_USERNAME);

		Role role = new Role();
		role.setCode(SecurityConfig.SUPER_ADMIN_ROLE_CODE);
		role.setSystem(true);
		role.setActive(true);

		user.setRole(role);
		return user;
	}

	private BusinessException invalidCredentialsException() {
		Map<String, String> errors = new HashMap<>();
		errors.put(SystemKeys.ERROR_GLOBAL, "Tài khoản hoặc mật khẩu không chính xác.");
		return new BusinessException(errors);
	}

	private BusinessException adminAccessDeniedException() {
		Map<String, String> errors = new HashMap<>();
		errors.put(SystemKeys.ERROR_GLOBAL, "Tài khoản của bạn thiếu quyền truy cập.");
		return new BusinessException(errors);
	}
}
