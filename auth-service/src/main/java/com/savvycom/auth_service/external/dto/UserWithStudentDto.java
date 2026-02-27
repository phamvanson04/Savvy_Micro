package com.savvycom.auth_service.external.dto;

import com.savvycom.auth_service.admin.dto.response.AdminUserSummaryResponse;
import com.savvycom.auth_service.external.Student;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWithStudentDto {
    private AdminUserSummaryResponse user;
    private Student student;
}
