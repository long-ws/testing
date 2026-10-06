package helpers;

import java.io.IOException;
import java.util.Map;

import constants.ViewAttributeConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class FormRenderHelper {

	private FormRenderHelper() {
	}

	public static void forward(HttpServletRequest request, HttpServletResponse response, String viewPath) throws ServletException, IOException {
		FlashMessageHelper.consumeSessionFlashToRequest(request);
		request.getRequestDispatcher(viewPath).forward(request, response);
	}

	public static void forwardWithErrors(HttpServletRequest request, HttpServletResponse response, String viewPath, Map<String, String> errors) throws ServletException, IOException {
		request.setAttribute(ViewAttributeConstants.ERRORS, errors);
		forward(request, response, viewPath);
	}

	public static void forwardWithFormState(HttpServletRequest request, HttpServletResponse response, String viewPath, String valueAttributeName, Object formValue, Map<String, String> errors)
			throws ServletException, IOException {
		if (valueAttributeName != null && formValue != null) {
			request.setAttribute(valueAttributeName, formValue);
		}
		request.setAttribute(ViewAttributeConstants.ERRORS, errors);
		forward(request, response, viewPath);
	}
}