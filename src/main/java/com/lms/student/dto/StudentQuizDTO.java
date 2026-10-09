package com.lms.student.dto;

public class StudentQuizDTO {
    private Long quizId;
    private Long courseId;
    private String courseTitle;
    private String title;
    private String description;
    private Integer timeLimitMinutes;

    public StudentQuizDTO() {}

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(Integer timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }
}
