package com.LMS_Student.repo;

import com.LMS_Student.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Integer> {

    List<Submission> findByStudentIdOrderBySubmittedAtDesc(Integer studentId);

    boolean existsByStudentIdAndAssignmentId(
            Integer studentId,
            Integer assignmentId
    );

    List<Submission> findByStudentIdAndAssignmentIdInOrderBySubmittedAtDesc(
            Integer studentId,
            List<Integer> assignmentIds
    );
}
