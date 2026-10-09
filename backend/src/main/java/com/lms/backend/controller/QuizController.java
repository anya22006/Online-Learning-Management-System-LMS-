package com.lms.backend.controller;

import com.lms.backend.entity.Quiz;
import com.lms.backend.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@CrossOrigin(origins = "http://localhost:5173")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public List<Quiz> getAllQuizzes() {
        return quizService.getAllQuizzes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuizById(
            @PathVariable Integer id) {

        return quizService.getQuizById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/course/{courseId}")
    public List<Quiz> getQuizzesByCourse(
            @PathVariable Integer courseId) {

        return quizService.getQuizzesByCourse(courseId);
    }

    @PostMapping
    public Quiz createQuiz(@RequestBody Quiz quiz) {
        return quizService.saveQuiz(quiz);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Quiz> updateQuiz(
            @PathVariable Integer id,
            @RequestBody Quiz quiz) {

        return quizService.getQuizById(id)
                .map(existingQuiz -> {

                    existingQuiz.setCourseId(quiz.getCourseId());
                    existingQuiz.setTitle(quiz.getTitle());
                    existingQuiz.setDescription(quiz.getDescription());
                    existingQuiz.setTotalMarks(quiz.getTotalMarks());
                    existingQuiz.setTimeLimit(quiz.getTimeLimit());

                    return ResponseEntity.ok(
                            quizService.saveQuiz(existingQuiz)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable Integer id) {

        if (quizService.getQuizById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        quizService.deleteQuiz(id);

        return ResponseEntity.noContent().build();
    }
}