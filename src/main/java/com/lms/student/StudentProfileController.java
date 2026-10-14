package com.lms.student;

import com.lms.common.SecurityHelper;
import com.lms.user.User;
import com.lms.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentProfileController {

    @Autowired
    private StudentProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityHelper securityHelper;

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentStudent(HttpSession session) {
        User currentUser = securityHelper.getCurrentUser(session);
        if (currentUser == null) {
            Map<String, Object> unauth = new HashMap<>();
            unauth.put("authenticated", false);
            unauth.put("error", "Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(unauth);
        }

        StudentProfile profile = getOrCreateProfile(currentUser.getId());
        Map<String, Object> resp = new HashMap<>();
        resp.put("authenticated", true);
        resp.put("id", currentUser.getId());
        resp.put("studentId", currentUser.getId());
        resp.put("name", currentUser.getFullName());
        resp.put("email", currentUser.getEmail());
        resp.put("role", currentUser.getRole());
        resp.put("department", currentUser.getDepartment() != null ? currentUser.getDepartment() : "General");
        resp.put("profile", profile);
        return ResponseEntity.ok(resp);
    }

    private StudentProfile getOrCreateProfile(Long id) {
        return profileRepository.findById(id).orElseGet(() -> {
            Optional<User> uOpt = userRepository.findById(id);
            String name = uOpt.map(User::getFullName).orElse("Student #" + id);
            String email = uOpt.map(User::getEmail).orElse("student" + id + "@lms.com");
            StudentProfile defaultProfile = new StudentProfile(id, name, email, "Student", "Active");
            return profileRepository.save(defaultProfile);
        });
    }

    @GetMapping("/profile")
    public ResponseEntity<StudentProfile> getDefaultProfile(HttpSession session) {
        User currentUser = securityHelper.getCurrentUser(session);
        Long id = (currentUser != null) ? currentUser.getId() : 1L;
        return ResponseEntity.ok(getOrCreateProfile(id));
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<StudentProfile> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(getOrCreateProfile(id));
    }

    @PutMapping("/profile")
    public ResponseEntity<StudentProfile> updateDefaultProfile(@RequestBody StudentProfile updated, HttpSession session) {
        User currentUser = securityHelper.getCurrentUser(session);
        Long id = (currentUser != null) ? currentUser.getId() : 1L;
        return updateProfileInternal(id, updated, session);
    }

    @PutMapping("/profile/{id}")
    public ResponseEntity<StudentProfile> updateProfileById(@PathVariable Long id, @RequestBody StudentProfile updated, HttpSession session) {
        return updateProfileInternal(id, updated, session);
    }

    private ResponseEntity<StudentProfile> updateProfileInternal(Long id, StudentProfile updated, HttpSession session) {
        StudentProfile profile = getOrCreateProfile(id);
        if (updated.getName() != null && !updated.getName().isBlank()) {
            profile.setName(updated.getName().trim());
        }
        if (updated.getEmail() != null && !updated.getEmail().isBlank()) {
            profile.setEmail(updated.getEmail().trim());
        }
        StudentProfile saved = profileRepository.save(profile);

        // Keep User entity and session in sync
        userRepository.findById(id).ifPresent(u -> {
            if (updated.getName() != null && !updated.getName().isBlank()) {
                u.setFullName(updated.getName().trim());
            }
            if (updated.getEmail() != null && !updated.getEmail().isBlank()) {
                u.setEmail(updated.getEmail().trim());
            }
            userRepository.save(u);
            if (session != null) {
                User sessionUser = (User) session.getAttribute("currentUser");
                if (sessionUser != null && sessionUser.getId().equals(id)) {
                    sessionUser.setFullName(u.getFullName());
                    sessionUser.setEmail(u.getEmail());
                    session.setAttribute("currentUser", sessionUser);
                }
            }
        });

        return ResponseEntity.ok(saved);
    }
}
