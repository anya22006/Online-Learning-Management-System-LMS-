package com.lms.backend.controller;

import com.lms.backend.entity.Lesson;
import com.lms.backend.service.LessonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@CrossOrigin(origins = "http://localhost:5173")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping
    public List<Lesson> getAllLessons() {
        return lessonService.getAllLessons();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lesson> getLessonById(
            @PathVariable Integer id) {

        return lessonService.getLessonById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/module/{moduleId}")
    public List<Lesson> getLessonsByModule(
            @PathVariable Integer moduleId) {

        return lessonService.getLessonsByModule(moduleId);
    }

    @PostMapping
    public Lesson createLesson(
            @RequestBody Lesson lesson) {

        return lessonService.saveLesson(lesson);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lesson> updateLesson(
            @PathVariable Integer id,
            @RequestBody Lesson lesson) {

        return lessonService.getLessonById(id)
                .map(existingLesson -> {

                    existingLesson.setModuleId(lesson.getModuleId());
                    existingLesson.setTitle(lesson.getTitle());
                    existingLesson.setContent(lesson.getContent());
                    existingLesson.setVideoUrl(lesson.getVideoUrl());
                    existingLesson.setLessonOrder(lesson.getLessonOrder());

                    return ResponseEntity.ok(
                            lessonService.saveLesson(existingLesson)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable Integer id) {

        if (lessonService.getLessonById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        lessonService.deleteLesson(id);

        return ResponseEntity.noContent().build();
    }
}