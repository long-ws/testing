package dao.user;

import static config.db.DatabaseSchema.COL_USER_ACCOUNT_CREATED_AT;
import static config.db.DatabaseSchema.COL_USER_ACCOUNT_DELETED_AT;
import static config.db.DatabaseSchema.COL_USER_ACCOUNT_DELETED_BY;
import static config.db.DatabaseSchema.COL_USER_ACCOUNT_DELETION_SCHEDULED_AT;
import static config.db.DatabaseSchema.COL_USER_ACCOUNT_ID;
import static config.db.DatabaseSchema.COL_USER_ACCOUNT_LAST_LOGIN_AT;
import static config.db.DatabaseSchema.COL_USER_ACCOUNT_REMEMBER_EXPIRES_AT;
import static config.db.DatabaseSchema.COL_USER_ACCOUNT_REMEMBER_TOKEN;
import static config.db.DatabaseSchema.COL_USER_ACCOUNT_STATUS_ID;
import static config.db.DatabaseSchema.COL_USER_ACCOUNT_TOKEN_VERSION;
import static config.db.DatabaseSchema.COL_USER_ACCOUNT_UPDATED_AT;
import static config.db.DatabaseSchema.COL_USER_ACCOUNT_STATUS_CODE;
import static config.db.DatabaseSchema.COL_USER_ACCOUNT_STATUS_NAME;
import static config.db.DatabaseSchema.COL_USER_ACCOUNT_STATUS_DESCRIPTION;
import static config.db.DatabaseSchema.COL_USER_ACCOUNT_STATUS_CREATED_AT;
import static config.db.DatabaseSchema.COL_USER_ACCOUNT_STATUS_IS_ACTIVE;
import static config.db.DatabaseSchema.COL_USER_ACCOUNT_STATUS_PK_ID;
import static config.db.DatabaseSchema.TABLE_USER_ACCOUNT;
import static config.db.DatabaseSchema.TABLE_USER_ACCOUNT_STATUS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import beans.common.UserAccountStatus;
import beans.user.UserAccount;
import config.security.SecurityConfig;

public class UserAccountDAOImpl implements UserAccountDAO {
	private static final String ALIAS_ACCOUNT_ID = "account_id";
	private static final String ALIAS_ACCOUNT_STATUS_ID = "account_status_id";
	private static final String ALIAS_ACCOUNT_TOKEN_VERSION = "account_token_version";
	private static final String ALIAS_ACCOUNT_LAST_LOGIN_AT = "account_last_login_at";
	private static final String ALIAS_ACCOUNT_REMEMBER_TOKEN = "account_remember_token";
	private static final String ALIAS_ACCOUNT_REMEMBER_EXPIRES_AT = "account_remember_expires_at";
	private static final String ALIAS_ACCOUNT_DELETED_AT = "account_deleted_at";
	private static final String ALIAS_ACCOUNT_DELETED_BY = "account_deleted_by";
	private static final String ALIAS_ACCOUNT_DELETION_SCHEDULED_AT = "account_deletion_scheduled_at";
	private static final String ALIAS_ACCOUNT_CREATED_AT = "account_created_at";
	private static final String ALIAS_ACCOUNT_UPDATED_AT = "account_updated_at";
	private static final String ALIAS_STATUS_ID = "status_id";
	private static final String ALIAS_STATUS_CODE = "status_code";
	private static final String ALIAS_STATUS_NAME = "status_name";
	private static final String ALIAS_STATUS_DESCRIPTION = "status_description";
	private static final String ALIAS_STATUS_CREATED_AT = "status_created_at";
	private static final String ALIAS_STATUS_IS_ACTIVE = "status_is_active";

