package com.savvycom.auth_service.admin.service;

import com.savvy.common.dto.PageResponse;
import com.savvycom.auth_service.admin.dto.request.AdminCreateUserRequest;
import com.savvycom.auth_service.admin.dto.request.AdminSetUserRolesRequest;
import com.savvycom.auth_service.admin.dto.request.AdminUpdateUserRequest;
import com.savvycom.auth_service.admin.dto.response.AdminUserDetailResponse;
import com.savvycom.auth_service.admin.dto.response.AdminUserSummaryResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminUserService {
    PageResponse<AdminUserSummaryResponse> listUsers(Pageable pageable);
    AdminUserDetailResponse getUser(UUID userId);
    AdminUserDetailResponse createUser(AdminCreateUserRequest request);
    AdminUserDetailResponse updateUser(UUID userId, AdminUpdateUserRequest request);
    void deleteUser(UUID userId);
    AdminUserDetailResponse setUserRoles(UUID userId, AdminSetUserRolesRequest request);
}
