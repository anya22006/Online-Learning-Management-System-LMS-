package com.lms.quiz;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quizId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes = 30;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Quiz() {}

    public Quiz(Long courseId, String title, String instructions, Integer timeLimitMinutes) {
        this.courseId = courseId;
        this.title = title;
        this.instructions = instructions;
        this.timeLimitMinutes = timeLimitMinutes != null ? timeLimitMinutes : 30;
        this.createdAt = LocalDateTime.now();
    }

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public Integer getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(Integer timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
