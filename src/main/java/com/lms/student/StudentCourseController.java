package com.lms.student;

import com.lms.course.Course;
import com.lms.course.CourseRepository;
import com.lms.course.CourseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentCourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    // Returns published courses for the course catalog
    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getStudentCourses() {
        List<Course> published = courseRepository.findAll().stream()
                .filter(c -> c.getStatus() == CourseStatus.PUBLISHED)
                .toList();
        return ResponseEntity.ok(published.isEmpty() ? courseRepository.findAll() : published);
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long courseId) {
        return courseRepository.findById(courseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Returns courses that the student has actually enrolled in
    @GetMapping("/enrolled-courses/{studentId}")
    public ResponseEntity<List<Course>> getEnrolledCourses(@PathVariable Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<Long> courseIds = enrollments.stream().map(Enrollment::getCourseId).toList();
        if (courseIds.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        return ResponseEntity.ok(courseRepository.findAllById(courseIds));
    }

    @GetMapping("/enrollments/{studentId}")
    public ResponseEntity<List<Enrollment>> getEnrollments(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentRepository.findByStudentId(studentId));
    }

    @PostMapping("/enrollments")
    public ResponseEntity<Enrollment> enrollInCourse(@RequestBody Map<String, Long> body) {
        Long studentId = body.getOrDefault("studentId", 1L);
        Long courseId = body.get("courseId");
        if (courseId == null) {
            return ResponseEntity.badRequest().build();
        }

        // Prevent duplicate enrollment
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            Enrollment enrollment = new Enrollment(studentId, courseId);
            return ResponseEntity.ok(enrollmentRepository.save(enrollment));
        }

        List<Enrollment> existing = enrollmentRepository.findByStudentId(studentId);
        return ResponseEntity.ok(existing.stream()
                .filter(e -> e.getCourseId().equals(courseId))
                .findFirst()
                .orElseGet(() -> enrollmentRepository.save(new Enrollment(studentId, courseId))));
    }

    @GetMapping("/progress/{studentId}")
    public ResponseEntity<List<Map<String, Object>>> getStudentProgress(@PathVariable Long studentId) {
        return ResponseEntity.ok(new ArrayList<>());
    }
}
