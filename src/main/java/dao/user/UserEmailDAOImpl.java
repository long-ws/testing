package dao.user;

import static config.db.DatabaseSchema.COL_USER_EMAIL_CREATED_AT;
import static config.db.DatabaseSchema.COL_USER_EMAIL_EMAIL;
import static config.db.DatabaseSchema.COL_USER_EMAIL_VERIFIED_AT;
import static config.db.DatabaseSchema.COL_USER_EMAIL_UPDATED_AT;
import static config.db.DatabaseSchema.COL_USER_EMAIL_ID;
import static config.db.DatabaseSchema.COL_USER_EMAIL_USER_ID;
import static config.db.DatabaseSchema.TABLE_USER_EMAIL;

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

import beans.user.UserEmail;

public class UserEmailDAOImpl implements UserEmailDAO {

	private static String SELECT_FIELDS = "%s, %s, %s, %s, %s, %s".formatted(COL_USER_EMAIL_ID, COL_USER_EMAIL_USER_ID, COL_USER_EMAIL_EMAIL, COL_USER_EMAIL_VERIFIED_AT, COL_USER_EMAIL_CREATED_AT,
			COL_USER_EMAIL_UPDATED_AT);

	private static String SQL_INSERT = """
			INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (?, ?, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
			""".formatted(TABLE_USER_EMAIL, COL_USER_EMAIL_USER_ID, COL_USER_EMAIL_EMAIL, COL_USER_EMAIL_VERIFIED_AT, COL_USER_EMAIL_CREATED_AT, COL_USER_EMAIL_UPDATED_AT);

	private static String SQL_UPDATE = """
			UPDATE %s SET %s = ?, %s = ?, %s = CURRENT_TIMESTAMP WHERE %s = ?
			""".formatted(TABLE_USER_EMAIL, COL_USER_EMAIL_EMAIL, COL_USER_EMAIL_VERIFIED_AT, COL_USER_EMAIL_UPDATED_AT, COL_USER_EMAIL_USER_ID);

	private static String SQL_FIND_BY_USER_ID = """
			SELECT %s FROM %s WHERE %s = ?
			""".formatted(SELECT_FIELDS, TABLE_USER_EMAIL, COL_USER_EMAIL_USER_ID);

	private static String SQL_FIND_BY_EMAIL = """
			SELECT %s FROM %s WHERE %s = ?
			""".formatted(SELECT_FIELDS, TABLE_USER_EMAIL, COL_USER_EMAIL_EMAIL);

	private static String SQL_FIND_USER_ID_BY_EMAIL = """
			SELECT %s FROM %s WHERE %s = ?
			""".formatted(COL_USER_EMAIL_USER_ID, TABLE_USER_EMAIL, COL_USER_EMAIL_EMAIL);

	private static String SQL_EXISTS_BY_EMAIL = """
			SELECT COUNT(*) FROM %s WHERE %s = ?
			""".formatted(TABLE_USER_EMAIL, COL_USER_EMAIL_EMAIL);

	private static String SQL_EXISTS_BY_EMAIL_EXCLUDE = """
			SELECT COUNT(*) FROM %s WHERE %s = ? AND %s != ?
			""".formatted(TABLE_USER_EMAIL, COL_USER_EMAIL_EMAIL, COL_USER_EMAIL_USER_ID);

	private static String SQL_FIND_BY_USER_IDS = """
			SELECT %s FROM %s WHERE %s IN (
			""".formatted(SELECT_FIELDS, TABLE_USER_EMAIL, COL_USER_EMAIL_USER_ID);

