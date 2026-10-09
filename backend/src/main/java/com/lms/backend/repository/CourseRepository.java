package com.lms.backend.repository;

import com.lms.backend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Integer> {

    List<Course> findByInstructorId(Integer instructorId);

    List<Course> findByStatus(String status);
}