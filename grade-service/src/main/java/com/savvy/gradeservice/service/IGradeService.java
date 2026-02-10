package com.savvy.gradeservice.service;

import com.savvy.gradeservice.api.dto.request.CreateGrade;
import com.savvy.gradeservice.api.dto.request.GradeSearch;
import com.savvy.gradeservice.api.dto.request.UpdateGrade;
import com.savvy.gradeservice.api.dto.response.GradeResponse;
import com.savvy.gradeservice.api.dto.response.StudentGradesResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface IGradeService {

    StudentGradesResponse getStudentGrades(UUID studentId, String term, UUID schoolId);
    
    List<GradeResponse> searchGrades(GradeSearch searchRequest);
    
    GradeResponse createGrade(@Valid CreateGrade request);
    
    GradeResponse updateGrade(UUID gradeId, UpdateGrade request);
}

