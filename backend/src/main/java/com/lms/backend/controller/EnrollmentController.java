package com.lms.backend.controller;

import com.lms.backend.entity.Enrollment;
import com.lms.backend.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = "http://localhost:5173")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public List<Enrollment> getAllEnrollments() {
        return enrollmentService.getAllEnrollments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Enrollment> getEnrollmentById(
            @PathVariable Integer id) {

        return enrollmentService.getEnrollmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    public List<Enrollment> getEnrollmentsByStudent(
            @PathVariable Integer studentId) {

        return enrollmentService.getEnrollmentsByStudent(studentId);
    }

    @GetMapping("/course/{courseId}")
    public List<Enrollment> getEnrollmentsByCourse(
            @PathVariable Integer courseId) {

        return enrollmentService.getEnrollmentsByCourse(courseId);
    }

    @PostMapping
    public Enrollment createEnrollment(
            @RequestBody Enrollment enrollment) {

        return enrollmentService.saveEnrollment(enrollment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollment(
            @PathVariable Integer id) {

        if (enrollmentService.getEnrollmentById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }
}