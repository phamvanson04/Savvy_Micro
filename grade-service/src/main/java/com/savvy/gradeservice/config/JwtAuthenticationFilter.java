package com.savvy.gradeservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String H_USER_ID = "X-User-Id";
    private static final String H_ROLES = "X-Roles";
    private static final String H_SCHOOL_IDS = "X-School-Ids";
    private static final String H_PERMISSIONS = "X-Permissions";
    private static final String H_DATA_SCOPE = "X-Data-Scope";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String userIdStr = request.getHeader(H_USER_ID);
        final String rolesStr = request.getHeader(H_ROLES);
        final String schoolIdsStr = request.getHeader(H_SCHOOL_IDS);
        final String permissionsStr = request.getHeader(H_PERMISSIONS);
        final String dataScopeStr = request.getHeader(H_DATA_SCOPE);

        // log thông tin
        logger.info("JWT Headers - UserId: " + userIdStr + ", Roles: " + rolesStr + 
                   ", SchoolIds: " + schoolIdsStr + ", Permissions: " + permissionsStr + ", DataScope: " + dataScopeStr);

        if (!StringUtils.hasText(userIdStr)) {

            filterChain.doFilter(request, response);
            return;
        }

        try {
            UUID userId = UUID.fromString(userIdStr);
            String roles = rolesStr; // Keep as String
            UUID schoolId = parseFirstSchoolId(schoolIdsStr);
            String permissions = permissionsStr; // Keep as String
            UUID dataScopeSchoolId = parseFirstDataScopeSchoolId(dataScopeStr);

            // Set user context
            UserContext userContext = new UserContext();
            userContext.setUserId(userId);
            userContext.setUsername(userIdStr); // Use userId as username
            userContext.setRoles(roles);
            userContext.setSchoolId(schoolId);
            userContext.setPermissions(permissions);
            userContext.setDataScopeSchoolId(dataScopeSchoolId);
            UserContext.set(userContext);
            
            // Debug logging
            logger.info("UserContext set - UserId: " + userContext.getUserId() + ", Roles: " + userContext.getRoles() + 
                       ", Permissions: " + userContext.getPermissions() + ", DataScopeSchoolId: " + userContext.getDataScopeSchoolId());

            // Set vào Security Context
            List<SimpleGrantedAuthority> authorities = parseCommaSeparated(roles).stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userIdStr,
                    null,
                    authorities
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception e) {
            logger.error("Failed to process gateway headers", e);
        }

        filterChain.doFilter(request, response);
    }

    private List<String> parseCommaSeparated(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private UUID parseFirstSchoolId(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            String firstId = Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .findFirst()
                    .orElse(null);
            return firstId != null ? UUID.fromString(firstId) : null;
        } catch (Exception e) {
            logger.warn("Failed to parse schoolId: " + value, e);
            return null;
        }
    }

    private UUID parseFirstDataScopeSchoolId(String dataScopeStr) {
        if (!StringUtils.hasText(dataScopeStr)) {
            return null;
        }
        try {
           
            String schoolIdsJson = dataScopeStr.replaceAll(".*\"schoolIds\":\\[([^\\]]+)\\].*", "$1");
            if (schoolIdsJson.equals(dataScopeStr)) {
                return null; // No match found
            }
            String firstId = Arrays.stream(schoolIdsJson.split(","))
                    .map(String::trim)
                    .map(s -> s.replaceAll("[\"\\s]", ""))
                    .filter(s -> !s.isEmpty())
                    .findFirst()
                    .orElse(null);
            return firstId != null ? UUID.fromString(firstId) : null;
        } catch (Exception e) {
            logger.warn("Failed to parse dataScope: " + dataScopeStr, e);
            return null;
        }
    }

    @Override
    public void destroy() {
        UserContext.clear();
    }
}
