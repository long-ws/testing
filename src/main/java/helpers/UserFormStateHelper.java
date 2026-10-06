package helpers;

import java.util.Map;

import constants.RequestParamConstants;
import constants.ViewAttributeConstants;
import domain.user.UserDefaults;
import jakarta.servlet.http.HttpServletRequest;

public final class UserFormStateHelper {

	private UserFormStateHelper() {
	}

	public static void applyCreateSelectedFormState(HttpServletRequest request, Map<String, String> values) {
		request.setAttribute(ViewAttributeConstants.User.SELECTED_ROLE_CODE, resolveSelectedCode(values, RequestParamConstants.User.ROLE, UserDefaults.DEFAULT_ROLE_CODE));
		request.setAttribute(ViewAttributeConstants.User.SELECTED_LANGUAGE_CODE, resolveSelectedCode(values, RequestParamConstants.User.PREFERRED_LANGUAGE_ID, UserDefaults.DEFAULT_LANGUAGE_CODE));
		request.setAttribute(ViewAttributeConstants.User.SELECTED_GENDER_CODE, resolveSelectedCode(values, RequestParamConstants.User.GENDER, UserDefaults.DEFAULT_GENDER_CODE));
	}

	public static void applyUpdateSelectedFormState(HttpServletRequest request, Map<String, String> values) {
		request.setAttribute(ViewAttributeConstants.User.SELECTED_ROLE_CODE, resolveSelectedCode(values, RequestParamConstants.User.ROLE, ""));
		request.setAttribute(ViewAttributeConstants.User.SELECTED_LANGUAGE_CODE, resolveSelectedCode(values, RequestParamConstants.User.PREFERRED_LANGUAGE_ID, UserDefaults.DEFAULT_LANGUAGE_CODE));
		request.setAttribute(ViewAttributeConstants.User.SELECTED_GENDER_CODE, resolveSelectedCode(values, RequestParamConstants.User.GENDER, UserDefaults.DEFAULT_GENDER_CODE));
	}

	private static String resolveSelectedCode(Map<String, String> values, String key, String defaultCode) {
		if (values != null) {
			String value = values.get(key);
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return defaultCode;
	}
}