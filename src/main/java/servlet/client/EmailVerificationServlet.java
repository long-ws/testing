package servlet.client;

import java.io.IOException;

import constants.RouteConstants;
import constants.SessionConstants;
import constants.ViewAttributeConstants;
import constants.ViewPathConstants;
import helpers.FormRenderHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "EmailVerificationServlet", value = "/verify-email")
public class EmailVerificationServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		Long userId = session != null ? (Long) session.getAttribute(SessionConstants.PENDING_VERIFICATION_USER_ID) : null;
		String email = session != null ? (String) session.getAttribute(SessionConstants.PENDING_VERIFICATION_EMAIL) : null;

		if (userId == null || email == null) {
			response.sendRedirect(request.getContextPath() + RouteConstants.Client.SIGNIN);
			return;
		}

		request.setAttribute(ViewAttributeConstants.User.EMAIL, email);
		FormRenderHelper.forward(request, response, ViewPathConstants.Client.VERIFY_EMAIL);
	}
}