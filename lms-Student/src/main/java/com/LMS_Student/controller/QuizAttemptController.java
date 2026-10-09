package com.LMS_Student.controller;

import com.LMS_Student.dto.QuizAttemptRequest;
import com.LMS_Student.dto.QuizAttemptResponse;
import com.LMS_Student.service.QuizAttemptService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/quiz-attempts")
@CrossOrigin
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    public QuizAttemptController(QuizAttemptService quizAttemptService) {
        this.quizAttemptService = quizAttemptService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizAttemptResponse submitAttempt(@RequestBody QuizAttemptRequest request) {
        return quizAttemptService.submitAttempt(request);
    }
}
