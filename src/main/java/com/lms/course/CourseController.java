package com.lms.course;

import com.lms.course.dto.CourseRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instructor/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping
    public ResponseEntity<Course> createCourse(
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long instructorId,
            @RequestBody CourseRequestDTO dto) {
        return ResponseEntity.ok(courseService.createCourse(instructorId, dto));
    }

    @GetMapping
    public ResponseEntity<List<Course>> getMyCourses(
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long instructorId) {
        return ResponseEntity.ok(courseService.getInstructorCourses(instructorId));
    }

    @PatchMapping("/{courseId}/status")
    public ResponseEntity<Course> updateStatus(
            @PathVariable Long courseId,
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long instructorId,
            @RequestParam CourseStatus status) {
        return ResponseEntity.ok(courseService.updateCourseStatus(courseId, instructorId, status));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long courseId,
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long instructorId) {
        courseService.deleteCourse(courseId, instructorId);
        return ResponseEntity.noContent().build();
    }
}
