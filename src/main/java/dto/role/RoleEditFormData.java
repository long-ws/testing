package dto.role;

import java.util.List;
import dto.permission.ManagePermissionResponse;

public class RoleEditFormData {

	private final RoleUpdateRequest role;
	private final List<ManagePermissionResponse> rolePermissions;
	private final List<ManagePermissionResponse> unassignedPermissions;
	private final boolean hasUnassignedPermissions;

	public RoleEditFormData(RoleUpdateRequest role, List<ManagePermissionResponse> rolePermissions, List<ManagePermissionResponse> unassignedPermissions, boolean hasUnassignedPermissions) {
		this.role = role;
		this.rolePermissions = rolePermissions;
		this.unassignedPermissions = unassignedPermissions;
		this.hasUnassignedPermissions = hasUnassignedPermissions;
	}

	public RoleUpdateRequest getRole() {
		return role;
	}

	public List<ManagePermissionResponse> getRolePermissions() {
		return rolePermissions;
	}

	public List<ManagePermissionResponse> getUnassignedPermissions() {
		return unassignedPermissions;
	}

	public boolean hasUnassignedPermissions() {
		return hasUnassignedPermissions;
	}
}
