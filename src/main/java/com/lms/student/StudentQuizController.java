package com.lms.student;

import com.lms.course.Course;
import com.lms.course.CourseRepository;
import com.lms.quiz.*;
import com.lms.student.dto.StudentQuizDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentQuizController {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping({"/quizzes", "/quizzes/{studentId}"})
    public ResponseEntity<List<StudentQuizDTO>> getStudentQuizzes(@PathVariable(required = false) Long studentId) {
        List<Quiz> quizzes = quizRepository.findAll();
        List<StudentQuizDTO> result = new ArrayList<>();

        for (Quiz q : quizzes) {
            StudentQuizDTO dto = new StudentQuizDTO();
            dto.setQuizId(q.getQuizId());
            dto.setCourseId(q.getCourseId());
            dto.setTitle(q.getTitle());
            dto.setDescription(q.getInstructions());
            dto.setTimeLimitMinutes(q.getTimeLimitMinutes());

            if (q.getCourseId() != null) {
                Optional<Course> courseOpt = courseRepository.findById(q.getCourseId());
                courseOpt.ifPresent(c -> dto.setCourseTitle(c.getTitle()));
            }
            result.add(dto);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/quizzes/{quizId}/questions")
    public ResponseEntity<List<QuizQuestion>> getQuizQuestions(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizQuestionRepository.findByQuizId(quizId));
    }

    @PostMapping("/quiz-attempts")
    public ResponseEntity<Map<String, Object>> submitQuizAttempt(@RequestBody Map<String, Object> payload) {
        Long quizId = payload.get("quizId") != null ? Long.valueOf(payload.get("quizId").toString()) : 1L;
        Long studentId = payload.get("studentId") != null ? Long.valueOf(payload.get("studentId").toString()) : 1L;
        String studentName = payload.get("studentName") != null ? payload.get("studentName").toString() : "Alex Johnson";
        Integer score = payload.get("score") != null ? Integer.valueOf(payload.get("score").toString()) : 100;
        Integer totalQuestions = payload.get("totalQuestions") != null ? Integer.valueOf(payload.get("totalQuestions").toString()) : 5;
        Double percentage = totalQuestions > 0 ? ((double) score / totalQuestions) * 100 : 100.0;

        Long courseId = null;
        Optional<Quiz> quizOpt = quizRepository.findById(quizId);
        if (quizOpt.isPresent()) {
            courseId = quizOpt.get().getCourseId();
        }

        QuizAttempt attempt = new QuizAttempt(quizId, studentId, studentName, courseId, score, totalQuestions, percentage);
        QuizAttempt saved = quizAttemptRepository.save(attempt);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Quiz attempt submitted successfully!",
                "attemptId", saved.getId(),
                "score", saved.getScore(),
                "percentage", saved.getPercentage()
        ));
    }

    @GetMapping("/quiz-attempts/{studentId}")
    public ResponseEntity<List<QuizAttempt>> getStudentQuizAttempts(@PathVariable Long studentId) {
        return ResponseEntity.ok(quizAttemptRepository.findByStudentId(studentId));
    }
}