	private static String SELECT_FIELDS = """
			ua.%s AS %s, ua.%s AS %s, ua.%s AS %s,
			ua.%s AS %s, ua.%s AS %s,
			ua.%s AS %s, ua.%s AS %s,
			ua.%s AS %s, ua.%s AS %s,
			ua.%s AS %s, ua.%s AS %s,
			uas.%s AS %s, uas.%s AS %s, uas.%s AS %s,
			uas.%s AS %s, uas.%s AS %s,
			uas.%s AS %s
			""".formatted(
					COL_USER_ACCOUNT_ID, ALIAS_ACCOUNT_ID, COL_USER_ACCOUNT_STATUS_ID, ALIAS_ACCOUNT_STATUS_ID, COL_USER_ACCOUNT_TOKEN_VERSION, ALIAS_ACCOUNT_TOKEN_VERSION,
					COL_USER_ACCOUNT_LAST_LOGIN_AT, ALIAS_ACCOUNT_LAST_LOGIN_AT, COL_USER_ACCOUNT_REMEMBER_TOKEN, ALIAS_ACCOUNT_REMEMBER_TOKEN,
					COL_USER_ACCOUNT_REMEMBER_EXPIRES_AT, ALIAS_ACCOUNT_REMEMBER_EXPIRES_AT, COL_USER_ACCOUNT_DELETED_AT, ALIAS_ACCOUNT_DELETED_AT,
					COL_USER_ACCOUNT_DELETED_BY, ALIAS_ACCOUNT_DELETED_BY, COL_USER_ACCOUNT_DELETION_SCHEDULED_AT, ALIAS_ACCOUNT_DELETION_SCHEDULED_AT,
					COL_USER_ACCOUNT_CREATED_AT, ALIAS_ACCOUNT_CREATED_AT, COL_USER_ACCOUNT_UPDATED_AT, ALIAS_ACCOUNT_UPDATED_AT,
					COL_USER_ACCOUNT_STATUS_PK_ID, ALIAS_STATUS_ID, COL_USER_ACCOUNT_STATUS_CODE, ALIAS_STATUS_CODE, COL_USER_ACCOUNT_STATUS_NAME, ALIAS_STATUS_NAME,
					COL_USER_ACCOUNT_STATUS_DESCRIPTION, ALIAS_STATUS_DESCRIPTION, COL_USER_ACCOUNT_STATUS_CREATED_AT, ALIAS_STATUS_CREATED_AT,
					COL_USER_ACCOUNT_STATUS_IS_ACTIVE, ALIAS_STATUS_IS_ACTIVE
			);

	private static String SQL_BASE = """
			SELECT %s
			FROM %s ua
			LEFT JOIN %s uas ON ua.%s = uas.%s
			""".formatted(SELECT_FIELDS, TABLE_USER_ACCOUNT, TABLE_USER_ACCOUNT_STATUS,
					COL_USER_ACCOUNT_STATUS_ID, COL_USER_ACCOUNT_STATUS_PK_ID);

	private static String SQL_INSERT = """
			INSERT INTO %s (
				%s, %s, created_at, updated_at
			) VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
			""".formatted(TABLE_USER_ACCOUNT, COL_USER_ACCOUNT_STATUS_ID, COL_USER_ACCOUNT_TOKEN_VERSION);

	private static String SQL_UPDATE = """
			UPDATE %s
			SET
				%s = ?,
				%s = ?,
				updated_at = CURRENT_TIMESTAMP
			WHERE %s = ?
			""".formatted(TABLE_USER_ACCOUNT, COL_USER_ACCOUNT_STATUS_ID, COL_USER_ACCOUNT_TOKEN_VERSION, COL_USER_ACCOUNT_ID);

	private static String SQL_SOFT_DELETE = """
			UPDATE %s
			SET
				%s = CURRENT_TIMESTAMP
			WHERE %s = ?
			""".formatted(TABLE_USER_ACCOUNT, COL_USER_ACCOUNT_DELETED_AT, COL_USER_ACCOUNT_ID);

	private static String SQL_FIND_BY_ID = SQL_BASE + " WHERE ua.%s = ? AND ua.%s IS NULL"
			.formatted(COL_USER_ACCOUNT_ID, COL_USER_ACCOUNT_DELETED_AT);

	private static String SQL_FIND_ALL = SQL_BASE + " WHERE ua.%s IS NULL ORDER BY ua.%s DESC"
			.formatted(COL_USER_ACCOUNT_DELETED_AT, COL_USER_ACCOUNT_ID);

