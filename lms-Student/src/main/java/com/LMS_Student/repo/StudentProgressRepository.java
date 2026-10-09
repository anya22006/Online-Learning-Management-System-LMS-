package com.LMS_Student.repo;

import com.LMS_Student.entity.StudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentProgressRepository
        extends JpaRepository<StudentProgress, Integer> {

    List<StudentProgress> findByStudentId(
            Integer studentId
    );

    Optional<StudentProgress>
    findByStudentIdAndCourseId(
            Integer studentId,
            Integer courseId
    );
}