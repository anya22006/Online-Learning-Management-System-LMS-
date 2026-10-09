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
    public ResponseEntity<List<Map<String, Object>>> getQuizQuestions(@PathVariable Long quizId) {
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quizId);
        List<Map<String, Object>> response = new ArrayList<>();
        for (QuizQuestion q : questions) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("questionId", q.getQuestionId());
            map.put("quizId", q.getQuizId());
            map.put("questionText", q.getQuestionText());
            map.put("optionA", q.getOptionA() != null ? q.getOptionA() : "");
            map.put("optionB", q.getOptionB() != null ? q.getOptionB() : "");
            map.put("optionC", q.getOptionC() != null ? q.getOptionC() : "");
            map.put("optionD", q.getOptionD() != null ? q.getOptionD() : "");
            map.put("marks", q.getMarks() != null ? q.getMarks() : 5);
            map.put("questionType", q.getQuestionType() != null ? q.getQuestionType() : "MULTIPLE_CHOICE");
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/quiz-attempts")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> submitQuizAttempt(@RequestBody Map<String, Object> payload) {
        Long quizId = payload.get("quizId") != null ? Long.valueOf(payload.get("quizId").toString()) : 1L;
        Long studentId = payload.get("studentId") != null ? Long.valueOf(payload.get("studentId").toString()) : 1L;
        String studentName = payload.get("studentName") != null ? payload.get("studentName").toString() : "Alex Johnson";

        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quizId);
        Map<String, Object> answers = (Map<String, Object>) payload.get("answers");

        int correctAnswers = 0;
        int totalQuestions = questions.size();
        int totalPoints = 0;
        int earnedPoints = 0;

        for (QuizQuestion q : questions) {
            int qMarks = q.getMarks() != null ? q.getMarks() : 5;
            totalPoints += qMarks;

            String qIdStr = String.valueOf(q.getQuestionId());
            String studentAns = answers != null && answers.get(qIdStr) != null ? answers.get(qIdStr).toString() : null;

            if (studentAns != null && q.getCorrectOption() != null && studentAns.trim().equalsIgnoreCase(q.getCorrectOption().trim())) {
                correctAnswers++;
                earnedPoints += qMarks;
            }
        }

        double percentage = totalPoints > 0 ? ((double) earnedPoints / totalPoints) * 100.0 : (totalQuestions > 0 ? ((double) correctAnswers / totalQuestions) * 100.0 : 100.0);

        Long courseId = null;
        Integer quizTotalMarks = 100;
        Optional<Quiz> quizOpt = quizRepository.findById(quizId);
        if (quizOpt.isPresent()) {
            courseId = quizOpt.get().getCourseId();
            if (quizOpt.get().getTotalMarks() != null) {
                quizTotalMarks = quizOpt.get().getTotalMarks();
            }
        }
        if (totalPoints > 0) {
            quizTotalMarks = totalPoints;
        }

        double roundedPercentage = Math.round(percentage * 10.0) / 10.0;
        QuizAttempt attempt = new QuizAttempt(quizId, studentId, studentName, courseId, earnedPoints, totalQuestions, roundedPercentage);
        attempt.setTotalMarks(quizTotalMarks);
        QuizAttempt saved = quizAttemptRepository.save(attempt);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Quiz attempt submitted successfully!",
                "attemptId", saved.getId(),
                "score", (int) Math.round(percentage),
                "earnedPoints", earnedPoints,
                "totalPoints", totalPoints,
                "correctAnswers", correctAnswers,
                "totalQuestions", totalQuestions,
                "percentage", saved.getPercentage(),
                "attemptedAt", saved.getSubmittedAt().toString()
        ));
    }

    @GetMapping("/quiz-attempts/{studentId}")
    public ResponseEntity<List<QuizAttempt>> getStudentQuizAttempts(@PathVariable Long studentId) {
        return ResponseEntity.ok(quizAttemptRepository.findByStudentId(studentId));
    }
}
