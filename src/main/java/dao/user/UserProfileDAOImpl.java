package dao.user;

import static config.db.DatabaseSchema.COL_GENDER_REGISTRY_CODE;
import static config.db.DatabaseSchema.COL_GENDER_REGISTRY_CREATED_AT;
import static config.db.DatabaseSchema.COL_GENDER_REGISTRY_DESCRIPTION;
import static config.db.DatabaseSchema.COL_GENDER_REGISTRY_ID;
import static config.db.DatabaseSchema.COL_GENDER_REGISTRY_IS_ACTIVE;
import static config.db.DatabaseSchema.COL_GENDER_REGISTRY_NAME;
import static config.db.DatabaseSchema.COL_LANGUAGE_REGISTRY_CODE;
import static config.db.DatabaseSchema.COL_LANGUAGE_REGISTRY_CREATED_AT;
import static config.db.DatabaseSchema.COL_LANGUAGE_REGISTRY_DESCRIPTION;
import static config.db.DatabaseSchema.COL_LANGUAGE_REGISTRY_ID;
import static config.db.DatabaseSchema.COL_LANGUAGE_REGISTRY_IS_ACTIVE;
import static config.db.DatabaseSchema.COL_LANGUAGE_REGISTRY_NAME;
import static config.db.DatabaseSchema.COL_USER_ACCOUNT_DELETED_AT;
import static config.db.DatabaseSchema.COL_USER_PROFILE_AVATAR_URL;
import static config.db.DatabaseSchema.COL_USER_PROFILE_FULLNAME;
import static config.db.DatabaseSchema.COL_USER_PROFILE_GENDER_ID;
import static config.db.DatabaseSchema.COL_USER_PROFILE_PHONE_NUMBER;
import static config.db.DatabaseSchema.COL_USER_PROFILE_PREFERRED_LANGUAGE_ID;
import static config.db.DatabaseSchema.COL_USER_PROFILE_UPDATED_AT;
import static config.db.DatabaseSchema.COL_USER_PROFILE_USER_ID;
import static config.db.DatabaseSchema.TABLE_GENDER_REGISTRY;
import static config.db.DatabaseSchema.TABLE_LANGUAGE_REGISTRY;
import static config.db.DatabaseSchema.TABLE_USER_ACCOUNT;
import static config.db.DatabaseSchema.TABLE_USER_PROFILE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import beans.common.Gender;
import beans.common.Language;
import beans.user.UserProfile;

public class UserProfileDAOImpl implements UserProfileDAO {
	private static final String ALIAS_PROFILE_USER_ID = "profile_user_id";
	private static final String ALIAS_PROFILE_FULLNAME = "profile_fullname";
	private static final String ALIAS_PROFILE_PHONE_NUMBER = "profile_phone_number";
	private static final String ALIAS_PROFILE_GENDER_ID = "profile_gender_id";
	private static final String ALIAS_PROFILE_PREFERRED_LANGUAGE_ID = "profile_preferred_language_id";
	private static final String ALIAS_PROFILE_AVATAR_URL = "profile_avatar_url";
	private static final String ALIAS_PROFILE_UPDATED_AT = "profile_updated_at";
	private static final String ALIAS_GENDER_ID = "gender_id";
	private static final String ALIAS_GENDER_CODE = "gender_code";
	private static final String ALIAS_GENDER_NAME = "gender_name";
	private static final String ALIAS_GENDER_DESCRIPTION = "gender_description";
	private static final String ALIAS_GENDER_CREATED_AT = "gender_created_at";
	private static final String ALIAS_GENDER_IS_ACTIVE = "gender_is_active";
	private static final String ALIAS_LANGUAGE_ID = "language_id";
	private static final String ALIAS_LANGUAGE_CODE = "language_code";
	private static final String ALIAS_LANGUAGE_NAME = "language_name";
	private static final String ALIAS_LANGUAGE_DESCRIPTION = "language_description";
	private static final String ALIAS_LANGUAGE_CREATED_AT = "language_created_at";
	private static final String ALIAS_LANGUAGE_IS_ACTIVE = "language_is_active";

