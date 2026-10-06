package dao.user;

import static config.db.DatabaseSchema.COL_ID;
import static config.db.DatabaseSchema.COL_USER_TOKEN_STATUS_ID;
import static config.db.DatabaseSchema.COL_USER_TOKEN_EXPIRES_AT;
import static config.db.DatabaseSchema.COL_USER_TOKEN_TOKEN_HASH;
import static config.db.DatabaseSchema.COL_USER_TOKEN_TYPE_ID;
import static config.db.DatabaseSchema.COL_USER_TOKEN_USED_AT;
import static config.db.DatabaseSchema.COL_USER_TOKEN_USER_ID;
import static config.db.DatabaseSchema.TABLE_TOKEN_STATUS;
import static config.db.DatabaseSchema.TABLE_TOKEN_TYPE;
import static config.db.DatabaseSchema.TABLE_USER_TOKEN;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import domain.token.TokenStatus;

public class UserTokenDAOImpl implements UserTokenDAO {

	private static String SQL_INSERT = """
			INSERT INTO %s (
				%s, %s, %s, %s, %s
			) VALUES (
				?, ?,
				(SELECT %s FROM %s WHERE code = ?),
				(SELECT %s FROM %s WHERE code = ?),
				DATE_ADD(NOW(), INTERVAL ? MINUTE)
			)
			""".formatted(TABLE_USER_TOKEN, COL_USER_TOKEN_USER_ID, COL_USER_TOKEN_TOKEN_HASH, COL_USER_TOKEN_TYPE_ID, COL_USER_TOKEN_STATUS_ID, COL_USER_TOKEN_EXPIRES_AT, COL_ID, TABLE_TOKEN_TYPE,
			COL_ID, TABLE_TOKEN_STATUS);

	private static String SQL_FIND_ACTIVE_TOKEN_ID = """
			SELECT %s
			FROM %s
			WHERE %s = ?
			  AND %s = ?
			  AND %s = (SELECT %s FROM %s WHERE code = ?)
			  AND %s = (SELECT %s FROM %s WHERE code = ?)
			  AND %s IS NULL
			  AND %s > NOW()
			ORDER BY created_at DESC
			LIMIT 1
			""".formatted(COL_ID, TABLE_USER_TOKEN, COL_USER_TOKEN_USER_ID, COL_USER_TOKEN_TOKEN_HASH, COL_USER_TOKEN_TYPE_ID, COL_ID, TABLE_TOKEN_TYPE, COL_USER_TOKEN_STATUS_ID, COL_ID,
			TABLE_TOKEN_STATUS, COL_USER_TOKEN_USED_AT, COL_USER_TOKEN_EXPIRES_AT);

	private static String SQL_MARK_USED = """
			UPDATE %s
			SET
				%s = NOW(),
				%s = (SELECT %s FROM %s WHERE code = ?)
			WHERE %s = ?
			""".formatted(TABLE_USER_TOKEN, COL_USER_TOKEN_USED_AT, COL_USER_TOKEN_STATUS_ID, COL_ID, TABLE_TOKEN_STATUS, COL_ID);

	private static String SQL_EXPIRE_ACTIVE_TOKENS = """
			UPDATE %s
			SET %s = (SELECT %s FROM %s WHERE code = ?)
			WHERE %s = ?
			  AND %s = (SELECT %s FROM %s WHERE code = ?)
			  AND %s = (SELECT %s FROM %s WHERE code = ?)
			  AND %s IS NULL
			  AND %s > NOW()
			""".formatted(TABLE_USER_TOKEN, COL_USER_TOKEN_STATUS_ID, COL_ID, TABLE_TOKEN_STATUS, COL_USER_TOKEN_USER_ID, COL_USER_TOKEN_TYPE_ID, COL_ID, TABLE_TOKEN_TYPE, COL_USER_TOKEN_STATUS_ID,
			COL_ID, TABLE_TOKEN_STATUS, COL_USER_TOKEN_USED_AT, COL_USER_TOKEN_EXPIRES_AT);

