package com.lms.assignment;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "max_score", nullable = false)
    private Integer maxScore = 100;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Assignment() {}

    public Assignment(Long courseId, String title, String instructions, LocalDateTime dueDate, Integer maxScore) {
        this.courseId = courseId;
        this.title = title;
        this.instructions = instructions;
        this.dueDate = dueDate;
        this.maxScore = maxScore != null ? maxScore : 100;
        this.createdAt = LocalDateTime.now();
    }

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public Integer getMaxScore() { return maxScore; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
