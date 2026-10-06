package mapper;

import beans.common.Permission;
import beans.common.Role;
import dto.permission.PermissionDetailResponse;
import dto.role.ManageRoleResponse;
import dto.role.RoleCreateRequest;
import dto.role.RoleDetailResponse;
import dto.role.RoleUpdateRequest;

import java.util.ArrayList;
import java.util.List;

public class RoleMapper {

    private final PermissionMapper permissionMapper;

    public RoleMapper() {
        this.permissionMapper = new PermissionMapper();
    }

    public RoleMapper(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    public RoleDetailResponse toRoleDetailResponse(Role role) {
        if (role == null) {
            return null;
        }

        RoleDetailResponse.Builder builder = new RoleDetailResponse.Builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .isSystem(role.isSystem())
                .isActive(role.isActive())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt());

        if (role.getPermissions() != null) {
            List<PermissionDetailResponse> permissionDTOs = new ArrayList<>();
            for (Permission permission : role.getPermissions()) {
                permissionDTOs.add(permissionMapper.toPermissionDetailResponse(permission));
            }
            builder.permissions(permissionDTOs);
        }

        return builder.build();
    }

    public ManageRoleResponse toManageRoleResponse(Role role) {
        if (role == null) {
            return null;
        }

        return new ManageRoleResponse.Builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .isSystem(role.isSystem())
                .isActive(role.isActive())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    public RoleCreateRequest toRoleCreateRequest(RoleDetailResponse dto) {
        if (dto == null) {
            return null;
        }

        return new RoleCreateRequest.Builder()
                .id(dto.getId())
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .isSystem(dto.getIsSystem())
                .isActive(dto.getIsActive())
                .build();
    }

    public RoleUpdateRequest toRoleUpdateRequest(RoleDetailResponse dto) {
        if (dto == null) {
            return null;
        }

        return new RoleUpdateRequest.Builder()
                .id(dto.getId())
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .isSystem(dto.getIsSystem())
                .isActive(dto.getIsActive())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public Role toEntity(RoleCreateRequest dto) {
        if (dto == null) {
            return null;
        }

        Role role = new Role();
        role.setCode(dto.getCode());
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        role.setSystem(dto.getIsSystem());
        role.setActive(dto.getIsActive());
        return role;
    }



    public Role toEntity(RoleUpdateRequest dto) {
        if (dto == null) {
            return null;
        }

        Role role = new Role();
        role.setId(dto.getId());
        role.setCode(dto.getCode());
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        role.setSystem(dto.getIsSystem());
        role.setActive(dto.getIsActive());
        return role;
    }
}