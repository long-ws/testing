package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import beans.common.Role;
import constants.system.SystemKeys;
import domain.common.RegistryDefaults;
import dto.role.RoleCreateRequest;
import dto.role.RoleDetailResponse;
import dto.role.RoleUpdateRequest;
import exception.BusinessException;
import mapper.RoleMapper;
import repository.RoleRepository;
import repository.RoleRepositoryImpl;
import utils.DbTransaction;
import utils.TransactionCallback;

public class RoleQueryWriteService {

	private final RoleRepository roleRepository;
	private final RoleMapper roleMapper;
	private final RoleQueryReadService roleQueryReadService;

	public RoleQueryWriteService() {
		this(new RoleRepositoryImpl(), new RoleMapper(), new RoleQueryReadService());
	}

	public RoleQueryWriteService(RoleRepository roleRepository, RoleMapper roleMapper, RoleQueryReadService queryService) {
		this.roleRepository = roleRepository;
		this.roleMapper = roleMapper;
		this.roleQueryReadService = queryService;
	}

	public RoleDetailResponse createRole(RoleCreateRequest dto) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<RoleDetailResponse>() {
				@Override
				public RoleDetailResponse doInTransaction(Connection conn) throws SQLException, BusinessException {
					if (roleRepository.existsByCode(conn, dto.getCode())) {
						Map<String, String> errors = new HashMap<String, String>();
						errors.put("code", "Mã vai trò '" + dto.getCode() + "' đã tồn tại.");
						throw new BusinessException(errors);
					}

					Role role = roleMapper.toEntity(normalizeCreateRequest(dto));
					int newId = roleRepository.insert(conn, role);

					return roleQueryReadService.getRoleById(conn, newId);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Lỗi hệ thống khi tạo vai trò.");
		}
	}

	public RoleDetailResponse updateRole(RoleUpdateRequest dto) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<RoleDetailResponse>() {
				@Override
				public RoleDetailResponse doInTransaction(Connection conn) throws SQLException, BusinessException {
					Role existing = roleQueryReadService.getRoleEntityById(conn, dto.getId());
					if (existing == null) {
						Map<String, String> errors = new HashMap<String, String>();
						errors.put(SystemKeys.ERROR_GLOBAL, "Vai trò không tồn tại.");
						throw new BusinessException(errors);
					}

					if (existing.isSystem()) {
						Map<String, String> errors = new HashMap<String, String>();
						errors.put(SystemKeys.ERROR_GLOBAL, "Không được phép sửa vai trò hệ thống.");
						throw new BusinessException(errors);
					}

					if (existsByCode(conn, dto.getCode(), dto.getId())) {
						Map<String, String> errors = new HashMap<String, String>();
						errors.put(SystemKeys.ERROR_GLOBAL, "Mã vai trò đã được sử dụng.");
						throw new BusinessException(errors);
					}

					Role role = roleMapper.toEntity(normalizeUpdateRequest(dto));
					roleRepository.update(conn, role);
					return roleQueryReadService.getRoleById(conn, dto.getId());
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Lỗi hệ thống khi cập nhật vai trò.");
		}
	}

	public boolean deleteRoles(List<Integer> ids) throws BusinessException {
		if (ids == null || ids.isEmpty()) {
			Map<String, String> errors = new HashMap<String, String>();
			errors.put(SystemKeys.ERROR_GLOBAL, "Vui lòng chọn vai trò để xóa.");
			throw new BusinessException(errors);
		}

		try {
			return DbTransaction.run(new TransactionCallback<Boolean>() {
				@Override
				public Boolean doInTransaction(Connection conn) throws SQLException, BusinessException {
					for (Integer id : ids) {
						Role role = roleQueryReadService.getRoleEntityById(conn, id);
						if (role == null || role.isSystem()) {
							Map<String, String> errors = new HashMap<String, String>();
							errors.put(SystemKeys.ERROR_GLOBAL, "Không thể xóa vai trò hệ thống hoặc vai trò không tồn tại.");
							throw new BusinessException(errors);
						}
					}
					return roleRepository.delete(conn, ids);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Lỗi khi xóa vai trò.");
		}
	}

	private RoleCreateRequest normalizeCreateRequest(RoleCreateRequest dto) {
		return new RoleCreateRequest.Builder().id(dto.getId()).code(dto.getCode()).name(dto.getName()).description(dto.getDescription())
				.isSystem(resolveBoolean(dto.getIsSystem(), RegistryDefaults.DEFAULT_IS_SYSTEM)).isActive(resolveBoolean(dto.getIsActive(), RegistryDefaults.DEFAULT_IS_ACTIVE))
				.permissionIds(dto.getPermissionIds()).build();
	}

	private RoleUpdateRequest normalizeUpdateRequest(RoleUpdateRequest dto) {
		return new RoleUpdateRequest.Builder().id(dto.getId()).code(dto.getCode()).name(dto.getName()).description(dto.getDescription())
				.isSystem(resolveBoolean(dto.getIsSystem(), RegistryDefaults.DEFAULT_IS_SYSTEM)).isActive(resolveBoolean(dto.getIsActive(), RegistryDefaults.DEFAULT_IS_ACTIVE))
				.permissionIds(dto.getPermissionIds()).assignedRoleIds(dto.getAssignedRoleIds()).createdAt(dto.getCreatedAt()).updatedAt(dto.getUpdatedAt()).build();
	}

	private boolean existsByCode(Connection conn, String code, int excludeId) throws SQLException {
		java.util.Optional<Role> roleOptional = roleRepository.findByCode(conn, code);
		return roleOptional.isPresent() && roleOptional.get().getId() != excludeId;
	}

	private boolean resolveBoolean(Boolean value, boolean defaultValue) {
		return value != null ? value.booleanValue() : defaultValue;
	}
}
