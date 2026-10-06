package helpers;

import java.util.Set;

import beans.User;
import constants.SessionConstants;
import context.PendingEmailChangeContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class SessionHelper {

	private SessionHelper() {
	}

	public static HttpSession renewSession(HttpServletRequest request) {
		HttpSession oldSession = request.getSession(false);
		if (oldSession != null) {
			oldSession.invalidate();
		}
		return request.getSession(true);
	}

	public static void setCurrentUser(HttpSession session, User user) {
		if (session == null || user == null) {
			return;
		}
		session.setAttribute(SessionConstants.CURRENT_USER, user);
	}

	public static void setSuperAdmin(HttpSession session, boolean isSuperAdmin) {
		if (session == null) {
			return;
		}
		session.setAttribute(SessionConstants.IS_SUPER_ADMIN, Boolean.valueOf(isSuperAdmin));
	}

	public static void setUserPermissions(HttpSession session, Set<String> permissionCodes) {
		if (session == null || permissionCodes == null) {
			return;
		}
		session.setAttribute(SessionConstants.USER_PERMISSIONS, permissionCodes);
	}

	public static void setPendingVerification(HttpSession session, Long userId, String email) {
		if (session == null) {
			return;
		}
		session.setAttribute(SessionConstants.PENDING_VERIFICATION_USER_ID, userId);
		session.setAttribute(SessionConstants.PENDING_VERIFICATION_EMAIL, email);
	}

	public static void clearPendingVerification(HttpSession session) {
		if (session == null) {
			return;
		}
		session.removeAttribute(SessionConstants.PENDING_VERIFICATION_USER_ID);
		session.removeAttribute(SessionConstants.PENDING_VERIFICATION_EMAIL);
	}

	public static void setPendingEmailChange(HttpSession session, PendingEmailChangeContext pendingEmailChange) {
		if (session == null || pendingEmailChange == null) {
			return;
		}
		session.setAttribute(SessionConstants.PENDING_EMAIL_CHANGE_CURRENT_EMAIL, pendingEmailChange.getCurrentEmail());
		session.setAttribute(SessionConstants.PENDING_EMAIL_CHANGE_NEW_EMAIL, pendingEmailChange.getNewEmail());
		session.setAttribute(SessionConstants.PENDING_EMAIL_CHANGE_CURRENT_CODE_HASH, pendingEmailChange.getCurrentEmailCodeHash());
		session.setAttribute(SessionConstants.PENDING_EMAIL_CHANGE_NEW_CODE_HASH, pendingEmailChange.getNewEmailCodeHash());
		session.setAttribute(SessionConstants.PENDING_EMAIL_CHANGE_EXPIRES_AT, Long.valueOf(pendingEmailChange.getExpiresAt()));
	}

	public static PendingEmailChangeContext getPendingEmailChange(HttpSession session) {
		if (session == null) {
			return null;
		}

		String currentEmail = (String) session.getAttribute(SessionConstants.PENDING_EMAIL_CHANGE_CURRENT_EMAIL);
		String newEmail = (String) session.getAttribute(SessionConstants.PENDING_EMAIL_CHANGE_NEW_EMAIL);
		String currentEmailCodeHash = (String) session.getAttribute(SessionConstants.PENDING_EMAIL_CHANGE_CURRENT_CODE_HASH);
		String newEmailCodeHash = (String) session.getAttribute(SessionConstants.PENDING_EMAIL_CHANGE_NEW_CODE_HASH);
		Long expiresAt = (Long) session.getAttribute(SessionConstants.PENDING_EMAIL_CHANGE_EXPIRES_AT);

		if (currentEmail == null || newEmail == null || currentEmailCodeHash == null || newEmailCodeHash == null || expiresAt == null) {
			return null;
		}

		return new PendingEmailChangeContext(currentEmail, newEmail, currentEmailCodeHash, newEmailCodeHash, expiresAt.longValue());
	}

	public static void clearPendingEmailChange(HttpSession session) {
		if (session == null) {
			return;
		}
		session.removeAttribute(SessionConstants.PENDING_EMAIL_CHANGE_CURRENT_EMAIL);
		session.removeAttribute(SessionConstants.PENDING_EMAIL_CHANGE_NEW_EMAIL);
		session.removeAttribute(SessionConstants.PENDING_EMAIL_CHANGE_CURRENT_CODE_HASH);
		session.removeAttribute(SessionConstants.PENDING_EMAIL_CHANGE_NEW_CODE_HASH);
		session.removeAttribute(SessionConstants.PENDING_EMAIL_CHANGE_EXPIRES_AT);
	}
}
