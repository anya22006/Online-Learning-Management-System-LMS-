package com.LMS_Student.repo;

import com.LMS_Student.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findByQuizIdOrderByIdAsc(Integer quizId);
}
