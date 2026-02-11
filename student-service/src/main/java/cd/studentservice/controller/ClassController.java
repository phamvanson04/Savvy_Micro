package cd.studentservice.controller;

import cd.studentservice.dto.request.CreateClassRequest;
import cd.studentservice.dto.request.UpdateClassRequest;
import cd.studentservice.dto.response.ClassResponse;
import cd.studentservice.service.ClassService;
import com.savvy.common.dto.BaseResponse;
import com.savvy.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/classes")
@RequiredArgsConstructor
public class ClassController {
    private final ClassService classService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL_MANAGER')")
    public BaseResponse<List<ClassResponse>>findBySchoolId(@RequestParam UUID schoolId){
        return BaseResponse.success(classService.findBySchoolId(schoolId),"Get data success");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL_MANAGER')")
    public BaseResponse<ClassResponse>findById(@PathVariable("id") UUID id){
        return BaseResponse.success(classService.findById(id),"Get data success");
    }
    
//    @GetMapping
//    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL_MANAGER')")
//    public BaseResponse<PageResponse<ClassResponse>>findPage(@RequestParam int page,
//                                                             @RequestParam int size){
//        return BaseResponse.success(classService.findPage(page,size),"Get data success");
//    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL_MANAGER')")
    public BaseResponse<ClassResponse>save(@RequestBody CreateClassRequest createClassRequest){
        return BaseResponse.created(classService.save(createClassRequest),"Save data success");
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL_MANAGER')")
    public BaseResponse<ClassResponse>update(@PathVariable("id") UUID id,
                                             @RequestBody UpdateClassRequest updateClassRequest){
        return BaseResponse.created(classService.update(id,updateClassRequest),"Update data success");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL_MANAGER')")
    public BaseResponse<?>delete(@PathVariable("id") UUID id){
        classService.delete(id);
        return BaseResponse.created(null,"Delete data success");
    }
    
}