	private static String SQL_COUNT = """
			SELECT COUNT(*)
			FROM %s
			WHERE %s IS NULL
			""".formatted(TABLE_USER_ACCOUNT, COL_USER_ACCOUNT_DELETED_AT);

	private static String SQL_INCREMENT_TOKEN = """
			UPDATE %s
			SET
				%s = %s + 1,
				updated_at = CURRENT_TIMESTAMP
			WHERE %s = ?
			""".formatted(TABLE_USER_ACCOUNT, COL_USER_ACCOUNT_TOKEN_VERSION, COL_USER_ACCOUNT_TOKEN_VERSION, COL_USER_ACCOUNT_ID);

	private static String SQL_GET_TOKEN_VERSION = """
			SELECT %s
			FROM %s
			WHERE %s = ?
			""".formatted(COL_USER_ACCOUNT_TOKEN_VERSION, TABLE_USER_ACCOUNT, COL_USER_ACCOUNT_ID);

	private static String SQL_FIND_ALL_IDS = "SELECT " + COL_USER_ACCOUNT_ID + " FROM " + TABLE_USER_ACCOUNT + " WHERE " + COL_USER_ACCOUNT_DELETED_AT + " IS NULL ORDER BY " + COL_USER_ACCOUNT_ID
			+ " DESC";

	private static String SQL_FIND_BY_IDS = """
			%s WHERE ua.%s IN (
			""".formatted(SQL_BASE, COL_USER_ACCOUNT_ID);

