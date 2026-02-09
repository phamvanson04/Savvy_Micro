package com.savvycom.auth_service.admin.helper;

import com.savvy.common.dto.PageResponse;
import com.savvycom.auth_service.admin.dto.response.AdminUserSummaryResponse;
import com.savvycom.auth_service.entity.User;
import com.savvycom.auth_service.entity.UserSchoolScope;
import com.savvycom.auth_service.entity.UserStudent;
import com.savvycom.auth_service.repository.UserSchoolScopeRepository;
import com.savvycom.auth_service.repository.UserStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdminUserPageAssembler {

    private final UserSchoolScopeRepository userSchoolScopeRepository;
    private final UserStudentRepository userStudentRepository;
    private final AdminUserMapper mapper;

    public PageResponse<AdminUserSummaryResponse> toSummaryPage(Page<User> page) {
        List<User> users = page.getContent();
        if (users.isEmpty()) {
            return PageResponse.<AdminUserSummaryResponse>builder()
                    .items(List.of())
                    .page(page.getNumber())
                    .size(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .build();
        }

        List<UUID> userIds = users.stream().map(User::getId).toList();

        Map<UUID, UUID> schoolIdByUserId = userSchoolScopeRepository.findByUserIdIn(userIds)
                .stream()
                .filter(x -> x.getSchoolId() != null)
                .collect(Collectors.toMap(
                        UserSchoolScope::getUserId,
                        UserSchoolScope::getSchoolId,
                        (a, b) -> a // nếu lỡ duplicate row thì lấy cái đầu
                ));

        Map<UUID, UUID> studentIdByUserId = userStudentRepository.findByUserIdIn(userIds)
                .stream()
                .filter(x -> x.getStudentId() != null)
                .collect(Collectors.toMap(
                        UserStudent::getUserId,
                        UserStudent::getStudentId,
                        (a, b) -> a
                ));

        List<AdminUserSummaryResponse> items = users.stream()
                .map(u -> mapper.toSummary(
                        u,
                        schoolIdByUserId.get(u.getId()),
                        studentIdByUserId.get(u.getId())
                ))
                .toList();

        return PageResponse.<AdminUserSummaryResponse>builder()
                .items(items)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
