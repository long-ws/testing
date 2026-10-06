package servlet.admin.role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import beans.common.Role;
import constants.RequestParamConstants;
import constants.RouteConstants;
import exception.BusinessException;
import helpers.ErrorHelper;
import helpers.MessageHelper;
import helpers.SessionPermissionCache;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.RoleService;
import service.RoleServiceImpl;

@WebServlet(name = "DeleteRoleServlet", urlPatterns = "/admin/role/delete")
public class DeleteRoleServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String INVALID_ROLE_ID_MESSAGE = "ID vai trò không hợp lệ.";
	private static final String NO_VALID_ROLES_SELECTED_MESSAGE = "Không có vai trò hợp lệ nào được chọn để xóa.";
	private static final String ROLE_DELETED_MESSAGE = "Đã xóa vai trò: ";
	private static final String DELETE_ROLE_FAILED_MESSAGE = "Không thể xóa vai trò (có thể là vai trò hệ thống).";
	private final RoleService roleService = new RoleServiceImpl();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Phương thức GET không được hỗ trợ cho hành động xóa.");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		List<Integer> idsToDelete;
		try {
			idsToDelete = parseRoleIds(request.getParameterValues(RequestParamConstants.Role.ROLE_IDS));
		} catch (BusinessException e) {
			MessageHelper.setErrorMessage(session, ErrorHelper.firstMessage(ErrorHelper.fromBusiness(e), INVALID_ROLE_ID_MESSAGE));
			response.sendRedirect(request.getContextPath() + RouteConstants.Admin.ROLE);
			return;
		}

		if (idsToDelete.isEmpty()) {
			MessageHelper.setErrorMessage(session, NO_VALID_ROLES_SELECTED_MESSAGE);
			response.sendRedirect(request.getContextPath() + RouteConstants.Admin.ROLE);
			return;
		}

		try {
			List<String> deletedInfo = new ArrayList<>();
			for (Integer id : idsToDelete) {
				Role role = roleService.getRoleEntityById(id);
				if (role != null) {
					deletedInfo.add(id + " - " + role.getCode());
				}
			}

			boolean success = roleService.deleteRoles(idsToDelete);
			SessionPermissionCache.clear(session);
			if (success) {
				String infoStr = String.join(", ", deletedInfo);
				MessageHelper.setSuccessMessage(session, ROLE_DELETED_MESSAGE + infoStr);
			} else {
				MessageHelper.setErrorMessage(session, DELETE_ROLE_FAILED_MESSAGE);
			}
		} catch (BusinessException e) {
			MessageHelper.setErrorMessage(session, ErrorHelper.firstMessage(ErrorHelper.fromBusiness(e), DELETE_ROLE_FAILED_MESSAGE));
		}

		response.sendRedirect(request.getContextPath() + RouteConstants.Admin.ROLE);
	}

	private List<Integer> parseRoleIds(String[] roleIds) {
		List<Integer> idsToDelete = new ArrayList<>();
		if (roleIds == null || roleIds.length == 0) {
			return idsToDelete;
		}
		for (String idStr : roleIds) {
			if (idStr == null || idStr.trim().isEmpty()) {
				continue;
			}
			try {
				idsToDelete.add(Integer.parseInt(idStr.trim()));
			} catch (NumberFormatException e) {
				throw new BusinessException(RequestParamConstants.Role.ROLE_IDS, INVALID_ROLE_ID_MESSAGE);
			}
		}
		return idsToDelete;
	}
}