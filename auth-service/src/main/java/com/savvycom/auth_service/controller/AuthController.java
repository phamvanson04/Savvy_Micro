package com.savvycom.auth_service.controller;

import com.savvy.common.dto.BaseResponse;
import com.savvycom.auth_service.dto.request.*;
import com.savvycom.auth_service.dto.response.IntrospectResponse;
import com.savvycom.auth_service.dto.response.RegisterResponse;
import com.savvycom.auth_service.dto.response.TokenResponse;
import com.savvycom.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/introspect")
    public ResponseEntity<BaseResponse<IntrospectResponse>> introspect(@Valid @RequestBody IntrospectRequest request) {
        IntrospectResponse res = authService.introspect(request);
        return ResponseEntity.ok(BaseResponse.success(res, "Introspect OK"));
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse res = authService.register(request);
        return ResponseEntity.ok(BaseResponse.success(res, "Register successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse res = authService.login(request);
        return ResponseEntity.ok(BaseResponse.success(res, "Login successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenResponse res = authService.refresh(request);
        return ResponseEntity.ok(BaseResponse.success(res, "Refresh token successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Map<String, Object>>> logout(
            @Valid @RequestBody LogoutRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        authService.logout(request, authorization);
        return ResponseEntity.ok(BaseResponse.success(Map.of("loggedOut", true), "Logout successfully"));
    }

}
