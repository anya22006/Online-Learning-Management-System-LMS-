package com.LMS_Student.service;

import com.LMS_Student.entity.Lesson;
import com.LMS_Student.repo.LessonRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;

    public LessonService(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    public List<Lesson> getLessonsByModule(Integer moduleId) {
        return lessonRepository.findByModuleIdOrderByLessonOrderAsc(moduleId);
    }

    public Lesson getLessonById(Integer lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() ->
                        new RuntimeException("Lesson not found"));
    }
}