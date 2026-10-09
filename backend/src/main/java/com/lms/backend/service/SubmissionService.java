package com.lms.backend.service;

import com.lms.backend.entity.Submission;
import com.lms.backend.repository.SubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;

    public SubmissionService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    public List<Submission> getAllSubmissions() {
        return submissionRepository.findAll();
    }

    public Optional<Submission> getSubmissionById(Integer id) {
        return submissionRepository.findById(id);
    }

    public List<Submission> getSubmissionsByAssignment(
            Integer assignmentId) {

        return submissionRepository.findByAssignmentId(assignmentId);
    }

    public List<Submission> getSubmissionsByStudent(
            Integer studentId) {

        return submissionRepository.findByStudentId(studentId);
    }

    public Optional<Submission> getSubmission(
            Integer assignmentId,
            Integer studentId) {

        return submissionRepository
                .findByAssignmentIdAndStudentId(
                        assignmentId,
                        studentId
                );
    }

    public Submission saveSubmission(Submission submission) {

        Optional<Submission> existingSubmission =
                submissionRepository.findByAssignmentIdAndStudentId(
                        submission.getAssignmentId(),
                        submission.getStudentId()
                );

        if (existingSubmission.isPresent()) {
            throw new RuntimeException(
                    "Student has already submitted this assignment"
            );
        }

        return submissionRepository.save(submission);
    }

    public Submission updateSubmission(Submission submission) {
        return submissionRepository.save(submission);
    }

    public void deleteSubmission(Integer id) {
        submissionRepository.deleteById(id);
    }
}