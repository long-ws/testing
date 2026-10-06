package servlet.admin.permission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import constants.RequestParamConstants;
import constants.RouteConstants;
import constants.ViewAttributeConstants;
import constants.ViewPathConstants;
import dto.permission.PermissionDetailResponse;
import dto.permission.PermissionUpdateRequest;
import exception.BusinessException;
import helpers.ErrorHelper;
import helpers.FormRenderHelper;
import helpers.MessageHelper;
import helpers.PermissionFormStateHelper;
import helpers.RequestParamHelper;
import helpers.SessionPermissionCache;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.PermissionService;
import service.PermissionServiceImpl;
import validator.core.ValidationResult;
import validator.permission.PermissionUpdateValidator;

@WebServlet(name = "UpdatePermissionServlet", urlPatterns = "/admin/permission/update")
public class UpdatePermissionServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String INVALID_PERMISSION_ID_MESSAGE = "ID quyền không hợp lệ.";
	private static final String PERMISSION_NOT_FOUND_MESSAGE = "Quyền không tồn tại trên hệ thống.";
	private static final String PERMISSION_UPDATED_MESSAGE = "Đã cập nhật quyền: ";
	private final PermissionService permissionService = new PermissionServiceImpl();
	private final PermissionUpdateValidator permissionUpdateValidator = new PermissionUpdateValidator();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			Integer permissionId = parsePermissionId(request.getParameter(RequestParamConstants.ID));
			PermissionDetailResponse detail = permissionService.getPermissionById(permissionId);
			if (detail == null) {
				redirectToPermissionManagerWithError(request, response, PERMISSION_NOT_FOUND_MESSAGE);
				return;
			}
			renderUpdateView(request, response, permissionService.toUpdateRequest(detail), null);
		} catch (BusinessException e) {
			redirectToPermissionManagerWithError(request, response, ErrorHelper.firstMessage(ErrorHelper.fromBusiness(e), PERMISSION_NOT_FOUND_MESSAGE));
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String moduleParam = request.getParameter(RequestParamConstants.MODULE);
		PermissionUpdateRequest dto = new PermissionUpdateRequest.Builder()
				.id(RequestParamHelper.parseInteger(request.getParameter(RequestParamConstants.ID)))
				.code(normalizeCode(request.getParameter(RequestParamConstants.CODE)))
				.name(request.getParameter(RequestParamConstants.NAME))
				.description(request.getParameter(RequestParamConstants.DESCRIPTION))
				.module(moduleParam != null ? moduleParam.toUpperCase() : null)
				.isSystem(RequestParamHelper.isCheckboxChecked(request, RequestParamConstants.IS_SYSTEM))
				.isActive(RequestParamHelper.isCheckboxChecked(request, RequestParamConstants.IS_ACTIVE))
				.build();

		if (dto.getId() == null) {
			redirectToPermissionManagerWithError(request, response, INVALID_PERMISSION_ID_MESSAGE);
			return;
		}

		ValidationResult validationResult = permissionUpdateValidator.validate(dto);
		if (validationResult.hasErrors()) {
			redirectToUpdateWithErrors(request, response, dto, validationResult.getErrors());
			return;
		}

		try {
			permissionService.updatePermission(dto);
			SessionPermissionCache.clear(request.getSession());
			MessageHelper.setSuccessMessage(request.getSession(), PERMISSION_UPDATED_MESSAGE + dto.getCode());
			response.sendRedirect(request.getContextPath() + RouteConstants.Admin.PERMISSION_UPDATE + "?" + RequestParamConstants.ID + "=" + dto.getId());
		} catch (BusinessException e) {
			redirectToUpdateWithErrors(request, response, dto, ErrorHelper.fromBusiness(e));
		}
	}

	private String normalizeCode(String codeParam) {
		if (codeParam == null) {
			return null;
		}
		return codeParam.trim().toLowerCase();
	}

	private Integer parsePermissionId(String permissionIdParam) {
		Integer permissionId = RequestParamHelper.parseInteger(permissionIdParam);
		if (permissionId == null) {
			throw new BusinessException(RequestParamConstants.ID, INVALID_PERMISSION_ID_MESSAGE);
		}
		return permissionId;
	}

	private Map<String, String> loadModuleOptionErrors(HttpServletRequest request) {
		try {
			request.setAttribute(ViewAttributeConstants.Permission.MODULES, permissionService.getAllModules());
			return new HashMap<>();
		} catch (BusinessException e) {
			request.setAttribute(ViewAttributeConstants.Permission.MODULES, new ArrayList<>());
			return ErrorHelper.fromBusiness(e);
		}
	}

	private void renderUpdateView(HttpServletRequest request, HttpServletResponse response, PermissionUpdateRequest dto, Map<String, String> errors)
			throws ServletException, IOException {
		request.setAttribute(ViewAttributeConstants.Permission.PERMISSION, dto);
		PermissionFormStateHelper.applyUpdateFormState(request, dto);
		FormRenderHelper.forwardWithErrors(request, response, ViewPathConstants.Admin.PERMISSION_UPDATE, ErrorHelper.merge(errors, loadModuleOptionErrors(request)));
	}

	private void redirectToUpdateWithErrors(HttpServletRequest request, HttpServletResponse response, PermissionUpdateRequest dto, Map<String, String> errors)
			throws IOException {
		HttpSession session = request.getSession();
		session.setAttribute(ViewAttributeConstants.Permission.PERMISSION, dto);
		session.setAttribute(ViewAttributeConstants.ERRORS, errors);
		response.sendRedirect(request.getContextPath() + RouteConstants.Admin.PERMISSION_UPDATE + "?" + RequestParamConstants.ID + "=" + dto.getId());
	}

	private void redirectToPermissionManagerWithError(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
		MessageHelper.setErrorMessage(request.getSession(), message);
		response.sendRedirect(request.getContextPath() + RouteConstants.Admin.PERMISSION);
	}
}