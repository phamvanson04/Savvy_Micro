package com.savvycom.auth_service.repository;

import com.savvycom.auth_service.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
    boolean existsByJtiAndExpiresAtAfter(String jti, Instant now);
}
