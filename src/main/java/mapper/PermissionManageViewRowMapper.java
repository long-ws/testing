package mapper;

import java.util.ArrayList;
import java.util.List;

import dto.permission.ManagePermissionResponse;
import dto.permission.PermissionManageViewRow;

public class PermissionManageViewRowMapper {

	public List<PermissionManageViewRow> toViewRows(List<ManagePermissionResponse> permissions, boolean canEdit) {
		List<PermissionManageViewRow> rows = new ArrayList<>();
		if (permissions == null) {
			return rows;
		}

		for (int i = 0; i < permissions.size(); i++) {
			ManagePermissionResponse permission = permissions.get(i);
			if (permission != null) {
				rows.add(toViewRow(permission, canEdit));
			}
		}
		return rows;
	}

	public PermissionManageViewRow toViewRow(ManagePermissionResponse permission, boolean canEdit) {
		if (permission == null) {
			return null;
		}

		boolean editable = canEdit && !permission.isSystem();
		return new PermissionManageViewRow(
				permission.getId(),
				permission.getCode(),
				permission.getName(),
				permission.getDescription(),
				permission.getModule(),
				permission.isSystem(),
				permission.isActive(),
				editable,
				!permission.isSystem());
	}
}