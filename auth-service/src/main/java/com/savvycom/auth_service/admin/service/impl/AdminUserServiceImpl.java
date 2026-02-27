package com.savvycom.auth_service.admin.service.impl;

import com.savvy.common.dto.BaseResponse;
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
import com.savvycom.auth_service.entity.UserSchoolScope;
import com.savvycom.auth_service.entity.UserStudent;
import com.savvycom.auth_service.external.Student;
import com.savvycom.auth_service.external.dto.UserWithStudentDto;
import com.savvycom.auth_service.repository.RefreshTokenRepository;
import com.savvycom.auth_service.repository.UserRepository;
import com.savvycom.auth_service.repository.UserSchoolScopeRepository;
import com.savvycom.auth_service.repository.UserStudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

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
    private final AdminUserMapper mapper;
    private final AdminUserPageAssembler pageAssembler;
    private final RestTemplate restTemplate;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserWithStudentDto> listUsers(Pageable pageable) {

        Page<User> page = userRepository.findAll(pageable);
        List<User> users = page.getContent();

        List<UUID> userIds = users.stream().map(User::getId).toList();

        Map<UUID, UUID> schoolByUserId = userSchoolScopeRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(UserSchoolScope::getUserId, UserSchoolScope::getSchoolId, (a, b) -> a));

        Map<UUID, UUID> studentByUserId = userStudentRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(UserStudent::getUserId, UserStudent::getStudentId, (a, b) -> a));

        Map<UUID, Student> studentCache = new HashMap<>();

        List<UserWithStudentDto> items = new ArrayList<>();

        for (User u : users) {
            UUID schoolId = schoolByUserId.get(u.getId());
            UUID studentId = studentByUserId.get(u.getId());

            AdminUserSummaryResponse summary = mapper.toSummary(u, schoolId, studentId);

            Student student = null;
            if (studentId != null) {
                student = studentCache.get(studentId);
                if (student == null) {
                    try {
                        String url = "http://localhost:8088/api/v1/students/students/" + studentId;

                        ResponseEntity<BaseResponse<Student>> resp = restTemplate.exchange(
                                url,
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<BaseResponse<Student>>() {}
                        );

                        student = resp.getBody() != null ? resp.getBody().getData() : null;
                        if (student != null) studentCache.put(studentId, student);
                    } catch (Exception ex) {
                        log.warn("Cannot fetch student for studentId={} (userId={}) : {}",
                                studentId, u.getId(), ex.getMessage());
                        student = null;
                    }
                }
            }

            items.add(UserWithStudentDto.builder()
                    .user(summary)
                    .student(student)
                    .build());
        }

        PageResponse<UserWithStudentDto> out = PageResponse.<UserWithStudentDto>builder()
                .items(items)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return BaseResponse.success(out, "success").getData();
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
