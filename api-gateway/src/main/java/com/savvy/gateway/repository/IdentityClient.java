package com.savvy.gateway.repository;


import com.savvy.common.dto.BaseResponse;
import com.savvy.gateway.dto.request.IntrospectRequest;
import com.savvy.gateway.dto.response.GatewayBaseResponse;
import com.savvy.gateway.dto.response.IntrospectResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface IdentityClient {

    @PostExchange(url = "/api/v1/auth/introspect", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<GatewayBaseResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request);
}
