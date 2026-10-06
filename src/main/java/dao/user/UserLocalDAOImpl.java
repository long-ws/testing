package dao.user;

import static config.db.DatabaseSchema.COL_USER_LOCAL_CREATED_AT;
import static config.db.DatabaseSchema.COL_USER_LOCAL_FAILED_ATTEMPTS;
import static config.db.DatabaseSchema.COL_USER_LOCAL_LOCKED_UNTIL;
import static config.db.DatabaseSchema.COL_USER_LOCAL_PASSWORD_HASH;
import static config.db.DatabaseSchema.COL_USER_LOCAL_USERNAME;
import static config.db.DatabaseSchema.COL_USER_LOCAL_UPDATED_AT;
import static config.db.DatabaseSchema.COL_USER_LOCAL_USER_ID;
import static config.db.DatabaseSchema.TABLE_USER_LOCAL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import beans.user.UserLocalAuth;

public class UserLocalDAOImpl implements UserLocalDAO {

	private static String SELECT_FIELDS = "%s, %s, %s, %s, %s, %s, %s".formatted(COL_USER_LOCAL_USER_ID, COL_USER_LOCAL_USERNAME, COL_USER_LOCAL_PASSWORD_HASH, COL_USER_LOCAL_FAILED_ATTEMPTS,
			COL_USER_LOCAL_LOCKED_UNTIL, COL_USER_LOCAL_CREATED_AT, COL_USER_LOCAL_UPDATED_AT);

	private static String SQL_INSERT = """
			INSERT INTO %s (
				%s, %s, %s, %s
			) VALUES (?, ?, ?, ?)
			""".formatted(TABLE_USER_LOCAL, COL_USER_LOCAL_USER_ID, COL_USER_LOCAL_USERNAME, COL_USER_LOCAL_PASSWORD_HASH, COL_USER_LOCAL_FAILED_ATTEMPTS);

	private static String SQL_UPDATE_PASSWORD = """
			UPDATE %s
			SET %s = ?, %s = CURRENT_TIMESTAMP
			WHERE %s = ?
			""".formatted(TABLE_USER_LOCAL, COL_USER_LOCAL_PASSWORD_HASH, COL_USER_LOCAL_UPDATED_AT, COL_USER_LOCAL_USER_ID);

	private static String SQL_FIND_BY_USER_ID = """
			SELECT %s
			FROM %s
			WHERE %s = ?
			""".formatted(SELECT_FIELDS, TABLE_USER_LOCAL, COL_USER_LOCAL_USER_ID);

	private static String SQL_FIND_BY_USER_IDS = """
			SELECT %s
			FROM %s
			WHERE %s IN (
			""".formatted(SELECT_FIELDS, TABLE_USER_LOCAL, COL_USER_LOCAL_USER_ID);

	private static String SQL_FIND_ID_BY_USERNAME = """
			SELECT %s
			FROM %s
			WHERE %s = ?
			""".formatted(COL_USER_LOCAL_USER_ID, TABLE_USER_LOCAL, COL_USER_LOCAL_USERNAME);

	private static String SQL_COUNT_BY_USERNAME = """
			SELECT COUNT(*)
			FROM %s
			WHERE %s = ?
			""".formatted(TABLE_USER_LOCAL, COL_USER_LOCAL_USERNAME);

	private static String SQL_COUNT_BY_USERNAME_EXCLUDE = """
			SELECT COUNT(*)
			FROM %s
			WHERE %s = ?
			  AND %s != ?
			""".formatted(TABLE_USER_LOCAL, COL_USER_LOCAL_USERNAME, COL_USER_LOCAL_USER_ID);

