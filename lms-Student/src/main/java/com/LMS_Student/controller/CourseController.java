package com.LMS_Student.controller;

import com.LMS_Student.entity.Course;
import com.LMS_Student.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@CrossOrigin
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/courses")
    public List<Course> getActiveCourses() {
        return courseService.getActiveCourses();
    }
}
