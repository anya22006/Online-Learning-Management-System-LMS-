package com.lms.instructor;

import jakarta.persistence.*;

@Entity
@Table(name = "instructor_profiles")
public class InstructorProfile {

    @Id
    private Long id = 2L; // Default instructor ID matches header (ID: 2)

    @Column(nullable = false)
    private String name = "Dr. Sarah Jenkins";

    @Column(nullable = false)
    private String email = "sarah.jenkins@lms.com";

    private String department = "Computer Science & Engineering";

    private String role = "Instructor";

    private String status = "Active";

    public InstructorProfile() {}

    public InstructorProfile(Long id, String name, String email, String department, String role, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department != null ? department : "Computer Science & Engineering";
        this.role = role != null ? role : "Instructor";
        this.status = status != null ? status : "Active";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
