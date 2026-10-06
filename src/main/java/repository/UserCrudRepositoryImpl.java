package repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import beans.User;
import beans.common.Role;
import beans.user.UserAccount;
import beans.user.UserEmail;
import beans.user.UserLocalAuth;
import beans.user.UserOAuthAuth;
import beans.user.UserProfile;
import config.security.SecurityConfig;
import domain.user.UserDefaults;
import domain.user.UserIds;
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

public class UserCrudRepositoryImpl implements UserCrudRepository {

	private final UserAccountDAO accountDAO;
	private final UserProfileDAO profileDAO;
	private final UserLocalDAO localDAO;
	private final UserRoleDAO userRoleDAO;
	private final UserOauthDAO userOauthDAO;
	private final UserEmailDAO userEmailDAO;
	private final RoleDAO roleDAO;

	public UserCrudRepositoryImpl() {
		this(new UserAccountDAOImpl(), new UserProfileDAOImpl(), new UserLocalDAOImpl(), new UserRoleDAOImpl(), new UserOauthDAOImpl(), new UserEmailDAOImpl(), new RoleDAOImpl());
	}

	public UserCrudRepositoryImpl(UserAccountDAO accountDAO, UserProfileDAO profileDAO, UserLocalDAO localDAO, UserRoleDAO userRoleDAO, UserOauthDAO userOauthDAO, UserEmailDAO userEmailDAO,
			RoleDAO roleDAO) {
		this.accountDAO = accountDAO;
		this.profileDAO = profileDAO;
		this.localDAO = localDAO;
		this.userRoleDAO = userRoleDAO;
		this.userOauthDAO = userOauthDAO;
		this.userEmailDAO = userEmailDAO;
		this.roleDAO = roleDAO;
	}

	@Override
	public long insert(Connection conn, User user) throws SQLException {
		if (user.getLocalAuth() == null) {
			throw new SQLException("Thiếu thông tin đăng nhập local");
		}
		if (user.getProfile() == null) {
			throw new SQLException("Thiếu thông tin profile");
		}
		if (user.getUserEmail() == null) {
			throw new SQLException("Thiếu thông tin email");
		}

		UserAccount account = toAccount(user);
		long userId = accountDAO.insert(conn, account);
		localDAO.insert(conn, userId, user.getLocalAuth());
		user.getProfile().setUserId(userId);
		profileDAO.insert(conn, user.getProfile());
		userEmailDAO.insert(conn, userId, user.getUserEmail());
		assignRole(conn, userId, user.getRole() != null ? user.getRole().getCode() : UserDefaults.DEFAULT_ROLE_CODE);
		return userId;
	}

	@Override
	public void update(Connection conn, User user) throws SQLException {
		if (user != null && SecurityConfig.isSystemGhostUserId(user.getId())) {
			throw new SQLException("Không thể cập nhật tài khoản hệ thống.");
		}
		UserAccount account = toAccount(user);
		account.setId(user.getId());
		accountDAO.update(conn, account);

		if (user.getLocalAuth() != null) {
			localDAO.update(conn, user.getId(), user.getLocalAuth());
		}

		if (user.getProfile() != null) {
			user.getProfile().setUserId(user.getId());
			profileDAO.update(conn, user.getProfile());
		}

		if (user.getUserEmail() != null) {
			user.getUserEmail().setUserId(user.getId());
			userEmailDAO.update(conn, user.getId(), user.getUserEmail());
		}

		if (user.getRole() != null) {
			userRoleDAO.delete(conn, user.getId());
			assignRole(conn, user.getId(), user.getRole().getCode());
		}
	}

	@Override
	public boolean delete(Connection conn, List<Long> userIds) throws SQLException {
		if (userIds == null || userIds.isEmpty())
			return false;

		for (int i = 0; i < userIds.size(); i++) {
			Long id = userIds.get(i);
			if (id != null && SecurityConfig.isSystemGhostUserId(id)) {
				throw new SQLException("Không thể xóa tài khoản hệ thống.");
			}
		}
		accountDAO.softDeleteBatch(conn, userIds);
		return true;
	}

	@Override
	public Optional<User> findById(Connection conn, long userId) throws SQLException {
		Optional<UserAccount> accountOpt = accountDAO.findById(conn, userId);
		if (!accountOpt.isPresent()) {
			return Optional.empty();
		}

		List<Long> ids = new ArrayList<>();
		ids.add(userId);

		Map<Long, UserLocalAuth> localByUserId = localDAO.findByUserIdsAsMap(conn, ids);
		Map<Long, UserProfile> profileByUserId = profileDAO.findByUserIdsAsMap(conn, ids);
		Map<Long, UserEmail> emailByUserId = userEmailDAO.findByUserIdsAsMap(conn, ids);
		Map<Long, Integer> primaryRoleIdByUserId = userRoleDAO.findPrimaryRoleIdByUserIdsAsMap(conn, ids);

		List<Integer> roleIds = listUniqueRoleIds(ids, primaryRoleIdByUserId);
		Map<Integer, Role> roleById = roleDAO.findByIdsAsMap(conn, roleIds);

		List<UserOAuthAuth> oauthAuths = userOauthDAO.findByUserId(conn, userId);

		return Optional.of(assembleUser(accountOpt.get(), localByUserId, profileByUserId, emailByUserId, primaryRoleIdByUserId, roleById, oauthAuths));
	}

