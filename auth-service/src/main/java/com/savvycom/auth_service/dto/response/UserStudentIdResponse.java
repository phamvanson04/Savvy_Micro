package com.savvycom.auth_service.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserStudentIdResponse {
    private UUID userId;
    private UUID studentId;
}
