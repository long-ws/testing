package dao.user;

import static config.db.DatabaseSchema.COL_OAUTH_PROVIDER_CODE;
import static config.db.DatabaseSchema.COL_OAUTH_PROVIDER_ID;
import static config.db.DatabaseSchema.COL_USER_OAUTH_AVATAR_URL;
import static config.db.DatabaseSchema.COL_USER_OAUTH_CREATED_AT;
import static config.db.DatabaseSchema.COL_USER_OAUTH_DISPLAY_NAME;
import static config.db.DatabaseSchema.COL_USER_OAUTH_ID;
import static config.db.DatabaseSchema.COL_USER_OAUTH_PROVIDER_EMAIL;
import static config.db.DatabaseSchema.COL_USER_OAUTH_PROVIDER_ID;
import static config.db.DatabaseSchema.COL_USER_OAUTH_PROVIDER_USER_ID;
import static config.db.DatabaseSchema.COL_USER_OAUTH_USER_ID;
import static config.db.DatabaseSchema.TABLE_OAUTH_PROVIDER;
import static config.db.DatabaseSchema.TABLE_USER_OAUTH;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import beans.user.UserOAuthAuth;

public class UserOauthDAOImpl implements UserOauthDAO {

	private static String SQL_SELECT_FIELDS = String.join(", ", COL_USER_OAUTH_ID, COL_USER_OAUTH_USER_ID, COL_USER_OAUTH_PROVIDER_ID, COL_USER_OAUTH_PROVIDER_USER_ID, COL_USER_OAUTH_PROVIDER_EMAIL,
			COL_USER_OAUTH_DISPLAY_NAME, COL_USER_OAUTH_AVATAR_URL, COL_USER_OAUTH_CREATED_AT);

	private static String SQL_INSERT = String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)", TABLE_USER_OAUTH, COL_USER_OAUTH_USER_ID, COL_USER_OAUTH_PROVIDER_ID,
			COL_USER_OAUTH_PROVIDER_USER_ID, COL_USER_OAUTH_PROVIDER_EMAIL, COL_USER_OAUTH_DISPLAY_NAME, COL_USER_OAUTH_AVATAR_URL);

	private static String SQL_DELETE = String.format("DELETE FROM %s WHERE %s = ?", TABLE_USER_OAUTH, COL_USER_OAUTH_ID);

	private static String SQL_FIND_BY_PROVIDER_AND_USER_ID = String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ?", SQL_SELECT_FIELDS, TABLE_USER_OAUTH, COL_USER_OAUTH_PROVIDER_ID,
			COL_USER_OAUTH_PROVIDER_USER_ID);

	private static String SQL_FIND_USER_ID_BY_OAUTH = String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ?", COL_USER_OAUTH_USER_ID, TABLE_USER_OAUTH, COL_USER_OAUTH_PROVIDER_ID,
			COL_USER_OAUTH_PROVIDER_USER_ID);

	private static String SQL_FIND_BY_USER_ID = String.format("SELECT %s FROM %s WHERE %s = ?", SQL_SELECT_FIELDS, TABLE_USER_OAUTH, COL_USER_OAUTH_USER_ID);

	private static String SQL_HAS_PROVIDER = String.format("SELECT COUNT(*) FROM %s WHERE %s = ? AND %s = ?", TABLE_USER_OAUTH, COL_USER_OAUTH_USER_ID, COL_USER_OAUTH_PROVIDER_ID);

	private static String SQL_COUNT_BY_USER_ID = String.format("SELECT COUNT(*) FROM %s WHERE %s = ?", TABLE_USER_OAUTH, COL_USER_OAUTH_USER_ID);

	private static String SQL_FIND_BY_PROVIDER_CODE = String.format("SELECT uo.* FROM %s uo JOIN %s op ON uo.%s = op.%s WHERE op.%s = ? AND uo.%s = ? LIMIT 1", TABLE_USER_OAUTH, TABLE_OAUTH_PROVIDER,
			COL_USER_OAUTH_PROVIDER_ID, COL_OAUTH_PROVIDER_ID, COL_OAUTH_PROVIDER_CODE, COL_USER_OAUTH_PROVIDER_USER_ID);

	@Override
	public void insert(Connection conn, UserOAuthAuth oauthInfo) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
			ps.setLong(1, oauthInfo.getUserId());
			ps.setInt(2, oauthInfo.getProviderId());
			ps.setString(3, oauthInfo.getProviderUserId());
			ps.setString(4, oauthInfo.getProviderEmail());
			ps.setString(5, oauthInfo.getDisplayName());
			ps.setString(6, oauthInfo.getAvatarUrl());
			ps.executeUpdate();
		}
	}

	@Override
	public void delete(Connection conn, long oauthId) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
			ps.setLong(1, oauthId);
			ps.executeUpdate();
		}
	}

	@Override
	public Optional<UserOAuthAuth> findByProviderAndProviderUserId(Connection conn, int providerId, String providerUserId) throws SQLException {
		if (providerUserId == null) {
			return Optional.empty();
		}
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_PROVIDER_AND_USER_ID)) {
			ps.setInt(1, providerId);
			ps.setString(2, providerUserId.trim());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapResultSetToOAuthAuth(rs));
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public long findUserIdByOAuth(Connection conn, int providerId, String providerUserId) throws SQLException {
		if (providerUserId == null) {
			return 0L;
		}
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_USER_ID_BY_OAUTH)) {
			ps.setInt(1, providerId);
			ps.setString(2, providerUserId.trim());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getLong(COL_USER_OAUTH_USER_ID);
				}
			}
		}
		return 0L;
	}

	@Override
	public List<UserOAuthAuth> findByUserId(Connection conn, long userId) throws SQLException {
		List<UserOAuthAuth> accounts = new ArrayList<>();
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USER_ID)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					accounts.add(mapResultSetToOAuthAuth(rs));
				}
			}
		}
		return accounts;
	}

	@Override
	public boolean hasOAuthProvider(Connection conn, long userId, int providerId) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_HAS_PROVIDER)) {
			ps.setLong(1, userId);
			ps.setInt(2, providerId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		}
	}

	@Override
	public int countByUserId(Connection conn, long userId) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_COUNT_BY_USER_ID)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		}
		return 0;
	}

	@Override
	public Optional<UserOAuthAuth> findByProviderCode(Connection conn, String providerCode, String providerUserId) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_PROVIDER_CODE)) {
			ps.setString(1, providerCode);
			ps.setString(2, providerUserId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapResultSetToOAuthAuth(rs));
				}
			}
		}
		return Optional.empty();
	}

	private UserOAuthAuth mapResultSetToOAuthAuth(ResultSet rs) throws SQLException {
		UserOAuthAuth auth = new UserOAuthAuth();
		auth.setId(rs.getLong(COL_USER_OAUTH_ID));
		auth.setUserId(rs.getLong(COL_USER_OAUTH_USER_ID));
		auth.setProviderId(rs.getInt(COL_USER_OAUTH_PROVIDER_ID));
		auth.setProviderUserId(rs.getString(COL_USER_OAUTH_PROVIDER_USER_ID));
		auth.setProviderEmail(rs.getString(COL_USER_OAUTH_PROVIDER_EMAIL));
		auth.setDisplayName(rs.getString(COL_USER_OAUTH_DISPLAY_NAME));
		auth.setAvatarUrl(rs.getString(COL_USER_OAUTH_AVATAR_URL));
		auth.setCreatedAt(rs.getTimestamp(COL_USER_OAUTH_CREATED_AT));
		return auth;
	}
}
