package com.savvycom.auth_service.admin.helper;

import com.savvy.common.exception.BusinessException;
import com.savvy.common.exception.ErrorCode;
import com.savvycom.auth_service.entity.UserSchoolScope;
import com.savvycom.auth_service.entity.UserStudent;
import com.savvycom.auth_service.repository.UserSchoolScopeRepository;
import com.savvycom.auth_service.repository.UserStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminUserScopeHelper {

    private final UserSchoolScopeRepository userSchoolScopeRepository;
    private final UserStudentRepository userStudentRepository;

    @Transactional(readOnly = true)
    public UUID loadSchoolId(UUID userId) {
        return userSchoolScopeRepository.findById(userId)
                .map(UserSchoolScope::getSchoolId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public UUID loadStudentId(UUID userId) {
        return userStudentRepository.findByUserId(userId)
                .map(UserStudent::getStudentId)
                .orElse(null);
    }

    @Transactional
    public void setSchoolScope(UUID userId, UUID schoolId) {
        // xóa mapping cũ (nếu bạn muốn set null)
        userSchoolScopeRepository.deleteByUserId(userId);

        if (schoolId == null) {
            return;
        }

        userSchoolScopeRepository.save(UserSchoolScope.builder()
                .userId(userId)
                .schoolId(schoolId)
                .build());
    }

    @Transactional
    public void replaceStudentMapping(UUID userId, UUID studentId) {
        userStudentRepository.deleteByUserId(userId);

        if (studentId == null) {
            return;
        }

        if (userStudentRepository.existsByStudentId(studentId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "StudentId already mapped to another user");
        }

        userStudentRepository.save(UserStudent.builder()
                .userId(userId)
                .studentId(studentId)
                .createdAt(Instant.now())
                .build());
    }
}
