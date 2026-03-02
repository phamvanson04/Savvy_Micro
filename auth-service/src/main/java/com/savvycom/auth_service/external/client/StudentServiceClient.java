package com.savvycom.auth_service.external.client;

import com.savvy.common.dto.BaseResponse;
import com.savvy.common.dto.PageResponse;
import com.savvycom.auth_service.external.Class;
import com.savvycom.auth_service.external.School;
import com.savvycom.auth_service.external.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentServiceClient {

    private final RestTemplate restTemplate;

    private static final String BASE = "http://localhost:8088/api/v1/students";

    public Student getStudentOrNull(UUID studentId) {
        String url = BASE + "/students/" + studentId;
        try {
            ResponseEntity<BaseResponse<Student>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<BaseResponse<Student>>() {}
            );
            return resp.getBody() != null ? resp.getBody().getData() : null;
        } catch (HttpClientErrorException e) {
            log.warn("getStudent failed url={} status={} body={}",
                    url, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.warn("getStudent failed url={} err={}", url, e.toString());
            return null;
        }
    }

    public School getSchoolOrNull(UUID schoolId) {
        String url = BASE + "/schools/" + schoolId;
        try {
            ResponseEntity<BaseResponse<School>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<BaseResponse<School>>() {}
            );
            return resp.getBody() != null ? resp.getBody().getData() : null;
        } catch (HttpClientErrorException e) {
            log.warn("getSchool failed url={} status={} body={}",
                    url, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.warn("getSchool failed url={} err={}", url, e.toString());
            return null;
        }
    }

    public PageResponse<Class> getClassesBySchoolOrNull(UUID schoolId) {
        String url = UriComponentsBuilder
                .fromHttpUrl(BASE + "/classes")
                .queryParam("schoolId", schoolId)
                .toUriString();

        try {
            ResponseEntity<BaseResponse<List<Class>>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<BaseResponse<List<Class>>>() {}
            );

            List<Class> items = (resp.getBody() != null && resp.getBody().getData() != null)
                    ? resp.getBody().getData()
                    : List.of();

            return PageResponse.<Class>builder()
                    .items(items)
                    .page(0)
                    .size(items.size())
                    .totalElements(items.size())
                    .totalPages(1)
                    .build();

        } catch (HttpClientErrorException e) {
            log.warn("getClassesBySchool failed url={} status={} body={}",
                    url, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.warn("getClassesBySchool failed url={} err={}", url, e.toString());
            return null;
        }
    }
}