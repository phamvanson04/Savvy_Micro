package cd.studentservice.service;

import cd.studentservice.dto.request.CreateClassRequest;
import cd.studentservice.dto.request.UpdateClassRequest;
import cd.studentservice.dto.response.ClassResponse;
import cd.studentservice.entity.Class;
import com.savvy.common.dto.PageResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface ClassService {
    ClassResponse findById(UUID id);
    PageResponse<ClassResponse>findPage(int page, int size);
    List<ClassResponse> findBySchoolId(UUID schoolId);
    Class getReferenceById(UUID id);
    ClassResponse save(CreateClassRequest createClassRequest);
    ClassResponse update(UUID id, UpdateClassRequest updateClassRequest);
    void delete(UUID id);
}
