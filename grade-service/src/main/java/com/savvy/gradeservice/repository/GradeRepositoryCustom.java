package com.savvy.gradeservice.repository;

import com.savvy.gradeservice.api.dto.request.GradeSearch;
import com.savvy.gradeservice.entity.Grade;

import java.util.List;
import java.util.UUID;

// custom rerpository interface để truy ấn  bằng querydsl(dynamic query) 
// List<UUID> accessibleSchoolIds là list schoolId mà user có quyền truy cập
public interface GradeRepositoryCustom {
    List<Grade> searchGradesWithAccessControl(GradeSearch searchRequest, List<UUID> accessibleSchoolIds);
}
