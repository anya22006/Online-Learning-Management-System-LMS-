package com.LMS_Student.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "modules")
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "course_id", nullable = false)
    private Integer courseId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "module_order")
    private Integer moduleOrder;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    // ========================================
    // GETTERS AND SETTERS
    // ========================================

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Integer getModuleOrder() {
        return moduleOrder;
    }

    public void setModuleOrder(Integer moduleOrder) {
        this.moduleOrder = moduleOrder;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}