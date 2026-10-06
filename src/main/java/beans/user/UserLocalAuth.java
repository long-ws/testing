package beans.user;

import java.sql.Timestamp;

public class UserLocalAuth {
	private long userId;
	private String username;
	private String passwordHash;
	private int failedAttempts;
	private Timestamp lockedUntil;
	private Timestamp createdAt;
	private Timestamp updatedAt;

	public UserLocalAuth() {
	}

	public UserLocalAuth(long userId, String username, String passwordHash, int failedAttempts, Timestamp lockedUntil, Timestamp createdAt, Timestamp updatedAt) {
		this.userId = userId;
		this.username = username;
		this.passwordHash = passwordHash;
		this.failedAttempts = failedAttempts;
		this.lockedUntil = lockedUntil;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public int getFailedAttempts() {
		return failedAttempts;
	}

	public void setFailedAttempts(int failedAttempts) {
		this.failedAttempts = failedAttempts;
	}

	public Timestamp getLockedUntil() {
		return lockedUntil;
	}

	public void setLockedUntil(Timestamp lockedUntil) {
		this.lockedUntil = lockedUntil;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}
}
