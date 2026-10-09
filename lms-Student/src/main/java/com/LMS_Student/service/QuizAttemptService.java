package com.LMS_Student.service;

import com.LMS_Student.dto.QuizAttemptRequest;
import com.LMS_Student.dto.QuizAttemptResponse;
import com.LMS_Student.entity.Enrollment;
import com.LMS_Student.entity.Question;
import com.LMS_Student.entity.Quiz;
import com.LMS_Student.entity.QuizAttempt;
import com.LMS_Student.repo.EnrollmentRepository;
import com.LMS_Student.repo.QuestionRepository;
import com.LMS_Student.repo.QuizAttemptRepository;
import com.LMS_Student.repo.QuizRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class QuizAttemptService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final EnrollmentRepository enrollmentRepository;

    public QuizAttemptService(
            QuizRepository quizRepository,
            QuestionRepository questionRepository,
            QuizAttemptRepository quizAttemptRepository,
            EnrollmentRepository enrollmentRepository
    ) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public QuizAttemptResponse submitAttempt(QuizAttemptRequest request) {
        if (request == null || request.getQuizId() == null || request.getQuizId() <= 0
                || request.getStudentId() == null || request.getStudentId() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid quiz ID and student ID are required."
            );
        }

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Quiz not found."
                ));

        boolean isEnrolled = enrollmentRepository
                .findByStudentIdAndCourseId(request.getStudentId(), quiz.getCourseId())
                .map(Enrollment::getId)
                .isPresent();
        if (!isEnrolled) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You must be enrolled in the course to attempt this quiz."
            );
        }

        List<Question> questions = questionRepository.findByQuizIdOrderByIdAsc(quiz.getId());
        if (questions.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This quiz does not have any questions yet."
            );
        }

        Map<Integer, String> answers = request.getAnswers();
        Set<Integer> quizQuestionIds = new HashSet<>();
        questions.forEach(question -> quizQuestionIds.add(question.getId()));
        if (answers != null && answers.keySet().stream().anyMatch(id -> !quizQuestionIds.contains(id))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Answers contain a question that does not belong to this quiz."
            );
        }

        int correctAnswers = 0;
        for (Question question : questions) {
            String answer = answers == null ? null : answers.get(question.getId());
            if (answer != null && answer.trim().equalsIgnoreCase(question.getCorrectOption())) {
                correctAnswers++;
            }
        }

        BigDecimal score = BigDecimal.valueOf(correctAnswers)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(questions.size()), 2, RoundingMode.HALF_UP);
        LocalDateTime attemptedAt = LocalDateTime.now();

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId(quiz.getId());
        attempt.setStudentId(request.getStudentId());
        attempt.setScore(score);
        attempt.setAttemptedAt(attemptedAt);
        quizAttemptRepository.save(attempt);

        return new QuizAttemptResponse(
                quiz.getId(),
                score,
                questions.size(),
                correctAnswers,
                attemptedAt
        );
    }
}
