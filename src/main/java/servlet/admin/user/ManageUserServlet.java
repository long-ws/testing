package servlet.admin.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.ViewPathConstants;
import constants.ViewAttributeConstants;
import context.UserPermissionContext;
import dto.user.UserManageResponse;
import exception.BusinessException;
import helpers.ErrorHelper;
import helpers.FormRenderHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mapper.user.UserManageViewRowMapper;
import service.UserManagementService;
import service.UserManagementServiceImpl;

@WebServlet(name = "ManageUserServlet", urlPatterns = "/admin/user")
public class ManageUserServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final UserManagementService userManagementService = new UserManagementServiceImpl();
	private final UserManageViewRowMapper userManageViewRowMapper = new UserManageViewRowMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		List<UserManageResponse> users = new ArrayList<>();
		Map<String, String> errors = new HashMap<>();
		try {
			users = userManagementService.getUsers();
		} catch (BusinessException e) {
			errors = ErrorHelper.fromBusiness(e);
		}

		UserPermissionContext userPermissionContext = (UserPermissionContext) request.getAttribute(ViewAttributeConstants.Security.USER_PERMISSION_CONTEXT);

		boolean canCreate = false;
		boolean canEdit = false;
		boolean canDelete = false;

		if (userPermissionContext != null) {
			canCreate = userPermissionContext.isCanCreateUser();
			canEdit = userPermissionContext.isCanEditUser();
			canDelete = userPermissionContext.isCanDeleteUser();
		}

		request.setAttribute(
				ViewAttributeConstants.User.USERS,
				userManageViewRowMapper.toViewRows(users, canEdit));
		request.setAttribute(ViewAttributeConstants.User.HAS_CREATE, canCreate);
		request.setAttribute(ViewAttributeConstants.User.HAS_EDIT, canEdit);
		request.setAttribute(ViewAttributeConstants.User.HAS_DELETE, canDelete);

		FormRenderHelper.forwardWithErrors(request, response, ViewPathConstants.Admin.USER_MANAGER, errors);
	}
}