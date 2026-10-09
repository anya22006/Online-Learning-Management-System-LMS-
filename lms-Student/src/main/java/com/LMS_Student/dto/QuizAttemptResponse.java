package com.LMS_Student.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class QuizAttemptResponse {

    private final Integer quizId;
    private final BigDecimal score;
    private final Integer totalQuestions;
    private final Integer correctAnswers;
    private final LocalDateTime attemptedAt;

    public QuizAttemptResponse(
            Integer quizId,
            BigDecimal score,
            Integer totalQuestions,
            Integer correctAnswers,
            LocalDateTime attemptedAt
    ) {
        this.quizId = quizId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.attemptedAt = attemptedAt;
    }

    public Integer getQuizId() {
        return quizId;
    }

    public BigDecimal getScore() {
        return score;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public LocalDateTime getAttemptedAt() {
        return attemptedAt;
    }
}
