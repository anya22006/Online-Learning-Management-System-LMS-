package com.LMS_Student.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StudentAssignmentResponse {

    private final Integer assignmentId;
    private final Integer courseId;
    private final String courseTitle;
    private final String title;
    private final String description;
    private final LocalDateTime dueDate;
    private final String submissionStatus;
    private final BigDecimal marks;
    private final String feedback;
    private final LocalDateTime submittedAt;

    public StudentAssignmentResponse(
            Integer assignmentId,
            Integer courseId,
            String courseTitle,
            String title,
            String description,
            LocalDateTime dueDate,
            String submissionStatus,
            BigDecimal marks,
            String feedback,
            LocalDateTime submittedAt
    ) {
        this.assignmentId = assignmentId;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.submissionStatus = submissionStatus;
        this.marks = marks;
        this.feedback = feedback;
        this.submittedAt = submittedAt;
    }

    public Integer getAssignmentId() {
        return assignmentId;
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

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public String getSubmissionStatus() {
        return submissionStatus;
    }

    public BigDecimal getMarks() {
        return marks;
    }

    public String getFeedback() {
        return feedback;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}
