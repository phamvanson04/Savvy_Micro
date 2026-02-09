package com.savvycom.auth_service.admin.helper;

import com.savvycom.auth_service.admin.dto.response.AdminUserDetailResponse;
import com.savvycom.auth_service.admin.dto.response.AdminUserSummaryResponse;
import com.savvycom.auth_service.entity.Permission;
import com.savvycom.auth_service.entity.Role;
import com.savvycom.auth_service.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class AdminUserMapper {

    public AdminUserSummaryResponse toSummary(User u, UUID schoolId, UUID studentId) {
        return AdminUserSummaryResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .username(u.getUsername())
                .enabled(u.isEnabled())
                .roles(extractRoleNames(u))
                .schoolId(schoolId)
                .studentId(studentId)
                .build();
    }

    public AdminUserDetailResponse toDetail(User u, UUID schoolId, UUID studentId) {
        return AdminUserDetailResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .username(u.getUsername())
                .enabled(u.isEnabled())
                .roles(extractRoleNames(u))
                .permissions(extractPermissionCodes(u))
                .schoolId(schoolId)
                .studentId(studentId)
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }

    public List<String> extractRoleNames(User u) {
        if (u == null || u.getRoles() == null) return List.of();
        return u.getRoles().stream()
                .map(Role::getName)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    public List<String> extractPermissionCodes(User u) {
        if (u == null || u.getRoles() == null) return List.of();
        return u.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getCode)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }
}
