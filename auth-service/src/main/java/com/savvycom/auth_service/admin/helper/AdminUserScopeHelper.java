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
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminUserScopeHelper {

    private final UserSchoolScopeRepository userSchoolScopeRepository;
    private final UserStudentRepository userStudentRepository;

    @Transactional(readOnly = true)
    public List<Long> loadSchoolIds(UUID userId) {
        return userSchoolScopeRepository.findByUserId(userId)
                .stream()
                .map(UserSchoolScope::getSchoolId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    @Transactional(readOnly = true)
    public Long loadStudentId(UUID userId) {
        return userStudentRepository.findByUserId(userId)
                .map(UserStudent::getStudentId)
                .orElse(null);
    }

    @Transactional
    public void replaceSchoolScope(UUID userId, List<Long> schoolIds) {
        userSchoolScopeRepository.deleteByUserId(userId);

        if (schoolIds == null || schoolIds.isEmpty()) {
            return;
        }

        for (Long sid : schoolIds.stream().filter(Objects::nonNull).distinct().toList()) {
            userSchoolScopeRepository.save(UserSchoolScope.builder()
                    .userId(userId)
                    .schoolId(sid)
                    .build());
        }
    }

    @Transactional
    public void replaceStudentMapping(UUID userId, Long studentId) {
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
