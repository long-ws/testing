package validator.core;

import java.util.HashMap;
import java.util.Map;

public class ValidationResult {
	private Map<String, String> errors = new HashMap<>();

	public void addError(String field, String message) {
		this.errors.put(field, message);
	}

	public Map<String, String> getErrors() {
		return this.errors;
	}

	public boolean hasErrors() {
		return !this.errors.isEmpty();
	}
}