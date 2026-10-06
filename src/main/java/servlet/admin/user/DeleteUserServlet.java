package servlet.admin.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import beans.User;
import constants.PermissionConstants;
import constants.RequestParamConstants;
import constants.RouteConstants;
import constants.SessionConstants;
import exception.BusinessException;
import helpers.ErrorHelper;
import helpers.MessageHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.AuthorizationService;
import service.AuthorizationServiceImpl;
import service.UserManagementService;
import service.UserManagementServiceImpl;

@WebServlet(name = "DeleteUserServlet", urlPatterns = "/admin/user/delete")
public class DeleteUserServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String INVALID_USER_ID_MESSAGE = "ID người dùng không hợp lệ.";
	private static final String NO_VALID_USERS_SELECTED_MESSAGE = "Không có người dùng hợp lệ nào được chọn để xóa.";
	private static final String DELETE_SUCCESS_MESSAGE = "Đã xóa người dùng thành công: id ";
	private static final String DELETE_FAILED_MESSAGE = "Xóa dữ liệu thất bại hoặc người dùng không tồn tại.";
	private final UserManagementService userManagementService = new UserManagementServiceImpl();
	private final AuthorizationService authorizationService = new AuthorizationServiceImpl();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Phương thức GET không được hỗ trợ cho hành động xóa.");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		User currentUser = (session != null) ? (User) session.getAttribute(SessionConstants.CURRENT_USER) : null;

		if (currentUser == null) {
			response.sendRedirect(request.getContextPath() + RouteConstants.Admin.SIGNIN);
			return;
		}

		if (!authorizationService.hasPermission(currentUser.getId(), PermissionConstants.USER_DELETE)) {
			response.sendRedirect(request.getContextPath() + RouteConstants.Admin.FORBIDDEN);
			return;
		}

		List<Long> idsToDelete;
		try {
			idsToDelete = parseIdsToDelete(request);
		} catch (BusinessException e) {
			MessageHelper.setErrorMessage(request.getSession(), ErrorHelper.firstMessage(ErrorHelper.fromBusiness(e), INVALID_USER_ID_MESSAGE));
			response.sendRedirect(request.getContextPath() + RouteConstants.Admin.USER);
			return;
		}

		if (idsToDelete.isEmpty()) {
			MessageHelper.setErrorMessage(request.getSession(), NO_VALID_USERS_SELECTED_MESSAGE);
			response.sendRedirect(request.getContextPath() + RouteConstants.Admin.USER);
			return;
		}

		try {
			boolean isDeleted = userManagementService.deleteUsers(idsToDelete);

			if (isDeleted) {
				StringBuilder idsBuilder = new StringBuilder();
				for (int i = 0; i < idsToDelete.size(); i++) {
					if (i > 0) {
						idsBuilder.append(", ");
					}
					idsBuilder.append(idsToDelete.get(i));
				}
				MessageHelper.setSuccessMessage(request.getSession(), DELETE_SUCCESS_MESSAGE + idsBuilder);
			} else {
				MessageHelper.setErrorMessage(request.getSession(), DELETE_FAILED_MESSAGE);
			}
		} catch (BusinessException e) {
			MessageHelper.setErrorMessage(request.getSession(), ErrorHelper.firstMessage(ErrorHelper.fromBusiness(e), DELETE_FAILED_MESSAGE));
		}

		response.sendRedirect(request.getContextPath() + RouteConstants.Admin.USER);
	}

	private List<Long> parseIdsToDelete(HttpServletRequest request) {
		String[] userIds = request.getParameterValues(RequestParamConstants.User.USER_IDS);
		String singleId = request.getParameter(RequestParamConstants.ID);
		List<Long> idsToDelete = new ArrayList<>();

		if (userIds != null && userIds.length > 0) {
			for (String idStr : userIds) {
				if (idStr != null && !idStr.trim().isEmpty()) {
					idsToDelete.add(parseUserId(idStr));
				}
			}
			return idsToDelete;
		}

		if (singleId != null && !singleId.trim().isEmpty()) {
			idsToDelete.add(parseUserId(singleId));
		}
		return idsToDelete;
	}

	private long parseUserId(String idStr) {
		try {
			return Long.parseLong(idStr.trim());
		} catch (NumberFormatException e) {
			throw new BusinessException(RequestParamConstants.ID, INVALID_USER_ID_MESSAGE);
		}
	}
}