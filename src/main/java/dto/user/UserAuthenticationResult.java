package dto.user;

import beans.User;

public class UserAuthenticationResult {
	private final User user;
	private final boolean isSuperAdmin;

	private UserAuthenticationResult(Builder builder) {
		this.user = builder.user;
		this.isSuperAdmin = builder.isSuperAdmin;
	}

	public User getUser() {
		return user;
	}

	public boolean isSuperAdmin() {
		return isSuperAdmin;
	}

	public static class Builder {
		private User user;
		private boolean isSuperAdmin;

		public Builder user(User user) {
			this.user = user;
			return this;
		}

		public Builder isSuperAdmin(boolean isSuperAdmin) {
			this.isSuperAdmin = isSuperAdmin;
			return this;
		}

		public UserAuthenticationResult build() {
			return new UserAuthenticationResult(this);
		}
	}
}
