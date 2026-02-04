package com.savvycom.auth_service.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeResponse {
    private UUID userId;
    private String username;
    private String email;
    private List<String> roles;
    private List<String> permissions;
    private DataScope dataScope;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataScope {
        private List<Long> schoolIds;
    }
}
