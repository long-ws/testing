package servlet.admin.role;

import java.io.IOException;
import java.util.Map;

import constants.RequestParamConstants;
import constants.RouteConstants;
import constants.ViewAttributeConstants;
import constants.ViewPathConstants;
import dto.role.RoleEditFormData;
import dto.role.RoleUpdateRequest;
import exception.BusinessException;
import helpers.ErrorHelper;
import helpers.FormRenderHelper;
import helpers.MessageHelper;
import helpers.RequestParamHelper;
import helpers.RoleEditFormViewHelper;
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
import validator.role.RoleUpdateValidator;

@WebServlet(name = "UpdateRoleServlet", urlPatterns = "/admin/role/update")
public class UpdateRoleServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String INVALID_ROLE_ID_MESSAGE = "ID vai trò không hợp lệ.";
	private static final String ROLE_NOT_FOUND_MESSAGE = "Vai trò không tồn tại trên hệ thống.";
	private static final String ROLE_UPDATED_MESSAGE = "Đã cập nhật vai trò: ";
	private final RoleService roleService = new RoleServiceImpl();
	private final RoleUpdateValidator roleUpdateValidator = new RoleUpdateValidator();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			Integer roleId = parseRoleId(request.getParameter(RequestParamConstants.ID));
			RoleEditFormData form = roleService.getRoleEditForm(roleId);
			if (form == null) {
				redirectToRoleManagerWithError(request, response, ROLE_NOT_FOUND_MESSAGE);
				return;
			}
			RoleEditFormViewHelper.applyForm(request, form);
			FormRenderHelper.forwardWithErrors(request, response, ViewPathConstants.Admin.ROLE_UPDATE, new java.util.HashMap<>());
		} catch (BusinessException e) {
			redirectToRoleManagerWithError(request, response, ErrorHelper.firstMessage(ErrorHelper.fromBusiness(e), ROLE_NOT_FOUND_MESSAGE));
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		RoleUpdateRequest dto = new RoleUpdateRequest.Builder().id(RequestParamHelper.parseInteger(request.getParameter(RequestParamConstants.ID)))
				.code(normalizeCode(request.getParameter(RequestParamConstants.CODE))).name(request.getParameter(RequestParamConstants.NAME))
				.description(request.getParameter(RequestParamConstants.DESCRIPTION)).isSystem(RequestParamHelper.isCheckboxChecked(request, RequestParamConstants.IS_SYSTEM))
				.isActive(RequestParamHelper.isCheckboxChecked(request, RequestParamConstants.IS_ACTIVE))
				.permissionIds(RequestParamHelper.parseIntegerList(request.getParameterValues(RequestParamConstants.Role.PERMISSION_IDS)))
				.assignedRoleIds(RequestParamHelper.parseIntegerList(request.getParameterValues(RequestParamConstants.Role.ASSIGNED_ROLE_IDS))).build();

		if (dto.getId() == null) {
			redirectToRoleManagerWithError(request, response, INVALID_ROLE_ID_MESSAGE);
			return;
		}

		ValidationResult validationResult = roleUpdateValidator.validate(dto);
		if (validationResult.hasErrors()) {
			redirectToUpdateWithErrors(request, response, dto, validationResult.getErrors());
			return;
		}

		try {
			roleService.updateRole(dto);
			SessionPermissionCache.clear(request.getSession());
			MessageHelper.setSuccessMessage(request.getSession(), ROLE_UPDATED_MESSAGE + dto.getCode());
			response.sendRedirect(request.getContextPath() + RouteConstants.Admin.ROLE_UPDATE + "?" + RequestParamConstants.ID + "=" + dto.getId());
		} catch (BusinessException e) {
			redirectToUpdateWithErrors(request, response, dto, ErrorHelper.fromBusiness(e));
		}
	}

	private String normalizeCode(String codeParam) {
		if (codeParam == null) {
			return null;
		}
		return codeParam.toUpperCase().trim().replace(" ", "_");
	}

	private Integer parseRoleId(String roleIdParam) {
		Integer roleId = RequestParamHelper.parseInteger(roleIdParam);
		if (roleId == null) {
			throw new BusinessException(RequestParamConstants.ID, INVALID_ROLE_ID_MESSAGE);
		}
		return roleId;
	}

	private void redirectToUpdateWithErrors(HttpServletRequest request, HttpServletResponse response, RoleUpdateRequest dto, Map<String, String> errors)
			throws IOException {
		HttpSession session = request.getSession();
		session.setAttribute(ViewAttributeConstants.Role.ROLE, dto);
		session.setAttribute(ViewAttributeConstants.ERRORS, errors);
		response.sendRedirect(request.getContextPath() + RouteConstants.Admin.ROLE_UPDATE + "?" + RequestParamConstants.ID + "=" + dto.getId());
	}

	private void redirectToRoleManagerWithError(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
		MessageHelper.setErrorMessage(request.getSession(), message);
		response.sendRedirect(request.getContextPath() + RouteConstants.Admin.ROLE);
	}
}