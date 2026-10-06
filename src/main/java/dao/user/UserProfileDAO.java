package dao.user;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import beans.user.UserProfile;

public interface UserProfileDAO {

	int insert(Connection conn, UserProfile profile) throws SQLException;

	int update(Connection conn, UserProfile profile) throws SQLException;

	int delete(Connection conn, long userId) throws SQLException;

	Optional<UserProfile> findUserProfileById(Connection conn, long userId) throws SQLException;

	List<Long> findAllIdsOrderByFullname(Connection conn, boolean ascending) throws SQLException;

	Map<Long, UserProfile> findByUserIdsAsMap(Connection conn, List<Long> userIds) throws SQLException;
}
