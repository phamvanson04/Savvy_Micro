package com.savvycom.auth_service.admin.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {
    private UUID id;
    private String name;
    private String description;
    private List<String> permissions;
}
