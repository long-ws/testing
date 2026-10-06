package helpers;

import java.util.HashMap;
import java.util.Map;

import constants.SessionConstants;
import constants.ViewAttributeConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class FlashMessageHelper {
	private FlashMessageHelper() {
	}

	public static void consumeSessionFlashToRequest(HttpServletRequest request) {
		if (request == null) {
			return;
		}

		HttpSession session = request.getSession(false);
		if (session == null) {
			return;
		}

		String[] keys = new String[] {
				SessionConstants.SUCCESS_MESSAGE,
				SessionConstants.ERROR_MESSAGE,
				SessionConstants.SIGNUP_SUCCESS,
				SessionConstants.OAUTH_ERROR,
				ViewAttributeConstants.ERRORS,
				ViewAttributeConstants.VALUES,
				ViewAttributeConstants.User.USER,
				ViewAttributeConstants.Role.ROLE,
				ViewAttributeConstants.Permission.PERMISSION
		};

		for (String key : keys) {
			consume(session, request, key);
		}
	}

	private static void consume(HttpSession session, HttpServletRequest request, String key) {
		if (key == null || key.isBlank()) {
			return;
		}
		Object sessionValue = session.getAttribute(key);
		if (sessionValue == null) {
			return;
		}

		Object requestValue = ViewAttributeConstants.ERRORS.equals(key) ? request.getAttribute(key) : null;
		Map<String, String> merged = (requestValue != null) ? mergeStringMaps(requestValue, sessionValue) : null;
		request.setAttribute(key, merged != null ? merged : sessionValue);
		session.removeAttribute(key);
	}

	private static Map<String, String> mergeStringMaps(Object requestValue, Object sessionValue) {
		if (!(requestValue instanceof Map<?, ?>) || !(sessionValue instanceof Map<?, ?>)) {
			return null;
		}

		Map<?, ?> requestMap = (Map<?, ?>) requestValue;
		Map<?, ?> sessionMap = (Map<?, ?>) sessionValue;

		Map<String, String> merged = new HashMap<String, String>();
		putAllStringEntries(merged, requestMap);
		putAllStringEntries(merged, sessionMap);
		return merged;
	}

	private static void putAllStringEntries(Map<String, String> target, Map<?, ?> source) {
		if (source.isEmpty()) {
			return;
		}
		for (Map.Entry<?, ?> entry : source.entrySet()) {
			Object key = entry.getKey();
			Object value = entry.getValue();
			if (key instanceof String && value instanceof String) {
				target.put((String) key, (String) value);
			}
		}
	}
}