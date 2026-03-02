package com.savvycom.auth_service.admin.helper;

import com.savvy.common.exception.BusinessException;
import com.savvy.common.exception.ErrorCode;
import com.savvycom.auth_service.entity.Role;
import com.savvycom.auth_service.external.Student;
import com.savvycom.auth_service.external.client.StudentServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminUserScopeValidator {

    private final StudentServiceClient studentServiceClient;

    public void validateCreateOrUpdate(UUID schoolId, UUID studentId, Set<Role> roles) {
        boolean isStudent = hasRole(roles, "STUDENT");
        boolean isManager = hasRole(roles, "SCHOOL_MANAGER") || hasRole(roles, "MANAGER_SCHOOL");

        if (isStudent && isManager) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "User cannot be both STUDENT and SCHOOL_MANAGER");
        }

        if (isStudent) {
            if (schoolId == null || studentId == null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "STUDENT requires schoolId and studentId");
            }

            Student st = studentServiceClient.getStudentOrNull(studentId);
            if (st == null || st.getClazz() == null || st.getClazz().getSchool() == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "studentId not found in student-service");
            }

            UUID schoolFromStudent = st.getClazz().getSchool().getId();
            if (!schoolId.equals(schoolFromStudent)) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "schoolId does not match student's schoolId");
            }
            return;
        }

        if (isManager) {
            if (schoolId == null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "SCHOOL_MANAGER requires schoolId");
            }
            if (studentId != null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "SCHOOL_MANAGER must not have studentId");
            }

            // verify school exists
            if (studentServiceClient.getSchoolOrNull(schoolId) == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "schoolId not found in student-service");
            }
        }
    }

    public UUID normalizeStudentId(UUID studentId, Set<Role> roles) {
        return hasRole(roles, "STUDENT") ? studentId : null;
    }

    private boolean hasRole(Set<Role> roles, String target) {
        if (roles == null) return false;
        String t = target.toUpperCase(Locale.ROOT);
        for (Role r : roles) {
            if (r == null || r.getName() == null) continue;
            String name = r.getName().trim().toUpperCase(Locale.ROOT);
            if (name.startsWith("ROLE_")) name = name.substring(5);
            if (name.equals(t)) return true;
        }
        return false;
    }
}