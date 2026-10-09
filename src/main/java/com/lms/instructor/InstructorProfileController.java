package com.lms.instructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instructor/profile")
@CrossOrigin(origins = "*")
public class InstructorProfileController {

    @Autowired
    private InstructorProfileRepository profileRepository;

    private InstructorProfile getOrCreateProfile(Long id) {
        return profileRepository.findById(id).orElseGet(() -> {
            InstructorProfile defaultProfile = new InstructorProfile(id, "Dr. Sarah Jenkins", "sarah.jenkins@lms.com", "Computer Science & Engineering", "Instructor", "Active");
            return profileRepository.save(defaultProfile);
        });
    }

    @GetMapping
    public ResponseEntity<InstructorProfile> getDefaultProfile(
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long instructorId) {
        return ResponseEntity.ok(getOrCreateProfile(instructorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstructorProfile> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(getOrCreateProfile(id));
    }

    @PutMapping
    public ResponseEntity<InstructorProfile> updateDefaultProfile(
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long instructorId,
            @RequestBody InstructorProfile updated) {
        return updateProfileInternal(instructorId, updated);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InstructorProfile> updateProfileById(@PathVariable Long id, @RequestBody InstructorProfile updated) {
        return updateProfileInternal(id, updated);
    }

    private ResponseEntity<InstructorProfile> updateProfileInternal(Long id, InstructorProfile updated) {
        InstructorProfile profile = getOrCreateProfile(id);
        if (updated.getName() != null && !updated.getName().isBlank()) {
            profile.setName(updated.getName());
        }
        if (updated.getEmail() != null && !updated.getEmail().isBlank()) {
            profile.setEmail(updated.getEmail());
        }
        if (updated.getDepartment() != null && !updated.getDepartment().isBlank()) {
            profile.setDepartment(updated.getDepartment());
        }
        return ResponseEntity.ok(profileRepository.save(profile));
    }
}
