package com.lms.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, Long> {
    List<LessonCompletion> findByStudentId(Long studentId);
    List<LessonCompletion> findByStudentIdAndCourseId(Long studentId, Long courseId);
    int countByStudentIdAndCourseId(Long studentId, Long courseId);
    boolean existsByStudentIdAndCourseIdAndLessonId(Long studentId, Long courseId, Long lessonId);
}
