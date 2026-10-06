package dao.oauth;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import beans.oauth.OAuthProvider;

public interface OAuthProviderDAO {
	
	Optional<OAuthProvider> findByCode(Connection conn, String code) throws SQLException;
	
	Optional<OAuthProvider> findById(Connection conn, int id) throws SQLException;
	
	List<OAuthProvider> findAll(Connection conn) throws SQLException;
	
	int findIdByCode(Connection conn, String code) throws SQLException;
}