package servlet.admin.role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import constants.ViewPathConstants;
import constants.ViewAttributeConstants;
import context.UserPermissionContext;
import dto.role.ManageRoleResponse;
import exception.BusinessException;
import helpers.ErrorHelper;
import helpers.FormRenderHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mapper.RoleManageViewRowMapper;
import service.RoleService;
import service.RoleServiceImpl;

@WebServlet(name = "ManageRoleServlet", urlPatterns = "/admin/role")
public class ManageRoleServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final RoleService roleService = new RoleServiceImpl();
	private final RoleManageViewRowMapper roleManageViewRowMapper = new RoleManageViewRowMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		List<ManageRoleResponse> roles = new ArrayList<>();
		Map<String, String> errors = new HashMap<>();
		boolean canEdit = false;
		boolean canDelete = false;
		try {
			roles = roleService.getRoles();
		} catch (BusinessException e) {
			errors = ErrorHelper.fromBusiness(e);
		}

		UserPermissionContext userPermissionContext = (UserPermissionContext) request.getAttribute(ViewAttributeConstants.Security.USER_PERMISSION_CONTEXT);
		if (userPermissionContext != null) {
			request.setAttribute(ViewAttributeConstants.Role.HAS_CREATE, userPermissionContext.isCanCreateRole());
			canEdit = userPermissionContext.isCanEditRole();
			canDelete = userPermissionContext.isCanDeleteRole();
		}
		request.setAttribute(ViewAttributeConstants.Role.HAS_EDIT, canEdit);
		request.setAttribute(ViewAttributeConstants.Role.HAS_DELETE, canDelete);

		request.setAttribute(
				ViewAttributeConstants.Role.ROLES,
				roleManageViewRowMapper.toViewRows(roles, canEdit));
		FormRenderHelper.forwardWithErrors(request, response, ViewPathConstants.Admin.ROLE_MANAGER, errors);
	}
}