package com.lms.assignment;

import com.lms.assignment.dto.AssignmentRequestDTO;
import com.lms.course.Course;
import com.lms.course.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    public Assignment createAssignment(Long instructorId, AssignmentRequestDTO dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + dto.getCourseId()));

        if (!course.getInstructorId().equals(instructorId)) {
            throw new SecurityException("Forbidden: You do not own this course");
        }

        Assignment assignment = new Assignment(
                dto.getCourseId(),
                dto.getTitle(),
                dto.getInstructions(),
                dto.getDueDate() != null ? dto.getDueDate() : LocalDateTime.now().plusDays(7),
                dto.getMaxScore() != null ? dto.getMaxScore() : 100
        );

        return assignmentRepository.save(assignment);
    }

    public List<Assignment> getAssignmentsByCourse(Long courseId) {
        return assignmentRepository.findByCourseId(courseId);
    }

    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    public void deleteAssignment(Long assignmentId, Long instructorId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found with ID: " + assignmentId));

        Course course = courseRepository.findById(assignment.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (!course.getInstructorId().equals(instructorId)) {
            throw new SecurityException("Forbidden: You do not own this course");
        }

        assignmentRepository.delete(assignment);
    }
}
