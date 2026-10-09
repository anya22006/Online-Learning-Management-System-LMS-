package com.lms.admin;

import com.lms.course.Course;
import com.lms.course.CourseRepository;
import com.lms.course.CourseStatus;
import com.lms.instructor.InstructorProfile;
import com.lms.instructor.InstructorProfileRepository;
import com.lms.student.Enrollment;
import com.lms.student.EnrollmentRepository;
import com.lms.student.StudentProfile;
import com.lms.student.StudentProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private InstructorProfileRepository instructorProfileRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private PlatformSettingRepository platformSettingRepository;

    // Admin Enroll Student into Course
    @PostMapping("/enrollments")
    public ResponseEntity<?> createAdminEnrollment(@RequestBody Map<String, Long> payload) {
        Long studentId = payload.get("studentId");
        Long courseId = payload.get("courseId");
        if (studentId == null || courseId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "studentId and courseId are required"));
        }
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            Enrollment enrollment = new Enrollment(studentId, courseId);
            enrollmentRepository.save(enrollment);
            activityLogRepository.save(new ActivityLog("Admin", "Enrolled Student #" + studentId, "Course #" + courseId));
        }
        return ResponseEntity.ok(Map.of("message", "Enrollment created successfully"));
    }

    // 1. Dashboard Metrics Summary
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getAdminMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        long totalStudents = studentProfileRepository.count();
        long totalInstructors = instructorProfileRepository.count();
        long totalCourses = courseRepository.count();
        long publishedCourses = courseRepository.countByStatus(CourseStatus.PUBLISHED);
        long pendingCourses = courseRepository.countByStatus(CourseStatus.DRAFT);
        long totalEnrollments = enrollmentRepository.count();

        metrics.put("totalStudents", totalStudents > 0 ? totalStudents : 1);
        metrics.put("totalInstructors", totalInstructors > 0 ? totalInstructors : 1);
        metrics.put("totalCourses", totalCourses);
        metrics.put("publishedCourses", publishedCourses);
        metrics.put("pendingCourses", pendingCourses);
        metrics.put("totalEnrollments", totalEnrollments);

        return ResponseEntity.ok(metrics);
    }

    // 2. User Management - List All Users
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        List<AdminUserDto> userList = new ArrayList<>();

        // Add Students
        List<StudentProfile> students = studentProfileRepository.findAll();
        for (StudentProfile s : students) {
            userList.add(new AdminUserDto(s.getId(), s.getName(), s.getEmail(), "STUDENT", s.getStatus()));
        }

        // Add Instructors
        List<InstructorProfile> instructors = instructorProfileRepository.findAll();
        for (InstructorProfile i : instructors) {
            userList.add(new AdminUserDto(i.getId(), i.getName(), i.getEmail(), "INSTRUCTOR", i.getStatus()));
        }

        // Add Default Admin if empty
        userList.add(new AdminUserDto(999L, "System Administrator", "admin@lms.com", "ADMIN", "ACTIVE"));

        return ResponseEntity.ok(userList);
    }

    // Create User
    @PostMapping("/users")
    public ResponseEntity<AdminUserDto> createUser(@RequestBody Map<String, String> payload) {
        String name = payload.getOrDefault("name", "New User");
        String email = payload.getOrDefault("email", "user@lms.com");
        String role = payload.getOrDefault("role", "STUDENT").toUpperCase();
        String status = payload.getOrDefault("status", "ACTIVE");

        AdminUserDto created;
        if ("INSTRUCTOR".equals(role)) {
            Long nextId = System.currentTimeMillis() % 10000;
            InstructorProfile ip = new InstructorProfile(nextId, name, email, "Computer Science", "Instructor", status);
            instructorProfileRepository.save(ip);
            created = new AdminUserDto(ip.getId(), ip.getName(), ip.getEmail(), "INSTRUCTOR", ip.getStatus());
        } else {
            Long nextId = System.currentTimeMillis() % 10000;
            StudentProfile sp = new StudentProfile(nextId, name, email, "Student", status);
            studentProfileRepository.save(sp);
            created = new AdminUserDto(sp.getId(), sp.getName(), sp.getEmail(), "STUDENT", sp.getStatus());
        }

        activityLogRepository.save(new ActivityLog("Admin", "Created User Account", "User #" + created.getId() + " (" + role + ")"));
        return ResponseEntity.ok(created);
    }

    // Update User
    @PutMapping("/users/{role}/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String role, @PathVariable Long id, @RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String email = payload.get("email");
        String status = payload.get("status");

        if ("INSTRUCTOR".equalsIgnoreCase(role)) {
            Optional<InstructorProfile> opt = instructorProfileRepository.findById(id);
            if (opt.isPresent()) {
                InstructorProfile ip = opt.get();
                if (name != null) ip.setName(name);
                if (email != null) ip.setEmail(email);
                if (status != null) ip.setStatus(status);
                instructorProfileRepository.save(ip);
                activityLogRepository.save(new ActivityLog("Admin", "Updated Instructor", ip.getName()));
                return ResponseEntity.ok(ip);
            }
        } else {
            Optional<StudentProfile> opt = studentProfileRepository.findById(id);
            if (opt.isPresent()) {
                StudentProfile sp = opt.get();
                if (name != null) sp.setName(name);
                if (email != null) sp.setEmail(email);
                if (status != null) sp.setStatus(status);
                studentProfileRepository.save(sp);
                activityLogRepository.save(new ActivityLog("Admin", "Updated Student", sp.getName()));
                return ResponseEntity.ok(sp);
            }
        }
        return ResponseEntity.notFound().build();
    }

    // Delete User
    @DeleteMapping("/users/{role}/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String role, @PathVariable Long id) {
        if ("INSTRUCTOR".equalsIgnoreCase(role)) {
            instructorProfileRepository.deleteById(id);
        } else {
            studentProfileRepository.deleteById(id);
        }
        activityLogRepository.save(new ActivityLog("Admin", "Deleted User Account", "User #" + id));
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    // 3. Course Management
    @GetMapping("/courses")
    public ResponseEntity<List<Map<String, Object>>> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Course c : courses) {
            Map<String, Object> item = new HashMap<>();
            item.put("courseId", c.getCourseId());
            item.put("title", c.getTitle());
            item.put("category", c.getCategory());
            item.put("status", c.getStatus().name());
            item.put("instructorId", c.getInstructorId());
            item.put("createdAt", c.getCreatedAt());

            // Instructor name
            Optional<InstructorProfile> instOpt = instructorProfileRepository.findById(c.getInstructorId());
            item.put("instructorName", instOpt.map(InstructorProfile::getName).orElse("Instructor #" + c.getInstructorId()));

            // Enrollment count
            long enrollCount = enrollmentRepository.countByCourseId(c.getCourseId());
            item.put("enrollmentCount", enrollCount);

            response.add(item);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/courses/{id}/status")
    public ResponseEntity<?> toggleCourseStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Optional<Course> opt = courseRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Course course = opt.get();
        String targetStatus = payload.getOrDefault("status", "PUBLISHED");
        course.setStatus(CourseStatus.valueOf(targetStatus));
        courseRepository.save(course);

        activityLogRepository.save(new ActivityLog("Admin", "Changed Course Status to " + targetStatus, course.getTitle()));
        return ResponseEntity.ok(course);
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        Optional<Course> opt = courseRepository.findById(id);
        String title = opt.map(Course::getTitle).orElse("Course #" + id);
        courseRepository.deleteById(id);

        activityLogRepository.save(new ActivityLog("Admin", "Deleted Course", title));
        return ResponseEntity.ok(Map.of("message", "Course deleted successfully"));
    }

    // 4. Activity Logs
    @GetMapping("/activity-logs")
    public ResponseEntity<List<ActivityLog>> getActivityLogs() {
        List<ActivityLog> logs = activityLogRepository.findAllByOrderByTimestampDesc();
        if (logs.isEmpty()) {
            // Seed initial logs if empty
            ActivityLog log1 = new ActivityLog("Admin", "System Initialization", "LMS Core Engine");
            ActivityLog log2 = new ActivityLog("Dr. Sarah Jenkins", "Published Course", "Advanced Java Microservices");
            activityLogRepository.saveAll(List.of(log1, log2));
            logs = activityLogRepository.findAllByOrderByTimestampDesc();
        }
        return ResponseEntity.ok(logs);
    }

    // 5. System Settings
    @GetMapping("/settings")
    public ResponseEntity<Map<String, String>> getSettings() {
        List<PlatformSetting> settingsList = platformSettingRepository.findAll();
        Map<String, String> result = new HashMap<>();
        for (PlatformSetting ps : settingsList) {
            result.put(ps.getSettingKey(), ps.getSettingValue());
        }
        if (result.isEmpty()) {
            result.put("platformName", "LMS Learning Management System");
            result.put("defaultRole", "Student");
            result.put("maintenanceMode", "Disabled");
            result.put("emailNotifications", "Enabled");
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/settings")
    public ResponseEntity<Map<String, String>> saveSettings(@RequestBody Map<String, String> payload) {
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            platformSettingRepository.save(new PlatformSetting(entry.getKey(), entry.getValue()));
        }
        activityLogRepository.save(new ActivityLog("Admin", "Updated Platform Settings", "System Configuration"));
        return ResponseEntity.ok(payload);
    }

    // 6. CSV Export Report
    @GetMapping("/reports/enrollments/csv")
    public ResponseEntity<String> exportEnrollmentsCsv() {
        StringBuilder csv = new StringBuilder("Course ID,Course Title,Category,Status,Enrollment Count\n");
        List<Course> courses = courseRepository.findAll();
        for (Course c : courses) {
            long count = enrollmentRepository.countByCourseId(c.getCourseId());
            csv.append(c.getCourseId()).append(",")
               .append("\"").append(c.getTitle()).append("\",")
               .append("\"").append(c.getCategory()).append("\",")
               .append(c.getStatus()).append(",")
               .append(count).append("\n");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=enrollment_report.csv");
        return ResponseEntity.ok().headers(headers).body(csv.toString());
    }
}
