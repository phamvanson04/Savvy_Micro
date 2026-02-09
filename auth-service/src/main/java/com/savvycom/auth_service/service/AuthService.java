package com.savvycom.auth_service.service;

import com.savvycom.auth_service.dto.request.*;
import com.savvycom.auth_service.dto.response.IntrospectResponse;
import com.savvycom.auth_service.dto.response.RegisterResponse;
import com.savvycom.auth_service.dto.response.TokenResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    TokenResponse login(LoginRequest request);
    TokenResponse refresh(RefreshRequest request);
    void logout(LogoutRequest request, String authorizationHeader);
    IntrospectResponse introspect(IntrospectRequest request);
}
