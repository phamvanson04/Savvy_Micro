package cd.studentservice.controller;

import cd.studentservice.dto.request.CreateSchoolRequest;
import cd.studentservice.dto.request.UpdateSchoolRequest;
import cd.studentservice.dto.response.SchoolResponse;
import cd.studentservice.entity.School;
import cd.studentservice.service.SchoolService;
import com.savvy.common.dto.BaseResponse;
import com.savvy.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/schools")
@RequiredArgsConstructor
public class SchoolController {
    private final SchoolService schoolService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<PageResponse<SchoolResponse>>findPage(@RequestParam int page,
                                                             @RequestParam int size){
        return BaseResponse.success(schoolService.getPages(size,page),"Get data success");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL_MANAGER')")
    public BaseResponse<SchoolResponse>findById(@PathVariable UUID id){
        return BaseResponse.success(schoolService.findById(id),"Get data success");
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<SchoolResponse>save(@RequestBody CreateSchoolRequest request){
        return BaseResponse.created(schoolService.save(request),"Save data success");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<SchoolResponse>update(@PathVariable UUID id,
                                                      @RequestBody UpdateSchoolRequest request){
        return BaseResponse.created(schoolService.update(id,request),"Update data success");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<?>delete(@PathVariable UUID id){
        schoolService.delete(id);
        return BaseResponse.success(null,"Delete data success");
    }
}
