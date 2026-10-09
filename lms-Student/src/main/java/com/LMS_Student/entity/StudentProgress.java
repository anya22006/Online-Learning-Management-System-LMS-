package com.LMS_Student.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_progress")
public class StudentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "course_id", nullable = false)
    private Integer courseId;

    @Column(name = "completed_lessons")
    private Integer completedLessons;

    @Column(name = "total_lessons")
    private Integer totalLessons;

    @Column(name = "progress_percentage")
    private BigDecimal progressPercentage;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;


    // ========================================
    // GETTERS AND SETTERS
    // ========================================

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }


    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }


    public Integer getCompletedLessons() {
        return completedLessons;
    }

    public void setCompletedLessons(Integer completedLessons) {
        this.completedLessons = completedLessons;
    }


    public Integer getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(Integer totalLessons) {
        this.totalLessons = totalLessons;
    }


    public BigDecimal getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(
            BigDecimal progressPercentage) {

        this.progressPercentage =
                progressPercentage;
    }


    public LocalDateTime getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(
            LocalDateTime lastAccessed) {

        this.lastAccessed =
                lastAccessed;
    }
}