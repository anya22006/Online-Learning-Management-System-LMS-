package com.lms.backend.controller;

import com.lms.backend.entity.Submission;
import com.lms.backend.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "http://localhost:5173")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping
    public List<Submission> getAllSubmissions() {
        return submissionService.getAllSubmissions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Submission> getSubmissionById(
            @PathVariable Integer id) {

        return submissionService.getSubmissionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/assignment/{assignmentId}")
    public List<Submission> getSubmissionsByAssignment(
            @PathVariable Integer assignmentId) {

        return submissionService
                .getSubmissionsByAssignment(assignmentId);
    }

    @GetMapping("/student/{studentId}")
    public List<Submission> getSubmissionsByStudent(
            @PathVariable Integer studentId) {

        return submissionService
                .getSubmissionsByStudent(studentId);
    }

    @GetMapping("/assignment/{assignmentId}/student/{studentId}")
    public ResponseEntity<Submission> getSubmission(
            @PathVariable Integer assignmentId,
            @PathVariable Integer studentId) {

        return submissionService
                .getSubmission(assignmentId, studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Submission createSubmission(
            @RequestBody Submission submission) {

        return submissionService.saveSubmission(submission);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Submission> updateSubmission(
            @PathVariable Integer id,
            @RequestBody Submission submission) {

        return submissionService.getSubmissionById(id)
                .map(existingSubmission -> {

                    existingSubmission.setAssignmentId(
                            submission.getAssignmentId());

                    existingSubmission.setStudentId(
                            submission.getStudentId());

                    existingSubmission.setContent(
                            submission.getContent());

                    existingSubmission.setFileUrl(
                            submission.getFileUrl());

                    existingSubmission.setMarks(
                            submission.getMarks());

                    existingSubmission.setStatus(
                            submission.getStatus());

                    return ResponseEntity.ok(
                            submissionService.updateSubmission(
                                    existingSubmission)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubmission(
            @PathVariable Integer id) {

        if (submissionService.getSubmissionById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        submissionService.deleteSubmission(id);

        return ResponseEntity.noContent().build();
    }
}