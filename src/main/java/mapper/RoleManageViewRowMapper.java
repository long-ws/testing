package mapper;

import java.util.ArrayList;
import java.util.List;

import dto.role.ManageRoleResponse;
import dto.role.RoleManageViewRow;

public class RoleManageViewRowMapper {

	public List<RoleManageViewRow> toViewRows(List<ManageRoleResponse> roles, boolean canEdit) {
		List<RoleManageViewRow> rows = new ArrayList<>();
		if (roles == null) {
			return rows;
		}

		for (int i = 0; i < roles.size(); i++) {
			ManageRoleResponse role = roles.get(i);
			if (role != null) {
				rows.add(toViewRow(role, canEdit));
			}
		}
		return rows;
	}

	public RoleManageViewRow toViewRow(ManageRoleResponse role, boolean canEdit) {
		if (role == null) {
			return null;
		}

		boolean system = Boolean.TRUE.equals(role.getIsSystem());
		boolean active = Boolean.TRUE.equals(role.getIsActive());
		boolean editable = canEdit && !system;

		return new RoleManageViewRow(role.getId(), role.getCode(), role.getName(), role.getDescription(), system, active, editable, !system);
	}
}