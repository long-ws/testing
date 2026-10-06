package servlet.admin.user;

import java.io.IOException;
import constants.RequestParamConstants;
import constants.RouteConstants;
import constants.ViewAttributeConstants;
import constants.ViewPathConstants;
import dto.user.UserDetailResponse;
import exception.BusinessException;
import helpers.ErrorHelper;
import helpers.FormRenderHelper;
import helpers.MessageHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.UserManagementService;
import service.UserManagementServiceImpl;

@WebServlet(name = "DetailUserServlet", urlPatterns = "/admin/user/detail")
public class DetailUserServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String INVALID_USER_ID_MESSAGE = "Định dạng ID người dùng không hợp lệ.";
	private static final String USER_NOT_FOUND_MESSAGE = "Người dùng không tồn tại trên hệ thống.";
	private final UserManagementService userManagementService = new UserManagementServiceImpl();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String idStr = request.getParameter(RequestParamConstants.ID);
		try {
			long id = parseUserId(idStr);
			UserDetailResponse user = userManagementService.getUserById(id);
			if (user == null) {
				redirectToUserManagerWithError(request, response, USER_NOT_FOUND_MESSAGE);
				return;
			}
			request.setAttribute(ViewAttributeConstants.User.USER, user);
			FormRenderHelper.forward(request, response, ViewPathConstants.Admin.USER_DETAIL);
		} catch (BusinessException e) {
			redirectToUserManagerWithError(request, response, ErrorHelper.firstMessage(ErrorHelper.fromBusiness(e), USER_NOT_FOUND_MESSAGE));
		}
	}

	private long parseUserId(String idStr) {
		if (idStr == null || idStr.trim().isEmpty()) {
			throw new BusinessException(RequestParamConstants.ID, INVALID_USER_ID_MESSAGE);
		}
		try {
			return Long.parseLong(idStr.trim());
		} catch (NumberFormatException e) {
			throw new BusinessException(RequestParamConstants.ID, INVALID_USER_ID_MESSAGE);
		}
	}

	private void redirectToUserManagerWithError(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
		MessageHelper.setErrorMessage(request.getSession(), message);
		response.sendRedirect(request.getContextPath() + RouteConstants.Admin.USER);
	}
}