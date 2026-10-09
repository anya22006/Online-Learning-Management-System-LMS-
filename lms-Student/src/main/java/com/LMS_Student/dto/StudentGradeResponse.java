package com.LMS_Student.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StudentGradeResponse {

    private final Integer assignmentId;
    private final String assignmentTitle;
    private final Integer courseId;
    private final String courseTitle;
    private final BigDecimal marks;
    private final String feedback;
    private final String submissionStatus;
    private final LocalDateTime submittedAt;

    public StudentGradeResponse(
            Integer assignmentId,
            String assignmentTitle,
            Integer courseId,
            String courseTitle,
            BigDecimal marks,
            String feedback,
            String submissionStatus,
            LocalDateTime submittedAt
    ) {
        this.assignmentId = assignmentId;
        this.assignmentTitle = assignmentTitle;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.marks = marks;
        this.feedback = feedback;
        this.submissionStatus = submissionStatus;
        this.submittedAt = submittedAt;
    }

    public Integer getAssignmentId() {
        return assignmentId;
    }

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public BigDecimal getMarks() {
        return marks;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getSubmissionStatus() {
        return submissionStatus;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}
