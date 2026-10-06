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

public interface RoleService {

	RoleDetailResponse createRole(RoleCreateRequest dto) throws BusinessException;

	RoleDetailResponse updateRole(RoleUpdateRequest dto) throws BusinessException;

	boolean deleteRoles(List<Integer> ids) throws BusinessException;

	RoleDetailResponse getRoleById(int id) throws BusinessException;

	RoleEditFormData getRoleEditForm(int roleId) throws BusinessException;

	Role getRoleEntityById(int id) throws BusinessException;

	Role getRoleByCode(String code) throws BusinessException;

	List<ManageRoleResponse> getRoles() throws BusinessException;

	List<Role> getAllActiveRoles() throws BusinessException;

	List<Role> getAllActiveNonSystemRoles() throws BusinessException;

	long countRoles() throws BusinessException;

	boolean existsByCode(String code) throws BusinessException;

	boolean existsByCode(String code, int excludeId) throws BusinessException;

	List<Permission> getPermissionsByRoleId(int roleId) throws BusinessException;

	Map<Integer, List<Integer>> getRolePermissionMappings() throws BusinessException;

	RoleCreateRequest toCreateRequest(RoleDetailResponse detail);

	RoleUpdateRequest toUpdateRequest(RoleDetailResponse detail);
}
