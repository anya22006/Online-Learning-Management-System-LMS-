package com.LMS_Student.service;

import com.LMS_Student.entity.Course;
import com.LMS_Student.repo.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Course> getActiveCourses() {
        return courseRepository.findByStatus("active");
    }
}