	private static String SELECT_FIELDS = """
			p.%s AS %s,
			p.%s AS %s,
			p.%s AS %s,
			p.%s AS %s,
			p.%s AS %s,
			p.%s AS %s,
			p.%s AS %s,
			g.%s AS %s,
			g.%s AS %s,
			g.%s AS %s,
			g.%s AS %s,
			g.%s AS %s,
			g.%s AS %s,
			l.%s AS %s,
			l.%s AS %s,
			l.%s AS %s,
			l.%s AS %s,
			l.%s AS %s,
			l.%s AS %s
			""".formatted(COL_USER_PROFILE_USER_ID, ALIAS_PROFILE_USER_ID, COL_USER_PROFILE_FULLNAME, ALIAS_PROFILE_FULLNAME, COL_USER_PROFILE_PHONE_NUMBER, ALIAS_PROFILE_PHONE_NUMBER,
			COL_USER_PROFILE_GENDER_ID, ALIAS_PROFILE_GENDER_ID, COL_USER_PROFILE_PREFERRED_LANGUAGE_ID, ALIAS_PROFILE_PREFERRED_LANGUAGE_ID, COL_USER_PROFILE_AVATAR_URL, ALIAS_PROFILE_AVATAR_URL,
			COL_USER_PROFILE_UPDATED_AT, ALIAS_PROFILE_UPDATED_AT, COL_GENDER_REGISTRY_ID, ALIAS_GENDER_ID, COL_GENDER_REGISTRY_CODE, ALIAS_GENDER_CODE, COL_GENDER_REGISTRY_NAME, ALIAS_GENDER_NAME,
			COL_GENDER_REGISTRY_DESCRIPTION, ALIAS_GENDER_DESCRIPTION, COL_GENDER_REGISTRY_CREATED_AT, ALIAS_GENDER_CREATED_AT, COL_GENDER_REGISTRY_IS_ACTIVE, ALIAS_GENDER_IS_ACTIVE,
			COL_LANGUAGE_REGISTRY_ID, ALIAS_LANGUAGE_ID, COL_LANGUAGE_REGISTRY_CODE, ALIAS_LANGUAGE_CODE, COL_LANGUAGE_REGISTRY_NAME, ALIAS_LANGUAGE_NAME, COL_LANGUAGE_REGISTRY_DESCRIPTION,
			ALIAS_LANGUAGE_DESCRIPTION, COL_LANGUAGE_REGISTRY_CREATED_AT, ALIAS_LANGUAGE_CREATED_AT, COL_LANGUAGE_REGISTRY_IS_ACTIVE, ALIAS_LANGUAGE_IS_ACTIVE);

	private static String SQL_BASE = """
			SELECT %s
			FROM %s p
			LEFT JOIN %s g ON p.%s = g.%s
			LEFT JOIN %s l ON p.%s = l.%s
			""".formatted(SELECT_FIELDS, TABLE_USER_PROFILE, TABLE_GENDER_REGISTRY, COL_USER_PROFILE_GENDER_ID, COL_GENDER_REGISTRY_ID, TABLE_LANGUAGE_REGISTRY, COL_USER_PROFILE_PREFERRED_LANGUAGE_ID,
			COL_LANGUAGE_REGISTRY_ID);

	private static String SQL_INSERT = """
			INSERT INTO %s (%s, %s, %s, %s, %s, %s)
			VALUES (?, ?, ?, ?, ?, ?)
			""".formatted(TABLE_USER_PROFILE, COL_USER_PROFILE_USER_ID, COL_USER_PROFILE_FULLNAME, COL_USER_PROFILE_PHONE_NUMBER, COL_USER_PROFILE_GENDER_ID, COL_USER_PROFILE_PREFERRED_LANGUAGE_ID,
			COL_USER_PROFILE_AVATAR_URL);

	private static String SQL_UPDATE = """
			UPDATE %s
			SET %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = CURRENT_TIMESTAMP
			WHERE %s = ?
			""".formatted(TABLE_USER_PROFILE, COL_USER_PROFILE_FULLNAME, COL_USER_PROFILE_PHONE_NUMBER, COL_USER_PROFILE_GENDER_ID, COL_USER_PROFILE_PREFERRED_LANGUAGE_ID, COL_USER_PROFILE_AVATAR_URL,
			COL_USER_PROFILE_UPDATED_AT, COL_USER_PROFILE_USER_ID);

	private static String SQL_DELETE = "DELETE FROM %s WHERE %s = ?".formatted(TABLE_USER_PROFILE, COL_USER_PROFILE_USER_ID);

	private static String SQL_FIND_BY_ID = SQL_BASE + " WHERE p.%s = ?".formatted(COL_USER_PROFILE_USER_ID);

	private static String SQL_FIND_BY_USER_IDS = """
			SELECT %s
			FROM %s p
			LEFT JOIN %s g ON p.%s = g.%s
			LEFT JOIN %s l ON p.%s = l.%s
			WHERE p.%s IN (
			""".formatted(SELECT_FIELDS, TABLE_USER_PROFILE, TABLE_GENDER_REGISTRY, COL_USER_PROFILE_GENDER_ID, COL_GENDER_REGISTRY_ID, TABLE_LANGUAGE_REGISTRY, COL_USER_PROFILE_PREFERRED_LANGUAGE_ID,
			COL_LANGUAGE_REGISTRY_ID, COL_USER_PROFILE_USER_ID);

