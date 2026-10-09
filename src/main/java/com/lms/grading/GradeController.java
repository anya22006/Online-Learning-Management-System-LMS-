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

    @PostMapping("/quiz-attempts/{attemptId}/grade")
    public ResponseEntity<QuizAttempt> gradeQuizAttempt(
            @PathVariable Long attemptId,
            @RequestBody java.util.Map<String, Object> payload) {
        return quizAttemptRepository.findById(attemptId).map(attempt -> {
            if (payload.get("score") != null) {
                attempt.setScore(Integer.parseInt(payload.get("score").toString()));
            }
            if (payload.get("totalMarks") != null) {
                attempt.setTotalMarks(Integer.parseInt(payload.get("totalMarks").toString()));
            }
            if (payload.get("percentage") != null) {
                attempt.setPercentage(Double.parseDouble(payload.get("percentage").toString()));
            } else if (attempt.getTotalMarks() != null && attempt.getTotalMarks() > 0 && attempt.getScore() != null) {
                double pct = ((double) attempt.getScore() / attempt.getTotalMarks()) * 100.0;
                attempt.setPercentage(Math.round(pct * 10.0) / 10.0);
            }
            QuizAttempt updated = quizAttemptRepository.save(attempt);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/quiz-attempts/{attemptId}")
    public ResponseEntity<QuizAttempt> updateQuizAttempt(
            @PathVariable Long attemptId,
            @RequestBody java.util.Map<String, Object> payload) {
        return gradeQuizAttempt(attemptId, payload);
    }
}
