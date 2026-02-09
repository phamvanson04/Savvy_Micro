package com.savvycom.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_school_scope")
public class UserSchoolScope {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;
}
