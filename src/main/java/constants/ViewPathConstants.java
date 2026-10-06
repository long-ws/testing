package constants;

public final class ViewPathConstants {

	private ViewPathConstants() {
	}

	public static final class Client {
		private Client() {
		}

		public static final String SIGNIN = "/WEB-INF/views/signinView.jsp";
		public static final String SIGNUP = "/WEB-INF/views/signupView.jsp";
		public static final String USER = "/WEB-INF/views/userView.jsp";
		public static final String SECURITY = "/WEB-INF/views/securityView.jsp";
		public static final String VERIFY_EMAIL = "/WEB-INF/views/verifyEmailView.jsp";
	}

	public static final class Admin {
		private Admin() {
		}

		public static final String SIGNIN = "/WEB-INF/views/signinAdminView.jsp";
		public static final String FORBIDDEN = "/WEB-INF/views/admin403View.jsp";
		public static final String USER_CREATE = "/WEB-INF/views/createUserView.jsp";
		public static final String USER_UPDATE = "/WEB-INF/views/updateUserView.jsp";
		public static final String USER_DETAIL = "/WEB-INF/views/userDetailView.jsp";
		public static final String USER_MANAGER = "/WEB-INF/views/userManagerView.jsp";
		public static final String ROLE_CREATE = "/WEB-INF/views/roleManagerCreateView.jsp";
		public static final String ROLE_UPDATE = "/WEB-INF/views/roleManagerEditView.jsp";
		public static final String ROLE_MANAGER = "/WEB-INF/views/roleManagerView.jsp";
		public static final String PERMISSION_CREATE = "/WEB-INF/views/permissionManagerCreateView.jsp";
		public static final String PERMISSION_UPDATE = "/WEB-INF/views/permissionManagerEditView.jsp";
		public static final String PERMISSION_MANAGER = "/WEB-INF/views/permissionManagerView.jsp";
	}

	public static final class Common {
		private Common() {
		}

		public static final String ERROR = "/WEB-INF/views/errorView.jsp";
	}
}
