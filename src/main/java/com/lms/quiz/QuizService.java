package com.lms.quiz;

import com.lms.course.Course;
import com.lms.course.CourseRepository;
import com.lms.quiz.dto.QuizQuestionDTO;
import com.lms.quiz.dto.QuizRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Transactional
    public Quiz createQuiz(Long instructorId, QuizRequestDTO dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + dto.getCourseId()));

        if (!course.getInstructorId().equals(instructorId)) {
            throw new SecurityException("Forbidden: You do not own this course");
        }

        Quiz quiz = new Quiz(
                dto.getCourseId(),
                dto.getTitle(),
                dto.getInstructions(),
                dto.getTimeLimitMinutes() != null ? dto.getTimeLimitMinutes() : 30,
                dto.getTotalMarks() != null ? dto.getTotalMarks() : 100
        );

        Quiz savedQuiz = quizRepository.save(quiz);

        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
            for (QuizQuestionDTO qDto : dto.getQuestions()) {
                QuizQuestion question = new QuizQuestion(
                        savedQuiz.getQuizId(),
                        qDto.getQuestionText(),
                        qDto.getOptionA(),
                        qDto.getOptionB(),
                        qDto.getOptionC(),
                        qDto.getOptionD(),
                        qDto.getCorrectOption(),
                        qDto.getMarks() != null ? qDto.getMarks() : 5,
                        qDto.getQuestionType() != null ? qDto.getQuestionType() : "MULTIPLE_CHOICE"
                );
                quizQuestionRepository.save(question);
            }
        }

        return savedQuiz;
    }

    public List<Quiz> getQuizzesByCourse(Long courseId) {
        return quizRepository.findByCourseId(courseId);
    }

    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    public List<QuizQuestion> getQuestionsForQuiz(Long quizId) {
        return quizQuestionRepository.findByQuizId(quizId);
    }

    @Transactional
    public void deleteQuiz(Long quizId) {
        quizQuestionRepository.deleteByQuizId(quizId);
        quizRepository.deleteById(quizId);
    }
}
