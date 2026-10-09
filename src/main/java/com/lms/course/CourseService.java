package com.lms.course;

import com.lms.course.dto.CourseRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    public Course createCourse(Long instructorId, CourseRequestDTO dto) {
        Course course = new Course(
            instructorId,
            dto.getTitle(),
            dto.getCategory(),
            dto.getDescription(),
            dto.getSyllabus(),
            CourseStatus.DRAFT
        );
        return courseRepository.save(course);
    }

    public List<Course> getInstructorCourses(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId);
    }

    public Course updateCourseStatus(Long courseId, Long instructorId, CourseStatus newStatus) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));

        if (!course.getInstructorId().equals(instructorId)) {
            throw new SecurityException("Forbidden: You do not own this course");
        }

        course.setStatus(newStatus);
        return courseRepository.save(course);
    }

    public void deleteCourse(Long courseId, Long instructorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));

        if (!course.getInstructorId().equals(instructorId)) {
            throw new SecurityException("Forbidden: You do not own this course");
        }

        courseRepository.delete(course);
    }
}
