package com.savvycom.auth_service.service;

import com.savvycom.auth_service.entity.InvalidatedToken;
import com.savvycom.auth_service.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InvalidatedTokenService {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Transactional
    public void blacklistAccessToken(String jti, Instant expiresAt, String reason) {
        Instant now = Instant.now();

        if (jti == null || jti.isBlank() || expiresAt == null) return;

        if (invalidatedTokenRepository.existsById(jti)) return;

        invalidatedTokenRepository.save(InvalidatedToken.builder()
                .jti(jti)
                .expiresAt(expiresAt)
                .createdAt(now)
                .type("ACCESS")
                .reason(reason)
                .build());
    }

    @Transactional(readOnly = true)
    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) return false;
        return invalidatedTokenRepository.existsByJtiAndExpiresAtAfter(jti, Instant.now());
    }
}
