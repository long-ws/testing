package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import beans.User;
import constants.RequestParamConstants;
import config.security.SecurityConfig;
import constants.system.SystemKeys;
import dto.user.ChangePasswordRequest;
import dto.user.ResetPasswordRequest;
import exception.BusinessException;
import repository.UserRepository;
import repository.UserRepositoryImpl;
import utils.BCryptPasswordEncoder;
import utils.DBConnection;
import utils.DbTransaction;
import utils.PasswordEncoder;
import utils.TransactionCallback;

public class PasswordServiceImpl implements PasswordService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	private static final String ERR_SYSTEM_BUSY = "Yêu cầu của bạn tạm thời không thể xử lý do sự cố hệ thống. Vui lòng thử lại sau.";

	public PasswordServiceImpl() {
		this(new UserRepositoryImpl(), new BCryptPasswordEncoder());
	}

	public PasswordServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void changePassword(Connection conn, long userId, ChangePasswordRequest request) throws BusinessException {
		if (SecurityConfig.isSuperAdminUserId(userId) || SecurityConfig.isSystemGhostUserId(userId)) {
			throw new BusinessException(SystemKeys.ERROR_GLOBAL, "Không thể thao tác trên tài khoản hệ thống.");
		}

		Map<String, String> errors = new HashMap<>();
		User user = validateBusinessLogic(conn, userId, request, errors);
		updatePassword(conn, user, request);
	}

	@Override
	public void resetPassword(long userId, ResetPasswordRequest request) throws BusinessException {
		Map<String, String> errors = new HashMap<>();

		try (Connection readConn = DBConnection.getConnection()) {
			Optional<User> userOptional = userRepository.findById(readConn, userId);
			if (!userOptional.isPresent()) {
				errors.put(SystemKeys.ERROR_GLOBAL, "Tài khoản cần đặt lại mật khẩu không tồn tại.");
			}
		} catch (SQLException e) {
			throw new BusinessException(ERR_SYSTEM_BUSY);
		}

		if (!errors.isEmpty()) {
			throw new BusinessException(errors);
		}

		try {
			String hashedNewPassword = passwordEncoder.encode(request.getNewPassword());
			DbTransaction.runVoid(new TransactionCallback<Void>() {
				@Override
				public Void doInTransaction(Connection conn) throws SQLException {
					userRepository.changePassword(conn, userId, hashedNewPassword);
					userRepository.incrementTokenVersion(conn, userId);
					return null;
				}
			});
		} catch (SQLException e) {
			throw new BusinessException(ERR_SYSTEM_BUSY);
		}
	}

	private User validateBusinessLogic(Connection conn, long userId, ChangePasswordRequest request, Map<String, String> errors) throws BusinessException {
		User user = null;
		try {
			Optional<User> userOptional = userRepository.findById(conn, userId);

			if (!userOptional.isPresent()) {
				errors.put(SystemKeys.ERROR_GLOBAL, "Tài khoản không tồn tại trên hệ thống.");
			} else {
				user = userOptional.get();
				String passwordHash = user.getPasswordHash();

				if (passwordHash == null) {
					errors.put(SystemKeys.ERROR_GLOBAL, "Tài khoản này chưa được cấu hình mật khẩu hệ thống.");
				} else {
					boolean isPasswordValid = passwordEncoder.matches(request.getCurrentPassword(), passwordHash);
					if (!isPasswordValid) {
						errors.put(RequestParamConstants.User.CURRENT_PASSWORD, "Mật khẩu hiện tại không chính xác.");
					}
				}
			}
		} catch (SQLException e) {
			throw new BusinessException(ERR_SYSTEM_BUSY);
		}

		if (!errors.isEmpty()) {
			throw new BusinessException(errors);
		}
		return user;
	}

	private void updatePassword(Connection conn, User user, ChangePasswordRequest request) throws BusinessException {
		try {
			String hashedNewPassword = passwordEncoder.encode(request.getNewPassword());
			userRepository.changePassword(conn, user.getId(), hashedNewPassword);
		} catch (SQLException e) {
			throw new BusinessException(ERR_SYSTEM_BUSY);
		}
	}
}
