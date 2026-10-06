package servlet.admin.permission;

import constants.ViewPathConstants;
import constants.ViewAttributeConstants;
import context.UserPermissionContext;
import exception.BusinessException;
import helpers.ErrorHelper;
import helpers.FormRenderHelper;
import helpers.PermissionManageViewHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mapper.PermissionManageViewRowMapper;
import service.PermissionService;
import service.PermissionServiceImpl;
import dto.permission.ManagePermissionResponse;
import dto.permission.PermissionManageViewRow;

@WebServlet(name = "ManagePermissionServlet", urlPatterns = "/admin/permission")
public class ManagePermissionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final PermissionService permissionService = new PermissionServiceImpl();
	private final PermissionManageViewRowMapper permissionManageViewRowMapper = new PermissionManageViewRowMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		List<ManagePermissionResponse> permissions = new ArrayList<>();
		Map<String, String> errors = new HashMap<>();
		boolean canEdit = false;
		boolean canDelete = false;
		try {
			permissions = permissionService.getAllPermissions();
		} catch (BusinessException e) {
			errors = ErrorHelper.fromBusiness(e);
		}

		UserPermissionContext userPermissionContext = (UserPermissionContext) request.getAttribute(ViewAttributeConstants.Security.USER_PERMISSION_CONTEXT);
		if (userPermissionContext != null) {
			request.setAttribute(ViewAttributeConstants.Permission.HAS_CREATE, userPermissionContext.isCanCreatePermission());
			canEdit = userPermissionContext.isCanEditPermission();
			canDelete = userPermissionContext.isCanDeletePermission();
		}
		request.setAttribute(ViewAttributeConstants.Permission.HAS_EDIT, canEdit);
		request.setAttribute(ViewAttributeConstants.Permission.HAS_DELETE, canDelete);

		Map<String, List<PermissionManageViewRow>> permissionsByModule = PermissionManageViewHelper.groupByModule(permissionManageViewRowMapper.toViewRows(permissions, canEdit));
		request.setAttribute(ViewAttributeConstants.Permission.PERMISSIONS_BY_MODULE, permissionsByModule);
		FormRenderHelper.forwardWithErrors(request, response, ViewPathConstants.Admin.PERMISSION_MANAGER, errors);
	}
}