package com.lms.backend.repository;

import com.lms.backend.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository
        extends JpaRepository<QuizAttempt, Integer> {

    List<QuizAttempt> findByQuizId(Integer quizId);

    List<QuizAttempt> findByStudentId(Integer studentId);

}