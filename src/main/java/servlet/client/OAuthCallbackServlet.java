package servlet.client;

import beans.User;
import constants.RequestParamConstants;
import constants.RouteConstants;
import constants.SessionConstants;
import dto.oauth.OAuthUserResponse;
import exception.BusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import helpers.ErrorHelper;
import helpers.SessionHelper;
import service.oauth.OAuthFactory;
import service.oauth.OAuthProvider;
import service.oauth.OAuthService;
import java.io.IOException;

@WebServlet(name = "OAuthCallbackServlet", value = "/oauth-callback")
public class OAuthCallbackServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final OAuthService oAuthService = new OAuthService();
	private static final long STATE_TTL_MS = 5 * 60 * 1000L;
	private static final String OAUTH_SESSION_INVALID_MESSAGE = "Phiên OAuth đã hết hạn hoặc không hợp lệ. Vui lòng thử lại.";
	private static final String OAUTH_REQUEST_INVALID_MESSAGE = "Phiên OAuth không hợp lệ. Vui lòng thử lại.";
	private static final String OAUTH_STATE_INVALID_MESSAGE = "Phiên OAuth không hợp lệ hoặc đã hết hạn. Vui lòng thử lại.";
	private static final String OAUTH_PROVIDER_INVALID_MESSAGE = "Không thể xác định OAuth provider. Vui lòng thử lại.";
	private static final String OAUTH_LOGIN_FAILED_MESSAGE = "Đăng nhập OAuth thất bại. Vui lòng thử lại.";
	private static final String OAUTH_ACCESS_DENIED_MESSAGE = "Bạn đã hủy đăng nhập OAuth.";
	private static final String OAUTH_PROVIDER_ERROR_MESSAGE = "OAuth provider trả về lỗi. Vui lòng thử lại.";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String provider = request.getParameter(RequestParamConstants.OAuth.PROVIDER);
		String code = request.getParameter(RequestParamConstants.OAuth.CODE);
		String state = request.getParameter(RequestParamConstants.OAuth.STATE);
		String error = request.getParameter(RequestParamConstants.ERROR);

		try {
			HttpSession session = request.getSession(false);
			if (session == null) {
				redirectToSigninWithOAuthError(request, response, OAUTH_SESSION_INVALID_MESSAGE);
				return;
			}

			if (error != null && !error.trim().isEmpty()) {
				redirectToSigninWithOAuthError(request, response, resolveOAuthProviderErrorMessage(error));
				return;
			}

			if (code == null || code.trim().isEmpty() || state == null || state.trim().isEmpty()) {
				redirectToSigninWithOAuthError(request, response, OAUTH_REQUEST_INVALID_MESSAGE);
				return;
			}

			if (!isValidState(session, state)) {
				redirectToSigninWithOAuthError(request, response, OAUTH_STATE_INVALID_MESSAGE);
				return;
			}

			if (provider == null || provider.trim().isEmpty()) {
				redirectToSigninWithOAuthError(request, response, OAUTH_PROVIDER_INVALID_MESSAGE);
				return;
			}

			String providerLower = provider.trim().toLowerCase();
			String callbackUrl = buildCallbackUrl(request, providerLower);
			OAuthProvider oauthProvider = OAuthFactory.get(providerLower);

			OAuthUserResponse oauthUser = oauthProvider.getUser(code, callbackUrl);
			User user = oAuthService.handleOAuthCallback(oauthUser, providerLower);

			session = SessionHelper.renewSession(request);
			SessionHelper.setCurrentUser(session, user);

			response.sendRedirect(request.getContextPath() + RouteConstants.Client.HOME);
		} catch (BusinessException e) {
			String message = ErrorHelper.firstMessage(ErrorHelper.fromBusiness(e), OAUTH_LOGIN_FAILED_MESSAGE);
			redirectToSigninWithOAuthError(request, response, message);
		} catch (Exception e) {
			redirectToSigninWithOAuthError(request, response, OAUTH_LOGIN_FAILED_MESSAGE);
		}
	}

	private String resolveOAuthProviderErrorMessage(String error) {
		if ("access_denied".equalsIgnoreCase(error)) {
			return OAUTH_ACCESS_DENIED_MESSAGE;
		}
		return OAUTH_PROVIDER_ERROR_MESSAGE;
	}

	private void redirectToSigninWithOAuthError(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
		HttpSession session = request.getSession(true);
		session.setAttribute(SessionConstants.OAUTH_ERROR, message);
		response.sendRedirect(request.getContextPath() + RouteConstants.Client.SIGNIN);
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

	private boolean isValidState(HttpSession session, String requestState) {
		Object expectedObj = session.getAttribute(SessionConstants.OAUTH_STATE);
		Object createdAtObj = session.getAttribute(SessionConstants.OAUTH_STATE_CREATED_AT);

		String expected = (expectedObj instanceof String) ? (String) expectedObj : null;
		Long createdAt = (createdAtObj instanceof Long) ? (Long) createdAtObj : null;

		session.removeAttribute(SessionConstants.OAUTH_STATE);
		session.removeAttribute(SessionConstants.OAUTH_STATE_CREATED_AT);

		if (expected == null || createdAt == null) {
			return false;
		}
		if (!expected.equals(requestState)) {
			return false;
		}

		long now = System.currentTimeMillis();
		long ageMs = now - createdAt.longValue();
		return ageMs >= 0 && ageMs <= STATE_TTL_MS;
	}
}
