package com.savvycom.auth_service.admin.service.impl;

import com.savvy.common.dto.PageResponse;
import com.savvy.common.exception.BusinessException;
import com.savvy.common.exception.ErrorCode;
import com.savvycom.auth_service.admin.dto.request.AdminCreateUserRequest;
import com.savvycom.auth_service.admin.dto.request.AdminSetUserRolesRequest;
import com.savvycom.auth_service.admin.dto.request.AdminUpdateUserRequest;
import com.savvycom.auth_service.admin.dto.response.AdminUserDetailResponse;
import com.savvycom.auth_service.admin.dto.response.AdminUserSummaryResponse;
import com.savvycom.auth_service.admin.helper.AdminUserMapper;
import com.savvycom.auth_service.admin.helper.AdminUserRoleResolver;
import com.savvycom.auth_service.admin.helper.AdminUserScopeHelper;
import com.savvycom.auth_service.admin.helper.AdminUserScopeValidator;
import com.savvycom.auth_service.admin.helper.AdminUserValidator;
import com.savvycom.auth_service.admin.service.AdminUserService;
import com.savvycom.auth_service.entity.User;
import com.savvycom.auth_service.entity.UserSchoolScope;
import com.savvycom.auth_service.entity.UserStudent;
import com.savvycom.auth_service.external.Class;
import com.savvycom.auth_service.external.School;
import com.savvycom.auth_service.external.Student;
import com.savvycom.auth_service.external.client.StudentServiceClient;
import com.savvycom.auth_service.external.dto.UserScopeViewDto;
import com.savvycom.auth_service.repository.RefreshTokenRepository;
import com.savvycom.auth_service.repository.UserRepository;
import com.savvycom.auth_service.repository.UserSchoolScopeRepository;
import com.savvycom.auth_service.repository.UserStudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserSchoolScopeRepository userSchoolScopeRepository;
    private final UserStudentRepository userStudentRepository;
    private final PasswordEncoder passwordEncoder;

    private final AdminUserValidator validator;
    private final AdminUserRoleResolver roleResolver;
    private final AdminUserScopeHelper scopeHelper;
    private final AdminUserScopeValidator scopeValidator;
    private final AdminUserMapper mapper;

    private final StudentServiceClient studentServiceClient;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserScopeViewDto> listUsers(Pageable pageable) {

        Page<User> page = userRepository.findAll(pageable);
        List<User> users = page.getContent();

        List<UUID> userIds = users.stream().map(User::getId).toList();

        Map<UUID, UUID> schoolByUserId = userSchoolScopeRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(UserSchoolScope::getUserId, UserSchoolScope::getSchoolId, (a, b) -> a));

        Map<UUID, UUID> studentByUserId = userStudentRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(UserStudent::getUserId, UserStudent::getStudentId, (a, b) -> a));

        Map<UUID, Student> studentCache = new HashMap<>();
        Map<UUID, School> schoolCache = new HashMap<>();
        Map<UUID, PageResponse<Class>> classesCache = new HashMap<>();

        List<UserScopeViewDto> items = new ArrayList<>();

        for (User u : users) {
            UUID schoolId = schoolByUserId.get(u.getId());
            UUID studentId = studentByUserId.get(u.getId());

            AdminUserSummaryResponse summary = mapper.toSummary(u, schoolId, studentId);

            Student student = null;
            School school = null;
            PageResponse<Class> classes = null;

            if (studentId != null) {
                student = studentCache.get(studentId);
                if (student == null) {
                    student = studentServiceClient.getStudentOrNull(studentId);
                    if (student != null) studentCache.put(studentId, student);
                }
            }

            else if (schoolId != null) {
                school = schoolCache.get(schoolId);
                if (school == null) {
                    school = studentServiceClient.getSchoolOrNull(schoolId);
                    if (school != null) schoolCache.put(schoolId, school);
                }

                classes = classesCache.get(schoolId);
                if (classes == null) {
                    classes = studentServiceClient.getClassesBySchoolOrNull(schoolId);
                    if (classes != null) classesCache.put(schoolId, classes);
                }
            }

            items.add(UserScopeViewDto.builder()
                    .user(summary)
                    .student(student)
                    .school(school)
                    .classes(classes)
                    .build());
        }

        return PageResponse.<UserScopeViewDto>builder()
                .items(items)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUser(UUID userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        return mapper.toDetail(u, scopeHelper.loadSchoolId(userId), scopeHelper.loadStudentId(userId));
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

        var roles = roleResolver.resolveRolesOrDefault(request.getRoleNames());

        scopeValidator.validateCreateOrUpdate(request.getSchoolId(), request.getStudentId(), roles);

        User u = User.builder()
                .email(email)
                .username(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .enabled(request.getEnabled() == null ? true : request.getEnabled())
                .roles(roles)
                .build();

        userRepository.save(u);

        scopeHelper.setSchoolScope(u.getId(), request.getSchoolId());

        UUID finalStudentId = scopeValidator.normalizeStudentId(request.getStudentId(), roles);
        scopeHelper.replaceStudentMapping(u.getId(), finalStudentId);

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

        var roles = roleResolver.resolveRolesOrDefault(request.getRoleNames());

        UUID schoolId = scopeHelper.loadSchoolId(userId);
        UUID studentId = scopeHelper.loadStudentId(userId);

        scopeValidator.validateCreateOrUpdate(schoolId, studentId, roles);

        u.setRoles(roles);
        userRepository.save(u);

        UUID finalStudentId = scopeValidator.normalizeStudentId(studentId, roles);
        if (!Objects.equals(finalStudentId, studentId)) {
            scopeHelper.replaceStudentMapping(userId, finalStudentId);
        }

        return getUser(userId);
    }
}