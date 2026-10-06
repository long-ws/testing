package helpers;

import java.util.HashMap;
import java.util.Map;

import constants.system.SystemKeys;
import exception.BusinessException;

public final class ErrorHelper {

	private ErrorHelper() {
	}

	public static Map<String, String> fromBusiness(BusinessException exception) {
		if (exception == null) {
			return new HashMap<>();
		}

		Map<String, String> errors = exception.getErrors();
		if (errors != null && !errors.isEmpty()) {
			return new HashMap<>(errors);
		}

		return withGlobal(exception.getMessage());
	}

	public static Map<String, String> withGlobal(String message) {
		Map<String, String> errors = new HashMap<>();
		errors.put(SystemKeys.ERROR_GLOBAL, message);
		return errors;
	}

	public static Map<String, String> merge(Map<String, String> primaryErrors, Map<String, String> secondaryErrors) {
		Map<String, String> mergedErrors = new HashMap<>();
		if (primaryErrors != null) {
			mergedErrors.putAll(primaryErrors);
		}
		if (secondaryErrors != null) {
			for (Map.Entry<String, String> entry : secondaryErrors.entrySet()) {
				mergedErrors.putIfAbsent(entry.getKey(), entry.getValue());
			}
		}
		return mergedErrors;
	}

	public static String firstMessage(Map<String, String> errors, String fallback) {
		if (errors != null) {
			String globalMessage = errors.get(SystemKeys.ERROR_GLOBAL);
			if (globalMessage != null && !globalMessage.isBlank()) {
				return globalMessage;
			}

			for (String value : errors.values()) {
				if (value != null && !value.isBlank()) {
					return value;
				}
			}
		}
		return fallback;
	}
}