	@Override
	public List<User> findAllUser(Connection conn) throws SQLException {
		List<Long> userIds = accountDAO.findAllIds(conn);
		if (userIds == null || userIds.isEmpty()) {
			return new ArrayList<>();
		}

		List<Long> filteredUserIds = new ArrayList<Long>();
		for (int i = 0; i < userIds.size(); i++) {
			Long id = userIds.get(i);
			if (id != null && !SecurityConfig.isSystemGhostUserId(id)) {
				filteredUserIds.add(id);
			}
		}
		return findUsersByIds(conn, filteredUserIds);
	}

	@Override
	public long count(Connection conn) throws SQLException {
		return accountDAO.countNotDeleted(conn);
	}

	private List<User> findUsersByIds(Connection conn, List<Long> userIds) throws SQLException {

		List<UserAccount> accounts = accountDAO.findAllAccountsByIds(conn, userIds);
		if (accounts.isEmpty()) {
			return new ArrayList<>();
		}

		Map<Long, UserLocalAuth> localByUserId = localDAO.findByUserIdsAsMap(conn, userIds);
		Map<Long, UserProfile> profileByUserId = profileDAO.findByUserIdsAsMap(conn, userIds);
		Map<Long, UserEmail> emailByUserId = userEmailDAO.findByUserIdsAsMap(conn, userIds);
		Map<Long, Integer> primaryRoleIdByUserId = userRoleDAO.findPrimaryRoleIdByUserIdsAsMap(conn, userIds);

		List<Integer> roleIds = listUniqueRoleIds(userIds, primaryRoleIdByUserId);
		Map<Integer, Role> roleById = roleDAO.findByIdsAsMap(conn, roleIds);

		List<User> users = new ArrayList<>();
		for (int i = 0; i < accounts.size(); i++) {
			UserAccount account = accounts.get(i);
			List<UserOAuthAuth> oauthAuths = userOauthDAO.findByUserId(conn, account.getId());
			User user = assembleUser(account, localByUserId, profileByUserId, emailByUserId, primaryRoleIdByUserId, roleById, oauthAuths);
			users.add(user);
		}

		return users;
	}

	private List<Integer> listUniqueRoleIds(List<Long> userIds, Map<Long, Integer> primaryRoleIdByUserId) {
		Set<Integer> uniqueRoleIds = new HashSet<>();
		for (int i = 0; i < userIds.size(); i++) {
			Long userId = userIds.get(i);
			if (userId != null) {
				Integer roleId = primaryRoleIdByUserId.get(userId);
				if (roleId != null) {
					uniqueRoleIds.add(roleId);
				}
			}
		}

		List<Integer> roleIds = new ArrayList<>();
		for (Integer roleId : uniqueRoleIds) {
			roleIds.add(roleId);
		}

		return roleIds;
	}

	private User assembleUser(UserAccount account, Map<Long, UserLocalAuth> localByUserId, Map<Long, UserProfile> profileByUserId, Map<Long, UserEmail> emailByUserId,
			Map<Long, Integer> primaryRoleIdByUserId, Map<Integer, Role> roleById, List<UserOAuthAuth> oauthAuths) {
		User user = new User();
		user.setUserAccount(account);

		UserLocalAuth local = localByUserId != null ? localByUserId.get(account.getId()) : null;
		if (local != null) {
			user.setLocalAuth(local);
		}

		UserProfile profile = profileByUserId != null ? profileByUserId.get(account.getId()) : null;
		if (profile != null) {
			user.setProfile(profile);
		}

		UserEmail userEmail = emailByUserId != null ? emailByUserId.get(account.getId()) : null;
		if (userEmail != null) {
			user.setUserEmail(userEmail);
		}

		user.setOauthAuths(oauthAuths);

		Integer roleId = primaryRoleIdByUserId != null ? primaryRoleIdByUserId.get(account.getId()) : null;
		if (roleId != null) {
			Role role = roleById != null ? roleById.get(roleId) : null;
			if (role != null) {
				user.setRole(role);
			}
		}

		return user;
	}

	private void assignRole(Connection conn, long userId, String roleCode) throws SQLException {
		if (SecurityConfig.isSystemGhostUserId(userId)) {
			throw new SQLException("Không thể gán vai trò cho tài khoản hệ thống.");
		}
		Optional<Role> roleOpt = roleDAO.findByCode(conn, roleCode);
		if (roleOpt.isEmpty()) {
			throw new SQLException("Không tìm thấy vai trò: " + roleCode);
		}
		userRoleDAO.assignByRoleId(conn, userId, roleOpt.get().getId());
	}

	private UserAccount toAccount(User user) {
		UserAccount account = new UserAccount();
		if (user.getUserAccount() != null) {
			account.setId(user.getUserAccount().getId());
			account.setStatusId(user.getUserAccount().getStatusId());
			account.setTokenVersion(user.getUserAccount().getTokenVersion());
		} else {
			account.setId(user.getId());
			account.setStatusId(user.getStatus() != null ? user.getStatus().getId() : UserIds.Status.ACTIVE);
			account.setTokenVersion(user.getTokenVersion());
		}
		return account;
	}
}