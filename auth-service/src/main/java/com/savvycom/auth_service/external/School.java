package com.savvycom.auth_service.external;

import com.savvycom.auth_service.external.enumerate.SchoolStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class School {
    private UUID id;
    private String code;
    private String name;
    private String address;
    private SchoolStatus status;
    private Instant createdAt;
}