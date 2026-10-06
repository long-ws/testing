package beans;

import java.sql.Timestamp;
import java.util.List;

import beans.common.Gender;
import beans.common.Language;
import beans.common.Role;
import beans.common.UserAccountStatus;
import beans.user.UserAccount;
import beans.user.UserEmail;
import beans.user.UserLocalAuth;
import beans.user.UserOAuthAuth;
import beans.user.UserProfile;

public class User {
	private UserAccount userAccount;
	private Role role;

	private UserProfile profile;
	private UserLocalAuth localAuth;
	private List<UserOAuthAuth> oauthAuths;
	private UserEmail userEmail;

	public User() {
	}

	public UserAccount getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(UserAccount userAccount) {
		this.userAccount = userAccount;
	}

	public long getId() {
		return userAccount != null ? userAccount.getId() : 0;
	}

	public void setId(long id) {
		if (this.userAccount == null) {
			this.userAccount = new UserAccount();
		}
		this.userAccount.setId(id);
	}

	public String getUsername() {
		return localAuth != null ? localAuth.getUsername() : null;
	}

	public void setUsername(String username) {
		if (this.localAuth == null) {
			this.localAuth = new UserLocalAuth();
		}
		this.localAuth.setUsername(username);
	}

	public int getTokenVersion() {
		return userAccount != null ? userAccount.getTokenVersion() : 0;
	}

	public void setTokenVersion(int tokenVersion) {
		if (this.userAccount == null) {
			this.userAccount = new UserAccount();
		}
		this.userAccount.setTokenVersion(tokenVersion);
	}

	public Timestamp getLastLoginAt() {
		return userAccount != null ? userAccount.getLastLoginAt() : null;
	}

	public void setLastLoginAt(Timestamp lastLoginAt) {
		if (this.userAccount == null) {
			this.userAccount = new UserAccount();
		}
		this.userAccount.setLastLoginAt(lastLoginAt);
	}

	public String getRememberToken() {
		return userAccount != null ? userAccount.getRememberToken() : null;
	}

	public void setRememberToken(String rememberToken) {
		if (this.userAccount == null) {
			this.userAccount = new UserAccount();
		}
		this.userAccount.setRememberToken(rememberToken);
	}

	public Timestamp getRememberExpiresAt() {
		return userAccount != null ? userAccount.getRememberExpiresAt() : null;
	}

	public void setRememberExpiresAt(Timestamp rememberExpiresAt) {
		if (this.userAccount == null) {
			this.userAccount = new UserAccount();
		}
		this.userAccount.setRememberExpiresAt(rememberExpiresAt);
	}

	public Timestamp getDeletedAt() {
		return userAccount != null ? userAccount.getDeletedAt() : null;
	}

	public void setDeletedAt(Timestamp deletedAt) {
		if (this.userAccount == null) {
			this.userAccount = new UserAccount();
		}
		this.userAccount.setDeletedAt(deletedAt);
	}

	public Long getDeletedBy() {
		return userAccount != null ? userAccount.getDeletedBy() : null;
	}

	public void setDeletedBy(Long deletedBy) {
		if (this.userAccount == null) {
			this.userAccount = new UserAccount();
		}
		this.userAccount.setDeletedBy(deletedBy);
	}

	public Timestamp getDeletionScheduledAt() {
		return userAccount != null ? userAccount.getDeletionScheduledAt() : null;
	}

	public void setDeletionScheduledAt(Timestamp deletionScheduledAt) {
		if (this.userAccount == null) {
			this.userAccount = new UserAccount();
		}
		this.userAccount.setDeletionScheduledAt(deletionScheduledAt);
	}

	public Timestamp getCreatedAt() {
		return userAccount != null ? userAccount.getCreatedAt() : null;
	}

	public void setCreatedAt(Timestamp createdAt) {
		if (this.userAccount == null) {
			this.userAccount = new UserAccount();
		}
		this.userAccount.setCreatedAt(createdAt);
	}

