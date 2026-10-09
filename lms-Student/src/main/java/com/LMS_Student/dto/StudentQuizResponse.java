package com.LMS_Student.dto;

public class StudentQuizResponse {

    private final Integer quizId;
    private final Integer courseId;
    private final String courseTitle;
    private final String title;
    private final String description;

    public StudentQuizResponse(
            Integer quizId,
            Integer courseId,
            String courseTitle,
            String title,
            String description
    ) {
        this.quizId = quizId;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.title = title;
        this.description = description;
    }

    public Integer getQuizId() {
        return quizId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
