package exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import constants.system.SystemKeys;

public class BusinessException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final Map<String, String> errors;

	public BusinessException(String message) {
		this(SystemKeys.ERROR_GLOBAL, message);
	}

	public BusinessException(String field, String message) {
		super(message);
		this.errors = new HashMap<>();
		this.errors.put(field != null ? field : SystemKeys.ERROR_GLOBAL, message);
	}

	public BusinessException(Map<String, String> errors) {
		super(resolveMessage(errors));
		this.errors = normalizeErrors(errors);
	}

	public Map<String, String> getErrors() {
		return Collections.unmodifiableMap(errors);
	}

	private static Map<String, String> normalizeErrors(Map<String, String> sourceErrors) {
		Map<String, String> normalizedErrors = new HashMap<>();
		if (sourceErrors != null) {
			normalizedErrors.putAll(sourceErrors);
		}

		if (normalizedErrors.isEmpty()) {
			normalizedErrors.put(SystemKeys.ERROR_GLOBAL, "Đã có lỗi xảy ra");
		}

		return normalizedErrors;
	}

	private static String resolveMessage(Map<String, String> errors) {
		Map<String, String> normalizedErrors = normalizeErrors(errors);
		String globalMessage = normalizedErrors.get(SystemKeys.ERROR_GLOBAL);
		
		if (globalMessage != null && !globalMessage.isBlank()) {
			return globalMessage;
		}

		for (String value : normalizedErrors.values()) {
			if (value != null && !value.isBlank()) {
				return value;
			}
		}

		return "Đã có lỗi xảy ra";
	}
}
