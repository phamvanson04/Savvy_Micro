package cd.studentservice.controller;

import cd.studentservice.dto.request.CreateStudentRequest;
import cd.studentservice.dto.request.UpdateStudentRequest;
import cd.studentservice.dto.response.StudentResponse;
import cd.studentservice.mapper.StudentMapper;
import cd.studentservice.service.StudentService;
import com.savvy.common.dto.BaseResponse;
import com.savvy.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL_MANAGER')")
    public BaseResponse<PageResponse<StudentResponse>>findPage(@RequestParam int page,
                                                               @RequestParam int size){
        return BaseResponse.success(studentService.getPage(size,page),"Get data success");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL_MANAGER','STUDENT')")
    public BaseResponse<StudentResponse>findById(@PathVariable UUID id){
        return BaseResponse.success(studentService.findById(id),"Get data success");
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL_MANAGER')")
    public BaseResponse<StudentResponse>save(@RequestBody CreateStudentRequest createStudentRequest){
        return BaseResponse.created(studentService.save(createStudentRequest),"Save data success");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL_MANAGER','STUDENT')")
    public BaseResponse<StudentResponse> update(@PathVariable UUID id,
                                                @RequestBody UpdateStudentRequest updateStudentRequest){
        return BaseResponse.created(studentService.update(id,updateStudentRequest),"Update data success");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL_MANAGER')")
    public BaseResponse<StudentResponse> delete(@PathVariable UUID id){
        studentService.delete(id);
        return BaseResponse.success(null,"Delete data success");
    }
}
