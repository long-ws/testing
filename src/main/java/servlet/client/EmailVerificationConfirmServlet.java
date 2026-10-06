package servlet.client;

import java.io.IOException;
import java.sql.SQLException;

import constants.RequestParamConstants;
import constants.RouteConstants;
import constants.SessionConstants;
import helpers.MessageHelper;
import helpers.SessionHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.EmailVerificationService;

@WebServlet(name = "EmailVerificationConfirmServlet", value = "/verify-email/confirm")
public class EmailVerificationConfirmServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String INVALID_LINK_MESSAGE = "Liên kết xác thực không hợp lệ.";
	private static final String EXPIRED_LINK_MESSAGE = "Liên kết xác thực không hợp lệ hoặc đã hết hạn.";
	private static final String SYSTEM_ERROR_MESSAGE = "Lỗi hệ thống khi xác thực email.";
	private static final String SIGNUP_SUCCESS_MESSAGE = "Xác thực email thành công! Vui lòng đăng nhập.";

	private final EmailVerificationService emailVerificationService = new EmailVerificationService();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		Long userId = session != null ? (Long) session.getAttribute(SessionConstants.PENDING_VERIFICATION_USER_ID) : null;
		String email = session != null ? (String) session.getAttribute(SessionConstants.PENDING_VERIFICATION_EMAIL) : null;

		if (userId == null || email == null) {
			response.sendRedirect(request.getContextPath() + RouteConstants.Client.SIGNIN);
			return;
		}

		String token = request.getParameter(RequestParamConstants.CODE);
		if (token == null || token.isBlank()) {
			MessageHelper.setErrorMessage(session, INVALID_LINK_MESSAGE);
			response.sendRedirect(request.getContextPath() + RouteConstants.Client.VERIFY_EMAIL);
			return;
		}

		try {
			boolean verified = emailVerificationService.verifyToken(userId, token);
			if (verified) {
				SessionHelper.clearPendingVerification(session);
				session.setAttribute(SessionConstants.SIGNUP_SUCCESS, SIGNUP_SUCCESS_MESSAGE);
				response.sendRedirect(request.getContextPath() + RouteConstants.Client.SIGNIN);
				return;
			}
			MessageHelper.setErrorMessage(session, EXPIRED_LINK_MESSAGE);
			response.sendRedirect(request.getContextPath() + RouteConstants.Client.VERIFY_EMAIL);
		} catch (SQLException e) {
			MessageHelper.setErrorMessage(session, SYSTEM_ERROR_MESSAGE);
			response.sendRedirect(request.getContextPath() + RouteConstants.Client.VERIFY_EMAIL);
		}
	}
}