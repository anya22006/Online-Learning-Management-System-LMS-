package com.LMS_Student.repo;

import com.LMS_Student.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Integer> {

    List<Lesson> findByModuleIdOrderByLessonOrderAsc(Integer moduleId);
}