	private static String SQL_FIND_LATEST_CREATED_AT = """
			SELECT MAX(created_at) AS latest_created_at
			FROM %s
			WHERE %s = ?
			  AND %s = (SELECT %s FROM %s WHERE code = ?)
			""".formatted(TABLE_USER_TOKEN, COL_USER_TOKEN_USER_ID, COL_USER_TOKEN_TYPE_ID, COL_ID, TABLE_TOKEN_TYPE);

	private static String SQL_COUNT_CREATED_AFTER_MINUTES = """
			SELECT COUNT(*)
			FROM %s
			WHERE %s = ?
			  AND %s = (SELECT %s FROM %s WHERE code = ?)
			  AND created_at >= DATE_SUB(NOW(), INTERVAL ? MINUTE)
			""".formatted(TABLE_USER_TOKEN, COL_USER_TOKEN_USER_ID, COL_USER_TOKEN_TYPE_ID, COL_ID, TABLE_TOKEN_TYPE);

	private static String SQL_COUNT_CREATED_AFTER_SECONDS = """
			SELECT COUNT(*)
			FROM %s
			WHERE %s = ?
			  AND %s = (SELECT %s FROM %s WHERE code = ?)
			  AND created_at >= DATE_SUB(NOW(), INTERVAL ? SECOND)
			""".formatted(TABLE_USER_TOKEN, COL_USER_TOKEN_USER_ID, COL_USER_TOKEN_TYPE_ID, COL_ID, TABLE_TOKEN_TYPE);

	@Override
	public long insertToken(Connection conn, long userId, String tokenHash, String tokenTypeCode, int expiresInMinutes) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, userId);
			ps.setString(2, tokenHash);
			ps.setString(3, tokenTypeCode);
			ps.setString(4, TokenStatus.ACTIVE.getCode());
			ps.setInt(5, expiresInMinutes);
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getLong(1);
				}
				throw new SQLException("Không thể tạo token xác thực.");
			}
		}
	}

	@Override
	public Long findActiveTokenId(Connection conn, long userId, String tokenHash, String tokenTypeCode) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ACTIVE_TOKEN_ID)) {
			ps.setLong(1, userId);
			ps.setString(2, tokenHash);
			ps.setString(3, tokenTypeCode);
			ps.setString(4, TokenStatus.ACTIVE.getCode());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getLong(COL_ID);
				}
				return null;
			}
		}
	}

	@Override
	public boolean markTokenUsed(Connection conn, long tokenId) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_MARK_USED)) {
			ps.setString(1, TokenStatus.USED.getCode());
			ps.setLong(2, tokenId);
			return ps.executeUpdate() > 0;
		}
	}

	@Override
	public int expireActiveTokens(Connection conn, long userId, String tokenTypeCode) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_EXPIRE_ACTIVE_TOKENS)) {
			ps.setString(1, TokenStatus.EXPIRED.getCode());
			ps.setLong(2, userId);
			ps.setString(3, tokenTypeCode);
			ps.setString(4, TokenStatus.ACTIVE.getCode());
			return ps.executeUpdate();
		}
	}

	@Override
	public Timestamp findLatestCreatedAt(Connection conn, long userId, String tokenTypeCode) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_LATEST_CREATED_AT)) {
			ps.setLong(1, userId);
			ps.setString(2, tokenTypeCode);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getTimestamp("latest_created_at");
				}
				return null;
			}
		}
	}

	@Override
	public int countCreatedAfterMinutes(Connection conn, long userId, String tokenTypeCode, int minutes) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_COUNT_CREATED_AFTER_MINUTES)) {
			ps.setLong(1, userId);
			ps.setString(2, tokenTypeCode);
			ps.setInt(3, minutes);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
				return 0;
			}
		}
	}

	@Override
	public int countCreatedAfterSeconds(Connection conn, long userId, String tokenTypeCode, int seconds) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_COUNT_CREATED_AFTER_SECONDS)) {
			ps.setLong(1, userId);
			ps.setString(2, tokenTypeCode);
			ps.setInt(3, seconds);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
				return 0;
			}
		}
	}
}
