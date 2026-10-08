package com.LMS_Student.service;

import com.LMS_Student.dto.StudentSubmissionRequest;
import com.LMS_Student.entity.Assignment;
import com.LMS_Student.entity.Submission;
import com.LMS_Student.repo.AssignmentRepository;
import com.LMS_Student.repo.EnrollmentRepository;
import com.LMS_Student.repo.SubmissionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class SubmissionService {

    private static final int MAX_SUBMISSION_LENGTH = 500;

    private final AssignmentRepository assignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SubmissionRepository submissionRepository;

    public SubmissionService(
            AssignmentRepository assignmentRepository,
            EnrollmentRepository enrollmentRepository,
            SubmissionRepository submissionRepository
    ) {
        this.assignmentRepository = assignmentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.submissionRepository = submissionRepository;
    }

    public Submission submitAssignment(StudentSubmissionRequest request) {
        if (request.getAssignmentId() == null || request.getAssignmentId() <= 0
                || request.getStudentId() == null || request.getStudentId() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid assignment ID and student ID are required."
            );
        }

        String submissionFile = request.getSubmissionFile();
        if (submissionFile == null || submissionFile.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Enter your assignment submission before submitting."
            );
        }

        submissionFile = submissionFile.trim();
        if (submissionFile.length() > MAX_SUBMISSION_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Submission text or reference must be 500 characters or fewer."
            );
        }

        Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Assignment not found."
                ));

        if (enrollmentRepository
                .findByStudentIdAndCourseId(request.getStudentId(), assignment.getCourseId())
                .isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You must be enrolled in the course to submit this assignment."
            );
        }

        if (submissionRepository.existsByStudentIdAndAssignmentId(
                request.getStudentId(),
                request.getAssignmentId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A submission already exists for this assignment."
            );
        }

        LocalDateTime submittedAt = LocalDateTime.now();
        Submission submission = new Submission();
        submission.setAssignmentId(assignment.getId());
        submission.setStudentId(request.getStudentId());
        submission.setSubmissionFile(submissionFile);
        submission.setSubmittedAt(submittedAt);
        submission.setStatus(
                assignment.getDueDate() != null && submittedAt.isAfter(assignment.getDueDate())
                        ? "late"
                        : "submitted"
        );

        return submissionRepository.save(submission);
    }
}
