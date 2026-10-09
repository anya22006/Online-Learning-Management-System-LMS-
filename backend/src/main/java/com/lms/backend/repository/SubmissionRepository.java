package com.lms.backend.repository;

import com.lms.backend.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository
        extends JpaRepository<Submission, Integer> {

    List<Submission> findByAssignmentId(Integer assignmentId);

    List<Submission> findByStudentId(Integer studentId);

    Optional<Submission> findByAssignmentIdAndStudentId(
            Integer assignmentId,
            Integer studentId
    );
}