package com.savvycom.auth_service.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {

    private UUID id;
    private String username;
    private String email;
    private List<String> roles;
    private DataScope dataScope;
    private UUID studentId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataScope {
        private UUID schoolId;
    }
}
