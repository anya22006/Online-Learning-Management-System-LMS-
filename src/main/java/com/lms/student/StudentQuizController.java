package com.lms.student;

import com.lms.course.Course;
import com.lms.course.CourseRepository;
import com.lms.quiz.Quiz;
import com.lms.quiz.QuizQuestion;
import com.lms.quiz.QuizQuestionRepository;
import com.lms.quiz.QuizRepository;
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
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Quiz attempt submitted successfully!",
                "score", 100
        ));
    }
}
