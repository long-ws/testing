package dto.user;

public class ChangeEmailVerificationRequest {
	private final String currentEmailCode;
	private final String newEmailCode;

	private ChangeEmailVerificationRequest(Builder builder) {
		this.currentEmailCode = builder.currentEmailCode;
		this.newEmailCode = builder.newEmailCode;
	}

	public String getCurrentEmailCode() {
		return currentEmailCode;
	}

	public String getNewEmailCode() {
		return newEmailCode;
	}

	public static class Builder {
		private String currentEmailCode;
		private String newEmailCode;

		public Builder currentEmailCode(String currentEmailCode) {
			this.currentEmailCode = currentEmailCode;
			return this;
		}

		public Builder newEmailCode(String newEmailCode) {
			this.newEmailCode = newEmailCode;
			return this;
		}

		public ChangeEmailVerificationRequest build() {
			return new ChangeEmailVerificationRequest(this);
		}
	}
}
