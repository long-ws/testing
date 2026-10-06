package helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import domain.common.RegistryDefaults;
import dto.permission.PermissionManageViewRow;

public final class PermissionManageViewHelper {

	private PermissionManageViewHelper() {
	}

	public static Map<String, List<PermissionManageViewRow>> groupByModule(List<PermissionManageViewRow> permissions) {
		Map<String, List<PermissionManageViewRow>> result = new TreeMap<>();
		if (permissions == null) {
			return result;
		}

		for (int i = 0; i < permissions.size(); i++) {
			PermissionManageViewRow permission = permissions.get(i);
			if (permission == null) {
				continue;
			}

			String module = permission.getModule() != null ? permission.getModule() : RegistryDefaults.DEFAULT_MODULE;
			List<PermissionManageViewRow> modulePermissions = result.get(module);
			if (modulePermissions == null) {
				modulePermissions = new ArrayList<>();
				result.put(module, modulePermissions);
			}
			modulePermissions.add(permission);
		}
		return result;
	}
}