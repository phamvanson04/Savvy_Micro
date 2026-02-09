package com.savvycom.auth_service.admin.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminSetUserSchoolScopeRequest {
    @NotEmpty(message = "schoolIds is required")
    private UUID schoolId;
}
