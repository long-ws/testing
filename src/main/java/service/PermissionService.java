package service;

import java.util.List;

import beans.common.Permission;
import dto.permission.ManagePermissionResponse;
import dto.permission.PermissionCreateRequest;
import dto.permission.PermissionDetailResponse;
import dto.permission.PermissionUpdateRequest;
import exception.BusinessException;

public interface PermissionService {
	PermissionDetailResponse createPermission(PermissionCreateRequest dto) throws BusinessException;

	PermissionDetailResponse updatePermission(PermissionUpdateRequest dto) throws BusinessException;

	boolean deletePermissions(List<Integer> ids) throws BusinessException;

	PermissionDetailResponse getPermissionById(int id) throws BusinessException;

	Permission getPermissionEntityById(int id) throws BusinessException;

	List<ManagePermissionResponse> getAllPermissions() throws BusinessException;

	long countPermissions() throws BusinessException;

	boolean existsByCode(String code) throws BusinessException;

	boolean existsByCode(String code, int excludeId) throws BusinessException;

	List<String> getAllModules() throws BusinessException;

	PermissionCreateRequest toCreateRequest(PermissionDetailResponse detail);

	PermissionUpdateRequest toUpdateRequest(PermissionDetailResponse detail);
}
