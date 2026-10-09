package com.LMS_Student.controller;

import com.LMS_Student.dto.StudentGradeResponse;
import com.LMS_Student.service.GradesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/grades")
@CrossOrigin
public class GradesController {

    private final GradesService gradesService;

    public GradesController(GradesService gradesService) {
        this.gradesService = gradesService;
    }

    @GetMapping("/{studentId}")
    public List<StudentGradeResponse> getStudentGrades(
            @PathVariable Integer studentId
    ) {
        return gradesService.getStudentGrades(studentId);
    }
}
