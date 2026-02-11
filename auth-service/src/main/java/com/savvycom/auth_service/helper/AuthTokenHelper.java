package com.savvycom.auth_service.helper;

import com.savvy.common.exception.BusinessException;
import com.savvy.common.exception.ErrorCode;
import com.savvycom.auth_service.entity.RefreshToken;
import com.savvycom.auth_service.repository.RefreshTokenRepository;
import com.savvycom.auth_service.service.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthTokenHelper {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Transactional
    public String issueRefreshToken(UUID userId) {
        Instant now = Instant.now();

        String raw = jwtService.generateRefreshToken(userId);
        String hash = sha256(raw);

        Instant exp = jwtService.parseAndValidateRefreshToken(raw).getExpiration().toInstant();

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(userId)
                .tokenHash(hash)
                .expiresAt(exp)
                .createdAt(now)
                .build());

        return raw;
    }

    @Transactional(readOnly = true)
    public RefreshToken getRefreshTokenOrThrow(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "refresh token is missing");
        }

        try {
            jwtService.parseAndValidateRefreshToken(rawToken);
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "Refresh token invalid");
        }

        // hash rawToken de so sanh voi token_hash trong db
        String hash = sha256(rawToken);
        return refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID, "refresh token revoked or not found"));
    }

    @Transactional(readOnly = true)
    public RefreshToken getUsableRefreshTokenOrThrow(String rawToken) {
        RefreshToken rt = getRefreshTokenOrThrow(rawToken);
        assertUsable(rt);
        return rt;
    }

    public void assertUsable(RefreshToken token) {
        Instant now = Instant.now();

        if (token.getRevokedAt() != null) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "refresh token revoked or not found");
        }
        if (token.getExpiresAt() == null || token.getExpiresAt().isBefore(now)) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED, "refresh token expired");
        }
    }

    @Transactional
    public String rotate(RefreshToken oldToken) {
        assertUsable(oldToken);

        Instant now = Instant.now();

        // sinh token moi cung userId
        String newRaw = jwtService.generateRefreshToken(oldToken.getUserId());
        String newHash = sha256(newRaw);
        Instant newExp = jwtService.parseAndValidateRefreshToken(newRaw).getExpiration().toInstant();

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(oldToken.getUserId())
                .tokenHash(newHash)
                .expiresAt(newExp)
                .createdAt(now)
                .build());

        // token da bi thu hoi (now) route
        oldToken.setRevokedAt(now);
        oldToken.setReplacedByTokenHash(newHash);
        refreshTokenRepository.save(oldToken);

        return newRaw;
    }

    @Transactional
    // thu hoi token
    public void revokeIfNeeded(RefreshToken token) {
        if (token.getRevokedAt() == null) {
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
        }
    }

    private String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Cannot hash token", e);
        }
    }
}
