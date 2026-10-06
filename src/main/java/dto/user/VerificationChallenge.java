package dto.user;

import context.PendingEmailChangeContext;

public class VerificationChallenge {
	private final PendingEmailChangeContext pendingEmailChange;
	private final String currentEmailCode;
	private final String newEmailCode;

	public VerificationChallenge(PendingEmailChangeContext pendingEmailChange, String currentEmailCode, String newEmailCode) {
		this.pendingEmailChange = pendingEmailChange;
		this.currentEmailCode = currentEmailCode;
		this.newEmailCode = newEmailCode;
	}

	public PendingEmailChangeContext getPendingEmailChange() {
		return pendingEmailChange;
	}

	public String getCurrentEmailCode() {
		return currentEmailCode;
	}

	public String getNewEmailCode() {
		return newEmailCode;
	}
}
