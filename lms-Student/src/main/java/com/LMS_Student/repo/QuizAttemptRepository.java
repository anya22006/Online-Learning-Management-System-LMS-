package com.LMS_Student.repo;

import com.LMS_Student.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Integer> {
}
