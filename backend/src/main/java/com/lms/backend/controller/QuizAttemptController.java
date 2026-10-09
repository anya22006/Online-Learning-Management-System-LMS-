package com.lms.backend.controller;

import com.lms.backend.entity.QuizAttempt;
import com.lms.backend.service.QuizAttemptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-attempts")
@CrossOrigin(origins = "http://localhost:5173")
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    public QuizAttemptController(QuizAttemptService quizAttemptService) {
        this.quizAttemptService = quizAttemptService;
    }

    @GetMapping
    public List<QuizAttempt> getAllAttempts() {
        return quizAttemptService.getAllAttempts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizAttempt> getAttemptById(
            @PathVariable Integer id) {

        return quizAttemptService.getAttemptById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/quiz/{quizId}")
    public List<QuizAttempt> getAttemptsByQuiz(
            @PathVariable Integer quizId) {

        return quizAttemptService.getAttemptsByQuiz(quizId);
    }

    @GetMapping("/student/{studentId}")
    public List<QuizAttempt> getAttemptsByStudent(
            @PathVariable Integer studentId) {

        return quizAttemptService.getAttemptsByStudent(studentId);
    }

    @PostMapping
    public QuizAttempt createAttempt(
            @RequestBody QuizAttempt attempt) {

        return quizAttemptService.saveAttempt(attempt);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizAttempt> updateAttempt(
            @PathVariable Integer id,
            @RequestBody QuizAttempt attempt) {

        return quizAttemptService.getAttemptById(id)
                .map(existingAttempt -> {

                    existingAttempt.setQuizId(
                            attempt.getQuizId());

                    existingAttempt.setStudentId(
                            attempt.getStudentId());

                    existingAttempt.setScore(
                            attempt.getScore());

                    existingAttempt.setTotalMarks(
                            attempt.getTotalMarks());

                    existingAttempt.setStatus(
                            attempt.getStatus());

                    return ResponseEntity.ok(
                            quizAttemptService.saveAttempt(
                                    existingAttempt)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttempt(
            @PathVariable Integer id) {

        if (quizAttemptService.getAttemptById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        quizAttemptService.deleteAttempt(id);

        return ResponseEntity.noContent().build();
    }
}