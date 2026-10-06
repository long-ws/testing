package constants;

public final class ViewAttributeConstants {

	private ViewAttributeConstants() {
	}

	public static final String ERRORS = "errors";
	public static final String VALUES = "values";
	public static final String ERROR_MESSAGE = SessionConstants.ERROR_MESSAGE;

	public static final class Dashboard {
		private Dashboard() {
		}

		public static final String TOTAL_USERS = "totalUsers";
		public static final String TOTAL_CATEGORIES = "totalCategories";
		public static final String TOTAL_PRODUCTS = "totalProducts";
		public static final String TOTAL_ORDERS = "totalOrders";
	}

	public static final class Security {
		private Security() {
		}

		public static final String USER_PERMISSION_CONTEXT = "userPermissionContext";
		public static final String PAGE = "securityPage";
		public static final String PAGE_CHANGE_PASSWORD = "CHANGE_PASSWORD";
		public static final String PAGE_CHANGE_EMAIL = "CHANGE_EMAIL";
		public static final String CHANGE_EMAIL_STEP = "securityChangeEmailStep";
		public static final String CHANGE_EMAIL_STEP_INPUT = "INPUT";
		public static final String CHANGE_EMAIL_STEP_VERIFY = "VERIFY";
		public static final String BACK_ROUTE = "securityBackRoute";
	}

	public static final class Error {
		private Error() {
		}

		public static final String STATUS_CODE = "statusCode";
		public static final String MESSAGE = "message";
		public static final String REQUEST_URI = "requestUri";
	}

	public static final class User {
		private User() {
		}

		public static final String USERS = "users";
		public static final String USER = "user";
		public static final String ALL_ROLES = "allRoles";
		public static final String LANGUAGES = "languages";
		public static final String ORDERED_LANGUAGES = "orderedLanguages";
		public static final String GENDERS = "genders";
		public static final String EMAIL = "email";
		public static final String DISPLAY_NAME = "displayName";
		public static final String AVATAR_SRC = "avatarSrc";
		public static final String SELECTED_ROLE_CODE = "selectedRoleCode";
		public static final String SELECTED_LANGUAGE_CODE = "selectedLanguageCode";
		public static final String SELECTED_GENDER_CODE = "selectedGenderCode";
		public static final String HAS_CREATE = "hasUserCreate";
		public static final String HAS_EDIT = "hasUserEdit";
		public static final String HAS_DELETE = "hasUserDelete";
		public static final String IS_SYSTEM = "isSystem";
	}

	public static final class Role {
		private Role() {
		}

		public static final String ROLES = "roles";
		public static final String ROLE = "role";
		public static final String ROLE_PERMISSIONS = "rolePermissions";
		public static final String UNASSIGNED_PERMISSIONS = "unassignedPermissions";
		public static final String HAS_UNASSIGNED_PERMISSIONS = "hasUnassignedPermissions";
		public static final String HAS_CREATE = "hasRoleCreate";
		public static final String HAS_EDIT = "hasRoleEdit";
		public static final String HAS_DELETE = "hasRoleDelete";
	}

	public static final class Permission {
		private Permission() {
		}

		public static final String PERMISSION = "permission";
		public static final String MODULES = "modules";
		public static final String SELECTED_MODULE = "selectedModule";
		public static final String FORM_LOCKED = "permissionFormLocked";
		public static final String PERMISSIONS_BY_MODULE = "permissionsByModule";
		public static final String HAS_CREATE = "hasPermissionCreate";
		public static final String HAS_EDIT = "hasPermissionEdit";
		public static final String HAS_DELETE = "hasPermissionDelete";
	}
}
