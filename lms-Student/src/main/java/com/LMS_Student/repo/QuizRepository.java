package com.LMS_Student.repo;

import com.LMS_Student.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Integer> {

    List<Quiz> findByCourseIdInOrderByIdAsc(List<Integer> courseIds);
}