	@Override
	public long insert(Connection conn, long userId, UserEmail userEmail) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, userId);
			ps.setString(2, userEmail.getEmail());
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getLong(1);
				}
				throw new SQLException("Thao tác khởi tạo bản ghi email thất bại.");
			}
		}
	}

	@Override
	public void update(Connection conn, long userId, UserEmail userEmail) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
			ps.setString(1, userEmail.getEmail());
			ps.setTimestamp(2, userEmail.getVerifiedAt());
			ps.setLong(3, userId);
			ps.executeUpdate();
		}
	}

	@Override
	public Optional<UserEmail> findByUserId(Connection conn, long userId) throws SQLException {
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
	public Optional<UserEmail> findByEmail(Connection conn, String email) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_EMAIL)) {
			ps.setString(1, email);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapRow(rs));
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public long findUserIdByEmail(Connection conn, String email) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_USER_ID_BY_EMAIL)) {
			ps.setString(1, email);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getLong(COL_USER_EMAIL_USER_ID);
				}
			}
		}
		return 0;
	}

	@Override
	public boolean existsByEmail(Connection conn, String email, Long excludeUserId) throws SQLException {
		String sql = excludeUserId == null ? SQL_EXISTS_BY_EMAIL : SQL_EXISTS_BY_EMAIL_EXCLUDE;
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, email);
			if (excludeUserId != null) {
				ps.setLong(2, excludeUserId);
			}
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		}
		return false;
	}

	@Override
	public Map<Long, UserEmail> findByUserIdsAsMap(Connection conn, List<Long> userIds) throws SQLException {
		Map<Long, UserEmail> result = new HashMap<>();
		if (userIds == null || userIds.isEmpty()) {
			return result;
		}

		int MAX_BATCH_SIZE = 500;

		for (int startIndex = 0; startIndex < userIds.size(); startIndex += MAX_BATCH_SIZE) {
			int endIndex = Math.min(startIndex + MAX_BATCH_SIZE, userIds.size());
			List<Long> currentBatchIds = userIds.subList(startIndex, endIndex);

			List<Long> sanitizedIds = new ArrayList<>();
			for (Long id : currentBatchIds) {
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

			String sqlQuery = SQL_FIND_BY_USER_IDS + placeholdersBuilder.toString() + ")";
			try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
				for (int i = 0; i < sanitizedIds.size(); i++) {
					ps.setLong(i + 1, sanitizedIds.get(i));
				}

				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						UserEmail userEmail = mapRow(rs);
						result.put(userEmail.getUserId(), userEmail);
					}
				}
			}
		}
		return result;
	}

	private UserEmail mapRow(ResultSet rs) throws SQLException {
		UserEmail userEmail = new UserEmail();
		userEmail.setId(rs.getLong(COL_USER_EMAIL_ID));
		userEmail.setUserId(rs.getLong(COL_USER_EMAIL_USER_ID));
		userEmail.setEmail(rs.getString(COL_USER_EMAIL_EMAIL));
		userEmail.setVerifiedAt(rs.getTimestamp(COL_USER_EMAIL_VERIFIED_AT));
		userEmail.setCreatedAt(rs.getTimestamp(COL_USER_EMAIL_CREATED_AT));
		userEmail.setUpdatedAt(rs.getTimestamp(COL_USER_EMAIL_UPDATED_AT));
		return userEmail;
	}

	public static void main(String[] args) {
		System.out.println("--- SQL INSERT ---");
		System.out.println(SQL_INSERT);

		System.out.println("\n--- SQL UPDATE ---");
		System.out.println(SQL_UPDATE);

		System.out.println("\n--- SQL FIND BY USER ID ---");
		System.out.println(SQL_FIND_BY_USER_ID);

		System.out.println("\n--- SQL FIND BY EMAIL ---");
		System.out.println(SQL_FIND_BY_EMAIL);

		System.out.println("\n--- SQL FIND USER ID BY EMAIL ---");
		System.out.println(SQL_FIND_USER_ID_BY_EMAIL);

		System.out.println("\n--- SQL EXISTS BY EMAIL ---");
		System.out.println(SQL_EXISTS_BY_EMAIL);

		System.out.println("\n--- SQL EXISTS BY EMAIL (EXCLUDE) ---");
		System.out.println(SQL_EXISTS_BY_EMAIL_EXCLUDE);

		System.out.println("\n--- SQL FIND BY USER IDS (BASE) ---");
		System.out.println(SQL_FIND_BY_USER_IDS);
	}
}
