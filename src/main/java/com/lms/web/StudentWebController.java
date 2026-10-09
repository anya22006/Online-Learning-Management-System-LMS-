package com.lms.web;

import com.lms.assignment.Assignment;
import com.lms.assignment.AssignmentRepository;
import com.lms.course.Course;
import com.lms.course.CourseRepository;
import com.lms.course.CourseStatus;
import com.lms.grading.Submission;
import com.lms.grading.GradeService;
import com.lms.quiz.*;
import com.lms.student.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/student")
public class StudentWebController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private GradeService gradeService;

    private final Long currentStudentId = 1L;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(currentStudentId);
        List<Course> enrolledCourses = new ArrayList<>();

        for (Enrollment e : enrollments) {
            courseRepository.findById(e.getCourseId()).ifPresent(enrolledCourses::add);
        }

        List<Assignment> assignments = assignmentRepository.findAll();
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentId(currentStudentId);

        StudentProfile student = studentProfileRepository.findById(currentStudentId).orElse(null);

        model.addAttribute("student", student);
        model.addAttribute("enrolledCount", enrollments.size());
        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("assignments", assignments);
        model.addAttribute("attempts", attempts);

        return "student-dashboard";
    }

    @GetMapping("/my-courses")
    public String myCourses(Model model) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(currentStudentId);
        List<Course> enrolledCourses = new ArrayList<>();

        for (Enrollment e : enrollments) {
            courseRepository.findById(e.getCourseId()).ifPresent(enrolledCourses::add);
        }

        model.addAttribute("enrolledCourses", enrolledCourses);
        return "my-courses";
    }

    @GetMapping("/my-assignments")
    public String myAssignments(Model model) {
        List<Assignment> assignments = assignmentRepository.findAll();
        model.addAttribute("assignments", assignments);
        return "my-assignments";
    }

    @GetMapping("/my-quizzes")
    public String myQuizzes(Model model) {
        List<Quiz> quizzes = quizRepository.findAll();
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentId(currentStudentId);

        model.addAttribute("quizzes", quizzes);
        model.addAttribute("attempts", attempts);
        return "my-quizzes";
    }

    @GetMapping("/take-quiz/{quizId}")
    public String takeQuiz(@PathVariable Long quizId, Model model) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quizId);

        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        return "take-quiz";
    }

    @PostMapping("/submit-quiz")
    public String submitQuiz(
            @RequestParam Long quizId,
            @RequestParam Map<String, String> allParams) {
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quizId);

        int correctAnswers = 0;
        int totalQuestions = questions.size();
        int totalPoints = 0;
        int earnedPoints = 0;

        for (QuizQuestion q : questions) {
            int qMarks = q.getMarks() != null ? q.getMarks() : 5;
            totalPoints += qMarks;

            String ansParam = allParams.get("q_" + q.getQuestionId());
            if (ansParam != null && q.getCorrectOption() != null && ansParam.trim().equalsIgnoreCase(q.getCorrectOption().trim())) {
                correctAnswers++;
                earnedPoints += qMarks;
            }
        }

        double percentage = totalPoints > 0 ? ((double) earnedPoints / totalPoints) * 100.0 : 100.0;
        double roundedPercentage = Math.round(percentage * 10.0) / 10.0;

        Long courseId = quizRepository.findById(quizId).map(Quiz::getCourseId).orElse(null);

        QuizAttempt attempt = new QuizAttempt(quizId, currentStudentId, "Alex Johnson", courseId, earnedPoints, totalQuestions, roundedPercentage);
        attempt.setTotalMarks(totalPoints > 0 ? totalPoints : 100);
        quizAttemptRepository.save(attempt);

        return "redirect:/student/my-quizzes";
    }

    @GetMapping("/my-grades")
    public String myGrades(Model model) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentId(currentStudentId);
        List<Submission> submissions = gradeService.getAllSubmissions();

        model.addAttribute("attempts", attempts);
        model.addAttribute("submissions", submissions);
        return "my-grades";
    }

    @GetMapping("/my-progress")
    public String myProgress(Model model) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(currentStudentId);
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentId(currentStudentId);

        model.addAttribute("enrolledCount", enrollments.size());
        model.addAttribute("quizAttemptsCount", attempts.size());

        return "my-progress";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        StudentProfile student = studentProfileRepository.findById(currentStudentId).orElse(null);
        model.addAttribute("student", student);
        return "settings";
    }
}
