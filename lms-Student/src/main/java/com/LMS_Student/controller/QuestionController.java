package com.LMS_Student.controller;

import com.LMS_Student.dto.StudentQuestionResponse;
import com.LMS_Student.service.QuestionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/quizzes")
@CrossOrigin
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/{quizId}/questions")
    public List<StudentQuestionResponse> getQuizQuestions(
            @PathVariable Integer quizId
    ) {
        return questionService.getQuestionsForQuiz(quizId);
    }
}
