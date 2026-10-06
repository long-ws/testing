package service;

import java.util.List;
import java.util.Map;

import beans.common.Permission;
import beans.common.Role;
import dto.role.ManageRoleResponse;
import dto.role.RoleCreateRequest;
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

public class RoleServiceImpl implements RoleService {

	private final RoleQueryReadService queryService;
	private final RoleQueryWriteService commandService;
	private final RoleMapper roleMapper;

	public RoleServiceImpl() {
		this.roleMapper = new RoleMapper();
		RoleRepository repository = new RoleRepositoryImpl();
		PermissionRepository permissionRepository = new PermissionRepositoryImpl();
		PermissionMapper permissionMapper = new PermissionMapper();

		this.queryService = new RoleQueryReadService(repository, roleMapper, permissionRepository, permissionMapper);
		this.commandService = new RoleQueryWriteService(repository, roleMapper, queryService);
	}

	public RoleServiceImpl(RoleQueryReadService queryService, RoleQueryWriteService commandService, RoleMapper roleMapper) {
		this.queryService = queryService;
		this.commandService = commandService;
		this.roleMapper = roleMapper;
	}

	@Override
	public RoleDetailResponse createRole(RoleCreateRequest dto) throws BusinessException {
		return commandService.createRole(dto);
	}

	@Override
	public RoleDetailResponse updateRole(RoleUpdateRequest dto) throws BusinessException {
		return commandService.updateRole(dto);
	}

	@Override
	public boolean deleteRoles(List<Integer> ids) throws BusinessException {
		return commandService.deleteRoles(ids);
	}

	@Override
	public RoleDetailResponse getRoleById(int id) throws BusinessException {
		return queryService.getRoleById(id);
	}

	@Override
	public RoleEditFormData getRoleEditForm(int roleId) throws BusinessException {
		return queryService.getRoleEditForm(roleId);
	}

	@Override
	public Role getRoleEntityById(int id) throws BusinessException {
		return queryService.getRoleEntityById(id);
	}

	@Override
	public Role getRoleByCode(String code) throws BusinessException {
		return queryService.getRoleByCode(code);
	}

	@Override
	public List<ManageRoleResponse> getRoles() throws BusinessException {
		return queryService.getRoles();
	}

	@Override
	public List<Role> getAllActiveRoles() throws BusinessException {
		return queryService.getAllActiveRoles();
	}

	@Override
	public List<Role> getAllActiveNonSystemRoles() throws BusinessException {
		return queryService.getAllActiveNonSystemRoles();
	}

	@Override
	public long countRoles() throws BusinessException {
		return queryService.countRoles();
	}

	@Override
	public boolean existsByCode(String code) throws BusinessException {
		return queryService.existsByCode(code);
	}

	@Override
	public boolean existsByCode(String code, int excludeId) throws BusinessException {
		return queryService.existsByCode(code, excludeId);
	}

	@Override
	public List<Permission> getPermissionsByRoleId(int roleId) throws BusinessException {
		return queryService.getPermissionsByRoleId(roleId);
	}

	@Override
	public Map<Integer, List<Integer>> getRolePermissionMappings() throws BusinessException {
		return queryService.getRolePermissionMappings();
	}

	@Override
	public RoleCreateRequest toCreateRequest(RoleDetailResponse detail) {
		return roleMapper.toRoleCreateRequest(detail);
	}

	@Override
	public RoleUpdateRequest toUpdateRequest(RoleDetailResponse detail) {
		return roleMapper.toRoleUpdateRequest(detail);
	}
}
