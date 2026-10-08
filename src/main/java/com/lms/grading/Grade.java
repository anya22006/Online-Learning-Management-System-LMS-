package com.lms.grading;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grades")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gradeId;

    @Column(name = "submission_id", nullable = false, unique = true)
    private Long submissionId;

    @Column(name = "instructor_id", nullable = false)
    private Long instructorId;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "max_score", nullable = false)
    private Integer maxScore;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt = LocalDateTime.now();

    public Grade() {}

    public Grade(Long submissionId, Long instructorId, Integer score, Integer maxScore, String feedback) {
        this.submissionId = submissionId;
        this.instructorId = instructorId;
        this.score = score;
        this.maxScore = maxScore;
        this.feedback = feedback;
        this.gradedAt = LocalDateTime.now();
    }

    public Long getGradeId() { return gradeId; }
    public void setGradeId(Long gradeId) { this.gradeId = gradeId; }

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public Long getInstructorId() { return instructorId; }
    public void setInstructorId(Long instructorId) { this.instructorId = instructorId; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getMaxScore() { return maxScore; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }
}
