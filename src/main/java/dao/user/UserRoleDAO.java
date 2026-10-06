package dao.user;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface UserRoleDAO {

	int delete(Connection conn, long userId) throws SQLException;

	int assignByRoleId(Connection conn, long userId, int roleId) throws SQLException;

	List<Integer> findRoleIdsByUserId(Connection conn, long userId) throws SQLException;

	Map<Long, Integer> findPrimaryRoleIdByUserIdsAsMap(Connection conn, List<Long> userIds) throws SQLException;
}
