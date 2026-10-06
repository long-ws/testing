package dao.user;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

public interface UserTokenDAO {
	long insertToken(Connection conn, long userId, String tokenHash, String tokenTypeCode, int expiresInMinutes) throws SQLException;

	Long findActiveTokenId(Connection conn, long userId, String tokenHash, String tokenTypeCode) throws SQLException;

	boolean markTokenUsed(Connection conn, long tokenId) throws SQLException;

	int expireActiveTokens(Connection conn, long userId, String tokenTypeCode) throws SQLException;

	Timestamp findLatestCreatedAt(Connection conn, long userId, String tokenTypeCode) throws SQLException;

	int countCreatedAfterMinutes(Connection conn, long userId, String tokenTypeCode, int minutes) throws SQLException;

	int countCreatedAfterSeconds(Connection conn, long userId, String tokenTypeCode, int seconds) throws SQLException;
}
