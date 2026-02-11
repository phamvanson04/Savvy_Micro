package cd.studentservice.service.impl;

import cd.studentservice.dto.request.CreateClassRequest;
import cd.studentservice.dto.request.UpdateClassRequest;
import cd.studentservice.dto.response.ClassResponse;
import cd.studentservice.entity.Class;
import cd.studentservice.entity.School;
import cd.studentservice.mapper.ClassMapper;
import cd.studentservice.repository.ClassRepository;
import cd.studentservice.service.ClassService;
import cd.studentservice.service.SchoolService;
import com.savvy.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {
    private final SchoolService schoolService;
    private final ClassRepository classRepository;
    private final ClassMapper classMapper;

    @Override
    public ClassResponse findById(UUID id) {
        return classMapper.toClassResponse(classRepository.findById(id).orElseThrow(() -> new RuntimeException("Cannot find class with id: " + id)));
    }

    @Override
    public PageResponse<ClassResponse> findPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Class> classPages = classRepository.findAll(pageable);
        return new PageResponse<>(
                classMapper.toClassResponses(classPages.getContent()),
                classPages.getNumber(),
                classPages.getSize(),
                classPages.getTotalElements(),
                classPages.getTotalPages()
        );
    }

    @Override
    public List<ClassResponse> findBySchoolId(UUID schoolId) {
        return classMapper.toClassResponses(classRepository.findAllBySchoolId(schoolId));
    }

    @Override
    public Class getReferenceById(UUID id) {
        return classRepository.getReferenceById(id);
    }

    @Override
    @Transactional
    public ClassResponse save(CreateClassRequest createClassRequest) {
        School existedSchool=schoolService.getReferenceById(createClassRequest.getSchoolId());
        Class newClass=Class.builder()
                .code(createClassRequest.getCode())
                .name(createClassRequest.getName())
                .grade(createClassRequest.getGrade())
                .status(createClassRequest.getStatus())
                .school(existedSchool)
                .createdAt(Instant.now())
                .build();
        return classMapper.toClassResponse(classRepository.save(newClass));
    }

    @Override
    @Transactional
    public ClassResponse update(UUID id, UpdateClassRequest updateClassRequest) {
        Class existedClass=classRepository.findById(id).orElseThrow(() -> new RuntimeException("Cannot find class with id: " + id));
        existedClass.setSchool(!Objects.equals(existedClass.getSchool().getId(),updateClassRequest.getSchoolId())?schoolService.getReferenceById(updateClassRequest.getSchoolId()):existedClass.getSchool());
        existedClass.setCode(updateClassRequest.getCode());
        existedClass.setName(updateClassRequest.getName());
        existedClass.setGrade(updateClassRequest.getGrade());
        existedClass.setStatus(updateClassRequest.getStatus());
        return classMapper.toClassResponse(classRepository.save(existedClass));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        classRepository.deleteById(id);
    }
}
