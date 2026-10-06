package service;

import java.util.List;

import beans.common.Permission;
import dto.permission.ManagePermissionResponse;
import dto.permission.PermissionCreateRequest;
import dto.permission.PermissionDetailResponse;
import dto.permission.PermissionUpdateRequest;
import exception.BusinessException;
import mapper.PermissionMapper;
import repository.PermissionRepository;
import repository.PermissionRepositoryImpl;

public class PermissionServiceImpl implements PermissionService {

	private final PermissionQueryReadService permissionQueryReadService;
	private final PermissionQueryWriteService permissionQueryWriteService;

	public PermissionServiceImpl() {
		this(new PermissionRepositoryImpl(), new PermissionMapper());
	}

	public PermissionServiceImpl(PermissionRepository permissionRepository) {
		this(permissionRepository, new PermissionMapper());
	}

	public PermissionServiceImpl(PermissionRepository permissionRepository, PermissionMapper permissionMapper) {
		this.permissionQueryReadService = new PermissionQueryReadService(permissionRepository, permissionMapper);
		this.permissionQueryWriteService = new PermissionQueryWriteService(permissionRepository, permissionMapper);
	}

	@Override
	public PermissionDetailResponse createPermission(PermissionCreateRequest dto) throws BusinessException {
		return permissionQueryWriteService.createPermission(dto);
	}

	@Override
	public PermissionDetailResponse updatePermission(PermissionUpdateRequest dto) throws BusinessException {
		return permissionQueryWriteService.updatePermission(dto);
	}

	@Override
	public boolean deletePermissions(List<Integer> ids) throws BusinessException {
		return permissionQueryWriteService.deletePermissions(ids);
	}

	@Override
	public Permission getPermissionEntityById(int id) throws BusinessException {
		return permissionQueryReadService.getPermissionEntityById(id);
	}

	@Override
	public PermissionDetailResponse getPermissionById(int id) throws BusinessException {
		return permissionQueryReadService.getPermissionById(id);
	}

	@Override
	public boolean existsByCode(String code) throws BusinessException {
		return permissionQueryReadService.existsByCode(code);
	}

	@Override
	public boolean existsByCode(String code, int excludeId) throws BusinessException {
		return permissionQueryReadService.existsByCode(code, excludeId);
	}

	@Override
	public List<ManagePermissionResponse> getAllPermissions() throws BusinessException {
		return permissionQueryReadService.getAllPermissions();
	}

	@Override
	public long countPermissions() throws BusinessException {
		return permissionQueryReadService.countPermissions();
	}

	@Override
	public List<String> getAllModules() throws BusinessException {
		return permissionQueryReadService.getAllModules();
	}

	@Override
	public PermissionCreateRequest toCreateRequest(PermissionDetailResponse detail) {
		return permissionQueryReadService.toCreateRequest(detail);
	}

	@Override
	public PermissionUpdateRequest toUpdateRequest(PermissionDetailResponse detail) {
		return permissionQueryReadService.toUpdateRequest(detail);
	}
}
