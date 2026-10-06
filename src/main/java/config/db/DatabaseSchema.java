package config.db;

public final class DatabaseSchema {

    // Common
    public static final String COL_ID = "id";
    public static final String COL_CODE = "code";
    public static final String COL_NAME = "name";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_UPDATED_AT = "updated_at";
    public static final String COL_IS_ACTIVE = "is_active";

    // Tables
    public static final String TABLE_USER_ACCOUNT_STATUS = "user_account_status";
    public static final String TABLE_OAUTH_PROVIDER = "oauth_provider";
    public static final String TABLE_TOKEN_TYPE = "token_type";
    public static final String TABLE_TOKEN_STATUS = "token_status";
    public static final String TABLE_GENDER_REGISTRY = "gender_registry";
    public static final String TABLE_LANGUAGE_REGISTRY = "language_registry";
    public static final String TABLE_ROLE_REGISTRY = "role_registry";
    public static final String TABLE_PERMISSION_REGISTRY = "permission_registry";
    public static final String TABLE_ROLE_PERMISSION_ASSIGNMENT = "role_permission_assignment";
    public static final String TABLE_USER_ACCOUNT = "user_account";
    public static final String TABLE_USER_ROLE_REGISTRY = "user_role_registry";
    public static final String TABLE_USER_EMAIL = "user_email";
    public static final String TABLE_USER_LOCAL = "user_local";
    public static final String TABLE_USER_OAUTH = "user_oauth";
    public static final String TABLE_USER_TOKEN = "user_token";
    public static final String TABLE_USER_PROFILE = "user_profile";

    // user_account_status
    public static final String COL_USER_ACCOUNT_STATUS_PK_ID = COL_ID;
    public static final String COL_USER_ACCOUNT_STATUS_CODE = COL_CODE;
    public static final String COL_USER_ACCOUNT_STATUS_NAME = COL_NAME;
    public static final String COL_USER_ACCOUNT_STATUS_DESCRIPTION = COL_DESCRIPTION;
    public static final String COL_USER_ACCOUNT_STATUS_CREATED_AT = COL_CREATED_AT;
    public static final String COL_USER_ACCOUNT_STATUS_IS_ACTIVE = COL_IS_ACTIVE;

    // oauth_provider
    public static final String COL_OAUTH_PROVIDER_ID = COL_ID;
    public static final String COL_OAUTH_PROVIDER_CODE = COL_CODE;
    public static final String COL_OAUTH_PROVIDER_NAME = COL_NAME;
    public static final String COL_OAUTH_PROVIDER_DESCRIPTION = COL_DESCRIPTION;
    public static final String COL_OAUTH_PROVIDER_CREATED_AT = COL_CREATED_AT;
    public static final String COL_OAUTH_PROVIDER_IS_ACTIVE = COL_IS_ACTIVE;

    // token_type
    public static final String COL_TOKEN_TYPE_ID = COL_ID;
    public static final String COL_TOKEN_TYPE_CODE = COL_CODE;
    public static final String COL_TOKEN_TYPE_NAME = COL_NAME;
    public static final String COL_TOKEN_TYPE_DESCRIPTION = COL_DESCRIPTION;
    public static final String COL_TOKEN_TYPE_CREATED_AT = COL_CREATED_AT;
    public static final String COL_TOKEN_TYPE_IS_ACTIVE = COL_IS_ACTIVE;

    // token_status
    public static final String COL_TOKEN_STATUS_ID = COL_ID;
    public static final String COL_TOKEN_STATUS_CODE = COL_CODE;
    public static final String COL_TOKEN_STATUS_NAME = COL_NAME;
    public static final String COL_TOKEN_STATUS_DESCRIPTION = COL_DESCRIPTION;
    public static final String COL_TOKEN_STATUS_CREATED_AT = COL_CREATED_AT;
    public static final String COL_TOKEN_STATUS_IS_ACTIVE = COL_IS_ACTIVE;

