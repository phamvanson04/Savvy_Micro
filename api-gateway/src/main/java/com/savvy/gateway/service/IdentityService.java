package com.savvy.gateway.service;


import com.savvy.gateway.dto.request.IntrospectRequest;
import com.savvy.gateway.dto.response.GatewayBaseResponse;
import com.savvy.gateway.dto.response.IntrospectResponse;
import com.savvy.gateway.repository.IdentityClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class IdentityService {

    private final IdentityClient identityClient;

    public Mono<GatewayBaseResponse<IntrospectResponse>> introspect(String token) {
        return identityClient.introspect(
                IntrospectRequest.builder()
                        .token(token)
                        .build()
        );
    }
}