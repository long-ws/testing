package constants;

public final class SessionConstants {

	public static final String SIGNUP_SUCCESS = "signupSuccess";
	public static final String OAUTH_ERROR = "oauthError";

	public static final String OAUTH_STATE = "oauthState";
	public static final String OAUTH_STATE_CREATED_AT = "oauthStateCreatedAt";

	public static final String CURRENT_USER = "currentUser";

	public static final String IS_SUPER_ADMIN = "isSuperAdmin";

	public static final String USER_PERMISSIONS = "userPermissions";

	public static final String SUCCESS_MESSAGE = "successMessage";
	public static final String ERROR_MESSAGE = "errorMessage";

	public static final String PENDING_VERIFICATION_USER_ID = "pendingVerificationUserId";
	public static final String PENDING_VERIFICATION_EMAIL = "pendingVerificationEmail";
	public static final String PENDING_EMAIL_CHANGE_CURRENT_EMAIL = "pendingEmailChangeCurrentEmail";
	public static final String PENDING_EMAIL_CHANGE_NEW_EMAIL = "pendingEmailChangeNewEmail";
	public static final String PENDING_EMAIL_CHANGE_CURRENT_CODE_HASH = "pendingEmailChangeCurrentCodeHash";
	public static final String PENDING_EMAIL_CHANGE_NEW_CODE_HASH = "pendingEmailChangeNewCodeHash";
	public static final String PENDING_EMAIL_CHANGE_EXPIRES_AT = "pendingEmailChangeExpiresAt";

	private SessionConstants() {
	}
}
