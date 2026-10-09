package com.LMS_Student.controller;

import com.LMS_Student.dto.StudentSubmissionRequest;
import com.LMS_Student.entity.Submission;
import com.LMS_Student.service.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/submissions")
@CrossOrigin
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Submission submitAssignment(@RequestBody StudentSubmissionRequest request) {
        return submissionService.submitAssignment(request);
    }
}