	@Override
	public int insert(Connection conn, long userId, UserLocalAuth local) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
			ps.setLong(1, userId);
			ps.setString(2, local.getUsername());
			ps.setString(3, local.getPasswordHash());
			ps.setInt(4, local.getFailedAttempts() > 0 ? local.getFailedAttempts() : 0);
			return ps.executeUpdate();
		}
	}

	@Override
	public void updatePassword(Connection conn, long userId, String passwordHash) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_PASSWORD)) {
			ps.setString(1, passwordHash);
			ps.setLong(2, userId);
			ps.executeUpdate();
		}
	}

	@Override
	public void update(Connection conn, long userId, UserLocalAuth local) throws SQLException {
		StringBuilder sql = new StringBuilder("UPDATE " + TABLE_USER_LOCAL + " SET ");
		List<Object> params = new ArrayList<>();

		if (local.getUsername() != null) {
			sql.append(COL_USER_LOCAL_USERNAME).append(" = ?, ");
			params.add(local.getUsername());
		}
		if (local.getPasswordHash() != null) {
			sql.append(COL_USER_LOCAL_PASSWORD_HASH).append(" = ?, ");
			params.add(local.getPasswordHash());
		}
		if (local.getFailedAttempts() >= 0) {
			sql.append(COL_USER_LOCAL_FAILED_ATTEMPTS).append(" = ?, ");
			params.add(local.getFailedAttempts());
		}
		if (local.getLockedUntil() != null) {
			sql.append(COL_USER_LOCAL_LOCKED_UNTIL).append(" = ?, ");
			params.add(local.getLockedUntil());
		}

		if (params.isEmpty()) {
			return;
		}

		sql.append(COL_USER_LOCAL_UPDATED_AT).append(" = CURRENT_TIMESTAMP ");
		sql.append(" WHERE ").append(COL_USER_LOCAL_USER_ID).append(" = ?");
		params.add(userId);

		try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}
			ps.executeUpdate();
		}
	}

	@Override
	public Optional<UserLocalAuth> findByUserId(Connection conn, long userId) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USER_ID)) {
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
	public long findUserIdByUsername(Connection conn, String username) throws SQLException {
		if (username == null) {
			return 0L;
		}
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ID_BY_USERNAME)) {
			ps.setString(1, username.trim());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getLong(COL_USER_LOCAL_USER_ID);
				}
			}
		}
		return 0L;
	}

	@Override
	public boolean existsByUsername(Connection conn, String username, Long excludeUserId) throws SQLException {
		if (username == null) {
			return false;
		}
		String sql = (excludeUserId == null) ? SQL_COUNT_BY_USERNAME : SQL_COUNT_BY_USERNAME_EXCLUDE;
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, username.trim());
			if (excludeUserId != null) {
				ps.setLong(2, excludeUserId);
			}
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		}
	}

	@Override
	public Map<Long, UserLocalAuth> findByUserIdsAsMap(Connection conn, List<Long> userIds) throws SQLException {
		Map<Long, UserLocalAuth> result = new HashMap<>();
		if (userIds == null || userIds.isEmpty()) {
			return result;
		}

		int MAX_BATCH_SIZE = 500;

		for (int startIndex = 0; startIndex < userIds.size(); startIndex = startIndex + MAX_BATCH_SIZE) {
			int endIndex = Math.min(startIndex + MAX_BATCH_SIZE, userIds.size());
			List<Long> currentBatchIds = userIds.subList(startIndex, endIndex);

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

			String sql = SQL_FIND_BY_USER_IDS + placeholdersBuilder.toString() + ")";
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				for (int i = 0; i < sanitizedIds.size(); i++) {
					ps.setLong(i + 1, sanitizedIds.get(i));
				}

				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						UserLocalAuth local = mapRow(rs);
						result.put(local.getUserId(), local);
					}
				}
			}
		}

		return result;
	}

	private UserLocalAuth mapRow(ResultSet rs) throws SQLException {
		UserLocalAuth local = new UserLocalAuth();
		local.setUserId(rs.getLong(COL_USER_LOCAL_USER_ID));
		local.setUsername(rs.getString(COL_USER_LOCAL_USERNAME));
		local.setPasswordHash(rs.getString(COL_USER_LOCAL_PASSWORD_HASH));
		local.setFailedAttempts(rs.getInt(COL_USER_LOCAL_FAILED_ATTEMPTS));
		local.setLockedUntil(rs.getTimestamp(COL_USER_LOCAL_LOCKED_UNTIL));
		local.setCreatedAt(rs.getTimestamp(COL_USER_LOCAL_CREATED_AT));
		local.setUpdatedAt(rs.getTimestamp(COL_USER_LOCAL_UPDATED_AT));
		return local;
	}
}
