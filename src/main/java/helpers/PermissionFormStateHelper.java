package helpers;

import constants.ViewAttributeConstants;
import dto.permission.PermissionCreateRequest;
import dto.permission.PermissionUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;

public final class PermissionFormStateHelper {

	private PermissionFormStateHelper() {
	}

	public static void applyCreateFormState(HttpServletRequest request, PermissionCreateRequest dto) {
		request.setAttribute(ViewAttributeConstants.Permission.SELECTED_MODULE, dto != null ? dto.getModule() : null);
		request.setAttribute(ViewAttributeConstants.Permission.FORM_LOCKED, false);
	}

	public static void applyUpdateFormState(HttpServletRequest request, PermissionUpdateRequest dto) {
		request.setAttribute(ViewAttributeConstants.Permission.SELECTED_MODULE, dto != null ? dto.getModule() : null);
		request.setAttribute(ViewAttributeConstants.Permission.FORM_LOCKED, dto != null && Boolean.TRUE.equals(dto.getIsSystem()));
	}
}