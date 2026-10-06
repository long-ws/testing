package servlet.admin.role;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import constants.RequestParamConstants;
import constants.RouteConstants;
import constants.ViewAttributeConstants;
import constants.ViewPathConstants;
import domain.common.RegistryDefaults;
import dto.role.RoleCreateRequest;
import exception.BusinessException;
import helpers.ErrorHelper;
import helpers.FormRenderHelper;
import helpers.MessageHelper;
import helpers.RequestParamHelper;
import helpers.SessionPermissionCache;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.RoleService;
import service.RoleServiceImpl;
import validator.core.ValidationResult;
import validator.role.RoleCreateValidator;

@WebServlet(name = "CreateRoleServlet", urlPatterns = "/admin/role/create")
public class CreateRoleServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String ROLE_CREATED_MESSAGE = "Đã tạo vai trò: ";
	private final RoleService roleService = new RoleServiceImpl();
	private final RoleCreateValidator roleCreateValidator = new RoleCreateValidator();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		renderCreateView(request, response, new RoleCreateRequest.Builder().build(), null);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		RoleCreateRequest dto = new RoleCreateRequest.Builder().code(normalizeCode(request.getParameter(RequestParamConstants.CODE))).name(request.getParameter(RequestParamConstants.NAME))
				.description(request.getParameter(RequestParamConstants.DESCRIPTION)).isSystem(RegistryDefaults.DEFAULT_IS_SYSTEM).isActive(RegistryDefaults.DEFAULT_IS_ACTIVE)
				.permissionIds(RequestParamHelper.parseIntegerList(request.getParameterValues(RequestParamConstants.Role.PERMISSION_IDS))).build();

		ValidationResult validationResult = roleCreateValidator.validate(dto);
		if (validationResult.hasErrors()) {
			redirectToCreateWithErrors(request, response, dto, validationResult.getErrors());
			return;
		}

		try {
			roleService.createRole(dto);
			SessionPermissionCache.clear(request.getSession());
			MessageHelper.setSuccessMessage(request.getSession(), ROLE_CREATED_MESSAGE + dto.getCode());
			response.sendRedirect(request.getContextPath() + RouteConstants.Admin.ROLE_CREATE);
		} catch (BusinessException e) {
			redirectToCreateWithErrors(request, response, dto, ErrorHelper.fromBusiness(e));
		}
	}

	private String normalizeCode(String codeParam) {
		if (codeParam == null) {
			return null;
		}
		return codeParam.toUpperCase().trim().replace(" ", "_");
	}

	private void renderCreateView(HttpServletRequest request, HttpServletResponse response, RoleCreateRequest role, Map<String, String> errors) throws ServletException, IOException {
		request.setAttribute(ViewAttributeConstants.Role.ROLE, role);
		FormRenderHelper.forwardWithErrors(request, response, ViewPathConstants.Admin.ROLE_CREATE, errors != null ? errors : new HashMap<String, String>());
	}

	private void redirectToCreateWithErrors(HttpServletRequest request, HttpServletResponse response, RoleCreateRequest role, Map<String, String> errors) throws IOException {
		HttpSession session = request.getSession();
		session.setAttribute(ViewAttributeConstants.Role.ROLE, role);
		session.setAttribute(ViewAttributeConstants.ERRORS, errors);
		response.sendRedirect(request.getContextPath() + RouteConstants.Admin.ROLE_CREATE);
	}
}