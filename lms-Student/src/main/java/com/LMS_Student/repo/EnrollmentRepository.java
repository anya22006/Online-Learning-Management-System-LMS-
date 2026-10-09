package com.LMS_Student.repo;

import com.LMS_Student.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {

    List<Enrollment> findByStudentId(Integer studentId);

    Optional<Enrollment> findByStudentIdAndCourseId(
            Integer studentId,
            Integer courseId
    );
}