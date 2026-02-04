package com.savvycom.auth_service.service;

import com.savvycom.auth_service.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;

@Service
public class JwtService {

    private static final String CLAIM_TYP = "typ";
    private static final String TYP_REFRESH = "refresh";

    private final SecretKey accessKey;
    private final long accessExpMs;

    private final SecretKey refreshKey;
    private final long refreshExpMs;

    public JwtService(
            @Value("${application.security.jwt.secret-key}") String accessSecret,
            @Value("${application.security.jwt.expiration}") long accessExpMs,
            @Value("${application.security.jwt.refresh-token.secret-key}") String refreshSecret,
            @Value("${application.security.jwt.refresh-token.expiration}") long refreshExpMs
    ) {
        this.accessKey = Keys.hmacShaKeyFor(decodeSecret(accessSecret));
        this.accessExpMs = accessExpMs;

        this.refreshKey = Keys.hmacShaKeyFor(decodeSecret(refreshSecret));
        this.refreshExpMs = refreshExpMs;
    }

    public String generateAccessToken(
            User user,
            List<String> roles,
            List<String> permissions,
            List<Long> schoolIds,
            Long studentId
    ) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date exp = Date.from(now.plusMillis(accessExpMs));

        Map<String, Object> dataScope = new HashMap<>();
        dataScope.put("schoolIds", schoolIds == null ? List.of() : schoolIds);

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles == null ? List.of() : roles);
        claims.put("permissions", permissions == null ? List.of() : permissions);
        claims.put("dataScope", dataScope);

        if (studentId != null) {
            claims.put("studentId", studentId);
        }

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(issuedAt)
                .setExpiration(exp)
                .addClaims(claims)
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date exp = Date.from(now.plusMillis(refreshExpMs));

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(issuedAt)
                .setExpiration(exp)
                .claim(CLAIM_TYP, TYP_REFRESH)
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseAndValidateRefreshToken(String rawRefreshToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(refreshKey)
                .build()
                .parseClaimsJws(rawRefreshToken)
                .getBody();

        String typ = claims.get(CLAIM_TYP, String.class);
        if (!TYP_REFRESH.equals(typ)) {
            throw new JwtException("Invalid token type");
        }
        return claims;
    }

    private static byte[] decodeSecret(String s) {
        if (s == null) throw new IllegalArgumentException("JWT secret is null");
        String t = s.trim();

        // hex
        if (t.matches("^[0-9a-fA-F]+$") && t.length() % 2 == 0) {
            byte[] out = new byte[t.length() / 2];
            for (int i = 0; i < out.length; i++) {
                int hi = Character.digit(t.charAt(i * 2), 16);
                int lo = Character.digit(t.charAt(i * 2 + 1), 16);
                out[i] = (byte) ((hi << 4) + lo);
            }
            return out;
        }

        // base64
        return Base64.getDecoder().decode(t);
    }
}
