package com.LMS_Student.service;

import com.LMS_Student.dto.StudentAssignmentResponse;
import com.LMS_Student.entity.Assignment;
import com.LMS_Student.entity.Course;
import com.LMS_Student.entity.Enrollment;
import com.LMS_Student.entity.Submission;
import com.LMS_Student.repo.AssignmentRepository;
import com.LMS_Student.repo.CourseRepository;
import com.LMS_Student.repo.EnrollmentRepository;
import com.LMS_Student.repo.SubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            SubmissionRepository submissionRepository,
            EnrollmentRepository enrollmentRepository,
            CourseRepository courseRepository
    ) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
    }

    public List<StudentAssignmentResponse> getAssignmentsForStudent(Integer studentId) {
        List<Integer> courseIds = enrollmentRepository.findByStudentId(studentId)
                .stream()
                .map(Enrollment::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (courseIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Assignment> assignments =
                assignmentRepository.findByCourseIdInOrderByDueDateAsc(courseIds);

        if (assignments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> assignmentIds = assignments.stream()
                .map(Assignment::getId)
                .toList();

        Map<Integer, Submission> latestSubmissionByAssignment = new LinkedHashMap<>();
        submissionRepository
                .findByStudentIdAndAssignmentIdInOrderBySubmittedAtDesc(studentId, assignmentIds)
                .forEach(submission ->
                        latestSubmissionByAssignment.putIfAbsent(
                                submission.getAssignmentId(),
                                submission
                        )
                );

        Map<Integer, String> courseTitles = new HashMap<>();
        for (Course course : courseRepository.findAllById(courseIds)) {
            courseTitles.put(course.getId(), course.getTitle());
        }

        return assignments.stream()
                .map(assignment -> toResponse(
                        assignment,
                        courseTitles.get(assignment.getCourseId()),
                        latestSubmissionByAssignment.get(assignment.getId())
                ))
                .toList();
    }

    private StudentAssignmentResponse toResponse(
            Assignment assignment,
            String courseTitle,
            Submission submission
    ) {
        return new StudentAssignmentResponse(
                assignment.getId(),
                assignment.getCourseId(),
                courseTitle,
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDueDate(),
                submission == null ? "Not submitted" : submission.getStatus(),
                submission == null ? null : submission.getMarks(),
                submission == null ? null : submission.getFeedback(),
                submission == null ? null : submission.getSubmittedAt()
        );
    }
}
