package com.lms.web;

import com.lms.common.SecurityHelper;
import com.lms.instructor.InstructorProfile;
import com.lms.instructor.InstructorProfileRepository;
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

import java.util.Optional;

@Controller
public class HomeWebController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private InstructorProfileRepository instructorProfileRepository;

    @Autowired
    private SecurityHelper securityHelper;

    @GetMapping("/")
    public String index(HttpSession session) {
        User currentUser = securityHelper.getCurrentUser(session);
        if (currentUser != null) {
            return getDashboardRedirect(currentUser.getRole());
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "logout", required = false) String logout,
                            HttpSession session,
                            Model model) {
        User currentUser = securityHelper.getCurrentUser(session);
        if (currentUser != null) {
            return getDashboardRedirect(currentUser.getRole());
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam("email") String email,
                              @RequestParam("password") String password,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (email == null || password == null) {
            redirectAttributes.addFlashAttribute("error", "Email and password are required.");
            return "redirect:/login";
        }

        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email.trim());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (password.trim().equals(user.getPassword())) {
                if ("INACTIVE".equalsIgnoreCase(user.getStatus())) {
                    redirectAttributes.addFlashAttribute("error", "Your account has been deactivated. Please contact administrator.");
                    return "redirect:/login";
                }
                session.setAttribute("currentUser", user);
                securityHelper.logActivity(user.getFullName(), "User Login", "Logged into system as " + user.getRole());
                return getDashboardRedirect(user.getRole());
            }
        }

        redirectAttributes.addFlashAttribute("error", "Invalid email or password. Please try again.");
        return "redirect:/login";
    }

    @PostMapping("/register")
    public String handleRegister(@RequestParam("fullName") String fullName,
                                 @RequestParam("email") String email,
                                 @RequestParam("password") String password,
                                 @RequestParam(value = "role", defaultValue = "STUDENT") String role,
                                 @RequestParam(value = "department", required = false) String department,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty() || fullName == null || fullName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("registerError", "All required fields must be filled.");
            return "redirect:/login?tab=register";
        }

        if (userRepository.findByEmailIgnoreCase(email.trim()).isPresent()) {
            redirectAttributes.addFlashAttribute("registerError", "An account with this email already exists.");
            return "redirect:/login?tab=register";
        }

        String normalizedRole = role.toUpperCase();
        if (!"STUDENT".equals(normalizedRole) && !"INSTRUCTOR".equals(normalizedRole)) {
            normalizedRole = "STUDENT";
        }

        User newUser = new User(email.trim(), password.trim(), fullName.trim(), normalizedRole, department);
        newUser = userRepository.save(newUser);

        if ("STUDENT".equals(normalizedRole)) {
            studentProfileRepository.save(new StudentProfile(newUser.getId(), newUser.getFullName(), newUser.getEmail(), "Student", "Active"));
        } else if ("INSTRUCTOR".equals(normalizedRole)) {
            instructorProfileRepository.save(new InstructorProfile(newUser.getId(), newUser.getFullName(), newUser.getEmail(),
                    department != null ? department : "General", "Instructor", "Active"));
        }

        securityHelper.logActivity(newUser.getFullName(), "Account Created", "Registered new " + normalizedRole + " account in SQLite");
        session.setAttribute("currentUser", newUser);

        return getDashboardRedirect(newUser.getRole());
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        User user = securityHelper.getCurrentUser(session);
        if (user != null) {
            securityHelper.logActivity(user.getFullName(), "User Logout", "Logged out from system");
        }
        session.invalidate();
        return "redirect:/login?logout=true";
    }

    @ResponseBody
    @GetMapping("/api/auth/me")
    public org.springframework.http.ResponseEntity<?> getCurrentAuthUser(HttpSession session) {
        User currentUser = securityHelper.getCurrentUser(session);
        if (currentUser == null) {
            return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("authenticated", false));
        }
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of(
            "authenticated", true,
            "id", currentUser.getId(),
            "studentId", currentUser.getId(),
            "name", currentUser.getFullName(),
            "email", currentUser.getEmail(),
            "role", currentUser.getRole()
        ));
    }

    private String getDashboardRedirect(String role) {
        if ("INSTRUCTOR".equalsIgnoreCase(role)) {
            return "redirect:/instructor-dashboard.html";
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            return "redirect:/admin-dashboard.html";
        } else {
            return "redirect:/student-dashboard.html";
        }
    }
}
