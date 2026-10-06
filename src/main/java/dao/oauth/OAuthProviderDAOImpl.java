package dao.oauth;

import static config.db.DatabaseSchema.COL_OAUTH_PROVIDER_CODE;
import static config.db.DatabaseSchema.COL_OAUTH_PROVIDER_CREATED_AT;
import static config.db.DatabaseSchema.COL_OAUTH_PROVIDER_DESCRIPTION;
import static config.db.DatabaseSchema.COL_OAUTH_PROVIDER_ID;
import static config.db.DatabaseSchema.COL_OAUTH_PROVIDER_IS_ACTIVE;
import static config.db.DatabaseSchema.COL_OAUTH_PROVIDER_NAME;
import static config.db.DatabaseSchema.TABLE_OAUTH_PROVIDER;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import beans.oauth.OAuthProvider;

public class OAuthProviderDAOImpl implements OAuthProviderDAO {

	private static String SQL_SELECT_FIELDS = String.join(", ", COL_OAUTH_PROVIDER_ID, COL_OAUTH_PROVIDER_CODE, COL_OAUTH_PROVIDER_NAME, COL_OAUTH_PROVIDER_DESCRIPTION,
			COL_OAUTH_PROVIDER_CREATED_AT, COL_OAUTH_PROVIDER_IS_ACTIVE);

	private static String SQL_FIND_BY_CODE = String.format("SELECT %s FROM %s WHERE %s = ?", SQL_SELECT_FIELDS, TABLE_OAUTH_PROVIDER, COL_OAUTH_PROVIDER_CODE);

	private static String SQL_FIND_BY_ID = String.format("SELECT %s FROM %s WHERE %s = ?", SQL_SELECT_FIELDS, TABLE_OAUTH_PROVIDER, COL_OAUTH_PROVIDER_ID);

	private static String SQL_FIND_ALL = String.format("SELECT %s FROM %s ORDER BY %s", SQL_SELECT_FIELDS, TABLE_OAUTH_PROVIDER, COL_OAUTH_PROVIDER_NAME);

	@Override
	public Optional<OAuthProvider> findByCode(Connection conn, String code) throws SQLException {
		if (code == null) {
			return Optional.empty();
		}

		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_CODE)) {
			ps.setString(1, code.toUpperCase().trim());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return Optional.of(extractOAuthProvider(rs));
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<OAuthProvider> findById(Connection conn, int id) throws SQLException {
		if (id <= 0) {
			return Optional.empty();
		}

		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return Optional.of(extractOAuthProvider(rs));
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public List<OAuthProvider> findAll(Connection conn) throws SQLException {
		List<OAuthProvider> providers = new ArrayList<>();

		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				providers.add(extractOAuthProvider(rs));
			}
		}
		return providers;
	}

	@Override
	public int findIdByCode(Connection conn, String code) throws SQLException {
		if (code == null) {
			return 0;
		}

		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_CODE)) {
			ps.setString(1, code.toUpperCase().trim());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(COL_OAUTH_PROVIDER_ID);
				}
			}
		}
		return 0;
	}

	private OAuthProvider extractOAuthProvider(ResultSet rs) throws SQLException {
		OAuthProvider provider = new OAuthProvider();
		provider.setId(rs.getInt(COL_OAUTH_PROVIDER_ID));
		provider.setCode(rs.getString(COL_OAUTH_PROVIDER_CODE));
		provider.setName(rs.getString(COL_OAUTH_PROVIDER_NAME));
		provider.setDescription(rs.getString(COL_OAUTH_PROVIDER_DESCRIPTION));
		provider.setCreatedAt(rs.getTimestamp(COL_OAUTH_PROVIDER_CREATED_AT));
		provider.setActive(rs.getBoolean(COL_OAUTH_PROVIDER_IS_ACTIVE));
		return provider;
	}
}
