package service;

import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import beans.User;
import beans.user.UserEmail;
import config.security.SecurityConfig;
import constants.RequestParamConstants;
import constants.system.SystemKeys;
import dao.user.UserEmailDAO;
import dao.user.UserEmailDAOImpl;
import dto.user.ChangeEmailRequest;
import exception.BusinessException;
import repository.UserRepository;
import repository.UserRepositoryImpl;
import utils.BCryptPasswordEncoder;
import utils.DbTransaction;
import utils.PasswordEncoder;
import utils.TransactionCallback;

public class EmailChangeService {

	private static final String ERR_SYSTEM_BUSY = "Yêu cầu của bạn tạm thời không thể xử lý do sự cố hệ thống. Vui lòng thử lại sau.";

	private final UserRepository userRepository;
	private final UserEmailDAO userEmailDAO;
	private final PasswordEncoder passwordEncoder;

	public EmailChangeService() {
		this(new UserRepositoryImpl(), new UserEmailDAOImpl(), new BCryptPasswordEncoder());
	}

	public EmailChangeService(UserRepository userRepository, UserEmailDAO userEmailDAO, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.userEmailDAO = userEmailDAO;
		this.passwordEncoder = passwordEncoder;
	}

	public void validateChangeRequest(long userId, ChangeEmailRequest request) throws BusinessException {
		ensureMutableUser(userId);

		try {
			DbTransaction.run(new TransactionCallback<Void>() {
				@Override
				public Void doInTransaction(Connection conn) throws SQLException, BusinessException {
					validateChangeRequest(conn, userId, request);
					return null;
				}
			});
		} catch (SQLException e) {
			throw new BusinessException(ERR_SYSTEM_BUSY);
		}
	}

	public void applyVerifiedChange(long userId, String currentEmail, String newEmail) throws BusinessException {
		ensureMutableUser(userId);

		try {
			DbTransaction.runVoid(new TransactionCallback<Void>() {
				@Override
				public Void doInTransaction(Connection conn) throws SQLException, BusinessException {
					applyVerifiedChange(conn, userId, currentEmail, newEmail);
					userRepository.incrementTokenVersion(conn, userId);
					return null;
				}
			});
		} catch (SQLException e) {
			throw new BusinessException(ERR_SYSTEM_BUSY);
		}
	}

	private void validateChangeRequest(Connection conn, long userId, ChangeEmailRequest request) throws BusinessException {
		Map<String, String> errors = new HashMap<>();

		try {
			User user = findUserById(conn, userId, errors);
			if (user != null) {
				validateCurrentEmail(errors, normalize(user.getEmail()), normalize(request.getCurrentEmail()));
				validateCurrentPassword(errors, user.getPasswordHash(), request.getCurrentPassword());
			}

			String currentEmail = normalize(request.getCurrentEmail());
			String newEmail = normalize(request.getNewEmail());
			if (newEmail != null && !newEmail.isEmpty()) {
				validateNewEmail(errors, conn, userId, currentEmail, newEmail);
			}
		} catch (SQLException e) {
			throw new BusinessException(ERR_SYSTEM_BUSY);
		}

		if (!errors.isEmpty()) {
			throw new BusinessException(errors);
		}
	}

	private void applyVerifiedChange(Connection conn, long userId, String currentEmail, String newEmail) throws BusinessException {
		Map<String, String> errors = new HashMap<>();

		try {
			User user = findUserById(conn, userId, errors);
			if (user != null) {
				validateApplyCurrentEmail(errors, normalize(user.getEmail()), currentEmail);
			}

			if (newEmail == null || newEmail.isEmpty()) {
				errors.put(RequestParamConstants.User.NEW_EMAIL, "Email mới không hợp lệ.");
			} else {
				validateNewEmailAvailability(errors, conn, userId, newEmail);
			}

			if (!errors.isEmpty()) {
				throw new BusinessException(errors);
			}

			saveUserEmail(conn, userId, newEmail);
		} catch (SQLException e) {
			throw new BusinessException(ERR_SYSTEM_BUSY);
		}
	}

