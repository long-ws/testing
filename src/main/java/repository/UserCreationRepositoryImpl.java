package repository;

import beans.common.Role;
import beans.user.UserAccount;
import beans.user.UserEmail;
import beans.user.UserLocalAuth;
import beans.user.UserOAuthAuth;
import beans.user.UserProfile;
import domain.user.UserDefaults;
import dao.common.RoleDAO;
import dao.common.RoleDAOImpl;
import dao.user.UserAccountDAO;
import dao.user.UserAccountDAOImpl;
import dao.user.UserEmailDAO;
import dao.user.UserEmailDAOImpl;
import dao.user.UserLocalDAO;
import dao.user.UserLocalDAOImpl;
import dao.user.UserOauthDAO;
import dao.user.UserOauthDAOImpl;
import dao.user.UserProfileDAO;
import dao.user.UserProfileDAOImpl;
import dao.user.UserRoleDAO;
import dao.user.UserRoleDAOImpl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class UserCreationRepositoryImpl implements UserCreationRepository {

	private final UserAccountDAO accountDAO;
	private final UserProfileDAO profileDAO;
	private final UserLocalDAO localDAO;
	private final UserRoleDAO userRoleDAO;
	private final UserOauthDAO userOauthDAO;
	private final UserEmailDAO userEmailDAO;
	private final RoleDAO roleDAO;

	public UserCreationRepositoryImpl() {
		this(new UserAccountDAOImpl(), new UserProfileDAOImpl(), new UserLocalDAOImpl(), new UserRoleDAOImpl(), new UserOauthDAOImpl(), new UserEmailDAOImpl(), new RoleDAOImpl());
	}

	public UserCreationRepositoryImpl(UserAccountDAO accountDAO, UserProfileDAO profileDAO, UserLocalDAO localDAO, UserRoleDAO userRoleDAO, UserOauthDAO userOauthDAO, UserEmailDAO userEmailDAO, RoleDAO roleDAO) {
		this.accountDAO = accountDAO;
		this.profileDAO = profileDAO;
		this.localDAO = localDAO;
		this.userRoleDAO = userRoleDAO;
		this.userOauthDAO = userOauthDAO;
		this.userEmailDAO = userEmailDAO;
		this.roleDAO = roleDAO;
	}

	@Override
	public long createLocalUser(Connection conn, UserAccount account, UserProfile profile, UserLocalAuth localAuth, UserEmail userEmail) throws SQLException {
	    long userId = accountDAO.insert(conn, account);
	    
	    localAuth.setUserId(userId);
	    profile.setUserId(userId);
	    userEmail.setUserId(userId);
	    
	    localDAO.insert(conn, userId, localAuth);
	    profileDAO.insert(conn, profile);
	    userEmailDAO.insert(conn, userId, userEmail);
	    assignRole(conn, userId, UserDefaults.DEFAULT_ROLE_CODE);
	    
	    return userId;
	}
	
	@Override
	public long createOAuthUser(Connection conn, UserAccount account, UserProfile profile, UserOAuthAuth oauthAuth, UserEmail userEmail) throws SQLException {
	    long userId = accountDAO.insert(conn, account);
	    
	    oauthAuth.setUserId(userId);
	    userOauthDAO.insert(conn, oauthAuth);
	    
	    profile.setUserId(userId);
	    profileDAO.insert(conn, profile);
	    
	    userEmail.setUserId(userId);
	    userEmailDAO.insert(conn, userId, userEmail);
	    
	    assignRole(conn, userId, UserDefaults.DEFAULT_ROLE_CODE);
	    
	    return userId;
	}

	private void assignRole(Connection conn, long userId, String roleCode) throws SQLException {
		Optional<Role> roleOpt = roleDAO.findByCode(conn, roleCode);
		if (roleOpt.isEmpty()) {
			throw new SQLException("Không tìm thấy vai trò: " + roleCode);
		}
		userRoleDAO.assignByRoleId(conn, userId, roleOpt.get().getId());
	}
}
