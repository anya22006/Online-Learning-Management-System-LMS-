package com.lms.backend.service;

import com.lms.backend.entity.QuizAttempt;
import com.lms.backend.repository.QuizAttemptRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuizAttemptService {

    private final QuizAttemptRepository quizAttemptRepository;

    public QuizAttemptService(QuizAttemptRepository quizAttemptRepository) {
        this.quizAttemptRepository = quizAttemptRepository;
    }

    public List<QuizAttempt> getAllAttempts() {
        return quizAttemptRepository.findAll();
    }

    public Optional<QuizAttempt> getAttemptById(Integer id) {
        return quizAttemptRepository.findById(id);
    }

    public List<QuizAttempt> getAttemptsByQuiz(Integer quizId) {
        return quizAttemptRepository.findByQuizId(quizId);
    }

    public List<QuizAttempt> getAttemptsByStudent(Integer studentId) {
        return quizAttemptRepository.findByStudentId(studentId);
    }

    public QuizAttempt saveAttempt(QuizAttempt attempt) {
        return quizAttemptRepository.save(attempt);
    }

    public void deleteAttempt(Integer id) {
        quizAttemptRepository.deleteById(id);
    }
}