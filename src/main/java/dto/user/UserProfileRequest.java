package dto.user;

public class UserProfileRequest {
	private final String fullname;
	private final String phoneNumber;
	private final String avatarUrl;
	private final String genderCode;
	private final String preferredLanguageCode;

	private UserProfileRequest(Builder builder) {
		this.fullname = builder.fullname;
		this.phoneNumber = builder.phoneNumber;
		this.avatarUrl = builder.avatarUrl;
		this.genderCode = builder.genderCode;
		this.preferredLanguageCode = builder.preferredLanguageCode;
	}

	public String getFullname() {
		return fullname;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public String getGenderCode() {
		return genderCode;
	}

	public String getPreferredLanguageCode() {
		return preferredLanguageCode;
	}

	public static class Builder {
		private String fullname;
		private String phoneNumber;
		private String avatarUrl;
		private String genderCode;
		private String preferredLanguageCode;

		public Builder fullname(String fullname) {
			this.fullname = fullname;
			return this;
		}

		public Builder phoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
			return this;
		}

		public Builder avatarUrl(String avatarUrl) {
			this.avatarUrl = avatarUrl;
			return this;
		}

		public Builder genderCode(String genderCode) {
			this.genderCode = genderCode;
			return this;
		}

		public Builder preferredLanguageCode(String preferredLanguageCode) {
			this.preferredLanguageCode = preferredLanguageCode;
			return this;
		}

		public UserProfileRequest build() {
			return new UserProfileRequest(this);
		}
	}
}