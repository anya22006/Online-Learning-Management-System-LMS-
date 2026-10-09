package com.LMS_Student.service;

import com.LMS_Student.dto.StudentQuestionResponse;
import com.LMS_Student.entity.Question;
import com.LMS_Student.repo.QuestionRepository;
import com.LMS_Student.repo.QuizRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class QuestionService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    public QuestionService(
            QuizRepository quizRepository,
            QuestionRepository questionRepository
    ) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    public List<StudentQuestionResponse> getQuestionsForQuiz(Integer quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found.");
        }

        return questionRepository.findByQuizIdOrderByIdAsc(quizId)
                .stream()
                .map(this::toStudentQuestion)
                .toList();
    }

    private StudentQuestionResponse toStudentQuestion(Question question) {
        return new StudentQuestionResponse(
                question.getId(),
                question.getQuestionText(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD()
        );
    }
}
