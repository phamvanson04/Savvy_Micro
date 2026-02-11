package com.savvy.gateway.dto.response;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntrospectResponse {
    private boolean valid;

    private String sub;
    private String jti;
    private long exp;

    private List<String> roles;
    private List<String> permissions;

    private UUID studentId;
    private Map<String, Object> dataScope;

    private String reason;
}
