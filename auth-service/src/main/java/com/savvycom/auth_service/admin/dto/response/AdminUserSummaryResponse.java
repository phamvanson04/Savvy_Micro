package com.savvycom.auth_service.admin.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserSummaryResponse {
    private UUID id;
    private String email;
    private String username;
    private boolean enabled;
    private List<String> roles;
    private UUID schoolId;
    private UUID studentId;
}
