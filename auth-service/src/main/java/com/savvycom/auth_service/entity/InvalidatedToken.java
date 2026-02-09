package com.savvycom.auth_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "invalidated_tokens")
@Entity
public class InvalidatedToken {
    @Id
    @Column(name = "jti", length = 64, nullable = false, updatable = false)
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "type", length = 20, nullable = false)
    private String type;

    @Column(name = "reason", length = 50)
    private String reason;
}

