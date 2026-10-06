package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import beans.common.Permission;
import beans.common.Role;
import dto.permission.ManagePermissionResponse;
import dto.role.ManageRoleResponse;
import dto.role.RoleDetailResponse;
import dto.role.RoleEditFormData;
import dto.role.RoleUpdateRequest;
import exception.BusinessException;
import mapper.PermissionMapper;
import mapper.RoleMapper;
import repository.PermissionRepository;
import repository.PermissionRepositoryImpl;
import repository.RoleRepository;
import repository.RoleRepositoryImpl;
import utils.DbTransaction;
import utils.TransactionCallback;

public class RoleQueryReadService {

	private final RoleRepository roleRepository;
	private final RoleMapper roleMapper;
	private final PermissionRepository permissionRepository;
	private final PermissionMapper permissionMapper;

	public RoleQueryReadService() {
		this(new RoleRepositoryImpl(), new RoleMapper(), new PermissionRepositoryImpl(), new PermissionMapper());
	}

	public RoleQueryReadService(RoleRepository roleRepository, RoleMapper roleMapper, PermissionRepository permissionRepository, PermissionMapper permissionMapper) {
		this.roleRepository = roleRepository;
		this.roleMapper = roleMapper;
		this.permissionRepository = permissionRepository;
		this.permissionMapper = permissionMapper;
	}

	public RoleDetailResponse getRoleById(int id) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<RoleDetailResponse>() {
				@Override
				public RoleDetailResponse doInTransaction(Connection conn) throws SQLException {
					Optional<Role> roleOptional = roleRepository.findById(conn, id);
					return roleOptional.isPresent() ? roleMapper.toRoleDetailResponse(roleOptional.get()) : null;
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Lỗi truy vấn thông tin vai trò.");
		}
	}

	public Role getRoleEntityById(int id) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<Role>() {
				@Override
				public Role doInTransaction(Connection conn) throws SQLException {
					Optional<Role> roleOptional = roleRepository.findById(conn, id);
					return roleOptional.isPresent() ? roleOptional.get() : null;
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Lỗi truy vấn vai trò.");
		}
	}

	public Role getRoleByCode(String code) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<Role>() {
				@Override
				public Role doInTransaction(Connection conn) throws SQLException {
					Optional<Role> roleOptional = roleRepository.findByCode(conn, code);
					return roleOptional.isPresent() ? roleOptional.get() : null;
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Lỗi truy vấn tìm kiếm vai trò.");
		}
	}

	public List<ManageRoleResponse> getRoles() throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<List<ManageRoleResponse>>() {
				@Override
				public List<ManageRoleResponse> doInTransaction(Connection conn) throws SQLException {
					List<Role> roles = roleRepository.findAll(conn);
					List<ManageRoleResponse> dtos = new ArrayList<ManageRoleResponse>();
					for (Role role : roles) {
						dtos.add(roleMapper.toManageRoleResponse(role));
					}
					return dtos;
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Lỗi lấy danh sách vai trò.");
		}
	}

	public List<Role> getAllActiveRoles() throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<List<Role>>() {
				@Override
				public List<Role> doInTransaction(Connection conn) throws SQLException {
					return roleRepository.findAllActive(conn);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Lỗi tải danh sách vai trò hoạt động.");
		}
	}

	public List<Role> getAllActiveNonSystemRoles() throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<List<Role>>() {
				@Override
				public List<Role> doInTransaction(Connection conn) throws SQLException {
					List<Role> allActiveRoles = roleRepository.findAllActive(conn);
					List<Role> nonSystemRoles = new ArrayList<>();
					for (Role role : allActiveRoles) {
						if (role != null && !role.isSystem()) {
							nonSystemRoles.add(role);
						}
					}
					return nonSystemRoles;
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Lỗi tải danh sách vai trò hoạt động.");
		}
	}

	public long countRoles() throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<Long>() {
				@Override
				public Long doInTransaction(Connection conn) throws SQLException {
					return roleRepository.count(conn);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Lỗi thống kê vai trò.");
		}
	}

	public boolean existsByCode(String code) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<Boolean>() {
				@Override
				public Boolean doInTransaction(Connection conn) throws SQLException {
					return roleRepository.existsByCode(conn, code);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Lỗi kiểm tra mã vai trò.");
		}
	}