	@Override
	public long insert(Connection conn, UserAccount account) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, account.getStatusId());
			ps.setInt(2, account.getTokenVersion());
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getLong(1);
				}
				throw new SQLException("Thao tác khởi tạo bản ghi tài khoản thất bại.");
			}
		}
	}

	@Override
	public void update(Connection conn, UserAccount account) throws SQLException {
		if (account != null && SecurityConfig.isSystemGhostUserId(account.getId())) {
			throw new SQLException("Không thể cập nhật tài khoản hệ thống.");
		}
		try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
			ps.setInt(1, account.getStatusId());
			ps.setInt(2, account.getTokenVersion());
			ps.setLong(3, account.getId());
			ps.executeUpdate();
		}
	}

	@Override
	public void softDeleteBatch(Connection conn, List<Long> userIds) throws SQLException {
		if (userIds == null || userIds.isEmpty()) {
			return;
		}
		try (PreparedStatement ps = conn.prepareStatement(SQL_SOFT_DELETE)) {
			for (Long id : userIds) {
				if (id != null && !SecurityConfig.isSystemGhostUserId(id)) {
					ps.setLong(1, id);
					ps.addBatch();
				}
			}
			ps.executeBatch();
		}
	}

	@Override
	public Optional<UserAccount> findById(Connection conn, long userId) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapRow(rs));
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public List<UserAccount> findAllNotDeleted(Connection conn) throws SQLException {
		List<UserAccount> list = new ArrayList<UserAccount>();
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				list.add(mapRow(rs));
			}
		}
		return list;
	}

	@Override
	public long countNotDeleted(Connection conn) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_COUNT); ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getLong(1);
			}
			return 0L;
		}
	}

	@Override
	public boolean incrementTokenVersion(Connection conn, long userId) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_INCREMENT_TOKEN)) {
			ps.setLong(1, userId);
			return ps.executeUpdate() > 0;
		}
	}

	@Override
	public int getTokenVersion(Connection conn, long userId) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_GET_TOKEN_VERSION)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(COL_USER_ACCOUNT_TOKEN_VERSION);
				}
				return 0;
			}
		}
	}

	@Override
	public List<Long> findAllIds(Connection conn) throws SQLException {
		List<Long> ids = new ArrayList<Long>();
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL_IDS); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				ids.add(rs.getLong(COL_USER_ACCOUNT_ID));
			}
		}
		return ids;
	}

	@Override
	public List<UserAccount> findAllAccountsByIds(Connection connection, List<Long> accountIds) throws SQLException {
		if (accountIds == null || accountIds.isEmpty()) {
			return new ArrayList<>();
		}

		Map<Long, UserAccount> accountMap = findByIdsAsMap(connection, accountIds);
		List<UserAccount> ordered = new ArrayList<>();

		for (int i = 0; i < accountIds.size(); i++) {
			Long id = accountIds.get(i);
			if (id != null) {
				UserAccount account = accountMap.get(id);
				if (account != null) {
					ordered.add(account);
				}
			}
		}

		return ordered;
	}

	@Override
	public Map<Long, UserAccount> findByIdsAsMap(Connection conn, List<Long> ids) throws SQLException {
		Map<Long, UserAccount> result = new HashMap<>();
		if (ids == null || ids.isEmpty()) {
			return result;
		}

		int MAX_BATCH_SIZE = 500;

		for (int startIndex = 0; startIndex < ids.size(); startIndex = startIndex + MAX_BATCH_SIZE) {
			int endIndex = Math.min(startIndex + MAX_BATCH_SIZE, ids.size());
			List<Long> currentBatchIds = ids.subList(startIndex, endIndex);

			List<Long> sanitizedIds = new ArrayList<>();
			for (int i = 0; i < currentBatchIds.size(); i++) {
				Long id = currentBatchIds.get(i);
				if (id != null) {
					sanitizedIds.add(id);
				}
			}
			if (sanitizedIds.isEmpty()) {
				continue;
			}

			StringBuilder placeholdersBuilder = new StringBuilder();
			for (int i = 0; i < sanitizedIds.size(); i++) {
				if (i > 0) {
					placeholdersBuilder.append(',');
				}
				placeholdersBuilder.append('?');
			}

			String sqlQuery = SQL_FIND_BY_IDS + placeholdersBuilder.toString() + ")";
			try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
				for (int i = 0; i < sanitizedIds.size(); i++) {
					ps.setLong(i + 1, sanitizedIds.get(i));
				}

				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						UserAccount account = mapRow(rs);
						result.put(account.getId(), account);
					}
				}
			}
		}

		return result;
	}

	private UserAccount mapRow(ResultSet rs) throws SQLException {
		UserAccount account = new UserAccount();
		account.setId(rs.getLong(ALIAS_ACCOUNT_ID));

		int statusId = rs.getInt(ALIAS_ACCOUNT_STATUS_ID);
		account.setStatusId(statusId);

		UserAccountStatus status = new UserAccountStatus();
		status.setId(rs.getInt(ALIAS_STATUS_ID));
		status.setCode(rs.getString(ALIAS_STATUS_CODE));
		status.setName(rs.getString(ALIAS_STATUS_NAME));
		status.setDescription(rs.getString(ALIAS_STATUS_DESCRIPTION));
		status.setCreatedAt(rs.getTimestamp(ALIAS_STATUS_CREATED_AT));
		status.setActive(rs.getBoolean(ALIAS_STATUS_IS_ACTIVE));
		account.setStatus(status);

		account.setTokenVersion(rs.getInt(ALIAS_ACCOUNT_TOKEN_VERSION));
		account.setLastLoginAt(rs.getTimestamp(ALIAS_ACCOUNT_LAST_LOGIN_AT));
		account.setRememberToken(rs.getString(ALIAS_ACCOUNT_REMEMBER_TOKEN));
		account.setRememberExpiresAt(rs.getTimestamp(ALIAS_ACCOUNT_REMEMBER_EXPIRES_AT));
		account.setDeletedAt(rs.getTimestamp(ALIAS_ACCOUNT_DELETED_AT));

		long deletedBy = rs.getLong(ALIAS_ACCOUNT_DELETED_BY);
		if (!rs.wasNull()) {
			account.setDeletedBy(deletedBy);
		}

		account.setDeletionScheduledAt(rs.getTimestamp(ALIAS_ACCOUNT_DELETION_SCHEDULED_AT));
		account.setCreatedAt(rs.getTimestamp(ALIAS_ACCOUNT_CREATED_AT));
		account.setUpdatedAt(rs.getTimestamp(ALIAS_ACCOUNT_UPDATED_AT));
		return account;
	}
}
