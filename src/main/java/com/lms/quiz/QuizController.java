package com.lms.quiz;

import com.lms.quiz.dto.QuizRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instructor/quizzes")
@CrossOrigin(origins = "*")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @PostMapping
    public ResponseEntity<Quiz> createQuiz(
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long instructorId,
            @RequestBody QuizRequestDTO dto) {
        return ResponseEntity.ok(quizService.createQuiz(instructorId, dto));
    }

    @GetMapping
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Quiz>> getQuizzesByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(quizService.getQuizzesByCourse(courseId));
    }

    @GetMapping("/{quizId}/questions")
    public ResponseEntity<List<QuizQuestion>> getQuestionsForQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuestionsForQuiz(quizId));
    }
}
