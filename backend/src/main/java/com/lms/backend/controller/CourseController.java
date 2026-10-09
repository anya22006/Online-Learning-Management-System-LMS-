package com.lms.backend.controller;

import com.lms.backend.entity.Course;
import com.lms.backend.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "http://localhost:5173")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(
            @PathVariable Integer id) {

        return courseService.getCourseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/instructor/{instructorId}")
    public List<Course> getCoursesByInstructor(
            @PathVariable Integer instructorId) {

        return courseService.getCoursesByInstructor(instructorId);
    }

    @GetMapping("/status/{status}")
    public List<Course> getCoursesByStatus(
            @PathVariable String status) {

        return courseService.getCoursesByStatus(status);
    }

    @PostMapping
    public Course createCourse(@RequestBody Course course) {
        return courseService.saveCourse(course);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Integer id,
            @RequestBody Course course) {

        return courseService.getCourseById(id)
                .map(existingCourse -> {

                    existingCourse.setTitle(course.getTitle());
                    existingCourse.setDescription(course.getDescription());
                    existingCourse.setInstructorId(course.getInstructorId());
                    existingCourse.setStatus(course.getStatus());

                    return ResponseEntity.ok(
                            courseService.saveCourse(existingCourse)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Integer id) {

        if (courseService.getCourseById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}