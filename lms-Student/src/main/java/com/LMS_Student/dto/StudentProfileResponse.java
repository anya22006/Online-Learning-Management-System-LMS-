package com.LMS_Student.dto;

public record StudentProfileResponse(
        Integer id,
        String name,
        String email,
        String role,
        String status
) {
}
