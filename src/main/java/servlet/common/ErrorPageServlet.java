package servlet.common;

import java.io.IOException;

import constants.ViewAttributeConstants;
import constants.ViewPathConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "ErrorPageServlet", value = "/error")
public class ErrorPageServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Integer statusCode = getStatusCode(request);
		request.setAttribute(ViewAttributeConstants.Error.STATUS_CODE, statusCode);
		request.setAttribute(ViewAttributeConstants.Error.MESSAGE, resolveMessage(request, statusCode));
		request.setAttribute(ViewAttributeConstants.Error.REQUEST_URI, getRequestUri(request));
		request.getRequestDispatcher(ViewPathConstants.Common.ERROR).forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private Integer getStatusCode(HttpServletRequest request) {
		Object value = request.getAttribute("jakarta.servlet.error.status_code");
		return value instanceof Integer ? (Integer) value : Integer.valueOf(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	private String getRequestUri(HttpServletRequest request) {
		Object value = request.getAttribute("jakarta.servlet.error.request_uri");
		return value instanceof String ? (String) value : request.getRequestURI();
	}

	private String resolveMessage(HttpServletRequest request, Integer statusCode) {
		Object explicitMessage = request.getAttribute("jakarta.servlet.error.message");
		if (explicitMessage instanceof String && !((String) explicitMessage).isBlank()) {
			return (String) explicitMessage;
		}

		if (statusCode != null) {
			switch (statusCode.intValue()) {
			case HttpServletResponse.SC_FORBIDDEN:
				return "Bạn không có quyền truy cập tài nguyên này";
			case HttpServletResponse.SC_NOT_FOUND:
				return "Không tìm thấy tài nguyên được yêu cầu";
			default:
				break;
			}
		}

		Throwable throwable = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
		if (throwable != null && throwable.getMessage() != null && !throwable.getMessage().isBlank()) {
			return throwable.getMessage();
		}

		return "Đã xảy ra lỗi. Vui lòng thử lại sau";
	}
}