    // gender_registry
    public static final String COL_GENDER_REGISTRY_ID = COL_ID;
    public static final String COL_GENDER_REGISTRY_CODE = COL_CODE;
    public static final String COL_GENDER_REGISTRY_NAME = COL_NAME;
    public static final String COL_GENDER_REGISTRY_DESCRIPTION = COL_DESCRIPTION;
    public static final String COL_GENDER_REGISTRY_CREATED_AT = COL_CREATED_AT;
    public static final String COL_GENDER_REGISTRY_IS_ACTIVE = COL_IS_ACTIVE;

    // language_registry
    public static final String COL_LANGUAGE_REGISTRY_ID = COL_ID;
    public static final String COL_LANGUAGE_REGISTRY_CODE = COL_CODE;
    public static final String COL_LANGUAGE_REGISTRY_NAME = COL_NAME;
    public static final String COL_LANGUAGE_REGISTRY_DESCRIPTION = COL_DESCRIPTION;
    public static final String COL_LANGUAGE_REGISTRY_CREATED_AT = COL_CREATED_AT;
    public static final String COL_LANGUAGE_REGISTRY_IS_ACTIVE = COL_IS_ACTIVE;

    // role_registry
    public static final String COL_ROLE_REGISTRY_ID = COL_ID;
    public static final String COL_ROLE_REGISTRY_CODE = COL_CODE;
    public static final String COL_ROLE_REGISTRY_NAME = COL_NAME;
    public static final String COL_ROLE_REGISTRY_DESCRIPTION = COL_DESCRIPTION;
    public static final String COL_ROLE_REGISTRY_IS_SYSTEM = "is_system";
    public static final String COL_ROLE_REGISTRY_IS_ACTIVE = COL_IS_ACTIVE;
    public static final String COL_ROLE_REGISTRY_CREATED_AT = COL_CREATED_AT;
    public static final String COL_ROLE_REGISTRY_UPDATED_AT = COL_UPDATED_AT;

    // permission_registry
    public static final String COL_PERMISSION_REGISTRY_ID = COL_ID;
    public static final String COL_PERMISSION_REGISTRY_CODE = COL_CODE;
    public static final String COL_PERMISSION_REGISTRY_NAME = COL_NAME;
    public static final String COL_PERMISSION_REGISTRY_DESCRIPTION = COL_DESCRIPTION;
    public static final String COL_PERMISSION_REGISTRY_MODULE = "module";
    public static final String COL_PERMISSION_REGISTRY_IS_SYSTEM = "is_system";
    public static final String COL_PERMISSION_REGISTRY_IS_ACTIVE = COL_IS_ACTIVE;
    public static final String COL_PERMISSION_REGISTRY_CREATED_AT = COL_CREATED_AT;

    // role_permission_assignment
    public static final String COL_ROLE_PERMISSION_ASSIGNMENT_ROLE_ID = "role_id";
    public static final String COL_ROLE_PERMISSION_ASSIGNMENT_PERMISSION_ID = "permission_id";
    public static final String COL_ROLE_PERMISSION_ASSIGNMENT_IS_ACTIVE = COL_IS_ACTIVE;
    public static final String COL_ROLE_PERMISSION_ASSIGNMENT_CREATED_AT = COL_CREATED_AT;

    // user_account
    public static final String COL_USER_ACCOUNT_ID = COL_ID;
    public static final String COL_USER_ACCOUNT_STATUS_ID = "status_id";
    public static final String COL_USER_ACCOUNT_TOKEN_VERSION = "token_version";
    public static final String COL_USER_ACCOUNT_LAST_LOGIN_AT = "last_login_at";
    public static final String COL_USER_ACCOUNT_REMEMBER_TOKEN = "remember_token";
    public static final String COL_USER_ACCOUNT_REMEMBER_EXPIRES_AT = "remember_expires_at";
    public static final String COL_USER_ACCOUNT_DELETED_AT = "deleted_at";
    public static final String COL_USER_ACCOUNT_DELETED_BY = "deleted_by";
    public static final String COL_USER_ACCOUNT_DELETION_SCHEDULED_AT = "deletion_scheduled_at";
    public static final String COL_USER_ACCOUNT_CREATED_AT = COL_CREATED_AT;
    public static final String COL_USER_ACCOUNT_UPDATED_AT = COL_UPDATED_AT;

