package com.lms.backend.repository;

import com.lms.backend.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {

    List<Enrollment> findByStudentId(Integer studentId);

    List<Enrollment> findByCourseId(Integer courseId);

    Optional<Enrollment> findByStudentIdAndCourseId(
            Integer studentId,
            Integer courseId
    );
}