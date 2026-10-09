package com.lms.web;

import com.lms.assignment.Assignment;
import com.lms.assignment.AssignmentRepository;
import com.lms.assignment.AssignmentService;
import com.lms.assignment.dto.AssignmentRequestDTO;
import com.lms.course.Course;
import com.lms.course.CourseRepository;
import com.lms.course.CourseService;
import com.lms.course.CourseStatus;
import com.lms.grading.GradeController;
import com.lms.grading.GradeService;
import com.lms.grading.Submission;
import com.lms.quiz.*;
import com.lms.quiz.dto.QuizQuestionDTO;
import com.lms.quiz.dto.QuizRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/instructor")
public class InstructorWebController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseService courseService;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private GradeService gradeService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Long instructorId = 2L;
        List<Course> courses = courseRepository.findByInstructorId(instructorId);
        List<Submission> submissions = gradeService.getAllSubmissions();
        List<QuizAttempt> attempts = quizAttemptRepository.findAll();

        model.addAttribute("totalCourses", courses.size());
        model.addAttribute("publishedCourses", courses.stream().filter(c -> c.getStatus() == CourseStatus.PUBLISHED).count());
        model.addAttribute("pendingSubmissions", submissions.size() + attempts.size());
        model.addAttribute("courses", courses);

        return "instructor-dashboard";
    }

    @GetMapping("/manage-courses")
    public String manageCourses(Model model) {
        Long instructorId = 2L;
        List<Course> courses = courseRepository.findByInstructorId(instructorId);
        List<Assignment> assignments = assignmentRepository.findAll();
        List<Quiz> quizzes = quizRepository.findAll();

        Map<Long, List<Assignment>> courseAssignments = new HashMap<>();
        Map<Long, List<Quiz>> courseQuizzes = new HashMap<>();

        for (Course c : courses) {
            courseAssignments.put(c.getCourseId(), new ArrayList<>());
            courseQuizzes.put(c.getCourseId(), new ArrayList<>());
        }

        for (Assignment a : assignments) {
            if (courseAssignments.containsKey(a.getCourseId())) {
                courseAssignments.get(a.getCourseId()).add(a);
            }
        }

        for (Quiz q : quizzes) {
            if (courseQuizzes.containsKey(q.getCourseId())) {
                courseQuizzes.get(q.getCourseId()).add(q);
            }
        }

        model.addAttribute("courses", courses);
        model.addAttribute("courseAssignments", courseAssignments);
        model.addAttribute("courseQuizzes", courseQuizzes);

        return "manage-courses";
    }

    @GetMapping("/create-course")
    public String createCoursePage() {
        return "create-course";
    }

    @PostMapping("/create-course")
    public String handleCreateCourse(
            @RequestParam String title,
            @RequestParam String category,
            @RequestParam String description,
            @RequestParam String syllabus) {
        Long instructorId = 2L;
        Course course = new Course(instructorId, title, category, description, syllabus, CourseStatus.DRAFT);
        courseRepository.save(course);
        return "redirect:/instructor/manage-courses";
    }

    @GetMapping("/create-assignment")
    public String createAssignmentPage(@RequestParam(required = false) Long courseId, Model model) {
        Long instructorId = 2L;
        List<Course> courses = courseRepository.findByInstructorId(instructorId);
        List<Assignment> assignments = assignmentRepository.findAll();
        List<Quiz> quizzes = quizRepository.findAll();

        model.addAttribute("courses", courses);
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("assignments", assignments);
        model.addAttribute("quizzes", quizzes);

        return "create-assignment";
    }

    @PostMapping("/create-assignment")
    public String handleCreateAssignment(
            @RequestParam Long courseId,
            @RequestParam String title,
            @RequestParam String instructions,
            @RequestParam(required = false) String dueDate,
            @RequestParam(defaultValue = "100") Integer maxScore) {
        Long instructorId = 2L;
        AssignmentRequestDTO dto = new AssignmentRequestDTO();
        dto.setCourseId(courseId);
        dto.setTitle(title);
        dto.setInstructions(instructions);
        dto.setMaxScore(maxScore);
        assignmentService.createAssignment(instructorId, dto);
        return "redirect:/instructor/create-assignment";
    }

    @GetMapping("/grade-submissions")
    public String gradeSubmissionsPage(Model model) {
        List<Submission> submissions = gradeService.getAllSubmissions();
        List<QuizAttempt> quizAttempts = quizAttemptRepository.findAll();

        model.addAttribute("submissions", submissions);
        model.addAttribute("quizAttempts", quizAttempts);

        return "grade-submissions";
    }

    @PostMapping("/grade-quiz-attempt")
    public String gradeQuizAttempt(
            @RequestParam Long attemptId,
            @RequestParam Integer score,
            @RequestParam Integer totalMarks) {
        quizAttemptRepository.findById(attemptId).ifPresent(attempt -> {
            attempt.setScore(score);
            attempt.setTotalMarks(totalMarks);
            if (totalMarks > 0) {
                double pct = ((double) score / totalMarks) * 100.0;
                attempt.setPercentage(Math.round(pct * 10.0) / 10.0);
            }
            quizAttemptRepository.save(attempt);
        });
        return "redirect:/instructor/grade-submissions";
    }
}
