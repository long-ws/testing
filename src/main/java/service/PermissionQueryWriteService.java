package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import beans.common.Permission;
import domain.common.RegistryDefaults;
import dto.permission.PermissionCreateRequest;
import dto.permission.PermissionDetailResponse;
import dto.permission.PermissionUpdateRequest;
import exception.BusinessException;
import mapper.PermissionMapper;
import repository.PermissionRepository;
import repository.PermissionRepositoryImpl;
import utils.DbTransaction;
import utils.TransactionCallback;

public class PermissionQueryWriteService {

	private final PermissionRepository permissionRepository;
	private final PermissionMapper permissionMapper;

	public PermissionQueryWriteService() {
		this(new PermissionRepositoryImpl(), new PermissionMapper());
	}

	public PermissionQueryWriteService(PermissionRepository permissionRepository, PermissionMapper permissionMapper) {
		this.permissionRepository = permissionRepository;
		this.permissionMapper = permissionMapper;
	}

	public PermissionDetailResponse createPermission(PermissionCreateRequest dto) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<PermissionDetailResponse>() {
				@Override
				public PermissionDetailResponse doInTransaction(Connection conn) throws SQLException, BusinessException {
					if (existsByCode(conn, dto.getCode())) {
						Map<String, String> errors = new HashMap<>();
						errors.put("code", "Mã quyền '" + dto.getCode() + "' đã tồn tại trên hệ thống");
						throw new BusinessException(errors);
					}

					Permission permission = permissionMapper.toEntity(normalizeCreateRequest(dto));
					int id = permissionRepository.insert(conn, permission);

					return getPermissionById(conn, id);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Không thể thêm mới quyền vào hệ thống. Vui lòng kiểm tra lại.");
		}
	}

	public PermissionDetailResponse updatePermission(PermissionUpdateRequest dto) throws BusinessException {
		if (dto.getId() == null) {
			throw new BusinessException("id", "Mã ID xác định quyền không được để trống");
		}

		try {
			return DbTransaction.run(new TransactionCallback<PermissionDetailResponse>() {
				@Override
				public PermissionDetailResponse doInTransaction(Connection conn) throws SQLException, BusinessException {
					Permission existing = getPermissionEntityById(conn, dto.getId());

					if (existing == null) {
						throw new BusinessException("Thao tác thất bại: Quyền này đã bị xóa hoặc không tồn tại.");
					}

					if (existing.isSystem()) {
						throw new BusinessException("Từ chối thực hiện: Không cho phép thay đổi quyền mặc định.");
					}

					if (existsByCode(conn, dto.getCode(), dto.getId())) {
						throw new BusinessException("code", "Mã quyền '" + dto.getCode() + "' đã được sử dụng.");
					}

					Permission permission = permissionMapper.toEntity(normalizeUpdateRequest(dto));
					permissionRepository.update(conn, permission);

					return getPermissionById(conn, dto.getId());
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Cập nhật thông tin quyền thất bại. Vui lòng thử lại sau.");
		}
	}

	public boolean deletePermissions(List<Integer> ids) throws BusinessException {
		if (ids == null || ids.isEmpty()) {
			throw new BusinessException("Vui lòng chọn ít nhất một quyền để thực hiện xóa");
		}

		try {
			return DbTransaction.run(new TransactionCallback<Boolean>() {
				@Override
				public Boolean doInTransaction(Connection conn) throws SQLException, BusinessException {
					for (Integer id : ids) {
						Permission permission = getPermissionEntityById(conn, id);
						if (permission == null) {
							throw new BusinessException("Hành động bị hủy: Không tồn tại quyền mang mã số ID = " + id);
						}
						if (permission.isSystem()) {
							throw new BusinessException("Từ chối thực hiện: Quyền '" + permission.getName() + "' là quyền mặc định và không thể xóa.");
						}
					}
					return permissionRepository.delete(conn, ids);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Quá trình xóa quyền gặp sự cố. Hệ thống chưa thể thực hiện hành động này.");
		}
	}

	private PermissionDetailResponse getPermissionById(Connection conn, int id) throws SQLException {
		Optional<Permission> permissionOpt = permissionRepository.findById(conn, id);
		if (permissionOpt.isPresent()) {
			return permissionMapper.toPermissionDetailResponse(permissionOpt.get());
		}
		return null;
	}

	private Permission getPermissionEntityById(Connection conn, int id) throws SQLException {
		Optional<Permission> permissionOpt = permissionRepository.findById(conn, id);
		if (permissionOpt.isPresent()) {
			return permissionOpt.get();
		}
		return null;
	}

	private boolean existsByCode(Connection conn, String code) throws SQLException {
		return permissionRepository.existsByCode(conn, code);
	}

	private boolean existsByCode(Connection conn, String code, int excludeId) throws SQLException {
		Optional<Permission> permissionOpt = permissionRepository.findByCode(conn, code);
		return permissionOpt.isPresent() && permissionOpt.get().getId() != excludeId;
	}

	private PermissionCreateRequest normalizeCreateRequest(PermissionCreateRequest dto) {
		return new PermissionCreateRequest.Builder()
				.id(dto.getId())
				.code(dto.getCode())
				.name(dto.getName())
				.description(dto.getDescription())
				.module(dto.getModule())
				.isSystem(resolveBoolean(dto.getIsSystem(), RegistryDefaults.DEFAULT_IS_SYSTEM))
				.isActive(resolveBoolean(dto.getIsActive(), RegistryDefaults.DEFAULT_IS_ACTIVE))
				.build();
	}

	private PermissionUpdateRequest normalizeUpdateRequest(PermissionUpdateRequest dto) {
		return new PermissionUpdateRequest.Builder()
				.id(dto.getId())
				.code(dto.getCode())
				.name(dto.getName())
				.description(dto.getDescription())
				.module(dto.getModule())
				.isSystem(resolveBoolean(dto.getIsSystem(), RegistryDefaults.DEFAULT_IS_SYSTEM))
				.isActive(resolveBoolean(dto.getIsActive(), RegistryDefaults.DEFAULT_IS_ACTIVE))
				.createdAt(dto.getCreatedAt())
				.build();
	}

	private boolean resolveBoolean(Boolean value, boolean defaultValue) {
		return value != null ? value.booleanValue() : defaultValue;
	}
}
