package com.lms.backend.repository;

import com.lms.backend.entity.StudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentProgressRepository
        extends JpaRepository<StudentProgress, Integer> {

    List<StudentProgress> findByStudentId(Integer studentId);

    List<StudentProgress> findByCourseId(Integer courseId);

    Optional<StudentProgress> findByStudentIdAndCourseId(
            Integer studentId,
            Integer courseId
    );
}