package com.lms.backend.service;

import com.lms.backend.entity.StudentProgress;
import com.lms.backend.repository.StudentProgressRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentProgressService {

    private final StudentProgressRepository progressRepository;

    public StudentProgressService(
            StudentProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
    }

    public List<StudentProgress> getAllProgress() {
        return progressRepository.findAll();
    }

    public Optional<StudentProgress> getProgressById(Integer id) {
        return progressRepository.findById(id);
    }

    public List<StudentProgress> getProgressByStudent(
            Integer studentId) {

        return progressRepository.findByStudentId(studentId);
    }

    public List<StudentProgress> getProgressByCourse(
            Integer courseId) {

        return progressRepository.findByCourseId(courseId);
    }

    public Optional<StudentProgress> getStudentCourseProgress(
            Integer studentId,
            Integer courseId) {

        return progressRepository.findByStudentIdAndCourseId(
                studentId,
                courseId
        );
    }

    public StudentProgress saveProgress(
            StudentProgress progress) {

        return progressRepository.save(progress);
    }

    public void deleteProgress(Integer id) {
        progressRepository.deleteById(id);
    }
}