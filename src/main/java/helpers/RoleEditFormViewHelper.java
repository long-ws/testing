package helpers;

import java.util.ArrayList;

import constants.ViewAttributeConstants;
import dto.role.RoleEditFormData;
import jakarta.servlet.http.HttpServletRequest;

public final class RoleEditFormViewHelper {

	private RoleEditFormViewHelper() {
	}

	public static void applyEmptyContext(HttpServletRequest request) {
		request.setAttribute(ViewAttributeConstants.Role.ROLE_PERMISSIONS, new ArrayList<>());
		request.setAttribute(ViewAttributeConstants.Role.UNASSIGNED_PERMISSIONS, new ArrayList<>());
		request.setAttribute(ViewAttributeConstants.Role.HAS_UNASSIGNED_PERMISSIONS, false);
	}

	public static void applyForm(HttpServletRequest request, RoleEditFormData form) {
		if (form == null) {
			applyEmptyContext(request);
			return;
		}

		request.setAttribute(ViewAttributeConstants.Role.ROLE, form.getRole());
		request.setAttribute(ViewAttributeConstants.Role.ROLE_PERMISSIONS, form.getRolePermissions());
		request.setAttribute(ViewAttributeConstants.Role.UNASSIGNED_PERMISSIONS, form.getUnassignedPermissions());
		request.setAttribute(ViewAttributeConstants.Role.HAS_UNASSIGNED_PERMISSIONS, form.hasUnassignedPermissions());
	}
}