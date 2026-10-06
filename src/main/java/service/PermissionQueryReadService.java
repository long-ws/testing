package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import beans.common.Permission;
import constants.system.SystemKeys;
import dto.permission.ManagePermissionResponse;
import dto.permission.PermissionCreateRequest;
import dto.permission.PermissionDetailResponse;
import dto.permission.PermissionUpdateRequest;
import exception.BusinessException;
import mapper.PermissionMapper;
import repository.PermissionRepository;
import repository.PermissionRepositoryImpl;
import utils.DbTransaction;
import utils.TransactionCallback;

public class PermissionQueryReadService {

	private final PermissionRepository permissionRepository;
	private final PermissionMapper permissionMapper;

	public PermissionQueryReadService() {
		this(new PermissionRepositoryImpl(), new PermissionMapper());
	}

	public PermissionQueryReadService(PermissionRepository permissionRepository, PermissionMapper permissionMapper) {
		this.permissionRepository = permissionRepository;
		this.permissionMapper = permissionMapper;
	}

	public PermissionDetailResponse getPermissionById(int id) throws BusinessException {
		Permission permission = getPermissionEntityById(id);
		return (permission != null) ? permissionMapper.toPermissionDetailResponse(permission) : null;
	}

	public Permission getPermissionEntityById(int id) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<Permission>() {
				@Override
				public Permission doInTransaction(Connection conn) throws SQLException, BusinessException {
					Permission permissionEntity = getPermissionEntityById(conn, id);
					if (permissionEntity == null) {
						Map<String, String> errors = new HashMap<String, String>();
						errors.put(SystemKeys.ERROR_GLOBAL, "Quyền có ID = " + id + " không tồn tại.");
						throw new BusinessException(errors);
					}
					return permissionEntity;
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Không thể lấy thông tin quyền lúc này.");
		}
	}

	public List<ManagePermissionResponse> getAllPermissions() throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<List<ManagePermissionResponse>>() {
				@Override
				public List<ManagePermissionResponse> doInTransaction(Connection conn) throws SQLException {
					List<Permission> list = permissionRepository.findAll(conn);
					List<ManagePermissionResponse> dtos = new ArrayList<ManagePermissionResponse>();
					for (Permission p : list) {
						dtos.add(permissionMapper.toManagePermissionResponse(p));
					}
					return dtos;
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Tải danh sách phân quyền thất bại.");
		}
	}

	public long countPermissions() throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<Long>() {
				@Override
				public Long doInTransaction(Connection conn) throws SQLException {
					return permissionRepository.count(conn);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Không thể thống kê số lượng quyền lúc này.");
		}
	}

	public boolean existsByCode(String code) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<Boolean>() {
				@Override
				public Boolean doInTransaction(Connection conn) throws SQLException {
					return existsByCode(conn, code);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Có lỗi trong quá trình kiểm tra mã quyền.");
		}
	}

	public boolean existsByCode(String code, int excludeId) throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<Boolean>() {
				@Override
				public Boolean doInTransaction(Connection conn) throws SQLException {
					return existsByCode(conn, code, excludeId);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Có lỗi xác thực quyền.");
		}
	}

	public List<String> getAllModules() throws BusinessException {
		try {
			return DbTransaction.run(new TransactionCallback<List<String>>() {
				@Override
				public List<String> doInTransaction(Connection conn) throws SQLException {
					return permissionRepository.findAllModules(conn);
				}
			});
		} catch (SQLException e) {
			throw new BusinessException("Tải danh mục phân hệ chức năng (Modules) thất bại.");
		}
	}

	public PermissionDetailResponse getPermissionById(Connection conn, int id) throws SQLException {
		Optional<Permission> permissionOpt = permissionRepository.findById(conn, id);
		if (permissionOpt.isPresent()) {
			return permissionMapper.toPermissionDetailResponse(permissionOpt.get());
		}
		return null;
	}

	public Permission getPermissionEntityById(Connection conn, int id) throws SQLException {
		Optional<Permission> opt = permissionRepository.findById(conn, id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}

	public boolean existsByCode(Connection conn, String code) throws SQLException {
		return permissionRepository.existsByCode(conn, code);
	}

	public boolean existsByCode(Connection conn, String code, int excludeId) throws SQLException {
		Optional<Permission> permissionOpt = permissionRepository.findByCode(conn, code);
		if (permissionOpt.isPresent()) {
			return permissionOpt.get().getId() != excludeId;
		}
		return false;
	}

	public PermissionCreateRequest toCreateRequest(PermissionDetailResponse detail) {
		return permissionMapper.toPermissionCreateRequest(detail);
	}

	public PermissionUpdateRequest toUpdateRequest(PermissionDetailResponse detail) {
		return permissionMapper.toPermissionUpdateRequest(detail);
	}
}
