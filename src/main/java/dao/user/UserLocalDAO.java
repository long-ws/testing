package dao.user;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import beans.user.UserLocalAuth;

public interface UserLocalDAO {

	int insert(Connection conn, long userId, UserLocalAuth local) throws SQLException;

	void updatePassword(Connection conn, long userId, String passwordHash) throws SQLException;

	void update(Connection conn, long userId, UserLocalAuth local) throws SQLException;

	Optional<UserLocalAuth> findByUserId(Connection conn, long userId) throws SQLException;

	long findUserIdByUsername(Connection conn, String username) throws SQLException;

	boolean existsByUsername(Connection conn, String username, Long excludeUserId) throws SQLException;

	Map<Long, UserLocalAuth> findByUserIdsAsMap(Connection conn, List<Long> userIds) throws SQLException;
}
