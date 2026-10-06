package dao.common;

import static config.db.DatabaseSchema.COL_GENDER_REGISTRY_CREATED_AT;
import static config.db.DatabaseSchema.COL_GENDER_REGISTRY_ID;
import static config.db.DatabaseSchema.COL_GENDER_REGISTRY_CODE;
import static config.db.DatabaseSchema.COL_GENDER_REGISTRY_DESCRIPTION;
import static config.db.DatabaseSchema.COL_GENDER_REGISTRY_IS_ACTIVE;
import static config.db.DatabaseSchema.COL_GENDER_REGISTRY_NAME;
import static config.db.DatabaseSchema.TABLE_GENDER_REGISTRY;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import beans.common.Gender;

public class GenderDAOImpl implements GenderDAO {

	private static String SELECT_FIELDS = "%s, %s, %s, %s, %s, %s".formatted(COL_GENDER_REGISTRY_ID, COL_GENDER_REGISTRY_CODE, COL_GENDER_REGISTRY_NAME, COL_GENDER_REGISTRY_DESCRIPTION,
			COL_GENDER_REGISTRY_CREATED_AT, COL_GENDER_REGISTRY_IS_ACTIVE);

	private static final String SQL_BASE = "SELECT " + SELECT_FIELDS + " FROM " + TABLE_GENDER_REGISTRY;

	private static String SQL_FIND_BY_ID = SQL_BASE + " WHERE %s = ? AND %s = true".formatted(COL_GENDER_REGISTRY_ID, COL_GENDER_REGISTRY_IS_ACTIVE);
	private static String SQL_FIND_BY_CODE = SQL_BASE + " WHERE %s = ? AND %s = true".formatted(COL_GENDER_REGISTRY_CODE, COL_GENDER_REGISTRY_IS_ACTIVE);
	private static String SQL_FIND_ALL_ACTIVE = SQL_BASE + " WHERE %s = true ORDER BY %s".formatted(COL_GENDER_REGISTRY_IS_ACTIVE, COL_GENDER_REGISTRY_NAME);
	private static String SQL_FIND_ALL = SQL_BASE + " ORDER BY %s".formatted(COL_GENDER_REGISTRY_NAME);

	@Override
	public Optional<Gender> findById(Connection conn, int id) throws SQLException {
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
	public Optional<Gender> findByCode(Connection conn, String code) throws SQLException {
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
	public List<Gender> findAllActive(Connection conn) throws SQLException {
		List<Gender> genders = new ArrayList<>();
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL_ACTIVE); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				genders.add(mapRow(rs));
			}
		}
		return genders;
	}

	@Override
	public List<Gender> findAll(Connection conn) throws SQLException {
		List<Gender> genders = new ArrayList<>();
		try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				genders.add(mapRow(rs));
			}
		}
		return genders;
	}

	private Gender mapRow(ResultSet rs) throws SQLException {
		Gender gender = new Gender();
		gender.setId(rs.getInt(COL_GENDER_REGISTRY_ID));
		gender.setCode(rs.getString(COL_GENDER_REGISTRY_CODE));
		gender.setName(rs.getString(COL_GENDER_REGISTRY_NAME));
		gender.setDescription(rs.getString(COL_GENDER_REGISTRY_DESCRIPTION));
		gender.setCreatedAt(rs.getTimestamp(COL_GENDER_REGISTRY_CREATED_AT));
		gender.setActive(rs.getBoolean(COL_GENDER_REGISTRY_IS_ACTIVE));
		return gender;
	}
}
