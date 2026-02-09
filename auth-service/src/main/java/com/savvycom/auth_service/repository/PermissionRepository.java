package com.savvycom.auth_service.repository;

import com.savvycom.auth_service.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByCode(String code);
    boolean existsByCode(String code);
    List<Permission> findByCodeIn(List<String> codes);
}
