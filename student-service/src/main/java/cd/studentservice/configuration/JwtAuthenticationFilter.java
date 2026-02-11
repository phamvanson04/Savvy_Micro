package cd.studentservice.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String userIdStr = request.getHeader("X-User-Id");
        final String rolesStr = request.getHeader("X-Roles");
        final String schoolIdStr = request.getHeader("X-School-Id");
        final String studentIdStr = request.getHeader("X-Student-Id");
        final String permissionsStr = request.getHeader("X-Permissions");
        final String dataScopeStr = request.getHeader("X-Data-Scope");
        logger.info("JWT Headers - UserId: " + userIdStr + ", Roles: " + rolesStr +
                    ", SchoolIds: " + schoolIdStr + ", Permissions: " + permissionsStr + ", DataScope: " + dataScopeStr);
        if (!StringUtils.hasText(userIdStr)) {
//            filterChain.doFilter(request, response);
            return;
        }
        try {
            CustomUserDetails customUserDetails= CustomUserDetails.builder()
                    .userId(UUID.fromString(userIdStr))
                    .roles(parseCommaSeparated(rolesStr))
                    .schoolId(schoolIdStr!=null&&!schoolIdStr.isBlank()?UUID.fromString(schoolIdStr):null)
                    .studentId(studentIdStr!=null&&!studentIdStr.isBlank()?UUID.fromString(studentIdStr):null)
                    .permissions(parseCommaSeparated(permissionsStr))
                    .build();

            List<SimpleGrantedAuthority> authorities = parseCommaSeparated(rolesStr).stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(
                    customUserDetails,
                    null,
                    authorities
            );
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        catch (Exception e) {
            logger.error("Failed to process gateway headers", e);
        }
        filterChain.doFilter(request,response);
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
}
