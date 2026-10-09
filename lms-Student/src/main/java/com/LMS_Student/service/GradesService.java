package com.LMS_Student.service;

import com.LMS_Student.dto.StudentGradeResponse;
import com.LMS_Student.entity.Assignment;
import com.LMS_Student.entity.Course;
import com.LMS_Student.entity.Submission;
import com.LMS_Student.repo.AssignmentRepository;
import com.LMS_Student.repo.CourseRepository;
import com.LMS_Student.repo.SubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class GradesService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;

    public GradesService(
            SubmissionRepository submissionRepository,
            AssignmentRepository assignmentRepository,
            CourseRepository courseRepository
    ) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
    }

    public List<StudentGradeResponse> getStudentGrades(Integer studentId) {
        List<Submission> submissions =
                submissionRepository.findByStudentIdOrderBySubmittedAtDesc(studentId);

        if (submissions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> assignmentIds = submissions.stream()
                .map(Submission::getAssignmentId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Integer, Assignment> assignmentsById = new HashMap<>();
        assignmentRepository.findAllById(assignmentIds)
                .forEach(assignment -> assignmentsById.put(assignment.getId(), assignment));

        List<Integer> courseIds = assignmentsById.values().stream()
                .map(Assignment::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Integer, Course> coursesById = new HashMap<>();
        courseRepository.findAllById(courseIds)
                .forEach(course -> coursesById.put(course.getId(), course));

        return submissions.stream()
                .map(submission -> toResponse(
                        submission,
                        assignmentsById.get(submission.getAssignmentId()),
                        coursesById
                ))
                .filter(Objects::nonNull)
                .toList();
    }

    private StudentGradeResponse toResponse(
            Submission submission,
            Assignment assignment,
            Map<Integer, Course> coursesById
    ) {
        if (assignment == null) {
            return null;
        }

        Course course = coursesById.get(assignment.getCourseId());
        return new StudentGradeResponse(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getCourseId(),
                course == null ? null : course.getTitle(),
                submission.getMarks(),
                submission.getFeedback(),
                submission.getStatus(),
                submission.getSubmittedAt()
        );
    }
}
