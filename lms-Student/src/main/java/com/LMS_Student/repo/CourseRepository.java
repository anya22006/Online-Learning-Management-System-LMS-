package com.LMS_Student.repo;

import com.LMS_Student.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Integer> {

    List<Course> findByStatus(String status);
}
