package com.LMS_Student.dto;

public record UpdateStudentProfileRequest(
        String name,
        String email
) {
}
