package com.lms.quiz.dto;

import java.util.List;

public class QuizRequestDTO {
    private Long courseId;
    private String title;
    private String instructions;
    private Integer timeLimitMinutes;
    private List<QuizQuestionDTO> questions;

    public QuizRequestDTO() {}

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public Integer getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(Integer timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }

    public List<QuizQuestionDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuizQuestionDTO> questions) { this.questions = questions; }
}
