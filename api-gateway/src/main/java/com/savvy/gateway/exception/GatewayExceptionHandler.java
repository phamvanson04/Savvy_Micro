package com.savvy.gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GatewayExceptionHandler {

    private final ObjectMapper om;

    public GatewayExceptionHandler(ObjectMapper om) {
        this.om = om;
    }

    public ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, ex) -> write(exchange, HttpStatus.UNAUTHORIZED, 1401, "Unauthenticated", ex);
    }

    public ServerAccessDeniedHandler accessDeniedHandler() {
        return (exchange, ex) -> write(exchange, HttpStatus.FORBIDDEN, 1403, "Forbidden", ex);
    }

    private Mono<Void> write(ServerWebExchange exchange, HttpStatus status, int code, String message, Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("status", status.value());
        body.put("details", ex == null ? null : ex.getMessage());

        byte[] bytes;
        try {
            bytes = om.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            bytes = ("{\"code\":" + code + ",\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        }

        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }
}

