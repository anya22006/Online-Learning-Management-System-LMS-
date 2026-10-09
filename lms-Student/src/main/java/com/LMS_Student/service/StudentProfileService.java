package com.LMS_Student.service;

import com.LMS_Student.dto.StudentProfileResponse;
import com.LMS_Student.dto.UpdateStudentProfileRequest;
import com.LMS_Student.entity.User;
import com.LMS_Student.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Pattern;

@Service
public class StudentProfileService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserRepository userRepository;

    public StudentProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public StudentProfileResponse getProfile(Integer studentId) {
        return toResponse(findStudent(studentId));
    }

    public StudentProfileResponse updateProfile(
            Integer studentId,
            UpdateStudentProfileRequest request
    ) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required.");
        }

        if (request.email() == null || request.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required.");
        }

        String name = request.name().trim();
        String email = request.email().trim();
        if (email.length() > 254 || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid email address.");
        }

        User user = findStudent(studentId);
        user.setName(name);
        user.setEmail(email);
        return toResponse(userRepository.save(user));
    }

    private User findStudent(Integer studentId) {
        return userRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student profile not found."
                ));
    }

    private StudentProfileResponse toResponse(User user) {
        return new StudentProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }
}
