package com.LMS_Student.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "users")
@DynamicUpdate
public class User {

    @Id
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String role;

    private String status;

    protected User() {
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }
}
