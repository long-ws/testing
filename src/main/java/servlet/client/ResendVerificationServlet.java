package servlet.client;

import java.io.IOException;
import java.sql.SQLException;

import constants.RouteConstants;
import constants.SessionConstants;
import helpers.MessageHelper;
import helpers.UrlHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.EmailVerificationService;
import service.EmailVerificationService.SendVerificationStatus;

@WebServlet(name = "ResendVerificationServlet", value = "/verify-email/resend")
public class ResendVerificationServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String RESEND_SUCCESS_MESSAGE = "Đã gửi lại email xác thực. Vui lòng kiểm tra hộp thư.";
	private static final String RATE_LIMIT_MESSAGE = "Vui lòng thử lại sau 60 giây.";
	private static final String TOO_MANY_REQUESTS_MESSAGE = "Bạn đã yêu cầu gửi email xác thực quá nhiều lần trong ngày. Vui lòng thử lại ngày sau.";
	private static final String SEND_FAILED_MESSAGE = "Không thể gửi email xác thực. Vui lòng thử lại sau.";
	private static final String SYSTEM_ERROR_MESSAGE = "Lỗi hệ thống khi gửi email xác thực.";

	private final EmailVerificationService emailVerificationService = new EmailVerificationService();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		Long userId = session != null ? (Long) session.getAttribute(SessionConstants.PENDING_VERIFICATION_USER_ID) : null;
		String email = session != null ? (String) session.getAttribute(SessionConstants.PENDING_VERIFICATION_EMAIL) : null;

		if (userId == null || email == null) {
			response.sendRedirect(request.getContextPath() + RouteConstants.Client.SIGNIN);
			return;
		}

		String baseUrl = UrlHelper.buildBaseUrl(request);

		try {
			SendVerificationStatus status = emailVerificationService.sendVerificationEmail(userId, email, baseUrl);
			if (status == SendVerificationStatus.SENT) {
				MessageHelper.setSuccessMessage(session, RESEND_SUCCESS_MESSAGE);
			} else if (status == SendVerificationStatus.RATE_LIMITED) {
				MessageHelper.setErrorMessage(session, RATE_LIMIT_MESSAGE);
			} else if (status == SendVerificationStatus.TOO_MANY_REQUESTS) {
				MessageHelper.setErrorMessage(session, TOO_MANY_REQUESTS_MESSAGE);
			} else {
				MessageHelper.setErrorMessage(session, SEND_FAILED_MESSAGE);
			}
		} catch (SQLException e) {
			MessageHelper.setErrorMessage(session, SYSTEM_ERROR_MESSAGE);
		}

		response.sendRedirect(request.getContextPath() + RouteConstants.Client.VERIFY_EMAIL);
	}
}