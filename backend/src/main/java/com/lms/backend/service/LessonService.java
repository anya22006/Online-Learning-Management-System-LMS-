package com.lms.backend.service;

import com.lms.backend.entity.Lesson;
import com.lms.backend.repository.LessonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;

    public LessonService(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    public List<Lesson> getAllLessons() {
        return lessonRepository.findAll();
    }

    public Optional<Lesson> getLessonById(Integer id) {
        return lessonRepository.findById(id);
    }

    public List<Lesson> getLessonsByModule(Integer moduleId) {
        return lessonRepository.findByModuleId(moduleId);
    }

    public Lesson saveLesson(Lesson lesson) {
        return lessonRepository.save(lesson);
    }

    public void deleteLesson(Integer id) {
        lessonRepository.deleteById(id);
    }
}