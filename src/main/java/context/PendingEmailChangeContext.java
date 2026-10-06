package context;

public class PendingEmailChangeContext {
	private final String currentEmail;
	private final String newEmail;
	private final String currentEmailCodeHash;
	private final String newEmailCodeHash;
	private final long expiresAt;

	public PendingEmailChangeContext(String currentEmail, String newEmail, String currentEmailCodeHash, String newEmailCodeHash, long expiresAt) {
		this.currentEmail = currentEmail;
		this.newEmail = newEmail;
		this.currentEmailCodeHash = currentEmailCodeHash;
		this.newEmailCodeHash = newEmailCodeHash;
		this.expiresAt = expiresAt;
	}

	public String getCurrentEmail() {
		return currentEmail;
	}

	public String getNewEmail() {
		return newEmail;
	}

	public String getCurrentEmailCodeHash() {
		return currentEmailCodeHash;
	}

	public String getNewEmailCodeHash() {
		return newEmailCodeHash;
	}

	public long getExpiresAt() {
		return expiresAt;
	}

	public boolean isExpired(long currentTimeMillis) {
		return expiresAt < currentTimeMillis;
	}
}
