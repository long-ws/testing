package servlet.admin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import constants.RequestParamConstants;
import constants.RouteConstants;
import constants.ViewAttributeConstants;
import constants.ViewPathConstants;
import dto.user.AdminAuthenticationResult;
import dto.user.AdminSigninRequest;
import exception.BusinessException;
import helpers.ErrorHelper;
import helpers.FormRenderHelper;
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
import validator.user.AdminSigninValidator;

@WebServlet(name = "SigninAdminServlet", value = "/admin/signin")
public class SigninAdminServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final AuthenticationService authenticationService = new AuthenticationServiceImpl();
	private final AdminSigninValidator adminSigninValidator = new AdminSigninValidator();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		FormRenderHelper.forward(request, response, ViewPathConstants.Admin.SIGNIN);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String username = request.getParameter(RequestParamConstants.Auth.USERNAME);
		String password = request.getParameter(RequestParamConstants.Auth.PASSWORD);

		Map<String, String> values = new HashMap<>();
		values.put(RequestParamConstants.Auth.USERNAME, username);

		Map<String, String> errors = new HashMap<>();

		AdminAuthenticationResult authResult = null;

		try {
			AdminSigninRequest adminSigninRequest = new AdminSigninRequest.Builder().username(username).password(password).build();

			ValidationResult validationResult = adminSigninValidator.validate(adminSigninRequest);
			if (validationResult.hasErrors()) {
				errors.putAll(validationResult.getErrors());
			} else {
				authResult = authenticationService.authenticateAdminWithResult(adminSigninRequest);
			}
		} catch (BusinessException e) {
			errors.putAll(ErrorHelper.fromBusiness(e));
		}

		if (!errors.isEmpty() || authResult == null) {
			redirectWithErrors(request, response, values, errors);
			return;
		}

		HttpSession session = SessionHelper.renewSession(request);
		SessionHelper.setCurrentUser(session, authResult.getUser());
		SessionHelper.setSuperAdmin(session, authResult.isSuperAdmin());
		SessionHelper.setUserPermissions(session, authResult.getPermissionCodes());
		response.sendRedirect(request.getContextPath() + RouteConstants.Admin.HOME);
	}

	private void redirectWithErrors(HttpServletRequest request, HttpServletResponse response, Map<String, String> values, Map<String, String> errors) throws IOException {
		HttpSession session = request.getSession();
		session.setAttribute(ViewAttributeConstants.VALUES, values);
		session.setAttribute(ViewAttributeConstants.ERRORS, errors);
		response.sendRedirect(request.getContextPath() + RouteConstants.Admin.SIGNIN);
	}
}
