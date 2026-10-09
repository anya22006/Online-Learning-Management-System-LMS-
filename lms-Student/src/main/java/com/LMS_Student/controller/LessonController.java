package com.LMS_Student.controller;

import com.LMS_Student.entity.Lesson;
import com.LMS_Student.service.LessonService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/lessons")
@CrossOrigin
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping("/module/{moduleId}")
    public List<Lesson> getLessonsByModule(
            @PathVariable Integer moduleId) {

        return lessonService.getLessonsByModule(moduleId);
    }

    @GetMapping("/{lessonId}")
    public Lesson getLessonById(
            @PathVariable Integer lessonId) {

        return lessonService.getLessonById(lessonId);
    }
}