package servlet.admin.permission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import beans.common.Permission;
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
import service.PermissionService;
import service.PermissionServiceImpl;

@WebServlet(name = "DeletePermissionServlet", urlPatterns = "/admin/permission/delete")
public class DeletePermissionServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String INVALID_PERMISSION_ID_MESSAGE = "ID quyền không hợp lệ.";
	private static final String NO_VALID_PERMISSIONS_SELECTED_MESSAGE = "Không có quyền hợp lệ nào được chọn để xóa.";
	private static final String PERMISSION_DELETED_MESSAGE = "Đã xóa quyền: ";
	private static final String DELETE_PERMISSION_FAILED_MESSAGE = "Không thể xóa quyền (có thể là quyền hệ thống).";
	private final PermissionService permissionService = new PermissionServiceImpl();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Phương thức GET không được hỗ trợ cho hành động xóa.");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		List<Integer> idsToDelete;
		try {
			idsToDelete = parsePermissionIds(request.getParameterValues(RequestParamConstants.Permission.PERMISSION_IDS));
		} catch (BusinessException e) {
			MessageHelper.setErrorMessage(session, ErrorHelper.firstMessage(ErrorHelper.fromBusiness(e), INVALID_PERMISSION_ID_MESSAGE));
			response.sendRedirect(request.getContextPath() + RouteConstants.Admin.PERMISSION);
			return;
		}

		if (idsToDelete.isEmpty()) {
			MessageHelper.setErrorMessage(session, NO_VALID_PERMISSIONS_SELECTED_MESSAGE);
			response.sendRedirect(request.getContextPath() + RouteConstants.Admin.PERMISSION);
			return;
		}

		try {
			List<String> deletedInfo = new ArrayList<>();
			for (Integer id : idsToDelete) {
				Permission permission = permissionService.getPermissionEntityById(id);
				if (permission != null) {
					deletedInfo.add(id + " - " + permission.getCode());
				}
			}

			boolean success = permissionService.deletePermissions(idsToDelete);
			SessionPermissionCache.clear(session);
			if (success) {
				String infoStr = String.join(", ", deletedInfo);
				MessageHelper.setSuccessMessage(session, PERMISSION_DELETED_MESSAGE + infoStr);
			} else {
				MessageHelper.setErrorMessage(session, DELETE_PERMISSION_FAILED_MESSAGE);
			}
		} catch (BusinessException e) {
			MessageHelper.setErrorMessage(session, ErrorHelper.firstMessage(ErrorHelper.fromBusiness(e), DELETE_PERMISSION_FAILED_MESSAGE));
		}

		response.sendRedirect(request.getContextPath() + RouteConstants.Admin.PERMISSION);
	}

	private List<Integer> parsePermissionIds(String[] permissionIds) {
		List<Integer> idsToDelete = new ArrayList<>();
		if (permissionIds == null || permissionIds.length == 0) {
			return idsToDelete;
		}
		for (String idStr : permissionIds) {
			if (idStr == null || idStr.trim().isEmpty()) {
				continue;
			}
			try {
				idsToDelete.add(Integer.parseInt(idStr.trim()));
			} catch (NumberFormatException e) {
				throw new BusinessException(RequestParamConstants.Permission.PERMISSION_IDS, INVALID_PERMISSION_ID_MESSAGE);
			}
		}
		return idsToDelete;
	}
}