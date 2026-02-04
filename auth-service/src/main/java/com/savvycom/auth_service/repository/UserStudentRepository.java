package com.savvycom.auth_service.repository;

import com.savvycom.auth_service.entity.UserStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStudentRepository extends JpaRepository<UserStudent, UUID> {
    Optional<UserStudent> findByUserId(UUID userId);

    boolean existsByStudentId(Long studentId);

    List<UserStudent> findByUserIdIn(Collection<UUID> userIds);

    void deleteByUserId(UUID userId);
}