    // user_role_registry
    public static final String COL_USER_ROLE_REGISTRY_USER_ID = "user_id";
    public static final String COL_USER_ROLE_REGISTRY_ROLE_ID = "role_id";
    public static final String COL_USER_ROLE_REGISTRY_ASSIGNED_AT = "assigned_at";
    public static final String COL_USER_ROLE_REGISTRY_ASSIGNED_BY = "assigned_by";

    // user_email
    public static final String COL_USER_EMAIL_ID = COL_ID;
    public static final String COL_USER_EMAIL_USER_ID = "user_id";
    public static final String COL_USER_EMAIL_EMAIL = "email";
    public static final String COL_USER_EMAIL_VERIFIED_AT = "verified_at";
    public static final String COL_USER_EMAIL_CREATED_AT = COL_CREATED_AT;
    public static final String COL_USER_EMAIL_UPDATED_AT = COL_UPDATED_AT;

    // user_local
    public static final String COL_USER_LOCAL_USER_ID = "user_id";
    public static final String COL_USER_LOCAL_USERNAME = "username";
    public static final String COL_USER_LOCAL_PASSWORD_HASH = "password_hash";
    public static final String COL_USER_LOCAL_FAILED_ATTEMPTS = "failed_attempts";
    public static final String COL_USER_LOCAL_LOCKED_UNTIL = "locked_until";
    public static final String COL_USER_LOCAL_CREATED_AT = COL_CREATED_AT;
    public static final String COL_USER_LOCAL_UPDATED_AT = COL_UPDATED_AT;

    // user_oauth
    public static final String COL_USER_OAUTH_ID = COL_ID;
    public static final String COL_USER_OAUTH_USER_ID = "user_id";
    public static final String COL_USER_OAUTH_PROVIDER_ID = "provider_id";
    public static final String COL_USER_OAUTH_PROVIDER_USER_ID = "provider_user_id";
    public static final String COL_USER_OAUTH_PROVIDER_EMAIL = "provider_email";
    public static final String COL_USER_OAUTH_DISPLAY_NAME = "display_name";
    public static final String COL_USER_OAUTH_AVATAR_URL = "avatar_url";
    public static final String COL_USER_OAUTH_CREATED_AT = COL_CREATED_AT;

    // user_token
    public static final String COL_USER_TOKEN_ID = COL_ID;
    public static final String COL_USER_TOKEN_USER_ID = "user_id";
    public static final String COL_USER_TOKEN_TOKEN_HASH = "token_hash";
    public static final String COL_USER_TOKEN_TYPE_ID = "type_id";
    public static final String COL_USER_TOKEN_STATUS_ID = "status_id";
    public static final String COL_USER_TOKEN_USED_AT = "used_at";
    public static final String COL_USER_TOKEN_EXPIRES_AT = "expires_at";
    public static final String COL_USER_TOKEN_CREATED_AT = COL_CREATED_AT;

    // user_profile
    public static final String COL_USER_PROFILE_USER_ID = "user_id";
    public static final String COL_USER_PROFILE_FULLNAME = "fullname";
    public static final String COL_USER_PROFILE_PHONE_NUMBER = "phone_number";
    public static final String COL_USER_PROFILE_GENDER_ID = "gender_id";
    public static final String COL_USER_PROFILE_AVATAR_URL = "avatar_url";
    public static final String COL_USER_PROFILE_PREFERRED_LANGUAGE_ID = "preferred_language_id";
    public static final String COL_USER_PROFILE_UPDATED_AT = COL_UPDATED_AT;

    private DatabaseSchema() {
    }
}
