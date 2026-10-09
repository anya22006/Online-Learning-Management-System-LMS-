package com.lms.backend.dto;

public class UserResponse {

    private Integer id;
    private String name;
    private String email;
    private String role;
    private String status;

    public UserResponse(Integer id, String name, String email,
                         String role, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }
}