	private static String SQL_FIND_IDS_ORDER_BY_FULLNAME = """
			SELECT p.%s
			FROM %s p
			JOIN %s a ON p.%s = a.%s
			WHERE a.%s IS NULL
			ORDER BY p.%s %s
			""".formatted(COL_USER_PROFILE_USER_ID, TABLE_USER_PROFILE, TABLE_USER_ACCOUNT, COL_USER_PROFILE_USER_ID, COL_USER_PROFILE_USER_ID, COL_USER_ACCOUNT_DELETED_AT, COL_USER_PROFILE_FULLNAME,
			"%s");

	@Override
	public int insert(Connection conn, UserProfile p) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
			setParameters(ps, p, false);
			return ps.executeUpdate();
		}
	}

	@Override
	public int update(Connection conn, UserProfile p) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
			setParameters(ps, p, true);
			return ps.executeUpdate();
		}
	}

	@Override
	public int delete(Connection conn, long userId) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
			ps.setLong(1, userId);
			return ps.executeUpdate();
		}
	}

	@Override
	public Optional<UserProfile> findUserProfileById(Connection conn, long userId) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return Optional.of(mapRow(rs));
			}
		}
		return Optional.empty();
	}

	@Override
	public List<Long> findAllIdsOrderByFullname(Connection conn, boolean ascending) throws SQLException {
		String direction = ascending ? "ASC" : "DESC";
		String sql = String.format(SQL_FIND_IDS_ORDER_BY_FULLNAME, direction);

		List<Long> ids = new ArrayList<Long>();
		try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				ids.add(rs.getLong(COL_USER_PROFILE_USER_ID));
			}
		}
		return ids;
	}

	@Override
	public Map<Long, UserProfile> findByUserIdsAsMap(Connection conn, List<Long> userIds) throws SQLException {
		Map<Long, UserProfile> result = new HashMap<>();
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
						UserProfile p = mapRow(rs);
						result.put(p.getUserId(), p);
					}
				}
			}
		}

		return result;
	}

	private void setParameters(PreparedStatement ps, UserProfile p, boolean isUpdate) throws SQLException {
		int idx = 1;
		if (!isUpdate)
			ps.setLong(idx++, p.getUserId());

		ps.setString(idx++, p.getFullname());
		ps.setString(idx++, p.getPhoneNumber());
		setNullableInt(ps, idx++, p.getGender() != null ? p.getGender().getId() : null);
		setNullableInt(ps, idx++, p.getPreferredLanguage() != null ? p.getPreferredLanguage().getId() : null);
		ps.setString(idx++, p.getAvatarUrl());

		if (isUpdate)
			ps.setLong(idx, p.getUserId());
	}

	private void setNullableInt(PreparedStatement ps, int index, Integer id) throws SQLException {
		if (id != null)
			ps.setInt(index, id);
		else
			ps.setNull(index, Types.INTEGER);
	}

	private UserProfile mapRow(ResultSet rs) throws SQLException {
		UserProfile p = new UserProfile();
		p.setUserId(rs.getLong(ALIAS_PROFILE_USER_ID));
		p.setFullname(rs.getString(ALIAS_PROFILE_FULLNAME));
		p.setPhoneNumber(rs.getString(ALIAS_PROFILE_PHONE_NUMBER));
		p.setAvatarUrl(rs.getString(ALIAS_PROFILE_AVATAR_URL));
		p.setUpdatedAt(rs.getTimestamp(ALIAS_PROFILE_UPDATED_AT));

		Integer genderId = (Integer) rs.getObject(ALIAS_PROFILE_GENDER_ID);
		if (genderId != null) {
			Gender g = new Gender();
			g.setId(genderId.intValue());
			g.setCode(rs.getString(ALIAS_GENDER_CODE));
			g.setName(rs.getString(ALIAS_GENDER_NAME));
			g.setDescription(rs.getString(ALIAS_GENDER_DESCRIPTION));
			g.setCreatedAt(rs.getTimestamp(ALIAS_GENDER_CREATED_AT));
			g.setActive(rs.getBoolean(ALIAS_GENDER_IS_ACTIVE));
			p.setGender(g);
		}

		Integer languageId = (Integer) rs.getObject(ALIAS_PROFILE_PREFERRED_LANGUAGE_ID);
		if (languageId != null) {
			Language l = new Language();
			l.setId(languageId.intValue());
			l.setCode(rs.getString(ALIAS_LANGUAGE_CODE));
			l.setName(rs.getString(ALIAS_LANGUAGE_NAME));
			l.setDescription(rs.getString(ALIAS_LANGUAGE_DESCRIPTION));
			l.setCreatedAt(rs.getTimestamp(ALIAS_LANGUAGE_CREATED_AT));
			l.setActive(rs.getBoolean(ALIAS_LANGUAGE_IS_ACTIVE));
			p.setPreferredLanguage(l);
		}
		return p;
	}

	public static void main(String[] args) {
		// System.out.println(SELECT_FIELDS);
		System.out.println(SQL_BASE);
	}
}
