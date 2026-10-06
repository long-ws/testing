package dao.common;

import beans.common.Gender;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface GenderDAO {
	Optional<Gender> findById(Connection conn, int id) throws SQLException;

	Optional<Gender> findByCode(Connection conn, String code) throws SQLException;

	List<Gender> findAllActive(Connection conn) throws SQLException;

	List<Gender> findAll(Connection conn) throws SQLException;
}
