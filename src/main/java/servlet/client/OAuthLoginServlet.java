package servlet.client;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

import constants.RequestParamConstants;
import constants.RouteConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import constants.SessionConstants;
import service.oauth.OAuthFactory;
import service.oauth.OAuthProvider;

@WebServlet(name = "OAuthLoginServlet", value = "/oauth-login")
public class OAuthLoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final String OAUTH_INIT_FAILED_MESSAGE = "Lỗi khởi tạo OAuth. Vui lòng thử lại.";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String provider = request.getParameter(RequestParamConstants.OAuth.PROVIDER);

		if (provider == null || provider.trim().isEmpty()) {
			response.sendRedirect(request.getContextPath() + RouteConstants.Client.SIGNIN);
			return;
		}

		try {
			String providerLower = provider.trim().toLowerCase();
			HttpSession session = request.getSession(true);
			String state = generateState();
			session.setAttribute(SessionConstants.OAUTH_STATE, state);
			session.setAttribute(SessionConstants.OAUTH_STATE_CREATED_AT, Long.valueOf(System.currentTimeMillis()));

			OAuthProvider oauthProvider = OAuthFactory.get(providerLower);
			String callbackUrl = buildCallbackUrl(request, providerLower);
			String authUrl = oauthProvider.getAuthorizationUrl(callbackUrl, state);
			response.sendRedirect(authUrl);
		} catch (Exception e) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.setAttribute(SessionConstants.OAUTH_ERROR, OAUTH_INIT_FAILED_MESSAGE);
			}
			response.sendRedirect(request.getContextPath() + RouteConstants.Client.SIGNIN);
		}
	}

	private String buildCallbackUrl(HttpServletRequest request, String providerLower) {
		return request.getScheme()
				+ "://"
				+ request.getServerName()
				+ ":"
				+ request.getServerPort()
				+ request.getContextPath()
				+ RouteConstants.Client.OAUTH_CALLBACK
				+ "?"
				+ RequestParamConstants.OAuth.PROVIDER
				+ "="
				+ providerLower;
	}

	private static String generateState() {
		byte[] bytes = new byte[32];
		SECURE_RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}