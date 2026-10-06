package servlet.admin.permission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import constants.RequestParamConstants;
import constants.RouteConstants;
import constants.ViewAttributeConstants;
import constants.ViewPathConstants;
import domain.common.RegistryDefaults;
import dto.permission.PermissionCreateRequest;
import exception.BusinessException;
import helpers.ErrorHelper;
import helpers.FormRenderHelper;
import helpers.MessageHelper;
import helpers.PermissionFormStateHelper;
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
import validator.permission.PermissionCreateValidator;

@WebServlet(name = "CreatePermissionServlet", urlPatterns = "/admin/permission/create")
public class CreatePermissionServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String PERMISSION_CREATED_MESSAGE = "Đã tạo quyền: ";
	private final PermissionService permissionService = new PermissionServiceImpl();
	private final PermissionCreateValidator permissionCreateValidator = new PermissionCreateValidator();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		renderCreateView(request, response, new PermissionCreateRequest.Builder().build(), null);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String moduleParam = request.getParameter(RequestParamConstants.MODULE);
		PermissionCreateRequest dto = new PermissionCreateRequest.Builder()
				.code(normalizeCode(request.getParameter(RequestParamConstants.CODE)))
				.name(request.getParameter(RequestParamConstants.NAME))
				.description(request.getParameter(RequestParamConstants.DESCRIPTION))
				.module(moduleParam != null ? moduleParam.toUpperCase() : null)
				.isSystem(RegistryDefaults.DEFAULT_IS_SYSTEM)
				.isActive(RegistryDefaults.DEFAULT_IS_ACTIVE)
				.build();

		ValidationResult validationResult = permissionCreateValidator.validate(dto);
		if (validationResult.hasErrors()) {
			redirectToCreateWithErrors(request, response, dto, validationResult.getErrors());
			return;
		}

		try {
			permissionService.createPermission(dto);
			SessionPermissionCache.clear(request.getSession());
			MessageHelper.setSuccessMessage(request.getSession(), PERMISSION_CREATED_MESSAGE + dto.getCode());
			response.sendRedirect(request.getContextPath() + RouteConstants.Admin.PERMISSION_CREATE);
		} catch (BusinessException e) {
			redirectToCreateWithErrors(request, response, dto, ErrorHelper.fromBusiness(e));
		}
	}

	private String normalizeCode(String codeParam) {
		if (codeParam == null) {
			return null;
		}
		return codeParam.trim().toLowerCase();
	}

	private void loadModuleOptions(HttpServletRequest request) {
		request.setAttribute(ViewAttributeConstants.Permission.MODULES, permissionService.getAllModules());
	}

	private Map<String, String> loadModuleOptionErrors(HttpServletRequest request) {
		try {
			loadModuleOptions(request);
			return new HashMap<>();
		} catch (BusinessException e) {
			request.setAttribute(ViewAttributeConstants.Permission.MODULES, new ArrayList<>());
			return ErrorHelper.fromBusiness(e);
		}
	}

	private void renderCreateView(HttpServletRequest request, HttpServletResponse response, PermissionCreateRequest dto, Map<String, String> errors)
			throws ServletException, IOException {
		request.setAttribute(ViewAttributeConstants.Permission.PERMISSION, dto);
		PermissionFormStateHelper.applyCreateFormState(request, dto);
		FormRenderHelper.forwardWithErrors(request, response, ViewPathConstants.Admin.PERMISSION_CREATE, ErrorHelper.merge(errors, loadModuleOptionErrors(request)));
	}

	private void redirectToCreateWithErrors(HttpServletRequest request, HttpServletResponse response, PermissionCreateRequest dto, Map<String, String> errors)
			throws IOException {
		HttpSession session = request.getSession();
		session.setAttribute(ViewAttributeConstants.Permission.PERMISSION, dto);
		session.setAttribute(ViewAttributeConstants.ERRORS, errors);
		response.sendRedirect(request.getContextPath() + RouteConstants.Admin.PERMISSION_CREATE);
	}
}