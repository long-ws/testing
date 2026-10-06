package mapper.user;

import java.util.ArrayList;
import java.util.List;

import domain.user.UserAccountStatusCodes;
import dto.user.UserManageResponse;
import dto.user.UserManageViewRow;

public class UserManageViewRowMapper {

	public List<UserManageViewRow> toViewRows(List<UserManageResponse> users, boolean canEdit) {
		List<UserManageViewRow> rows = new ArrayList<>();
		if (users == null) {
			return rows;
		}

		for (int i = 0; i < users.size(); i++) {
			UserManageResponse user = users.get(i);
			if (user != null) {
				rows.add(toViewRow(user, canEdit));
			}
		}
		return rows;
	}

	public UserManageViewRow toViewRow(UserManageResponse user, boolean canEdit) {
		if (user == null) {
			return null;
		}

		boolean editable = canEdit && !user.isSystem();
		return new UserManageViewRow(
				user.getId(),
				user.getUsername(),
				user.getFullname(),
				user.getEmail(),
				user.getPhoneNumber(),
				user.getRole(),
				user.isSystem(),
				isActive(user),
				editable,
				!user.isSystem());
	}

	private boolean isActive(UserManageResponse user) {
		if (user.getStatus() == null || user.getStatus().getCode() == null) {
			return false;
		}
		return UserAccountStatusCodes.ACTIVE.equalsIgnoreCase(user.getStatus().getCode());
	}
}