	private void ensureMutableUser(long userId) throws BusinessException {
		if (SecurityConfig.isSuperAdminUserId(userId) || SecurityConfig.isSystemGhostUserId(userId)) {
			throw new BusinessException(SystemKeys.ERROR_GLOBAL, "Không thể thao tác trên tài khoản hệ thống.");
		}
	}

	private User findUserById(Connection conn, long userId, Map<String, String> errors) throws SQLException {
		Optional<User> userOptional = userRepository.findById(conn, userId);
		if (!userOptional.isPresent()) {
			errors.put(SystemKeys.ERROR_GLOBAL, "Tài khoản không tồn tại trên hệ thống.");
			return null;
		}
		return userOptional.get();
	}

	private void validateCurrentEmail(Map<String, String> errors, String actualEmail, String requestCurrentEmail) {
		if (actualEmail == null || actualEmail.isEmpty()) {
			errors.put(SystemKeys.ERROR_GLOBAL, "Tài khoản này chưa có email hiện tại.");
			return;
		}
		if (!actualEmail.equalsIgnoreCase(requestCurrentEmail)) {
			errors.put(RequestParamConstants.User.CURRENT_EMAIL, "Email hiện tại không khớp với tài khoản đang đăng nhập.");
		}
	}

	private void validateApplyCurrentEmail(Map<String, String> errors, String actualEmail, String currentEmail) {
		if (actualEmail == null || currentEmail == null || !actualEmail.equalsIgnoreCase(currentEmail)) {
			errors.put(SystemKeys.ERROR_GLOBAL, "Email hiện tại của tài khoản đã thay đổi. Vui lòng thực hiện lại.");
		}
	}

	private void validateCurrentPassword(Map<String, String> errors, String passwordHash, String rawPassword) {
		if (passwordHash == null) {
			errors.put(SystemKeys.ERROR_GLOBAL, "Tài khoản này chưa được cấu hình mật khẩu hệ thống.");
			return;
		}
		if (!passwordEncoder.matches(rawPassword, passwordHash)) {
			errors.put(RequestParamConstants.User.CURRENT_PASSWORD, "Mật khẩu hiện tại không chính xác.");
		}
	}

	private void validateNewEmail(Map<String, String> errors, Connection conn, long userId, String currentEmail, String newEmail) throws SQLException {
		if (currentEmail != null && currentEmail.equalsIgnoreCase(newEmail)) {
			errors.put(RequestParamConstants.User.NEW_EMAIL, "Email mới phải khác email hiện tại.");
			return;
		}
		validateNewEmailAvailability(errors, conn, userId, newEmail);
	}

	private void validateNewEmailAvailability(Map<String, String> errors, Connection conn, long userId, String newEmail) throws SQLException {
		if (userEmailDAO.existsByEmail(conn, newEmail, Long.valueOf(userId))) {
			errors.put(RequestParamConstants.User.NEW_EMAIL, "Email mới đã được sử dụng.");
		}
	}

	private void saveUserEmail(Connection conn, long userId, String newEmail) throws SQLException {
		Optional<UserEmail> userEmailOpt = userEmailDAO.findByUserId(conn, userId);
		UserEmail userEmail = userEmailOpt.isPresent() ? userEmailOpt.get() : new UserEmail();
		userEmail.setUserId(userId);
		userEmail.setEmail(newEmail);
		userEmail.setVerifiedAt(new Timestamp(System.currentTimeMillis()));

		if (userEmailOpt.isPresent()) {
			userEmailDAO.update(conn, userId, userEmail);
			return;
		}
		userEmailDAO.insert(conn, userId, userEmail);
	}

	private String normalize(String value) {
		return value == null ? null : value.trim();
	}
}
