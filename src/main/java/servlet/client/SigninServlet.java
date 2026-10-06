package servlet.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import beans.User;
import constants.RequestParamConstants;
import constants.RouteConstants;
import constants.ViewAttributeConstants;
import constants.ViewPathConstants;
import dto.user.SigninRequest;
import dto.user.UserAuthenticationResult;
import exception.BusinessException;
import helpers.ErrorHelper;
import helpers.FormRenderHelper;
import helpers.MessageHelper;
import helpers.SessionHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.AuthenticationService;
import service.AuthenticationServiceImpl;
import validator.core.ValidationResult;
import validator.user.SigninValidator;

@WebServlet(name = "SigninServlet", value = "/signin")
public class SigninServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String UNVERIFIED_EMAIL_MESSAGE = "Email chưa được xác thực. Vui lòng kiểm tra hộp thư để xác thực.";
	private static final String UNVERIFIED_EMAIL_MISSING_MESSAGE = "Email chưa được xác thực.";

	private final AuthenticationService authenticationService = new AuthenticationServiceImpl();
	private final SigninValidator signinValidator = new SigninValidator();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		FormRenderHelper.forward(request, response, ViewPathConstants.Client.SIGNIN);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter(RequestParamConstants.User.USERNAME);
		String password = request.getParameter(RequestParamConstants.User.PASSWORD);

		Map<String, String> values = new HashMap<>();
		values.put(RequestParamConstants.User.USERNAME, username);

		Map<String, String> errors = new HashMap<>();
		UserAuthenticationResult authResult = null;

		try {
			SigninRequest signinRequest = new SigninRequest.Builder().username(username).password(password).build();
			ValidationResult validationResult = signinValidator.validate(signinRequest);
			if (validationResult.hasErrors()) {
				errors.putAll(validationResult.getErrors());
			} else {
				authResult = authenticationService.authenticateWithResult(signinRequest);
			}
		} catch (BusinessException e) {
			errors.putAll(ErrorHelper.fromBusiness(e));
		}

		if (!errors.isEmpty() || authResult == null) {
			redirectWithErrors(request, response, values, errors);
			return;
		}

		User userFromServer = authResult.getUser();
		boolean isSuperAdmin = authResult.isSuperAdmin();

		if (!isSuperAdmin && !userFromServer.isEmailVerified()) {
			handleUnverifiedEmail(request, response, userFromServer, userFromServer.getEmail(), values, errors);
			return;
		}

		handleSuccessfulLogin(request, response, userFromServer, isSuperAdmin);
	}

	private void redirectWithErrors(HttpServletRequest request, HttpServletResponse response, Map<String, String> values, Map<String, String> errors) throws IOException {
		HttpSession session = request.getSession();
		session.setAttribute(ViewAttributeConstants.VALUES, values);
		session.setAttribute(ViewAttributeConstants.ERRORS, errors);
		response.sendRedirect(request.getContextPath() + RouteConstants.Client.SIGNIN);
	}

	private void handleUnverifiedEmail(HttpServletRequest request, HttpServletResponse response, User user, String email, Map<String, String> values, Map<String, String> errors) throws ServletException, IOException {
		if (email == null || email.isBlank()) {
			errors.putAll(ErrorHelper.withGlobal(UNVERIFIED_EMAIL_MISSING_MESSAGE));
			redirectWithErrors(request, response, values, errors);
			return;
		}

		HttpSession session = SessionHelper.renewSession(request);
		SessionHelper.setPendingVerification(session, user.getId(), email);
		MessageHelper.setErrorMessage(session, UNVERIFIED_EMAIL_MESSAGE);
		response.sendRedirect(request.getContextPath() + RouteConstants.Client.VERIFY_EMAIL);
	}

	private void handleSuccessfulLogin(HttpServletRequest request, HttpServletResponse response, User user, boolean isSuperAdmin) throws IOException {
		HttpSession session = SessionHelper.renewSession(request);
		SessionHelper.setCurrentUser(session, user);
		if (isSuperAdmin) {
			SessionHelper.setSuperAdmin(session, true);
		}
		response.sendRedirect(request.getContextPath() + RouteConstants.Client.HOME);
	}
}