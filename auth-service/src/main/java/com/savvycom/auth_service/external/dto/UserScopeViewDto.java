package com.savvycom.auth_service.external.dto;

import com.savvy.common.dto.PageResponse;
import com.savvycom.auth_service.admin.dto.response.AdminUserSummaryResponse;
import com.savvycom.auth_service.external.Class;
import com.savvycom.auth_service.external.School;
import com.savvycom.auth_service.external.Student;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserScopeViewDto {
    private AdminUserSummaryResponse user;
    private Student student;
    private School school;
    private PageResponse<Class> classes;
}