	public boolean existsByCode(String code, int excludeId) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<Boolean>() {
				@Override
				public Boolean doInTransaction(Connection conn) throws SQLException {
					Optional<Role> roleOptional = roleRepository.findByCode(conn, code);
					if (roleOptional.isPresent()) {
						return roleOptional.get().getId() != excludeId;
					}
					return false;
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Lỗi kiểm tra trùng lặp mã.");
		}
	}

	public List<Permission> getPermissionsByRoleId(int roleId) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<List<Permission>>() {
				@Override
				public List<Permission> doInTransaction(Connection conn) throws SQLException {
					return roleRepository.loadPermissionsForRole(conn, roleId);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Lỗi lấy danh sách quyền của vai trò.");
		}
	}

	public Map<Integer, List<Integer>> getRolePermissionMappings() throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<Map<Integer, List<Integer>>>() {
				@Override
				public Map<Integer, List<Integer>> doInTransaction(Connection conn) throws SQLException {
					return roleRepository.findAllRolePermissionMappings(conn);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Lỗi lấy sơ đồ quyền.");
		}
	}

	public RoleEditFormData getRoleEditForm(int roleId) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<RoleEditFormData>() {
				@Override
				public RoleEditFormData doInTransaction(Connection conn) throws SQLException, BusinessException {
					List<ManagePermissionResponse> allPermissions = loadAllManagePermissions(conn);

					Optional<Role> roleOptional = roleRepository.findById(conn, roleId);
					if (!roleOptional.isPresent())
						return null;

					RoleDetailResponse detail = roleMapper.toRoleDetailResponse(roleOptional.get());
					RoleUpdateRequest role = roleMapper.toRoleUpdateRequest(detail);

					// Map permissions already assigned to the current role.
					List<ManagePermissionResponse> rolePermissions = new ArrayList<>();
					List<Permission> permissions = roleRepository.loadPermissionsForRole(conn, roleId);
					if (permissions != null) {
						for (Permission permission : permissions) {
							ManagePermissionResponse permissionResponse = permissionMapper.toManagePermissionResponse(permission);
							if (permissionResponse != null) {
								rolePermissions.add(permissionResponse);
							}
						}
					}

					List<ManagePermissionResponse> unassignedPermissions = buildUnassignedPermissions(allPermissions, rolePermissions);
					return new RoleEditFormData(role, rolePermissions, unassignedPermissions, !unassignedPermissions.isEmpty());
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Lỗi tải form sửa vai trò.");
		}
	}

	private List<ManagePermissionResponse> loadAllManagePermissions(Connection conn) throws SQLException {
		List<Permission> permissions = permissionRepository.findAll(conn);
		List<ManagePermissionResponse> dtos = new ArrayList<>();
		if (permissions == null) {
			return dtos;
		}
		for (int i = 0; i < permissions.size(); i++) {
			Permission permission = permissions.get(i);
			ManagePermissionResponse dto = permissionMapper.toManagePermissionResponse(permission);
			if (dto != null) {
				dtos.add(dto);
			}
		}
		return dtos;
	}

	private List<ManagePermissionResponse> buildUnassignedPermissions(List<ManagePermissionResponse> allPermissions, List<ManagePermissionResponse> rolePermissions) {
		List<ManagePermissionResponse> unassignedPermissions = new ArrayList<>();
		Set<Integer> assignedPermissionIds = new HashSet<>();

		if (rolePermissions != null) {
			for (int i = 0; i < rolePermissions.size(); i++) {
				ManagePermissionResponse permission = rolePermissions.get(i);
				if (permission != null) {
					assignedPermissionIds.add(permission.getId());
				}
			}
		}

		if (allPermissions != null) {
			for (int i = 0; i < allPermissions.size(); i++) {
				ManagePermissionResponse permission = allPermissions.get(i);
				if (permission != null && !assignedPermissionIds.contains(permission.getId())) {
					unassignedPermissions.add(permission);
				}
			}
		}
		return unassignedPermissions;
	}

	public RoleDetailResponse getRoleById(Connection conn, int id) throws SQLException {
		Optional<Role> opt = roleRepository.findById(conn, id);
		return opt.isPresent() ? roleMapper.toRoleDetailResponse(opt.get()) : null;
	}

	public Role getRoleEntityById(Connection conn, int id) throws SQLException {
		return roleRepository.findById(conn, id).orElse(null);
	}
}
