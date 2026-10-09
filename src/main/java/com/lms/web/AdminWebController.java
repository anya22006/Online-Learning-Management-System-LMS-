package com.lms.web;

import com.lms.admin.*;
import com.lms.course.Course;
import com.lms.course.CourseRepository;
import com.lms.course.CourseStatus;
import com.lms.instructor.InstructorProfile;
import com.lms.instructor.InstructorProfileRepository;
import com.lms.student.StudentProfile;
import com.lms.student.StudentProfileRepository;
import com.lms.student.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminWebController {

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

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        long totalStudents = studentProfileRepository.count();
        long totalInstructors = instructorProfileRepository.count();
        long totalCourses = courseRepository.count();
        long publishedCourses = courseRepository.countByStatus(CourseStatus.PUBLISHED);
        long pendingCourses = courseRepository.countByStatus(CourseStatus.DRAFT);
        long totalEnrollments = enrollmentRepository.count();

        model.addAttribute("totalStudents", totalStudents > 0 ? totalStudents : 1);
        model.addAttribute("totalInstructors", totalInstructors > 0 ? totalInstructors : 1);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("publishedCourses", publishedCourses);
        model.addAttribute("pendingCourses", pendingCourses);
        model.addAttribute("totalEnrollments", totalEnrollments);

        // Add Users list
        List<AdminUserDto> userList = new ArrayList<>();
        for (StudentProfile s : studentProfileRepository.findAll()) {
            userList.add(new AdminUserDto(s.getId(), s.getName(), s.getEmail(), "STUDENT", s.getStatus()));
        }
        for (InstructorProfile i : instructorProfileRepository.findAll()) {
            userList.add(new AdminUserDto(i.getId(), i.getName(), i.getEmail(), "INSTRUCTOR", i.getStatus()));
        }
        model.addAttribute("users", userList);

        // Add Courses list
        List<Map<String, Object>> courseList = new ArrayList<>();
        for (Course c : courseRepository.findAll()) {
            Map<String, Object> map = new HashMap<>();
            map.put("courseId", c.getCourseId());
            map.put("title", c.getTitle());
            map.put("category", c.getCategory());
            map.put("status", c.getStatus().name());
            map.put("createdAt", c.getCreatedAt());
            map.put("enrollmentCount", enrollmentRepository.countByCourseId(c.getCourseId()));
            courseList.add(map);
        }
        model.addAttribute("courses", courseList);

        // Logs
        List<ActivityLog> logs = activityLogRepository.findAllByOrderByTimestampDesc();
        model.addAttribute("logs", logs);

        return "admin-dashboard";
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam String name, @RequestParam String email, @RequestParam String role) {
        if ("INSTRUCTOR".equalsIgnoreCase(role)) {
            Long nextId = System.currentTimeMillis() % 10000;
            InstructorProfile ip = new InstructorProfile(nextId, name, email, "Computer Science", "Instructor", "ACTIVE");
            instructorProfileRepository.save(ip);
        } else {
            Long nextId = System.currentTimeMillis() % 10000;
            StudentProfile sp = new StudentProfile(nextId, name, email, "Student", "ACTIVE");
            studentProfileRepository.save(sp);
        }
        activityLogRepository.save(new ActivityLog("Admin", "Created User Account", name + " (" + role + ")"));
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam String role, @RequestParam Long id) {
        if ("INSTRUCTOR".equalsIgnoreCase(role)) {
            instructorProfileRepository.deleteById(id);
        } else {
            studentProfileRepository.deleteById(id);
        }
        activityLogRepository.save(new ActivityLog("Admin", "Deleted User Account", "User #" + id));
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/courses/toggle-status")
    public String toggleCourseStatus(@RequestParam Long courseId, @RequestParam String status) {
        courseRepository.findById(courseId).ifPresent(c -> {
            c.setStatus(CourseStatus.valueOf(status));
            courseRepository.save(c);
            activityLogRepository.save(new ActivityLog("Admin", "Changed Course Status to " + status, c.getTitle()));
        });
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/courses/delete")
    public String deleteCourse(@RequestParam Long courseId) {
        courseRepository.findById(courseId).ifPresent(c -> {
            courseRepository.deleteById(courseId);
            activityLogRepository.save(new ActivityLog("Admin", "Deleted Course", c.getTitle()));
        });
        return "redirect:/admin/dashboard";
    }
}
