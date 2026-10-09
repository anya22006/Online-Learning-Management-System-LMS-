package com.lms.grading;

import com.lms.grading.dto.GradeRequestDTO;
import com.lms.quiz.QuizAttempt;
import com.lms.quiz.QuizAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instructor")
@CrossOrigin(origins = "*")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @GetMapping("/submissions")
    public ResponseEntity<List<Submission>> getSubmissions() {
        return ResponseEntity.ok(gradeService.getAllSubmissions());
    }

    @GetMapping("/quiz-attempts")
    public ResponseEntity<List<QuizAttempt>> getQuizAttempts() {
        return ResponseEntity.ok(quizAttemptRepository.findAll());
    }

    @PostMapping("/submissions")
    public ResponseEntity<Submission> createSampleSubmission(@RequestBody Submission submission) {
        return ResponseEntity.ok(gradeService.createSampleSubmission(submission));
    }

    @PostMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<Grade> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long instructorId,
            @RequestBody GradeRequestDTO dto) {
        dto.setSubmissionId(submissionId);
        return ResponseEntity.ok(gradeService.gradeSubmission(instructorId, dto));
    }
}
