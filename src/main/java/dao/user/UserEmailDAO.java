package dao.user;

import beans.user.UserEmail;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserEmailDAO {
	long insert(Connection conn, long userId, UserEmail userEmail) throws SQLException;

	void update(Connection conn, long userId, UserEmail userEmail) throws SQLException;

	Optional<UserEmail> findByUserId(Connection conn, long userId) throws SQLException;

	Optional<UserEmail> findByEmail(Connection conn, String email) throws SQLException;

	long findUserIdByEmail(Connection conn, String email) throws SQLException;

	boolean existsByEmail(Connection conn, String email, Long excludeUserId) throws SQLException;

	Map<Long, UserEmail> findByUserIdsAsMap(Connection conn, List<Long> userIds) throws SQLException;
}
