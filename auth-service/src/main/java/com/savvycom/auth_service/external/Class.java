package com.savvycom.auth_service.external;

import com.savvycom.auth_service.external.enumerate.ClassStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Class {
    private UUID id;
    private String code;
    private String name;
    private Integer grade;
    private ClassStatus status;
    private Instant createdAt;
    private School school;
}