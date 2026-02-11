package com.savvy.gateway.filter;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.savvy.common.dto.BaseResponse;
import com.savvy.gateway.config.SecurityProperties;
import com.savvy.gateway.dto.response.IntrospectResponse;
import com.savvy.gateway.service.IdentityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationGlobalFilter implements org.springframework.cloud.gateway.filter.GlobalFilter, Ordered {

    private final SecurityProperties securityProperties;
    private final IdentityService identityService;
    private final ObjectMapper objectMapper;

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        // lay path de check public/private
        String path = exchange.getRequest().getURI().getPath();

        log.info("[GW] path={}, Authorization={}",
                path,
                exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION)
        );

        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        // lay authorization header và kiểm tra format bearer
        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(auth) || !auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return unauthenticated(exchange.getResponse(), "Missing Bearer token");
        }

        String token = auth.substring(7).trim();

        return identityService.introspect(token)
                .flatMap(res -> {
                    // Lay data, payload chua data o response
                    IntrospectResponse data = res == null ? null : res.getData();
                    if (data != null && data.isValid()) {

                        ServerHttpRequest mutated = exchange.getRequest().mutate()
                                .header("X-User-Id", safe(data.getSub()))
                                .header("X-Jti", safe(data.getJti()))
                                .header("X-Roles", join(data.getRoles()))
                                .header("X-Permissions", join(data.getPermissions()))
                                .header("X-Student-Id", data.getStudentId() == null ? "" : String.valueOf(data.getStudentId()))
                                .header("X-School-Id", getSchoolId(data.getDataScope()))
                                .build();
                        var h = mutated.getHeaders();

                        log.info("Injected headers map: X-User-Id={}, X-Jti={}, X-Roles={}, X-Permissions={}, X-Student-Id={}, X-School-Ids={}",
                                h.getFirst("X-User-Id"),
                                h.getFirst("X-Jti"),
                                h.getFirst("X-Roles"),
                                h.getFirst("X-Permissions"),
                                h.getFirst("X-Student-Id"),
                                h.getFirst("X-School-Id")
                        );

                        // Tao exchange moi chua request moi, chay filter chan va router xuong service
                        return chain.filter(exchange.mutate().request(mutated).build());
                    }

                    String reason = data == null ? "INVALID" : data.getReason();
                    return unauthenticated(exchange.getResponse(), "Unauthenticated: " + reason);
                })
                .onErrorResume(e -> {
                    log.error("Introspect error", e);
                    return unauthenticated(exchange.getResponse(), "Unauthenticated");
                });
    }

    private boolean isPublic(String path) {
        if (!StringUtils.hasText(path)) return false;

        for (String p : securityProperties.getPublicPaths()) {
            if (!StringUtils.hasText(p)) continue;

            String rule = p.trim();

            if (rule.endsWith("/**")) {
                String prefix = rule.substring(0, rule.length() - 3);
                if (path.startsWith(prefix)) return true;
            } else {
                if (path.equals(rule)) return true;
            }
        }
        return false;
    }

    private Mono<Void> unauthenticated(ServerHttpResponse response, String msg) {
        BaseResponse<?> body = BaseResponse.error("1401", msg, HttpStatus.UNAUTHORIZED);

        String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            json = "{\"success\":false,\"status\":401,\"message\":\"Unauthenticated\"}";
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(json.getBytes())));
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String join(List<String> xs) {
        if (xs == null || xs.isEmpty()) return "";
        return String.join(",", xs);
    }

    private String getSchoolId(Map<String, Object> dataScope) {
        if (dataScope == null) return "";
        Object v = dataScope.get("schoolId");
        return v == null ? "" : String.valueOf(v).trim();
    }
}

