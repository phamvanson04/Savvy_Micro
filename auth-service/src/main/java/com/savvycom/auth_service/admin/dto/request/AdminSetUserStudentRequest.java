package com.savvycom.auth_service.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminSetUserStudentRequest {
    @NotNull(message = "studentId is required")
    private UUID studentId;
}
