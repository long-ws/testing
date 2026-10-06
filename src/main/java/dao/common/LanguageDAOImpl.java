package dao.common;

import static config.db.DatabaseSchema.COL_LANGUAGE_REGISTRY_CREATED_AT;
import static config.db.DatabaseSchema.COL_LANGUAGE_REGISTRY_ID;
import static config.db.DatabaseSchema.COL_LANGUAGE_REGISTRY_CODE;
import static config.db.DatabaseSchema.COL_LANGUAGE_REGISTRY_DESCRIPTION;
import static config.db.DatabaseSchema.COL_LANGUAGE_REGISTRY_IS_ACTIVE;
import static config.db.DatabaseSchema.COL_LANGUAGE_REGISTRY_NAME;
import static config.db.DatabaseSchema.TABLE_LANGUAGE_REGISTRY;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import beans.common.Language;

public class LanguageDAOImpl implements LanguageDAO {

	private static String SELECT_FIELDS = "%s, %s, %s, %s, %s, %s".formatted(COL_LANGUAGE_REGISTRY_ID, COL_LANGUAGE_REGISTRY_CODE, COL_LANGUAGE_REGISTRY_NAME, COL_LANGUAGE_REGISTRY_DESCRIPTION,
			COL_LANGUAGE_REGISTRY_CREATED_AT, COL_LANGUAGE_REGISTRY_IS_ACTIVE);

	private static final String SQL_BASE = "SELECT " + SELECT_FIELDS + " FROM " + TABLE_LANGUAGE_REGISTRY;

	private static String SQL_FIND_BY_ID = SQL_BASE + " WHERE %s = ? AND %s = true".formatted(COL_LANGUAGE_REGISTRY_ID, COL_LANGUAGE_REGISTRY_IS_ACTIVE);
	private static String SQL_FIND_BY_CODE = SQL_BASE + " WHERE %s = ? AND %s = true".formatted(COL_LANGUAGE_REGISTRY_CODE, COL_LANGUAGE_REGISTRY_IS_ACTIVE);
	private static String SQL_FIND_ALL_ACTIVE = SQL_BASE + " WHERE %s = true ORDER BY %s".formatted(COL_LANGUAGE_REGISTRY_IS_ACTIVE, COL_LANGUAGE_REGISTRY_NAME);
	private static String SQL_FIND_ALL = SQL_BASE + " ORDER BY %s".formatted(COL_LANGUAGE_REGISTRY_NAME);

	@Override
	public Optional<Language> findById(Connection conn, int id) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapRow(rs));
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<Language> findByCode(Connection conn, String code) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_CODE)) {
			ps.setString(1, code);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapRow(rs));
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public List<Language> findAllActive(Connection conn) throws SQLException {
		List<Language> languages = new ArrayList<>();
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL_ACTIVE); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				languages.add(mapRow(rs));
			}
		}
		return languages;
	}

	@Override
	public List<Language> findAll(Connection conn) throws SQLException {
		List<Language> languages = new ArrayList<>();
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				languages.add(mapRow(rs));
			}
		}
		return languages;
	}

	private Language mapRow(ResultSet rs) throws SQLException {
		Language language = new Language();
		language.setId(rs.getInt(COL_LANGUAGE_REGISTRY_ID));
		language.setCode(rs.getString(COL_LANGUAGE_REGISTRY_CODE));
		language.setName(rs.getString(COL_LANGUAGE_REGISTRY_NAME));
		language.setDescription(rs.getString(COL_LANGUAGE_REGISTRY_DESCRIPTION));
		language.setCreatedAt(rs.getTimestamp(COL_LANGUAGE_REGISTRY_CREATED_AT));
		language.setActive(rs.getBoolean(COL_LANGUAGE_REGISTRY_IS_ACTIVE));
		return language;
	}
}
