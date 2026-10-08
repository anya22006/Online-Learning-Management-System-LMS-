package com.LMS_Student.controller;

import com.LMS_Student.dto.StudentQuizResponse;
import com.LMS_Student.service.QuizService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/quizzes")
@CrossOrigin
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/{studentId}")
    public List<StudentQuizResponse> getStudentQuizzes(
            @PathVariable Integer studentId
    ) {
        return quizService.getQuizzesForStudent(studentId);
    }
}
