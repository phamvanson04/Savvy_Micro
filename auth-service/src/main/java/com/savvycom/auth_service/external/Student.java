package com.savvycom.auth_service.external;

import com.savvycom.auth_service.external.enumerate.StudentStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {
    private UUID id;
    private String code;
    private String fullName;
    private LocalDate dob;
    private Boolean gender;
    private StudentStatus status;
    private Instant createdAt;
    private Class clazz;
}