package com.LMS_Student.service;

import com.LMS_Student.dto.LessonCompletionResponse;
import com.LMS_Student.entity.ActivityLog;
import com.LMS_Student.entity.Lesson;
import com.LMS_Student.entity.Module;
import com.LMS_Student.entity.StudentProgress;
import com.LMS_Student.repo.ActivityLogRepository;
import com.LMS_Student.repo.LessonRepository;
import com.LMS_Student.repo.ModuleRepository;
import com.LMS_Student.repo.StudentProgressRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentProgressService {

    private static final String LESSON_COMPLETED_ACTION = "LESSON_COMPLETED";

    private final StudentProgressRepository progressRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;


    public StudentProgressService(
            StudentProgressRepository progressRepository,
            ActivityLogRepository activityLogRepository,
            ModuleRepository moduleRepository,
            LessonRepository lessonRepository) {

        this.progressRepository = progressRepository;
        this.activityLogRepository = activityLogRepository;
        this.moduleRepository = moduleRepository;
        this.lessonRepository = lessonRepository;
    }


    // ========================================
    // GET ALL PROGRESS OF STUDENT
    // ========================================

    public List<StudentProgress> getStudentProgress(
            Integer studentId) {

        return progressRepository
                .findByStudentId(studentId);
    }


    // ========================================
    // GET PROGRESS OF ONE COURSE
    // ========================================

    public StudentProgress getCourseProgress(
            Integer studentId,
            Integer courseId) {

        return progressRepository
                .findByStudentIdAndCourseId(
                        studentId,
                        courseId
                )
                .orElseThrow(
                        () -> new RuntimeException(
                                "Progress not found"
                        )
                );
    }


    // ========================================
    // UPDATE PROGRESS
    // ========================================

    public StudentProgress updateProgress(
            Integer studentId,
            Integer courseId,
            Integer completedLessons,
            Integer totalLessons) {

        int safeTotalLessons = totalLessons == null
                ? 0
                : Math.max(0, totalLessons);
        int safeCompletedLessons = completedLessons == null
                ? 0
                : Math.max(0, Math.min(completedLessons, safeTotalLessons));

        StudentProgress progress =
                progressRepository
                        .findByStudentIdAndCourseId(
                                studentId,
                                courseId
                        )
                        .orElseGet(
                                StudentProgress::new
                        );


        progress.setStudentId(studentId);

        progress.setCourseId(courseId);

        progress.setCompletedLessons(
                safeCompletedLessons
        );

        progress.setTotalLessons(
                safeTotalLessons
        );


        // ====================================
        // CALCULATE PERCENTAGE
        // ====================================

        BigDecimal percentage =
                BigDecimal.ZERO;


        if (
            safeTotalLessons > 0
        ) {

            percentage =
                    BigDecimal
                            .valueOf(
                                safeCompletedLessons
                            )
                            .multiply(
                                BigDecimal.valueOf(100)
                            )
                            .divide(
                                BigDecimal.valueOf(
                                    safeTotalLessons
                                ),
                                2,
                                RoundingMode.HALF_UP
                            );


            // Don't allow progress above 100%

            if (
                percentage.compareTo(
                    BigDecimal.valueOf(100)
                ) > 0
            ) {

                percentage =
                        BigDecimal.valueOf(100);
            }
        }


        progress.setProgressPercentage(
                percentage
        );


        // ====================================
        // LAST ACCESSED
        // ====================================

        progress.setLastAccessed(
                LocalDateTime.now()
        );


        return progressRepository.save(
                progress
        );
    }

    @Transactional
    public synchronized LessonCompletionResponse completeLesson(
            Integer studentId,
            Integer courseId,
            Integer lessonId) {

        if (studentId == null || studentId <= 0
                || courseId == null || courseId <= 0
                || lessonId == null || lessonId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid student, course, and lesson ID are required."
            );
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lesson not found."
                ));

        Module lessonModule = moduleRepository.findById(lesson.getModuleId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "The lesson module was not found."
                ));

        if (!courseId.equals(lessonModule.getCourseId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The lesson does not belong to the specified course."
            );
        }

        List<Module> modules =
                moduleRepository.findByCourseIdOrderByModuleOrderAsc(courseId);
        int totalLessons = modules.stream()
                .mapToInt(module -> lessonRepository
                        .findByModuleIdOrderByLessonOrderAsc(module.getId())
                        .size())
                .sum();

        String completionDescription =
                "courseId=" + courseId + ";lessonId=" + lessonId;

        StudentProgress progress = progressRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElseGet(() -> newProgress(studentId, courseId));

        boolean alreadyCompleted = activityLogRepository
                .existsByUserIdAndActionAndDescription(
                        studentId,
                        LESSON_COMPLETED_ACTION,
                        completionDescription
                );

        if (alreadyCompleted) {
            if (progress.getId() == null) {
                long recordedCompletions = activityLogRepository
                        .countByUserIdAndActionAndDescriptionStartingWith(
                                studentId,
                                LESSON_COMPLETED_ACTION,
                                "courseId=" + courseId + ";lessonId="
                        );
                progress = updateProgress(
                        studentId,
                        courseId,
                        (int) Math.min(recordedCompletions, totalLessons),
                        totalLessons
                );
            }
            return new LessonCompletionResponse(progress, true);
        }

        int currentCompletedLessons = progress.getCompletedLessons() == null
                ? 0
                : progress.getCompletedLessons();

        StudentProgress updatedProgress = updateProgress(
                studentId,
                courseId,
                Math.min(currentCompletedLessons + 1, totalLessons),
                totalLessons
        );

        ActivityLog activityLog = new ActivityLog();
        activityLog.setUserId(studentId);
        activityLog.setAction(LESSON_COMPLETED_ACTION);
        activityLog.setDescription(completionDescription);
        activityLog.setCreatedAt(LocalDateTime.now());
        activityLogRepository.save(activityLog);

        return new LessonCompletionResponse(updatedProgress, false);
    }

    private StudentProgress newProgress(Integer studentId, Integer courseId) {
        StudentProgress progress = new StudentProgress();
        progress.setStudentId(studentId);
        progress.setCourseId(courseId);
        progress.setCompletedLessons(0);
        progress.setTotalLessons(0);
        progress.setProgressPercentage(BigDecimal.ZERO);
        return progress;
    }
}