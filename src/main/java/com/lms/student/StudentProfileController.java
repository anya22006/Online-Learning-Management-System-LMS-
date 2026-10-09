package com.lms.student;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/profile")
@CrossOrigin(origins = "*")
public class StudentProfileController {

    @Autowired
    private StudentProfileRepository profileRepository;

    private StudentProfile getOrCreateProfile(Long id) {
        return profileRepository.findById(id).orElseGet(() -> {
            StudentProfile defaultProfile = new StudentProfile(id, "Alex Johnson", "alex.johnson@student.lms.com", "Student", "Active");
            return profileRepository.save(defaultProfile);
        });
    }

    @GetMapping
    public ResponseEntity<StudentProfile> getDefaultProfile() {
        return ResponseEntity.ok(getOrCreateProfile(1L));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentProfile> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(getOrCreateProfile(id));
    }

    @PutMapping
    public ResponseEntity<StudentProfile> updateDefaultProfile(@RequestBody StudentProfile updated) {
        return updateProfileInternal(1L, updated);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentProfile> updateProfileById(@PathVariable Long id, @RequestBody StudentProfile updated) {
        return updateProfileInternal(id, updated);
    }

    private ResponseEntity<StudentProfile> updateProfileInternal(Long id, StudentProfile updated) {
        StudentProfile profile = getOrCreateProfile(id);
        if (updated.getName() != null && !updated.getName().isBlank()) {
            profile.setName(updated.getName());
        }
        if (updated.getEmail() != null && !updated.getEmail().isBlank()) {
            profile.setEmail(updated.getEmail());
        }
        return ResponseEntity.ok(profileRepository.save(profile));
    }
}
