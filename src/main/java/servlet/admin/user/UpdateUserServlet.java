package servlet.admin.user;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import beans.common.Role;
import constants.RequestParamConstants;
import constants.RouteConstants;
import constants.ViewAttributeConstants;
import constants.ViewPathConstants;
import dto.user.UserDetailResponse;
import dto.user.UserUpdateRequest;
import domain.user.UserDefaults;
import exception.BusinessException;
import helpers.ErrorHelper;
import helpers.FormRenderHelper;
import helpers.MessageHelper;
import helpers.SessionPermissionCache;
import helpers.UserFormStateHelper;
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
import service.RoleService;
import service.RoleServiceImpl;
import service.UserManagementService;
import service.UserManagementServiceImpl;
import validator.core.ValidationResult;
import validator.user.UserUpdateValidator;

@WebServlet(name = "UpdateUserServlet", urlPatterns = "/admin/user/update")
public class UpdateUserServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String INVALID_USER_ID_MESSAGE = "ID người dùng không hợp lệ.";
	private static final String USER_NOT_FOUND_MESSAGE = "Người dùng không tồn tại trên hệ thống.";
	private static final String USER_UPDATED_MESSAGE = "Đã cập nhật người dùng: ";
	private final UserManagementService userManagementService = new UserManagementServiceImpl();
	private final RoleService roleService = new RoleServiceImpl();
	private final LanguageService languageService = new LanguageServiceImpl();
	private final GenderService genderService = new GenderServiceImpl();
	private final UserUpdateValidator userUpdateValidator = new UserUpdateValidator();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String idStr = request.getParameter(RequestParamConstants.ID);
		try {
			long id = parseUserId(idStr);
			UserDetailResponse user = userManagementService.getUserById(id);
			if (user == null) {
				redirectToUserManagerWithError(request, response, USER_NOT_FOUND_MESSAGE);
				return;
			}
			renderUpdateView(request, response, buildValues(userManagementService.toUpdateRequest(user)), null, isSystemUser(user));
		} catch (BusinessException e) {
			redirectToUserManagerWithError(request, response, ErrorHelper.firstMessage(ErrorHelper.fromBusiness(e), USER_NOT_FOUND_MESSAGE));
			return;
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, String> errors = new HashMap<>();
		String idStr = request.getParameter(RequestParamConstants.ID);
		Map<String, String> values = buildSubmittedValues(request, idStr);
		long id = 0;

		try {
			id = parseUserId(idStr);
			UserDetailResponse existingUser = userManagementService.getUserById(id);
			if (existingUser == null) {
				redirectToUserManagerWithError(request, response, USER_NOT_FOUND_MESSAGE);
				return;
			}
		} catch (BusinessException e) {
			redirectToUserManagerWithError(request, response, ErrorHelper.firstMessage(ErrorHelper.fromBusiness(e), INVALID_USER_ID_MESSAGE));
			return;
		}

		String username = request.getParameter(RequestParamConstants.User.USERNAME);
		String fullname = request.getParameter(RequestParamConstants.User.FULLNAME);
		String email = request.getParameter(RequestParamConstants.User.EMAIL);
		String phoneNumber = request.getParameter(RequestParamConstants.User.PHONE_NUMBER);
		String genderCode = request.getParameter(RequestParamConstants.User.GENDER);
		String roleStr = request.getParameter(RequestParamConstants.User.ROLE);
		String preferredLanguageCode = request.getParameter(RequestParamConstants.User.PREFERRED_LANGUAGE_ID);

		Role role = null;
		if (roleStr != null && !roleStr.trim().isEmpty()) {
			role = new Role();
			role.setCode(roleStr);
		}

		UserUpdateRequest dto = new UserUpdateRequest.Builder().id(id).username(username).password(request.getParameter(RequestParamConstants.User.PASSWORD)).fullname(fullname).email(email)
				.phoneNumber(phoneNumber).genderCode(genderCode).role(role).preferredLanguageCode(preferredLanguageCode).build();

		if (errors.isEmpty()) {
			ValidationResult validationResult = userUpdateValidator.validate(dto);
			if (validationResult.hasErrors()) {
				errors.putAll(validationResult.getErrors());
			}
		}

		if (!errors.isEmpty()) {
			redirectToUpdateWithErrors(request, response, id, values, errors);
			return;
		}

		try {
			userManagementService.updateUser(dto);
		} catch (BusinessException e) {
			redirectToUpdateWithErrors(request, response, id, values, ErrorHelper.fromBusiness(e));
			return;
		}

		SessionPermissionCache.clear(request.getSession());
		MessageHelper.setSuccessMessage(request.getSession(), USER_UPDATED_MESSAGE + dto.getUsername());
		response.sendRedirect(request.getContextPath() + RouteConstants.Admin.USER_UPDATE + "?" + RequestParamConstants.ID + "=" + dto.getId());
	}

	private void loadDropdownData(HttpServletRequest request) {
		request.setAttribute(ViewAttributeConstants.User.ALL_ROLES, roleService.getAllActiveNonSystemRoles());
		request.setAttribute(ViewAttributeConstants.User.LANGUAGES, languageService.getAllActiveLanguages());
		request.setAttribute(ViewAttributeConstants.User.GENDERS, genderService.getAllActiveGenders());
	}

	private Map<String, String> buildValues(UserUpdateRequest formDTO) {
		Map<String, String> values = new HashMap<>();
		values.put(RequestParamConstants.ID, String.valueOf(formDTO.getId()));
		values.put(RequestParamConstants.User.USERNAME, formDTO.getUsername());
		values.put(RequestParamConstants.User.FULLNAME, formDTO.getFullname());
		values.put(RequestParamConstants.User.EMAIL, formDTO.getEmail());
		values.put(RequestParamConstants.User.PHONE_NUMBER, formDTO.getPhoneNumber());
		values.put(RequestParamConstants.User.GENDER, formDTO.getGenderCode() != null ? formDTO.getGenderCode() : "");
		values.put(RequestParamConstants.User.ROLE, formDTO.getRole() != null ? formDTO.getRole().getCode() : "");
		values.put(RequestParamConstants.User.PREFERRED_LANGUAGE_ID, formDTO.getPreferredLanguageCode() != null ? formDTO.getPreferredLanguageCode() : "");
		return values;
	}

	private Map<String, String> buildSubmittedValues(HttpServletRequest request, String idStr) {
		Map<String, String> values = new HashMap<>();
		values.put(RequestParamConstants.ID, idStr != null ? idStr.trim() : "");
		values.put(RequestParamConstants.User.USERNAME, request.getParameter(RequestParamConstants.User.USERNAME));
		values.put(RequestParamConstants.User.FULLNAME, request.getParameter(RequestParamConstants.User.FULLNAME));
		values.put(RequestParamConstants.User.EMAIL, request.getParameter(RequestParamConstants.User.EMAIL));
		values.put(RequestParamConstants.User.PHONE_NUMBER, request.getParameter(RequestParamConstants.User.PHONE_NUMBER));
		values.put(RequestParamConstants.User.GENDER, request.getParameter(RequestParamConstants.User.GENDER));
		values.put(RequestParamConstants.User.ROLE, request.getParameter(RequestParamConstants.User.ROLE));
		values.put(RequestParamConstants.User.PREFERRED_LANGUAGE_ID, request.getParameter(RequestParamConstants.User.PREFERRED_LANGUAGE_ID));
		return values;
	}

	private long parseUserId(String idStr) {
		if (idStr == null || idStr.trim().isEmpty()) {
			throw new BusinessException(RequestParamConstants.ID, INVALID_USER_ID_MESSAGE);
		}
		try {
			return Long.parseLong(idStr.trim());
		} catch (NumberFormatException e) {
			throw new BusinessException(RequestParamConstants.ID, INVALID_USER_ID_MESSAGE);
		}
	}

	private boolean isSystemUser(UserDetailResponse user) {
		return user.getRole() != null && user.getRole().isSystem();
	}

	private Map<String, String> loadDropdownErrors(HttpServletRequest request) {
		try {
			loadDropdownData(request);
			return new HashMap<>();
		} catch (BusinessException e) {
			return ErrorHelper.fromBusiness(e);
		}
	}

	private void renderUpdateView(HttpServletRequest request, HttpServletResponse response, Map<String, String> values, Map<String, String> errors,
			boolean isSystemUser) throws ServletException, IOException {
		request.setAttribute(ViewAttributeConstants.VALUES, values);
		request.setAttribute(ViewAttributeConstants.User.IS_SYSTEM, isSystemUser);
		UserFormStateHelper.applyUpdateSelectedFormState(request, values);
		FormRenderHelper.forwardWithErrors(request, response, ViewPathConstants.Admin.USER_UPDATE, ErrorHelper.merge(errors, loadDropdownErrors(request)));
	}

	private void redirectToUpdateWithErrors(HttpServletRequest request, HttpServletResponse response, long userId, Map<String, String> values, Map<String, String> errors)
			throws IOException {
		HttpSession session = request.getSession();
		session.setAttribute(ViewAttributeConstants.VALUES, values);
		session.setAttribute(ViewAttributeConstants.ERRORS, errors);
		response.sendRedirect(request.getContextPath() + RouteConstants.Admin.USER_UPDATE + "?" + RequestParamConstants.ID + "=" + userId);
	}

	private void redirectToUserManagerWithError(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
		MessageHelper.setErrorMessage(request.getSession(), message);
		response.sendRedirect(request.getContextPath() + RouteConstants.Admin.USER);
	}
}