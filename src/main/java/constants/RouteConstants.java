package constants;

public final class RouteConstants {

	private RouteConstants() {
	}

	public static final class Client {
		private Client() {
		}

		public static final String HOME = "/";
		public static final String SIGNIN = "/signin";
		public static final String SIGNUP = "/signup";
		public static final String SIGNOUT = "/signout";
		public static final String USER = "/user";
		public static final String SECURITY = "/security";
		public static final String SECURITY_CHANGE_PASSWORD = "/security/changePassword";
		public static final String SECURITY_CHANGE_EMAIL = "/security/changeEmail";
		public static final String VERIFY_EMAIL = "/verify-email";
		public static final String VERIFY_EMAIL_CONFIRM = "/verify-email/confirm";
		public static final String VERIFY_EMAIL_RESEND = "/verify-email/resend";
		public static final String OAUTH_LOGIN = "/oauth-login";
		public static final String OAUTH_CALLBACK = "/oauth-callback";
	}

	public static final class Admin {
		private Admin() {
		}

		public static final String HOME = "/admin";
		public static final String SIGNIN = "/admin/signin";
		public static final String SIGNOUT = "/admin/signout";
		public static final String FORBIDDEN = "/admin/403";
		public static final String USER = "/admin/user";
		public static final String USER_CREATE = "/admin/user/create";
		public static final String USER_DELETE = "/admin/user/delete";
		public static final String USER_UPDATE = "/admin/user/update";
		public static final String ROLE = "/admin/role";
		public static final String ROLE_CREATE = "/admin/role/create";
		public static final String ROLE_DELETE = "/admin/role/delete";
		public static final String ROLE_UPDATE = "/admin/role/update";
		public static final String ROLE_ASSIGN_PERMISSIONS = "/admin/role/batchAssignPermission";
		public static final String ROLE_REMOVE_PERMISSIONS = "/admin/role/batchRemovePermission";
		public static final String PERMISSION = "/admin/permission";
		public static final String PERMISSION_CREATE = "/admin/permission/create";
		public static final String PERMISSION_DELETE = "/admin/permission/delete";
		public static final String PERMISSION_UPDATE = "/admin/permission/update";
	}

	public static final class Common {
		private Common() {
		}

		public static final String ERROR = "/error";
	}
}
