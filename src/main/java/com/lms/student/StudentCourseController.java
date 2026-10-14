package com.lms.student;

import com.lms.common.SecurityHelper;
import com.lms.course.Course;
import com.lms.course.CourseRepository;
import com.lms.course.CourseStatus;
import com.lms.user.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
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

    @Autowired
    private LessonCompletionRepository lessonCompletionRepository;

    @Autowired
    private SecurityHelper securityHelper;

    private Long resolveStudentId(Long providedId, HttpSession session) {
        if (providedId != null) return providedId;
        User currentUser = securityHelper.getCurrentUser(session);
        return (currentUser != null) ? currentUser.getId() : 1L;
    }

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
    @GetMapping({"/enrolled-courses", "/enrolled-courses/{studentId}"})
    public ResponseEntity<List<Course>> getEnrolledCourses(
            @PathVariable(required = false) Long studentId,
            HttpSession session) {
        Long targetId = resolveStudentId(studentId, session);
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(targetId);
        List<Long> courseIds = enrollments.stream().map(Enrollment::getCourseId).toList();
        if (courseIds.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        return ResponseEntity.ok(courseRepository.findAllById(courseIds));
    }

    @GetMapping({"/enrollments", "/enrollments/{studentId}"})
    public ResponseEntity<List<Enrollment>> getEnrollments(
            @PathVariable(required = false) Long studentId,
            HttpSession session) {
        Long targetId = resolveStudentId(studentId, session);
        return ResponseEntity.ok(enrollmentRepository.findByStudentId(targetId));
    }

    @PostMapping("/enrollments")
    public ResponseEntity<Enrollment> enrollInCourse(
            @RequestBody Map<String, Long> body,
            HttpSession session) {
        Long rawStudentId = body.get("studentId");
        final Long studentId = (rawStudentId != null) ? rawStudentId : resolveStudentId(null, session);
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

    @GetMapping({"/progress", "/progress/{studentId}"})
    public ResponseEntity<List<Map<String, Object>>> getStudentProgress(
            @PathVariable(required = false) Long studentId,
            HttpSession session) {
        Long targetId = resolveStudentId(studentId, session);
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(targetId);
        List<Map<String, Object>> progressList = new ArrayList<>();

        for (Enrollment e : enrollments) {
            Long courseId = e.getCourseId();
            int completedLessons = lessonCompletionRepository.countByStudentIdAndCourseId(targetId, courseId);
            int totalLessons = 5; // Standard 5 lessons per course curriculum
            int pct = (int) Math.min(100, Math.round(((double) completedLessons / totalLessons) * 100));

            Map<String, Object> progress = new HashMap<>();
            progress.put("courseId", courseId);
            progress.put("completedLessons", completedLessons);
            progress.put("totalLessons", totalLessons);
            progress.put("progressPercentage", pct);
            progressList.add(progress);
        }
        return ResponseEntity.ok(progressList);
    }

    @PostMapping({"/progress/{studentId}/course/{courseId}/lesson/{lessonId}/complete",
                  "/progress/course/{courseId}/lesson/{lessonId}/complete"})
    public ResponseEntity<Map<String, Object>> completeLesson(
            @PathVariable(required = false) Long studentId,
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            HttpSession session) {
        Long targetId = resolveStudentId(studentId, session);
        boolean alreadyCompleted = lessonCompletionRepository.existsByStudentIdAndCourseIdAndLessonId(targetId, courseId, lessonId);
        if (!alreadyCompleted) {
            lessonCompletionRepository.save(new LessonCompletion(targetId, courseId, lessonId));
        }

        int completedLessons = lessonCompletionRepository.countByStudentIdAndCourseId(targetId, courseId);
        int totalLessons = 5;
        int pct = (int) Math.min(100, Math.round(((double) completedLessons / totalLessons) * 100));

        Map<String, Object> progress = new HashMap<>();
        progress.put("courseId", courseId);
        progress.put("completedLessons", completedLessons);
        progress.put("totalLessons", totalLessons);
        progress.put("progressPercentage", pct);

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "success");
        resp.put("alreadyCompleted", alreadyCompleted);
        resp.put("progress", progress);
        return ResponseEntity.ok(resp);
    }
}
