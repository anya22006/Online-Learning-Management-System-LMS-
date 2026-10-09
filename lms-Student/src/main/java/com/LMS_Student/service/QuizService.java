package com.LMS_Student.service;

import com.LMS_Student.dto.StudentQuizResponse;
import com.LMS_Student.entity.Course;
import com.LMS_Student.entity.Enrollment;
import com.LMS_Student.entity.Quiz;
import com.LMS_Student.repo.CourseRepository;
import com.LMS_Student.repo.EnrollmentRepository;
import com.LMS_Student.repo.QuizRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public QuizService(
            QuizRepository quizRepository,
            EnrollmentRepository enrollmentRepository,
            CourseRepository courseRepository
    ) {
        this.quizRepository = quizRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
    }

    public List<StudentQuizResponse> getQuizzesForStudent(Integer studentId) {
        List<Integer> courseIds = enrollmentRepository.findByStudentId(studentId)
                .stream()
                .map(Enrollment::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (courseIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Quiz> quizzes = quizRepository.findByCourseIdInOrderByIdAsc(courseIds);
        if (quizzes.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, String> courseTitles = new HashMap<>();
        for (Course course : courseRepository.findAllById(courseIds)) {
            courseTitles.put(course.getId(), course.getTitle());
        }

        return quizzes.stream()
                .map(quiz -> new StudentQuizResponse(
                        quiz.getId(),
                        quiz.getCourseId(),
                        courseTitles.get(quiz.getCourseId()),
                        quiz.getTitle(),
                        quiz.getDescription()
                ))
                .toList();
    }
}
