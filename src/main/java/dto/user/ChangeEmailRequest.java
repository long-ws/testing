package dto.user;

public class ChangeEmailRequest {
	private final String currentEmail;
	private final String currentPassword;
	private final String newEmail;

	private ChangeEmailRequest(Builder builder) {
		this.currentEmail = builder.currentEmail;
		this.currentPassword = builder.currentPassword;
		this.newEmail = builder.newEmail;
	}

	public String getCurrentEmail() {
		return currentEmail;
	}

	public String getCurrentPassword() {
		return currentPassword;
	}

	public String getNewEmail() {
		return newEmail;
	}

	public static class Builder {
		private String currentEmail;
		private String currentPassword;
		private String newEmail;

		public Builder currentEmail(String currentEmail) {
			this.currentEmail = currentEmail;
			return this;
		}

		public Builder currentPassword(String currentPassword) {
			this.currentPassword = currentPassword;
			return this;
		}

		public Builder newEmail(String newEmail) {
			this.newEmail = newEmail;
			return this;
		}

		public ChangeEmailRequest build() {
			return new ChangeEmailRequest(this);
		}
	}
}
