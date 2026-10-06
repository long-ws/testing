package beans.user;

import java.sql.Timestamp;

public class UserOAuthAuth {
	private long id;
	private long userId;
	private int providerId;
	private String providerUserId;
	private String providerEmail;
	private String displayName;
	private String avatarUrl;
	private Timestamp createdAt;

	public UserOAuthAuth() {
	}

	public UserOAuthAuth(long id, long userId, int providerId, String providerUserId, String providerEmail, String displayName,
			String avatarUrl, Timestamp createdAt) {
		this.id = id;
		this.userId = userId;
		this.providerId = providerId;
		this.providerUserId = providerUserId;
		this.providerEmail = providerEmail;
		this.displayName = displayName;
		this.avatarUrl = avatarUrl;
		this.createdAt = createdAt;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public int getProviderId() {
		return providerId;
	}

	public void setProviderId(int providerId) {
		this.providerId = providerId;
	}

	public String getProviderUserId() {
		return providerUserId;
	}

	public void setProviderUserId(String providerUserId) {
		this.providerUserId = providerUserId;
	}

	public String getProviderEmail() {
		return providerEmail;
	}

	public void setProviderEmail(String providerEmail) {
		this.providerEmail = providerEmail;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "UserOAuthAuth{" + "id=" + id + ", userId=" + userId + ", providerId=" + providerId
				+ ", providerUserId='" + providerUserId + '\'' + ", providerEmail='" + providerEmail + '\'' + ", displayName='"
				+ displayName + '\'' + ", avatarUrl='" + avatarUrl + '\'' + ", createdAt=" + createdAt + '}';
	}
}
