package servlet.client;

import java.io.IOException;
import java.util.Map;

import beans.User;
import constants.RequestParamConstants;
import constants.RouteConstants;
import constants.SessionConstants;
import constants.ViewAttributeConstants;
import constants.ViewPathConstants;
import dto.user.ChangePasswordRequest;
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
import service.UserProfileService;
import service.UserProfileServiceImpl;
import validator.core.ValidationResult;
import validator.user.ChangePasswordValidator;

@WebServlet(name = "ChangePasswordServlet", value = "/security/changePassword")
public class ChangePasswordServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String PASSWORD_CHANGED_MESSAGE = "Đổi mật khẩu thành công!";
	private final AuthenticationService authenticationService = new AuthenticationServiceImpl();
	private final UserProfileService userProfileService = new UserProfileServiceImpl();
	private final ChangePasswordValidator changePasswordValidator = new ChangePasswordValidator();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute(ViewAttributeConstants.Security.PAGE, ViewAttributeConstants.Security.PAGE_CHANGE_PASSWORD);
		FormRenderHelper.forward(request, response, ViewPathConstants.Client.SECURITY);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		User currentUser = getCurrentUser(session);
		if (currentUser == null) {
			response.sendRedirect(request.getContextPath() + RouteConstants.Client.SIGNIN);
			return;
		}

		ChangePasswordRequest changePasswordRequest = buildChangePasswordRequest(request);
		ValidationResult validationResult = changePasswordValidator.validate(changePasswordRequest);
		if (validationResult.hasErrors()) {
			redirectWithErrors(request, response, validationResult.getErrors());
			return;
		}

		try {
			authenticationService.changePassword(currentUser.getId(), changePasswordRequest);
			refreshCurrentUser(session, currentUser.getId());
			MessageHelper.setSuccessMessage(session, PASSWORD_CHANGED_MESSAGE);
			response.sendRedirect(request.getContextPath() + RouteConstants.Client.SECURITY_CHANGE_PASSWORD);
		} catch (BusinessException e) {
			redirectWithErrors(request, response, ErrorHelper.fromBusiness(e));
		}
	}

	private ChangePasswordRequest buildChangePasswordRequest(HttpServletRequest request) {
		return new ChangePasswordRequest.Builder()
				.currentPassword(request.getParameter(RequestParamConstants.User.CURRENT_PASSWORD))
				.newPassword(request.getParameter(RequestParamConstants.User.NEW_PASSWORD))
				.confirmPassword(request.getParameter(RequestParamConstants.User.CONFIRM_PASSWORD))
				.build();
	}

	private User getCurrentUser(HttpSession session) {
		if (session == null) {
			return null;
		}
		return (User) session.getAttribute(SessionConstants.CURRENT_USER);
	}

	private void refreshCurrentUser(HttpSession session, long userId) {
		User updatedUser = userProfileService.getById(userId);
		SessionHelper.setCurrentUser(session, updatedUser);
	}

	private void redirectWithErrors(HttpServletRequest request, HttpServletResponse response, Map<String, String> errors) throws IOException {
		HttpSession session = request.getSession();
		session.setAttribute(ViewAttributeConstants.ERRORS, errors);
		response.sendRedirect(request.getContextPath() + RouteConstants.Client.SECURITY_CHANGE_PASSWORD);
	}
}
