package servlet.admin.role;

import java.io.IOException;
import java.util.List;

import constants.RequestParamConstants;
import constants.RouteConstants;
import exception.BusinessException;
import helpers.ErrorHelper;
import helpers.MessageHelper;
import helpers.RequestParamHelper;
import helpers.SessionPermissionCache;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.AssignRolePermissionService;
import service.AssignRolePermissionServiceImpl;

@WebServlet(name = "RemoveRolePermissionServlet", urlPatterns = "/admin/role/batchRemovePermission")
public class RemoveRolePermissionServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String INVALID_ROLE_ID_MESSAGE = "ID vai trò không hợp lệ.";
	private static final String NO_PERMISSIONS_SELECTED_MESSAGE = "Chưa chọn quyền nào để gỡ.";
	private static final String REMOVE_PERMISSION_SUCCESS_MESSAGE = "Đã xóa quyền thành công!";
	private static final String REMOVE_PERMISSION_FAILED_MESSAGE = "Không thể gỡ quyền khỏi vai trò.";
	private final AssignRolePermissionService assignRolePermissionService = new AssignRolePermissionServiceImpl();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Integer roleId;
		try {
			roleId = parseRoleId(request.getParameter(RequestParamConstants.Role.ROLE_ID));
		} catch (BusinessException e) {
			MessageHelper.setErrorMessage(request.getSession(), ErrorHelper.firstMessage(ErrorHelper.fromBusiness(e), INVALID_ROLE_ID_MESSAGE));
			response.sendRedirect(request.getContextPath() + RouteConstants.Admin.ROLE);
			return;
		}

		List<Integer> permissionIds = RequestParamHelper.parseIntegerList(request.getParameterValues(RequestParamConstants.Role.PERMISSION_IDS));
		if (permissionIds.isEmpty()) {
			MessageHelper.setErrorMessage(request.getSession(), NO_PERMISSIONS_SELECTED_MESSAGE);
			response.sendRedirect(request.getContextPath() + RouteConstants.Admin.ROLE_UPDATE + "?" + RequestParamConstants.ID + "=" + roleId);
			return;
		}

		try {
			assignRolePermissionService.removePermissionsFromRole(roleId, permissionIds);
			SessionPermissionCache.clear(request.getSession());
			MessageHelper.setSuccessMessage(request.getSession(), REMOVE_PERMISSION_SUCCESS_MESSAGE);
		} catch (BusinessException e) {
			MessageHelper.setErrorMessage(request.getSession(), ErrorHelper.firstMessage(ErrorHelper.fromBusiness(e), REMOVE_PERMISSION_FAILED_MESSAGE));
		}

		response.sendRedirect(request.getContextPath() + RouteConstants.Admin.ROLE_UPDATE + "?" + RequestParamConstants.ID + "=" + roleId);
	}

	private Integer parseRoleId(String roleIdParam) {
		Integer roleId = RequestParamHelper.parseInteger(roleIdParam);
		if (roleId == null) {
			throw new BusinessException(RequestParamConstants.Role.ROLE_ID, INVALID_ROLE_ID_MESSAGE);
		}
		return roleId;
	}
}