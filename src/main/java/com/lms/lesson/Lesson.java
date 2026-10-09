package com.lms.lesson;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lessonId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private String title;

    @Column(name = "content_url", columnDefinition = "TEXT")
    private String contentUrl;

    @Column(name = "lesson_order", nullable = false)
    private Integer lessonOrder = 1;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Lesson() {}

    public Lesson(Long courseId, String title, String contentUrl, Integer lessonOrder) {
        this.courseId = courseId;
        this.title = title;
        this.contentUrl = contentUrl;
        this.lessonOrder = lessonOrder != null ? lessonOrder : 1;
        this.createdAt = LocalDateTime.now();
    }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContentUrl() { return contentUrl; }
    public void setContentUrl(String contentUrl) { this.contentUrl = contentUrl; }

    public Integer getLessonOrder() { return lessonOrder; }
    public void setLessonOrder(Integer lessonOrder) { this.lessonOrder = lessonOrder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
