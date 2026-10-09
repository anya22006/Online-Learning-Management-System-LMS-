package com.lms.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructorId(Long instructorId);
    List<Course> findByInstructorIdAndStatus(Long instructorId, CourseStatus status);
    long countByStatus(CourseStatus status);
}
