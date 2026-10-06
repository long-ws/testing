package servlet.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import beans.User;
import beans.common.Gender;
import beans.common.Language;
import constants.RequestParamConstants;
import constants.RouteConstants;
import constants.SessionConstants;
import constants.ViewAttributeConstants;
import constants.ViewPathConstants;
import dto.user.UserProfileRequest;
import domain.user.UserDefaults;
import exception.BusinessException;
import helpers.ErrorHelper;
import helpers.FormRenderHelper;
import helpers.MessageHelper;
import helpers.SessionHelper;
import helpers.UserProfileViewHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.GenderService;
import service.GenderServiceImpl;
import service.LanguageService;
import service.LanguageServiceImpl;
import service.UserProfileService;
import service.UserProfileServiceImpl;
import validator.core.ValidationResult;
import validator.user.UserProfileValidator;

@WebServlet(name = "UserServlet", value = "/user")
public class UserServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_AVATAR_PATH = "/images/default-avatar.svg";
	private static final String PROFILE_UPDATED_MESSAGE = "Cập nhật thành công!";

	private final UserProfileService userProfileService = new UserProfileServiceImpl();
	private final GenderService genderService = new GenderServiceImpl();
	private final LanguageService languageService = new LanguageServiceImpl();
	private final UserProfileValidator userProfileValidator = new UserProfileValidator();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		renderUserView(request, response, null, null);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		User currentUser = getCurrentUser(session);
		if (currentUser == null) {
			response.sendRedirect(request.getContextPath() + RouteConstants.Client.SIGNIN);
			return;
		}

		UserProfileRequest dto = buildUserProfileRequest(request);

		ValidationResult validationResult = userProfileValidator.validate(dto);
		if (validationResult.hasErrors()) {
			redirectWithFormErrors(request, response, dto, validationResult.getErrors());
			return;
		}

		try {
			userProfileService.updateUserProfile(currentUser.getId(), dto);
			refreshCurrentUser(session, currentUser.getId());
			MessageHelper.setSuccessMessage(session, PROFILE_UPDATED_MESSAGE);
			response.sendRedirect(request.getContextPath() + RouteConstants.Client.USER);
		} catch (BusinessException e) {
			redirectWithFormErrors(request, response, dto, ErrorHelper.fromBusiness(e));
		}
	}

	private UserProfileRequest buildUserProfileRequest(HttpServletRequest request) {
		return new UserProfileRequest.Builder()
				.fullname(request.getParameter(RequestParamConstants.User.FULLNAME))
				.phoneNumber(request.getParameter(RequestParamConstants.User.PHONE_NUMBER))
				.avatarUrl(request.getParameter(RequestParamConstants.User.AVATAR_URL))
				.genderCode(request.getParameter(RequestParamConstants.User.GENDER))
				.preferredLanguageCode(request.getParameter(RequestParamConstants.User.PREFERRED_LANGUAGE))
				.build();
	}

	private void refreshCurrentUser(HttpSession session, long userId) throws BusinessException {
		User updatedUser = userProfileService.getById(userId);
		SessionHelper.setCurrentUser(session, updatedUser);
	}

	private void loadReferenceData(HttpServletRequest request) throws BusinessException {
		List<Gender> genders = genderService.getAllActiveGenders();
		List<Language> languages = languageService.getAllActiveLanguages();
		request.setAttribute(ViewAttributeConstants.User.GENDERS, genders);
		request.setAttribute(ViewAttributeConstants.User.LANGUAGES, languages);
	}

	private Map<String, String> loadReferenceDataErrors(HttpServletRequest request) {
		try {
			loadReferenceData(request);
			return new HashMap<>();
		} catch (BusinessException e) {
			return ErrorHelper.fromBusiness(e);
		}
	}

	private User getCurrentUser(HttpSession session) {
		if (session == null) {
			return null;
		}
		return (User) session.getAttribute(SessionConstants.CURRENT_USER);
	}

	private void renderUserView(HttpServletRequest request, HttpServletResponse response, UserProfileRequest formValue, Map<String, String> errors) throws ServletException, IOException {
		Map<String, String> mergedErrors = ErrorHelper.merge(errors, loadReferenceDataErrors(request));
		UserProfileViewHelper.applyDisplayState(
				request,
				getCurrentUser(request.getSession(false)),
				formValue,
				(List<Language>) request.getAttribute(ViewAttributeConstants.User.LANGUAGES),
				DEFAULT_AVATAR_PATH);
		if (formValue != null) {
			FormRenderHelper.forwardWithFormState(request, response, ViewPathConstants.Client.USER, ViewAttributeConstants.User.USER, formValue, mergedErrors);
			return;
		}
		FormRenderHelper.forwardWithErrors(request, response, ViewPathConstants.Client.USER, mergedErrors);
	}

	private void redirectWithFormErrors(HttpServletRequest request, HttpServletResponse response, UserProfileRequest formValue, Map<String, String> errors)
			throws IOException {
		HttpSession session = request.getSession();
		if (formValue != null) {
			session.setAttribute(ViewAttributeConstants.User.USER, formValue);
		}
		session.setAttribute(ViewAttributeConstants.ERRORS, errors);
		response.sendRedirect(request.getContextPath() + RouteConstants.Client.USER);
	}
}