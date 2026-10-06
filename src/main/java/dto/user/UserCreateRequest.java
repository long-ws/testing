package dto.user;

import beans.common.Role;

public class UserCreateRequest {
	private final Long id;
	private final String username;
	private final String password;
	private final String fullname;
	private final String email;
	private final String phoneNumber;
	private final String genderCode;
	private final String preferredLanguageCode;
	private final String avatarUrl;
	private final Role role;

	private UserCreateRequest(Builder builder) {
		this.id = builder.id;
		this.username = builder.username;
		this.password = builder.password;
		this.fullname = builder.fullname;
		this.email = builder.email;
		this.phoneNumber = builder.phoneNumber;
		this.genderCode = builder.genderCode;
		this.preferredLanguageCode = builder.preferredLanguageCode;
		this.avatarUrl = builder.avatarUrl;
		this.role = builder.role;
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getFullname() {
		return fullname;
	}

	public String getEmail() {
		return email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getGenderCode() {
		return genderCode;
	}

	public String getPreferredLanguageCode() {
		return preferredLanguageCode;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public Role getRole() {
		return role;
	}

	public static class Builder {
		private Long id;
		private String username;
		private String password;
		private String fullname;
		private String email;
		private String phoneNumber;
		private String genderCode;
		private String preferredLanguageCode;
		private String avatarUrl;
		private Role role;

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder username(String username) {
			this.username = username;
			return this;
		}

		public Builder password(String password) {
			this.password = password;
			return this;
		}

		public Builder fullname(String fullname) {
			this.fullname = fullname;
			return this;
		}

		public Builder email(String email) {
			this.email = email;
			return this;
		}

		public Builder phoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
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

		public Builder avatarUrl(String avatarUrl) {
			this.avatarUrl = avatarUrl;
			return this;
		}

		public Builder role(Role role) {
			this.role = role;
			return this;
		}

		public UserCreateRequest build() {
			return new UserCreateRequest(this);
		}
	}
}