package com.savvycom.auth_service.admin.service.impl;

import com.savvy.common.dto.PageResponse;
import com.savvy.common.exception.BusinessException;
import com.savvy.common.exception.ErrorCode;
import com.savvycom.auth_service.admin.dto.request.AdminCreateUserRequest;
import com.savvycom.auth_service.admin.dto.request.AdminSetUserRolesRequest;
import com.savvycom.auth_service.admin.dto.request.AdminUpdateUserRequest;
import com.savvycom.auth_service.admin.dto.response.AdminUserDetailResponse;
import com.savvycom.auth_service.admin.dto.response.AdminUserSummaryResponse;
import com.savvycom.auth_service.admin.helper.*;
import com.savvycom.auth_service.admin.service.AdminUserService;
import com.savvycom.auth_service.entity.User;
import com.savvycom.auth_service.repository.RefreshTokenRepository;
import com.savvycom.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    private final AdminUserValidator validator;
    private final AdminUserRoleResolver roleResolver;
    private final AdminUserScopeHelper scopeHelper;
    private final AdminUserMapper mapper;
    private final AdminUserPageAssembler pageAssembler;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserSummaryResponse> listUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        return pageAssembler.toSummaryPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUser(UUID userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        return mapper.toDetail(
                u,
                scopeHelper.loadSchoolId(userId),
                scopeHelper.loadStudentId(userId)
        );
    }

    @Override
    @Transactional
    public AdminUserDetailResponse createUser(AdminCreateUserRequest request) {
        String email = validator.normalizeEmailRequired(request.getEmail());
        String username = validator.normalizeUsernameRequired(request.getUsername());

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Email already exists");
        }
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Username already exists");
        }

        User u = User.builder()
                .email(email)
                .username(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .enabled(request.getEnabled() == null ? true : request.getEnabled())
                .roles(roleResolver.resolveRolesOrDefault(request.getRoleNames()))
                .build();

        userRepository.save(u);

        scopeHelper.setSchoolScope(u.getId(), request.getSchoolId());

        scopeHelper.replaceStudentMapping(u.getId(), request.getStudentId());

        return getUser(u.getId());
    }

    @Override
    @Transactional
    public AdminUserDetailResponse updateUser(UUID userId, AdminUpdateUserRequest request) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        if (StringUtils.hasText(request.getUsername())) {
            String username = validator.normalizeUsernameRequired(request.getUsername());

            if (!username.equalsIgnoreCase(u.getUsername()) && userRepository.existsByUsername(username)) {
                throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Username already exists");
            }
            u.setUsername(username);
        }

        if (request.getEnabled() != null) {
            u.setEnabled(request.getEnabled());
        }

        userRepository.save(u);
        return getUser(userId);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "User not found");
        }

        refreshTokenRepository.deleteByUserId(userId);

        scopeHelper.replaceStudentMapping(userId, null);
        scopeHelper.setSchoolScope(userId, null);

        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public AdminUserDetailResponse setUserRoles(UUID userId, AdminSetUserRolesRequest request) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        u.setRoles(roleResolver.resolveRolesOrDefault(request.getRoleNames()));
        userRepository.save(u);

        return getUser(userId);
    }
}
