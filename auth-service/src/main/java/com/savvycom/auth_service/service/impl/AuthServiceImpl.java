package com.savvycom.auth_service.service.impl;

import com.savvy.common.exception.BusinessException;
import com.savvy.common.exception.ErrorCode;
import com.savvycom.auth_service.dto.request.*;
import com.savvycom.auth_service.dto.response.*;
import com.savvycom.auth_service.entity.*;
import com.savvycom.auth_service.helper.AuthTokenHelper;
import com.savvycom.auth_service.helper.AuthUserMapper;
import com.savvycom.auth_service.repository.*;
import com.savvycom.auth_service.service.AuthService;
import com.savvycom.auth_service.service.InvalidatedTokenService;
import com.savvycom.auth_service.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE_STUDENT = "ROLE_STUDENT";
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserSchoolScopeRepository userSchoolScopeRepository;
    private final UserStudentRepository userStudentRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;
    private final AuthTokenHelper tokenHelper;
    private final AuthUserMapper userMapper;

    private final InvalidatedTokenService invalidatedTokenService;

    @Value("${application.security.jwt.expiration}")
    private long accessTokenExpirationMs;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = userMapper.normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Email already exists");
        }

        Role studentRole = roleRepository.findByName(DEFAULT_ROLE_STUDENT)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INTERNAL_SERVER_ERROR,
                        "Missing role ROLE_STUDENT (seed not run?)"
                ));

        Instant now = Instant.now();

        String username = deriveUsername(email);

        User user = User.builder()
                .email(email)
                .username(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        user.getRoles().add(studentRole);
        userRepository.save(user);

        UUID schoolId = request.getSchoolId();
        if (schoolId != null) {
            userSchoolScopeRepository.save(UserSchoolScope.builder()
                    .userId(user.getId())
                    .schoolId(schoolId)
                    .build());
        }

        UUID studentId = request.getStudentId();
        if (studentId != null) {
            userStudentRepository.save(UserStudent.builder()
                    .userId(user.getId())
                    .studentId(studentId)
                    .createdAt(now)
                    .build());
        }

        return userMapper.toRegisterResponse(user, schoolId, studentId);
    }


    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        String email = userMapper.normalizeEmail(request.getEmail());

        authenticate(email, request.getPassword());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid credentials"));

        assertUserEnabled(user);

        UUID schoolId = userMapper.loadSchoolId(user.getId());
        UUID studentId = userStudentRepository.findById(user.getId())
                .map(UserStudent::getStudentId)
                .orElse(null);

        List<String> roles = userMapper.extractRoleNames(user);
        List<String> permissions = userMapper.extractPermissionCodes(user);

        String accessToken = jwtService.generateAccessToken(user, roles, permissions, schoolId, studentId);

        String refreshToken = tokenHelper.issueRefreshToken(user.getId());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    @Override
    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        RefreshToken old = tokenHelper.getUsableRefreshTokenOrThrow(request.getRefreshToken());

        User user = userRepository.findById(old.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "User not found"));

        assertUserEnabled(user);

        UUID schoolId = userMapper.loadSchoolId(user.getId());
        UUID studentId = userStudentRepository.findById(user.getId())
                .map(UserStudent::getStudentId)
                .orElse(null);

        List<String> roles = userMapper.extractRoleNames(user);
        List<String> permissions = userMapper.extractPermissionCodes(user);

        String newAccessToken = jwtService.generateAccessToken(user, roles, permissions, schoolId, studentId);
        String newRefreshToken = tokenHelper.rotate(old);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }


    @Override
    @Transactional
    public void logout(LogoutRequest request, String authorizationHeader) {
        RefreshToken rt = tokenHelper.getRefreshTokenOrThrow(request.getRefreshToken());
        tokenHelper.revokeIfNeeded(rt);

        String accessToken = extractBearerToken(authorizationHeader);
        if (!StringUtils.hasText(accessToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "Access token is mising");
        }

        Claims claims;
        try {
            claims = jwtService.parseAndValidateAccessToken(accessToken);
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "Access token invalid");
        }

        String jti = claims.getId();
        if (!StringUtils.hasText(jti)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "Access token jti is missing");
        }

        var exp = claims.getExpiration();
        if (exp == null) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "Access token exp is missing");
        }

        invalidatedTokenService.blacklistAccessToken(jti, exp.toInstant(), "LOGOUT");
    }

    @Override
    @Transactional(readOnly = true)
    public IntrospectResponse introspect(IntrospectRequest request) {
        String token = request == null ? null : request.getToken();
        if (token == null || token.isBlank()) {
            return IntrospectResponse.builder()
                    .valid(false)
                    .reason("MISSING")
                    .build();
        }

        token = stripBearer(token);

        try {
            var claims = jwtService.parseAndValidateAccessToken(token);

            String jti = claims.getId();
            if (jti != null && invalidatedTokenService.isBlacklisted(jti)) {
                return IntrospectResponse.builder()
                        .valid(false)
                        .reason("REVOKED")
                        .build();
            }

            String sub = claims.getSubject();
            long exp = claims.getExpiration() == null ? 0L : claims.getExpiration().toInstant().getEpochSecond();

            @SuppressWarnings("unchecked")
            var roles = (java.util.List<String>) claims.get("roles", java.util.List.class);

            @SuppressWarnings("unchecked")
            var permissions = (java.util.List<String>) claims.get("permissions", java.util.List.class);

            UUID studentId = null;
            Object sid = claims.get("studentId");
            if (sid instanceof String s && StringUtils.hasText(s)) {
                studentId = UUID.fromString(s.trim());
            }

            @SuppressWarnings("unchecked")
            var dataScope = (java.util.Map<String, Object>) claims.get("dataScope", java.util.Map.class);

            return IntrospectResponse.builder()
                    .valid(true)
                    .sub(sub)
                    .jti(jti)
                    .exp(exp)
                    .roles(roles)
                    .permissions(permissions)
                    .studentId(studentId)
                    .dataScope(dataScope)
                    .build();
        } catch (JwtException e) {
            return IntrospectResponse.builder()
                    .valid(false)
                    .reason("INVALID_OR_EXPIRED")
                    .build();
        }
    }


    private String stripBearer(String token) {
        String t = token.trim();
        if (t.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return t.substring(7).trim();
        }
        return t;
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null) return null;
        String v = authorizationHeader.trim();
        if (v.isEmpty()) return null;

        if (v.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return v.substring(7).trim();
        }
        return v;
    }

    private void authenticate(String email, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid credentials");
        }
    }

    private void assertUserEnabled(User user) {
        if (user == null || !user.isEnabled()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Account is disabled");
        }
    }

    private String deriveUsername(String email) {
        // lấy phần trước @
        String base = email == null ? "user" : email.split("@")[0];
        base = base.replaceAll("[^a-zA-Z0-9._-]", "");
        if (base.length() < 3) base = base + "_user";
        // tránh trùng username
        String candidate = base;
        int i = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + i;
            i++;
        }
        return candidate;
    }
}
