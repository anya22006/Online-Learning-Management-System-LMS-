package com.LMS_Student.controller;

import com.LMS_Student.entity.Enrollment;
import com.LMS_Student.service.EnrollmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/enrollments")
@CrossOrigin
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public Enrollment enrollStudent(
            @RequestParam Integer studentId,
            @RequestParam Integer courseId) {

        return enrollmentService.enrollStudent(studentId, courseId);
    }

    @GetMapping("/{studentId}")
    public List<Enrollment> getStudentEnrollments(
            @PathVariable Integer studentId) {

        return enrollmentService.getStudentEnrollments(studentId);
    }
}