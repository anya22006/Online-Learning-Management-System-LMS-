package com.lms.web;

import com.lms.admin.*;
import com.lms.common.SecurityHelper;
import com.lms.course.Course;
import com.lms.course.CourseRepository;
import com.lms.course.CourseStatus;
import com.lms.instructor.InstructorProfile;
import com.lms.instructor.InstructorProfileRepository;
import com.lms.student.Enrollment;
import com.lms.student.EnrollmentRepository;
import com.lms.student.StudentProfile;
import com.lms.student.StudentProfileRepository;
import com.lms.user.User;
import com.lms.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminWebController {

    @Autowired
    private UserRepository userRepository;

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

    @Autowired
    private SecurityHelper securityHelper;

    private User getAuthenticatedAdmin(HttpSession session) {
        if (!securityHelper.hasAnyRole(session, "ADMIN")) {
            return null;
        }
        return securityHelper.getCurrentUser(session);
    }

    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        User currentUser = getAuthenticatedAdmin(session);
        if (currentUser == null) return "redirect:/login";

        long totalStudents = userRepository.countByRole("STUDENT");
        long totalInstructors = userRepository.countByRole("INSTRUCTOR");
        long totalCourses = courseRepository.count();
        long publishedCourses = courseRepository.countByStatus(CourseStatus.PUBLISHED);
        long pendingCourses = courseRepository.countByStatus(CourseStatus.DRAFT);
        long totalEnrollments = enrollmentRepository.count();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalInstructors", totalInstructors);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("publishedCourses", publishedCourses);
        model.addAttribute("pendingCourses", pendingCourses);
        model.addAttribute("totalEnrollments", totalEnrollments);

        List<User> userList = userRepository.findAll();
        model.addAttribute("users", userList);

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

        List<ActivityLog> logs = activityLogRepository.findAllByOrderByTimestampDesc();
        model.addAttribute("logs", logs);

        List<PlatformSetting> settings = platformSettingRepository.findAll();
        model.addAttribute("settings", settings);

        return "admin-dashboard";
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String role,
                             @RequestParam(defaultValue = "password123") String password,
                             @RequestParam(required = false) String department,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedAdmin(session);
        if (currentUser == null) return "redirect:/login";

        if (userRepository.findByEmailIgnoreCase(email.trim()).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User with email " + email + " already exists.");
            return "redirect:/admin/dashboard";
        }

        String normRole = role.toUpperCase();
        User newUser = new User(email.trim(), password.trim(), name.trim(), normRole, department);
        newUser = userRepository.save(newUser);

        if ("INSTRUCTOR".equals(normRole)) {
            instructorProfileRepository.save(new InstructorProfile(newUser.getId(), name, email, department != null ? department : "General", "Instructor", "Active"));
        } else if ("STUDENT".equals(normRole)) {
            studentProfileRepository.save(new StudentProfile(newUser.getId(), name, email, "Student", "Active"));
        }

        securityHelper.logActivity("Admin (" + currentUser.getFullName() + ")", "Created User Account", name + " (" + normRole + ") in SQLite");
        redirectAttributes.addFlashAttribute("successMessage", "User account created! Email: " + email + " (Default password: " + password + ")");

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/toggle-status")
    public String toggleUserStatus(@RequestParam Long id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedAdmin(session);
        if (currentUser == null) return "redirect:/login";

        userRepository.findById(id).ifPresent(user -> {
            if ("ACTIVE".equalsIgnoreCase(user.getStatus())) {
                user.setStatus("INACTIVE");
            } else {
                user.setStatus("ACTIVE");
            }
            userRepository.save(user);
            securityHelper.logActivity("Admin", "Toggled User Status", user.getFullName() + " -> " + user.getStatus());
            redirectAttributes.addFlashAttribute("successMessage", "User status changed to " + user.getStatus());
        });

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam Long id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedAdmin(session);
        if (currentUser == null) return "redirect:/login";

        userRepository.findById(id).ifPresent(user -> {
            userRepository.delete(user);
            if ("STUDENT".equalsIgnoreCase(user.getRole())) {
                studentProfileRepository.deleteById(id);
            } else if ("INSTRUCTOR".equalsIgnoreCase(user.getRole())) {
                instructorProfileRepository.deleteById(id);
            }
            securityHelper.logActivity("Admin", "Deleted User Account", user.getFullName() + " (" + user.getEmail() + ")");
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully from SQLite.");
        });

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/courses/toggle-status")
    public String toggleCourseStatus(@RequestParam Long courseId,
                                     @RequestParam String status,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedAdmin(session);
        if (currentUser == null) return "redirect:/login";

        courseRepository.findById(courseId).ifPresent(c -> {
            c.setStatus(CourseStatus.valueOf(status));
            courseRepository.save(c);
            securityHelper.logActivity("Admin", "Changed Course Status", c.getTitle() + " -> " + status);
            redirectAttributes.addFlashAttribute("successMessage", "Course status updated.");
        });

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/courses/delete")
    public String deleteCourse(@RequestParam Long courseId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedAdmin(session);
        if (currentUser == null) return "redirect:/login";

        courseRepository.findById(courseId).ifPresent(c -> {
            courseRepository.deleteById(courseId);
            securityHelper.logActivity("Admin", "Deleted Course", c.getTitle());
            redirectAttributes.addFlashAttribute("successMessage", "Course deleted successfully.");
        });

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/edit")
    public String editUser(@RequestParam Long id,
                           @RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String role,
                           @RequestParam String status,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedAdmin(session);
        if (currentUser == null) return "redirect:/login";

        userRepository.findById(id).ifPresent(user -> {
            user.setFullName(name.trim());
            user.setEmail(email.trim());
            user.setRole(role.trim().toUpperCase());
            user.setStatus(status.trim().toUpperCase());
            userRepository.save(user);

            if ("STUDENT".equalsIgnoreCase(user.getRole())) {
                studentProfileRepository.findById(id).ifPresent(sp -> {
                    sp.setName(user.getFullName());
                    sp.setEmail(user.getEmail());
                    sp.setStatus(user.getStatus());
                    studentProfileRepository.save(sp);
                });
            } else if ("INSTRUCTOR".equalsIgnoreCase(user.getRole())) {
                instructorProfileRepository.findById(id).ifPresent(ip -> {
                    ip.setName(user.getFullName());
                    ip.setEmail(user.getEmail());
                    ip.setStatus(user.getStatus());
                    instructorProfileRepository.save(ip);
                });
            }
            securityHelper.logActivity("Admin (" + currentUser.getFullName() + ")", "Updated User Account", user.getFullName() + " (" + user.getRole() + ")");
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
        });

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/enroll")
    public String enrollStudent(@RequestParam Long studentId,
                                @RequestParam Long courseId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedAdmin(session);
        if (currentUser == null) return "redirect:/login";

        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            enrollmentRepository.save(new Enrollment(studentId, courseId));
            securityHelper.logActivity("Admin", "Enrolled Student", "Student #" + studentId + " -> Course #" + courseId);
            redirectAttributes.addFlashAttribute("successMessage", "Student enrolled successfully into course!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Student is already enrolled in this course.");
        }

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/settings/save")
    public String saveSettings(@RequestParam Map<String, String> allParams,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedAdmin(session);
        if (currentUser == null) return "redirect:/login";

        allParams.forEach((key, value) -> {
            if (!key.startsWith("_") && !"save".equals(key)) {
                PlatformSetting setting = platformSettingRepository.findById(key)
                        .orElse(new PlatformSetting(key, value));
                setting.setSettingValue(value);
                platformSettingRepository.save(setting);
            }
        });
        securityHelper.logActivity("Admin (" + currentUser.getFullName() + ")", "Updated System Settings", "Saved platform settings to SQLite");
        redirectAttributes.addFlashAttribute("successMessage", "System settings saved successfully.");

        return "redirect:/admin/dashboard";
    }
}
