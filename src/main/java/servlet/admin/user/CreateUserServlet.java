package servlet.admin.user;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import beans.common.Role;
import constants.RequestParamConstants;
import constants.RouteConstants;
import constants.ViewAttributeConstants;
import constants.ViewPathConstants;
import dto.user.UserCreateRequest;
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
import validator.user.UserCreateValidator;

@WebServlet(name = "CreateUserServlet", urlPatterns = "/admin/user/create")
public class CreateUserServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String USER_CREATED_MESSAGE = "Đã tạo người dùng: ";
	private final UserManagementService userManagementService = new UserManagementServiceImpl();
	private final RoleService roleService = new RoleServiceImpl();
	private final LanguageService languageService = new LanguageServiceImpl();
	private final GenderService genderService = new GenderServiceImpl();
	private final UserCreateValidator userCreateValidator = new UserCreateValidator();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		renderCreateView(request, response, null, null);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter(RequestParamConstants.User.USERNAME);
		String fullname = request.getParameter(RequestParamConstants.User.FULLNAME);
		String email = request.getParameter(RequestParamConstants.User.EMAIL);
		String phoneNumber = request.getParameter(RequestParamConstants.User.PHONE_NUMBER);
		String genderCode = request.getParameter(RequestParamConstants.User.GENDER);
		String roleStr = request.getParameter(RequestParamConstants.User.ROLE);
		String preferredLanguageCode = request.getParameter(RequestParamConstants.User.PREFERRED_LANGUAGE_ID);

		Map<String, String> values = new HashMap<>();
		values.put(RequestParamConstants.User.USERNAME, username);
		values.put(RequestParamConstants.User.FULLNAME, fullname);
		values.put(RequestParamConstants.User.EMAIL, email);
		values.put(RequestParamConstants.User.PHONE_NUMBER, phoneNumber);
		values.put(RequestParamConstants.User.GENDER, genderCode);
		values.put(RequestParamConstants.User.ROLE, roleStr);
		values.put(RequestParamConstants.User.PREFERRED_LANGUAGE_ID, preferredLanguageCode);

		Role role = null;
		if (roleStr != null && !roleStr.trim().isEmpty()) {
			role = new Role();
			role.setCode(roleStr);
		}

		UserCreateRequest dto = new UserCreateRequest.Builder().username(username).password(request.getParameter(RequestParamConstants.User.PASSWORD)).fullname(fullname).email(email)
				.phoneNumber(phoneNumber).genderCode(genderCode).role(role).preferredLanguageCode(preferredLanguageCode).build();

		ValidationResult validationResult = userCreateValidator.validate(dto);
		if (validationResult.hasErrors()) {
			redirectWithErrors(request, response, values, validationResult.getErrors());
			return;
		}

		try {
			userManagementService.createUser(dto);
		} catch (BusinessException e) {
			redirectWithErrors(request, response, values, ErrorHelper.fromBusiness(e));
			return;
		}

		SessionPermissionCache.clear(request.getSession());
		MessageHelper.setSuccessMessage(request.getSession(), USER_CREATED_MESSAGE + dto.getUsername());
		response.sendRedirect(request.getContextPath() + RouteConstants.Admin.USER_CREATE);
	}

	private void redirectWithErrors(HttpServletRequest request, HttpServletResponse response, Map<String, String> values, Map<String, String> errors) throws IOException {
		HttpSession session = request.getSession();
		if (values != null) {
			session.setAttribute(ViewAttributeConstants.VALUES, values);
		}
		session.setAttribute(ViewAttributeConstants.ERRORS, errors);
		response.sendRedirect(request.getContextPath() + RouteConstants.Admin.USER_CREATE);
	}

	private void loadDropdownData(HttpServletRequest request) {
		request.setAttribute(ViewAttributeConstants.User.ALL_ROLES, roleService.getAllActiveNonSystemRoles());
		request.setAttribute(ViewAttributeConstants.User.LANGUAGES, languageService.getAllActiveLanguages());
		request.setAttribute(ViewAttributeConstants.User.GENDERS, genderService.getAllActiveGenders());
	}

	private Map<String, String> loadDropdownErrors(HttpServletRequest request) {
		try {
			loadDropdownData(request);
			return new HashMap<>();
		} catch (BusinessException e) {
			return ErrorHelper.fromBusiness(e);
		}
	}

	private void renderCreateView(HttpServletRequest request, HttpServletResponse response, Map<String, String> values, Map<String, String> errors)
			throws ServletException, IOException {
		if (values != null) {
			request.setAttribute(ViewAttributeConstants.VALUES, values);
		}
		UserFormStateHelper.applyCreateSelectedFormState(request, values);
		FormRenderHelper.forwardWithErrors(request, response, ViewPathConstants.Admin.USER_CREATE, ErrorHelper.merge(errors, loadDropdownErrors(request)));
	}
}