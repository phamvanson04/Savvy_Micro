package com.savvycom.auth_service.repository;

import com.savvycom.auth_service.entity.UserSchoolScope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserSchoolScopeRepository extends JpaRepository<UserSchoolScope, UUID> {

    List<UserSchoolScope> findByUserIdIn(Collection<UUID> userIds);

    void deleteByUserId(UUID userId);
}
