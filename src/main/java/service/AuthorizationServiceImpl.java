package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import beans.common.Permission;
import beans.common.Role;
import dao.common.PermissionDAO;
import dao.common.PermissionDAOImpl;
import dao.common.RoleDAO;
import dao.common.RoleDAOImpl;
import exception.BusinessException;
import repository.AuthorizationRepository;
import repository.AuthorizationRepositoryImpl;
import utils.DBConnection;

public class AuthorizationServiceImpl implements AuthorizationService {

	private final AuthorizationRepository authorizationRepository;
	private final PermissionDAO permissionDAO;
	private final RoleDAO roleDAO;

	public AuthorizationServiceImpl() {
		this(new AuthorizationRepositoryImpl(), new PermissionDAOImpl(), new RoleDAOImpl());
	}

	public AuthorizationServiceImpl(AuthorizationRepository authorizationRepository, PermissionDAO permissionDAO, RoleDAO roleDAO) {
		this.authorizationRepository = authorizationRepository;
		this.permissionDAO = permissionDAO;
		this.roleDAO = roleDAO;
	}

	@Override
	public boolean hasPermission(long userId, String permissionCode) throws BusinessException {
		try (Connection conn = DBConnection.getConnection()) {
			return hasPermission(conn, userId, permissionCode);
		} catch (SQLException e) {
			throw new BusinessException("Không thể kiểm tra quyền truy cập của bạn lúc này.");
		}
	}

	@Override
	public boolean hasAnyPermission(long userId, List<String> permissionCodes) throws BusinessException {
		try (Connection conn = DBConnection.getConnection()) {
			return hasAnyPermission(conn, userId, permissionCodes);
		} catch (SQLException e) {
			throw new BusinessException("Không thể kiểm tra quyền truy cập của bạn lúc này.");
		}
	}

	@Override
	public boolean hasRole(long userId, String roleCode) throws BusinessException {
		try (Connection conn = DBConnection.getConnection()) {
			return hasRole(conn, userId, roleCode);
		} catch (SQLException e) {
			throw new BusinessException("Không thể kiểm tra quyền truy cập của bạn lúc này.");
		}
	}

	@Override
	public boolean isSuperAdmin(long userId) throws BusinessException {
		try (Connection conn = DBConnection.getConnection()) {
			return isSuperAdmin(conn, userId);
		} catch (SQLException e) {
			throw new BusinessException("Không thể kiểm tra quyền truy cập của bạn lúc này.");
		}
	}

	@Override
	public List<Permission> getPermissionsByUserId(long userId) throws BusinessException {
		try (Connection conn = DBConnection.getConnection()) {
			return getPermissionsByUserId(conn, userId);
		} catch (SQLException e) {
			throw new BusinessException("Không thể tải thông tin phân quyền lúc này.");
		}
	}

	@Override
	public List<Role> getRolesByUserId(long userId) throws BusinessException {
		try (Connection conn = DBConnection.getConnection()) {
			return getRolesByUserId(conn, userId);
		} catch (SQLException e) {
			throw new BusinessException("Không thể tải thông tin phân quyền lúc này.");
		}
	}

	@Override
	public boolean hasPermission(Connection conn, long userId, String permissionCode) throws SQLException {
		if (isSuperAdmin(conn, userId)) {
			return true;
		}
		String requiredViewPermission = resolveRequiredViewPermission(permissionCode);
		if (requiredViewPermission != null && !authorizationRepository.userHasPermission(conn, userId, requiredViewPermission)) {
			return false;
		}
		return authorizationRepository.userHasPermission(conn, userId, permissionCode);
	}

	@Override
	public boolean hasAnyPermission(Connection conn, long userId, List<String> permissionCodes) throws SQLException {
		if (isSuperAdmin(conn, userId)) {
			return true;
		}
		if (permissionCodes == null || permissionCodes.isEmpty()) {
			return false;
		}
		for (String permissionCode : permissionCodes) {
			if (hasPermission(conn, userId, permissionCode)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasRole(Connection conn, long userId, String roleCode) throws SQLException {
		if (isSuperAdmin(conn, userId)) {
			return true;
		}
		return authorizationRepository.userHasRole(conn, userId, roleCode);
	}

	@Override
	public boolean isSuperAdmin(Connection conn, long userId) throws SQLException {
		return authorizationRepository.userIsSuperAdmin(conn, userId);
	}

	@Override
	public List<Permission> getPermissionsByUserId(Connection conn, long userId) throws SQLException {
		if (isSuperAdmin(conn, userId)) {
			List<Permission> allPermissions = permissionDAO.findAll(conn);
			List<Permission> activePermissions = new ArrayList<>();
			for (Permission permission : allPermissions) {
				if (permission.isActive()) {
					activePermissions.add(permission);
				}
			}
			Collections.sort(activePermissions, new Comparator<Permission>() {
				@Override
				public int compare(Permission a, Permission b) {
					String moduleA = a.getModule() == null ? "" : a.getModule();
					String moduleB = b.getModule() == null ? "" : b.getModule();
					int moduleCompare = moduleA.compareToIgnoreCase(moduleB);
					if (moduleCompare != 0) {
						return moduleCompare;
					}
					String codeA = a.getCode() == null ? "" : a.getCode();
					String codeB = b.getCode() == null ? "" : b.getCode();
					return codeA.compareToIgnoreCase(codeB);
				}
			});
			return activePermissions;
		}
		return authorizationRepository.findPermissionsByUserId(conn, userId);
	}

	@Override
	public List<Role> getRolesByUserId(Connection conn, long userId) throws SQLException {
		if (isSuperAdmin(conn, userId)) {
			List<Role> allRoles = roleDAO.findAll(conn);
			List<Role> activeRoles = new ArrayList<>();
			for (Role role : allRoles) {
				if (role.isActive()) {
					activeRoles.add(role);
				}
			}
			Collections.sort(activeRoles, new Comparator<Role>() {
				@Override
				public int compare(Role a, Role b) {
					int idA = a.getId();
					int idB = b.getId();
					if (idA < idB) {
						return -1;
					}
					if (idA > idB) {
						return 1;
					}
					return 0;
				}
			});
			return activeRoles;
		}
		return authorizationRepository.findRolesByUserId(conn, userId);
	}

	private String resolveRequiredViewPermission(String permissionCode) {
		if (permissionCode == null || permissionCode.isBlank()) {
			return null;
		}
		int dotIndex = permissionCode.lastIndexOf('.');
		if (dotIndex <= 0 || dotIndex >= permissionCode.length() - 1) {
			return null;
		}
		String action = permissionCode.substring(dotIndex + 1);
		if ("view".equals(action)) {
			return null;
		}
		String module = permissionCode.substring(0, dotIndex);
		return module + ".view";
	}
}
