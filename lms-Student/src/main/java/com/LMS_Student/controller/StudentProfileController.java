package com.LMS_Student.controller;

import com.LMS_Student.dto.StudentProfileResponse;
import com.LMS_Student.dto.UpdateStudentProfileRequest;
import com.LMS_Student.service.StudentProfileService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/profile")
@CrossOrigin
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    public StudentProfileController(StudentProfileService studentProfileService) {
        this.studentProfileService = studentProfileService;
    }

    @GetMapping("/{studentId}")
    public StudentProfileResponse getProfile(@PathVariable Integer studentId) {
        return studentProfileService.getProfile(studentId);
    }

    @PutMapping("/{studentId}")
    public StudentProfileResponse updateProfile(
            @PathVariable Integer studentId,
            @RequestBody UpdateStudentProfileRequest request
    ) {
        return studentProfileService.updateProfile(studentId, request);
    }
}
