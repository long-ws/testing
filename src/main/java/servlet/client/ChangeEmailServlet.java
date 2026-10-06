package servlet.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import beans.User;
import constants.RequestParamConstants;
import constants.RouteConstants;
import constants.SessionConstants;
import constants.ViewAttributeConstants;
import constants.ViewPathConstants;
import context.PendingEmailChangeContext;
import dto.user.ChangeEmailRequest;
import dto.user.ChangeEmailVerificationRequest;
import dto.user.VerificationChallenge;
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
import service.EmailChangeService;
import service.EmailChangeVerificationService;
import service.UserProfileService;
import service.UserProfileServiceImpl;
import validator.core.ValidationResult;
import validator.user.ChangeEmailValidator;
import validator.user.ChangeEmailVerificationValidator;

@WebServlet(name = "ChangeEmailServlet", value = "/security/changeEmail")
public class ChangeEmailServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String STEP_VERIFY = "verify";
	private static final String CHANGE_EMAIL_CODES_SENT_MESSAGE = "Đã gửi mã xác thực tới email hiện tại và email mới, vui lòng nhập đầy đủ 2 mã để hoàn tất";
	private static final String CHANGE_EMAIL_SUCCESS_MESSAGE = "Đã cập nhật email thành công";
	private static final String CHANGE_EMAIL_SEND_FAILED_MESSAGE = "Không thể gửi mã xác thực lúc này, vui lòng thử lại sau";
	private static final String CHANGE_EMAIL_EXPIRED_MESSAGE = "Phiên xác thực đổi email đã hết hạn. Vui lòng thực hiện lại";
	private static final String CHANGE_EMAIL_BACK_ROUTE = RouteConstants.Client.SECURITY_CHANGE_EMAIL;
	private static final String SECURITY_BACK_ROUTE = RouteConstants.Client.SECURITY;

	private final EmailChangeService emailChangeService = new EmailChangeService();
	private final EmailChangeVerificationService emailChangeVerificationService = new EmailChangeVerificationService();
	private final UserProfileService userProfileService = new UserProfileServiceImpl();
	private final ChangeEmailValidator changeEmailValidator = new ChangeEmailValidator();
	private final ChangeEmailVerificationValidator changeEmailVerificationValidator = new ChangeEmailVerificationValidator();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		User currentUser = getCurrentUser(session);
		if (currentUser == null) {
			response.sendRedirect(request.getContextPath() + RouteConstants.Client.SIGNIN);
			return;
		}

		PendingEmailChangeContext pendingContext = getActivePendingEmailChange(session);
		boolean verifyStep = isVerifyStep(request) && pendingContext != null;
		if (isVerifyStep(request) && pendingContext == null) {
			MessageHelper.setErrorMessage(session, CHANGE_EMAIL_EXPIRED_MESSAGE);
			response.sendRedirect(request.getContextPath() + RouteConstants.Client.SECURITY_CHANGE_EMAIL);
			return;
		}

		request.setAttribute(ViewAttributeConstants.Security.PAGE, ViewAttributeConstants.Security.PAGE_CHANGE_EMAIL);
		request.setAttribute(ViewAttributeConstants.Security.CHANGE_EMAIL_STEP,
				verifyStep ? ViewAttributeConstants.Security.CHANGE_EMAIL_STEP_VERIFY : ViewAttributeConstants.Security.CHANGE_EMAIL_STEP_INPUT);
		request.setAttribute(ViewAttributeConstants.Security.BACK_ROUTE, verifyStep ? CHANGE_EMAIL_BACK_ROUTE : SECURITY_BACK_ROUTE);
		if (pendingContext != null) {
			request.setAttribute(ViewAttributeConstants.VALUES, buildPendingValues(pendingContext));
		}
		FormRenderHelper.forward(request, response, ViewPathConstants.Client.SECURITY);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(true);
		User currentUser = getCurrentUser(session);
		if (currentUser == null) {
			response.sendRedirect(request.getContextPath() + RouteConstants.Client.SIGNIN);
			return;
		}

		if (isVerifyStep(request)) {
			handleVerificationSubmit(request, response, session, currentUser);
			return;
		}
		handleInitialSubmit(request, response, session, currentUser);
	}

	private void handleInitialSubmit(HttpServletRequest request, HttpServletResponse response, HttpSession session, User currentUser) throws IOException {
		ChangeEmailRequest dto = buildChangeEmailRequest(request);
		Map<String, String> values = buildInitialValues(dto);

		ValidationResult validationResult = changeEmailValidator.validate(dto);
		if (validationResult.hasErrors()) {
			redirectWithErrors(request, response, validationResult.getErrors(), values, false);
			return;
		}

		try {
			emailChangeService.validateChangeRequest(currentUser.getId(), dto);
		} catch (BusinessException e) {
			redirectWithErrors(request, response, ErrorHelper.fromBusiness(e), values, false);
			return;
		}

		VerificationChallenge verificationChallenge = emailChangeVerificationService.createVerificationChallenge(
				normalize(dto.getCurrentEmail()), normalize(dto.getNewEmail()));
		boolean sent = emailChangeVerificationService.sendVerificationCodes(verificationChallenge);
		if (!sent) {
			redirectWithErrors(request, response, ErrorHelper.withGlobal(CHANGE_EMAIL_SEND_FAILED_MESSAGE), values, false);
			return;
		}

		SessionHelper.setPendingEmailChange(session, verificationChallenge.getPendingEmailChange());
		MessageHelper.setSuccessMessage(session, CHANGE_EMAIL_CODES_SENT_MESSAGE);
		response.sendRedirect(request.getContextPath() + buildChangeEmailUrl(true));
	}

	private void handleVerificationSubmit(HttpServletRequest request, HttpServletResponse response, HttpSession session, User currentUser) throws IOException {
		PendingEmailChangeContext pendingContext = getActivePendingEmailChange(session);
		if (pendingContext == null) {
			MessageHelper.setErrorMessage(session, CHANGE_EMAIL_EXPIRED_MESSAGE);
			response.sendRedirect(request.getContextPath() + RouteConstants.Client.SECURITY_CHANGE_EMAIL);
			return;
		}

		ChangeEmailVerificationRequest dto = buildChangeEmailVerificationRequest(request);
		ValidationResult validationResult = changeEmailVerificationValidator.validate(dto);
		Map<String, String> errors = validationResult.getErrors();
		if (!validationResult.hasErrors()) {
			errors.putAll(emailChangeVerificationService.validateVerificationCodes(dto, pendingContext));
		}

		if (!errors.isEmpty()) {
			redirectWithErrors(request, response, errors, null, true);
			return;
		}

		try {
			emailChangeService.applyVerifiedChange(currentUser.getId(), pendingContext.getCurrentEmail(), pendingContext.getNewEmail());
			refreshCurrentUser(session, currentUser.getId());
			SessionHelper.clearPendingEmailChange(session);
			MessageHelper.setSuccessMessage(session, CHANGE_EMAIL_SUCCESS_MESSAGE);
			response.sendRedirect(request.getContextPath() + RouteConstants.Client.SECURITY_CHANGE_EMAIL);
		} catch (BusinessException e) {
			SessionHelper.clearPendingEmailChange(session);
			redirectWithErrors(request, response, ErrorHelper.fromBusiness(e), buildPendingValues(pendingContext), false);
		}
	}

	private ChangeEmailRequest buildChangeEmailRequest(HttpServletRequest request) {
		return new ChangeEmailRequest.Builder()
				.currentEmail(request.getParameter(RequestParamConstants.User.CURRENT_EMAIL))
				.currentPassword(request.getParameter(RequestParamConstants.User.CURRENT_PASSWORD))
				.newEmail(request.getParameter(RequestParamConstants.User.NEW_EMAIL))
				.build();
	}

	private ChangeEmailVerificationRequest buildChangeEmailVerificationRequest(HttpServletRequest request) {
		return new ChangeEmailVerificationRequest.Builder()
				.currentEmailCode(request.getParameter(RequestParamConstants.User.CURRENT_EMAIL_CODE))
				.newEmailCode(request.getParameter(RequestParamConstants.User.NEW_EMAIL_CODE))
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

	private PendingEmailChangeContext getActivePendingEmailChange(HttpSession session) {
		PendingEmailChangeContext pendingEmailChange = SessionHelper.getPendingEmailChange(session);
		if (pendingEmailChange == null) {
			return null;
		}
		if (pendingEmailChange.isExpired(System.currentTimeMillis())) {
			SessionHelper.clearPendingEmailChange(session);
			return null;
		}
		return pendingEmailChange;
	}

	private Map<String, String> buildInitialValues(ChangeEmailRequest dto) {
		return buildEmailValues(normalize(dto.getCurrentEmail()), normalize(dto.getNewEmail()));
	}

	private Map<String, String> buildPendingValues(PendingEmailChangeContext pendingContext) {
		return buildEmailValues(pendingContext.getCurrentEmail(), pendingContext.getNewEmail());
	}

	private Map<String, String> buildEmailValues(String currentEmail, String newEmail) {
		Map<String, String> values = new HashMap<>();
		values.put(RequestParamConstants.User.CURRENT_EMAIL, currentEmail);
		values.put(RequestParamConstants.User.NEW_EMAIL, newEmail);
		return values;
	}

	private void redirectWithErrors(HttpServletRequest request, HttpServletResponse response, Map<String, String> errors, Map<String, String> values, boolean verifyStep)
			throws IOException {
		HttpSession session = request.getSession(true);
		session.setAttribute(ViewAttributeConstants.ERRORS, errors);
		if (values != null) {
			session.setAttribute(ViewAttributeConstants.VALUES, values);
		}
		response.sendRedirect(request.getContextPath() + buildChangeEmailUrl(verifyStep));
	}

	private String buildChangeEmailUrl(boolean verifyStep) {
		if (!verifyStep) {
			return RouteConstants.Client.SECURITY_CHANGE_EMAIL;
		}
		return RouteConstants.Client.SECURITY_CHANGE_EMAIL + "?" + RequestParamConstants.STEP + "=" + STEP_VERIFY;
	}

	private boolean isVerifyStep(HttpServletRequest request) {
		return STEP_VERIFY.equalsIgnoreCase(request.getParameter(RequestParamConstants.STEP));
	}

	private String normalize(String value) {
		return value == null ? null : value.trim();
	}
}
