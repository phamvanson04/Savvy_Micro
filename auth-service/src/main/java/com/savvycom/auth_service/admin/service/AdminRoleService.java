package com.savvycom.auth_service.admin.service;

import com.savvycom.auth_service.admin.dto.request.CreateRoleRequest;
import com.savvycom.auth_service.admin.dto.request.UpdateRoleRequest;
import com.savvycom.auth_service.admin.dto.response.RoleResponse;

import java.util.List;
import java.util.UUID;

public interface AdminRoleService {
    List<RoleResponse> listRoles();
    RoleResponse createRole(CreateRoleRequest request);
    RoleResponse updateRole(UUID roleId, UpdateRoleRequest request);
    void deleteRole(UUID roleId);
}
