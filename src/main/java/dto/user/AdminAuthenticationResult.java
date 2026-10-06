package dto.user;

import beans.User;
import java.util.Set;

public class AdminAuthenticationResult {
	private final User user;
	private final boolean isSuperAdmin;
	private final Set<String> permissionCodes;

	private AdminAuthenticationResult(Builder builder) {
		this.user = builder.user;
		this.isSuperAdmin = builder.isSuperAdmin;
		this.permissionCodes = builder.permissionCodes;
	}

	public User getUser() {
		return user;
	}

	public boolean isSuperAdmin() {
		return isSuperAdmin;
	}

	public Set<String> getPermissionCodes() {
		return permissionCodes;
	}

	public static class Builder {
		private User user;
		private boolean isSuperAdmin;
		private Set<String> permissionCodes;

		public Builder user(User user) {
			this.user = user;
			return this;
		}

		public Builder isSuperAdmin(boolean isSuperAdmin) {
			this.isSuperAdmin = isSuperAdmin;
			return this;
		}

		public Builder permissionCodes(Set<String> permissionCodes) {
			this.permissionCodes = permissionCodes;
			return this;
		}

		public AdminAuthenticationResult build() {
			return new AdminAuthenticationResult(this);
		}
	}
}
