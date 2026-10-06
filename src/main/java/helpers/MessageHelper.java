package helpers;

import constants.SessionConstants;
import jakarta.servlet.http.HttpSession;

public final class MessageHelper {

	private MessageHelper() {
	}

	public static void setSuccessMessage(HttpSession session, String message) {
		setSessionMessage(session, SessionConstants.SUCCESS_MESSAGE, message);
	}

	public static void setErrorMessage(HttpSession session, String message) {
		setSessionMessage(session, SessionConstants.ERROR_MESSAGE, message);
	}

	private static void setSessionMessage(HttpSession session, String key, String message) {
		if (session == null || key == null) {
			return;
		}
		session.setAttribute(key, message);
	}
}
