package com.lms.backend.controller;

import com.lms.backend.entity.StudentProgress;
import com.lms.backend.service.StudentProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentProgressController {

    private final StudentProgressService progressService;

    public StudentProgressController(
            StudentProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping
    public List<StudentProgress> getAllProgress() {
        return progressService.getAllProgress();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentProgress> getProgressById(
            @PathVariable Integer id) {

        return progressService.getProgressById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    public List<StudentProgress> getProgressByStudent(
            @PathVariable Integer studentId) {

        return progressService.getProgressByStudent(studentId);
    }

    @GetMapping("/course/{courseId}")
    public List<StudentProgress> getProgressByCourse(
            @PathVariable Integer courseId) {

        return progressService.getProgressByCourse(courseId);
    }

    @GetMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<StudentProgress> getStudentCourseProgress(
            @PathVariable Integer studentId,
            @PathVariable Integer courseId) {

        return progressService
                .getStudentCourseProgress(studentId, courseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public StudentProgress createProgress(
            @RequestBody StudentProgress progress) {

        return progressService.saveProgress(progress);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentProgress> updateProgress(
            @PathVariable Integer id,
            @RequestBody StudentProgress progress) {

        return progressService.getProgressById(id)
                .map(existingProgress -> {

                    existingProgress.setStudentId(
                            progress.getStudentId());

                    existingProgress.setCourseId(
                            progress.getCourseId());

                    existingProgress.setCompletedLessons(
                            progress.getCompletedLessons());

                    existingProgress.setTotalLessons(
                            progress.getTotalLessons());

                    existingProgress.setPercentage(
                            progress.getPercentage());

                    return ResponseEntity.ok(
                            progressService.saveProgress(
                                    existingProgress)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgress(
            @PathVariable Integer id) {

        if (progressService.getProgressById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        progressService.deleteProgress(id);

        return ResponseEntity.noContent().build();
    }
}