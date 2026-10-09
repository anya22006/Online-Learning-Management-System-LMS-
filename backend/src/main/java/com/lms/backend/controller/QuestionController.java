package com.lms.backend.controller;

import com.lms.backend.entity.Question;
import com.lms.backend.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "http://localhost:5173")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public List<Question> getAllQuestions() {
        return questionService.getAllQuestions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Question> getQuestionById(
            @PathVariable Integer id) {

        return questionService.getQuestionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/quiz/{quizId}")
    public List<Question> getQuestionsByQuiz(
            @PathVariable Integer quizId) {

        return questionService.getQuestionsByQuiz(quizId);
    }

    @PostMapping
    public Question createQuestion(
            @RequestBody Question question) {

        return questionService.saveQuestion(question);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Question> updateQuestion(
            @PathVariable Integer id,
            @RequestBody Question question) {

        return questionService.getQuestionById(id)
                .map(existingQuestion -> {

                    existingQuestion.setQuizId(
                            question.getQuizId());

                    existingQuestion.setQuestion(
                            question.getQuestion());

                    existingQuestion.setOptionA(
                            question.getOptionA());

                    existingQuestion.setOptionB(
                            question.getOptionB());

                    existingQuestion.setOptionC(
                            question.getOptionC());

                    existingQuestion.setOptionD(
                            question.getOptionD());

                    existingQuestion.setCorrectAnswer(
                            question.getCorrectAnswer());

                    existingQuestion.setMarks(
                            question.getMarks());

                    return ResponseEntity.ok(
                            questionService.saveQuestion(
                                    existingQuestion)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Integer id) {

        if (questionService.getQuestionById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        questionService.deleteQuestion(id);

        return ResponseEntity.noContent().build();
    }
}