	public Timestamp getUpdatedAt() {
		return userAccount != null ? userAccount.getUpdatedAt() : null;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		if (this.userAccount == null) {
			this.userAccount = new UserAccount();
		}
		this.userAccount.setUpdatedAt(updatedAt);
	}

	public UserAccountStatus getStatus() {
		return userAccount != null ? userAccount.getStatus() : null;
	}

	public void setStatus(UserAccountStatus status) {
		if (this.userAccount == null) {
			this.userAccount = new UserAccount();
		}
		this.userAccount.setStatus(status);
	}

	public int getStatusId() {
		return userAccount != null ? userAccount.getStatusId() : 0;
	}

	public void setStatusId(int statusId) {
		if (this.userAccount == null) {
			this.userAccount = new UserAccount();
		}
		this.userAccount.setStatusId(statusId);
	}

	public String getEmail() {
		return userEmail != null ? userEmail.getEmail() : null;
	}

	public void setEmail(String email) {
		if (this.userEmail == null) {
			this.userEmail = new UserEmail();
		}
		this.userEmail.setEmail(email);
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public UserProfile getProfile() {
		return profile;
	}

	public void setProfile(UserProfile profile) {
		this.profile = profile;
	}

	public UserLocalAuth getLocalAuth() {
		return localAuth;
	}

	public void setLocalAuth(UserLocalAuth localAuth) {
		this.localAuth = localAuth;
	}

	public List<UserOAuthAuth> getOauthAuths() {
		return oauthAuths;
	}

	public void setOauthAuths(List<UserOAuthAuth> oauthAuths) {
		this.oauthAuths = oauthAuths;
	}

	public UserEmail getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(UserEmail userEmail) {
		this.userEmail = userEmail;
	}

	public boolean isDeleted() {
		return userAccount != null && userAccount.getDeletedAt() != null;
	}

	public boolean isLocked() {
		return localAuth != null && localAuth.getLockedUntil() != null && localAuth.getLockedUntil().after(new Timestamp(System.currentTimeMillis()));
	}

	public boolean isEmailVerified() {
		return userEmail != null && userEmail.getVerifiedAt() != null;
	}

	public String getFullname() {
		return profile != null ? profile.getFullname() : null;
	}

	public void setFullname(String fullname) {
		if (this.profile == null) {
			this.profile = new UserProfile();
		}
		this.profile.setFullname(fullname);
	}

	public String getPhoneNumber() {
		return profile != null ? profile.getPhoneNumber() : null;
	}

	public void setPhoneNumber(String phoneNumber) {
		if (this.profile == null) {
			this.profile = new UserProfile();
		}
		this.profile.setPhoneNumber(phoneNumber);
	}

	public String getAvatarUrl() {
		return profile != null ? profile.getAvatarUrl() : null;
	}

	public void setAvatarUrl(String avatarUrl) {
		if (this.profile == null) {
			this.profile = new UserProfile();
		}
		this.profile.setAvatarUrl(avatarUrl);
	}

	public Gender getGender() {
		return profile != null ? profile.getGender() : null;
	}

	public void setGender(Gender gender) {
		if (this.profile == null) {
			this.profile = new UserProfile();
		}
		this.profile.setGender(gender);
	}

	public String getPasswordHash() {
		return localAuth != null ? localAuth.getPasswordHash() : null;
	}

	public void setPasswordHash(String passwordHash) {
		if (this.localAuth == null) {
			this.localAuth = new UserLocalAuth();
		}
		this.localAuth.setPasswordHash(passwordHash);
	}

	public Timestamp getLockedUntil() {
		return localAuth != null ? localAuth.getLockedUntil() : null;
	}

	public Language getPreferredLanguage() {
		return profile != null ? profile.getPreferredLanguage() : null;
	}

	public void setPreferredLanguage(Language language) {
		if (this.profile == null) {
			this.profile = new UserProfile();
		}
		this.profile.setPreferredLanguage(language);
	}
}
