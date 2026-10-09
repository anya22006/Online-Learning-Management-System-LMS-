package com.LMS_Student.controller;

import com.LMS_Student.dto.LessonCompletionResponse;
import com.LMS_Student.entity.StudentProgress;
import com.LMS_Student.service.StudentProgressService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/progress")
@CrossOrigin
public class StudentProgressController {

    private final StudentProgressService progressService;


    public StudentProgressController(
            StudentProgressService progressService) {

        this.progressService =
                progressService;
    }


    // ========================================
    // GET ALL STUDENT PROGRESS
    // ========================================

    @GetMapping("/{studentId}")
    public List<StudentProgress> getStudentProgress(
            @PathVariable Integer studentId) {

        return progressService
                .getStudentProgress(studentId);
    }


    // ========================================
    // GET COURSE PROGRESS
    // ========================================

    @GetMapping("/{studentId}/course/{courseId}")
    public StudentProgress getCourseProgress(
            @PathVariable Integer studentId,
            @PathVariable Integer courseId) {

        return progressService
                .getCourseProgress(
                        studentId,
                        courseId
                );
    }


    // ========================================
    // UPDATE COURSE PROGRESS
    // ========================================

    @PutMapping("/{studentId}/course/{courseId}")
    public StudentProgress updateProgress(
            @PathVariable Integer studentId,
            @PathVariable Integer courseId,
            @RequestParam Integer completedLessons,
            @RequestParam Integer totalLessons) {

        return progressService.updateProgress(
                studentId,
                courseId,
                completedLessons,
                totalLessons
        );
    }

    @PostMapping("/{studentId}/course/{courseId}/lesson/{lessonId}/complete")
    public LessonCompletionResponse completeLesson(
            @PathVariable Integer studentId,
            @PathVariable Integer courseId,
            @PathVariable Integer lessonId) {

        return progressService.completeLesson(
                studentId,
                courseId,
                lessonId
        );
    }
}