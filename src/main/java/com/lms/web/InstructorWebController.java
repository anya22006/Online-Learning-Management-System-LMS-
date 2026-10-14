package com.lms.web;

import com.lms.assignment.Assignment;
import com.lms.assignment.AssignmentRepository;
import com.lms.common.SecurityHelper;
import com.lms.course.Course;
import com.lms.course.CourseRepository;
import com.lms.course.CourseStatus;
import com.lms.grading.Grade;
import com.lms.grading.GradeRepository;
import com.lms.grading.Submission;
import com.lms.grading.SubmissionRepository;
import com.lms.lesson.Lesson;
import com.lms.lesson.LessonRepository;
import com.lms.quiz.*;
import com.lms.user.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/instructor")
public class InstructorWebController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private SecurityHelper securityHelper;

    private User getAuthenticatedInstructor(HttpSession session) {
        if (!securityHelper.hasAnyRole(session, "INSTRUCTOR", "ADMIN")) {
            return null;
        }
        return securityHelper.getCurrentUser(session);
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User currentUser = getAuthenticatedInstructor(session);
        if (currentUser == null) return "redirect:/login";

        Long instructorId = currentUser.getId();
        List<Course> courses = courseRepository.findByInstructorId(instructorId);
        if (courses.isEmpty() && "ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            courses = courseRepository.findAll();
        }

        List<Submission> allSubmissions = submissionRepository.findAll();
        List<QuizAttempt> allAttempts = quizAttemptRepository.findAll();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("totalCourses", courses.size());
        model.addAttribute("publishedCourses", courses.stream().filter(c -> c.getStatus() == CourseStatus.PUBLISHED).count());
        model.addAttribute("pendingSubmissions", allSubmissions.size() + allAttempts.size());
        model.addAttribute("courses", courses);

        return "instructor-dashboard";
    }

    @GetMapping("/manage-courses")
    public String manageCourses(HttpSession session, Model model) {
        User currentUser = getAuthenticatedInstructor(session);
        if (currentUser == null) return "redirect:/login";

        Long instructorId = currentUser.getId();
        List<Course> courses = courseRepository.findByInstructorId(instructorId);
        if (courses.isEmpty() && "ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            courses = courseRepository.findAll();
        }

        List<Assignment> assignments = assignmentRepository.findAll();
        List<Quiz> quizzes = quizRepository.findAll();
        List<Lesson> lessons = lessonRepository.findAll();

        Map<Long, List<Assignment>> courseAssignments = new HashMap<>();
        Map<Long, List<Quiz>> courseQuizzes = new HashMap<>();
        Map<Long, List<Lesson>> courseLessons = new HashMap<>();

        for (Course c : courses) {
            courseAssignments.put(c.getCourseId(), new ArrayList<>());
            courseQuizzes.put(c.getCourseId(), new ArrayList<>());
            courseLessons.put(c.getCourseId(), new ArrayList<>());
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

        for (Lesson l : lessons) {
            if (courseLessons.containsKey(l.getCourseId())) {
                courseLessons.get(l.getCourseId()).add(l);
            }
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("courses", courses);
        model.addAttribute("courseAssignments", courseAssignments);
        model.addAttribute("courseQuizzes", courseQuizzes);
        model.addAttribute("courseLessons", courseLessons);

        return "manage-courses";
    }

    @GetMapping("/create-course")
    public String createCoursePage(HttpSession session, Model model) {
        User currentUser = getAuthenticatedInstructor(session);
        if (currentUser == null) return "redirect:/login";

        model.addAttribute("currentUser", currentUser);
        return "create-course";
    }

    @PostMapping("/create-course")
    public String handleCreateCourse(@RequestParam String title,
                                     @RequestParam String category,
                                     @RequestParam String description,
                                     @RequestParam(required = false) String syllabus,
                                     @RequestParam(value = "publishNow", required = false) String publishNow,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedInstructor(session);
        if (currentUser == null) return "redirect:/login";

        CourseStatus status = "true".equalsIgnoreCase(publishNow) ? CourseStatus.PUBLISHED : CourseStatus.DRAFT;
        Course course = new Course(currentUser.getId(), title, category, description, syllabus, status);
        courseRepository.save(course);

        securityHelper.logActivity(currentUser.getFullName(), "Created Course", "Created course: " + title + " in SQLite");
        redirectAttributes.addFlashAttribute("successMessage", "Course '" + title + "' created successfully!");

        return "redirect:/instructor/manage-courses";
    }

    @PostMapping("/course/{courseId}/toggle-status")
    public String toggleCourseStatus(@PathVariable Long courseId,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedInstructor(session);
        if (currentUser == null) return "redirect:/login";

        courseRepository.findById(courseId).ifPresent(c -> {
            if (c.getStatus() == CourseStatus.PUBLISHED) {
                c.setStatus(CourseStatus.DRAFT);
            } else {
                c.setStatus(CourseStatus.PUBLISHED);
            }
            courseRepository.save(c);
            securityHelper.logActivity(currentUser.getFullName(), "Updated Course Status", c.getTitle() + " -> " + c.getStatus());
            redirectAttributes.addFlashAttribute("successMessage", "Course status updated to " + c.getStatus());
        });

        return "redirect:/instructor/manage-courses";
    }

    @PostMapping("/course/{courseId}/delete")
    public String deleteCourse(@PathVariable Long courseId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedInstructor(session);
        if (currentUser == null) return "redirect:/login";

        courseRepository.findById(courseId).ifPresent(c -> {
            courseRepository.delete(c);
            securityHelper.logActivity(currentUser.getFullName(), "Deleted Course", "Deleted course: " + c.getTitle());
            redirectAttributes.addFlashAttribute("successMessage", "Course deleted successfully.");
        });

        return "redirect:/instructor/manage-courses";
    }

    @PostMapping("/course/{courseId}/add-lesson")
    public String addLesson(@PathVariable Long courseId,
                            @RequestParam String title,
                            @RequestParam String contentUrl,
                            @RequestParam(defaultValue = "1") Integer lessonOrder,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedInstructor(session);
        if (currentUser == null) return "redirect:/login";

        Lesson lesson = new Lesson(courseId, title, contentUrl, lessonOrder);
        lessonRepository.save(lesson);

        securityHelper.logActivity(currentUser.getFullName(), "Added Lesson", "Added lesson '" + title + "' to course #" + courseId);
        redirectAttributes.addFlashAttribute("successMessage", "Lesson added successfully!");

        return "redirect:/instructor/manage-courses";
    }

    @GetMapping("/create-assignment")
    public String createAssignmentPage(@RequestParam(required = false) Long courseId,
                                       HttpSession session,
                                       Model model) {
        User currentUser = getAuthenticatedInstructor(session);
        if (currentUser == null) return "redirect:/login";

        List<Course> courses = courseRepository.findByInstructorId(currentUser.getId());
        if (courses.isEmpty() && "ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            courses = courseRepository.findAll();
        }

        List<Assignment> assignments = assignmentRepository.findAll();
        List<Quiz> quizzes = quizRepository.findAll();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("courses", courses);
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("assignments", assignments);
        model.addAttribute("quizzes", quizzes);

        return "create-assignment";
    }

    @PostMapping("/create-assignment")
    public String handleCreateAssignment(@RequestParam Long courseId,
                                         @RequestParam String title,
                                         @RequestParam String instructions,
                                         @RequestParam(required = false) String dueDate,
                                         @RequestParam(defaultValue = "100") Integer maxScore,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedInstructor(session);
        if (currentUser == null) return "redirect:/login";

        Assignment assignment = new Assignment(courseId, title, instructions, LocalDateTime.now().plusDays(7), maxScore);
        assignmentRepository.save(assignment);

        securityHelper.logActivity(currentUser.getFullName(), "Created Assignment", "Created assignment: " + title);
        redirectAttributes.addFlashAttribute("successMessage", "Assignment created successfully in SQLite!");

        return "redirect:/instructor/create-assignment";
    }

    @PostMapping("/create-quiz")
    public String handleCreateQuiz(@RequestParam Long courseId,
                                   @RequestParam String title,
                                   @RequestParam String instructions,
                                   @RequestParam(defaultValue = "30") Integer timeLimitMinutes,
                                   @RequestParam(defaultValue = "100") Integer totalMarks,
                                   @RequestParam String questionText,
                                   @RequestParam String optionA,
                                   @RequestParam String optionB,
                                   @RequestParam String optionC,
                                   @RequestParam String optionD,
                                   @RequestParam String correctOption,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedInstructor(session);
        if (currentUser == null) return "redirect:/login";

        Quiz quiz = new Quiz(courseId, title, instructions, timeLimitMinutes, totalMarks);
        Quiz savedQuiz = quizRepository.save(quiz);

        QuizQuestion q = new QuizQuestion(savedQuiz.getQuizId(), questionText, optionA, optionB, optionC, optionD, correctOption, totalMarks, "MULTIPLE_CHOICE");
        quizQuestionRepository.save(q);

        securityHelper.logActivity(currentUser.getFullName(), "Created Quiz", "Created quiz: " + title);
        redirectAttributes.addFlashAttribute("successMessage", "Quiz with questions created successfully!");

        return "redirect:/instructor/create-assignment";
    }

    @GetMapping("/grade-submissions")
    public String gradeSubmissionsPage(HttpSession session, Model model) {
        User currentUser = getAuthenticatedInstructor(session);
        if (currentUser == null) return "redirect:/login";

        List<Submission> submissions = submissionRepository.findAll();
        List<QuizAttempt> quizAttempts = quizAttemptRepository.findAll();

        List<Map<String, Object>> submissionRows = new ArrayList<>();
        for (Submission s : submissions) {
            Map<String, Object> row = new HashMap<>();
            row.put("submission", s);
            assignmentRepository.findById(s.getAssignmentId()).ifPresent(a -> row.put("assignment", a));
            gradeRepository.findBySubmissionId(s.getSubmissionId()).ifPresent(g -> row.put("grade", g));
            submissionRows.add(row);
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("submissionRows", submissionRows);
        model.addAttribute("quizAttempts", quizAttempts);

        return "grade-submissions";
    }

    @PostMapping("/grade-submission")
    public String gradeStudentSubmission(@RequestParam Long submissionId,
                                         @RequestParam Integer score,
                                         @RequestParam(defaultValue = "100") Integer maxScore,
                                         @RequestParam(required = false) String feedback,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedInstructor(session);
        if (currentUser == null) return "redirect:/login";

        Grade grade = gradeRepository.findBySubmissionId(submissionId).orElseGet(Grade::new);
        grade.setSubmissionId(submissionId);
        grade.setInstructorId(currentUser.getId());
        grade.setScore(score);
        grade.setMaxScore(maxScore);
        grade.setFeedback(feedback);
        grade.setGradedAt(LocalDateTime.now());
        gradeRepository.save(grade);

        securityHelper.logActivity(currentUser.getFullName(), "Graded Submission", "Graded submission #" + submissionId + " (" + score + "/" + maxScore + ")");
        redirectAttributes.addFlashAttribute("successMessage", "Grade & feedback submitted successfully into SQLite!");

        return "redirect:/instructor/grade-submissions";
    }

    @PostMapping("/grade-quiz-attempt")
    public String gradeQuizAttempt(@RequestParam Long attemptId,
                                   @RequestParam Integer score,
                                   @RequestParam Integer totalMarks,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedInstructor(session);
        if (currentUser == null) return "redirect:/login";

        quizAttemptRepository.findById(attemptId).ifPresent(attempt -> {
            attempt.setScore(score);
            attempt.setTotalMarks(totalMarks);
            if (totalMarks > 0) {
                double pct = ((double) score / totalMarks) * 100.0;
                attempt.setPercentage(Math.round(pct * 10.0) / 10.0);
            }
            quizAttemptRepository.save(attempt);
            securityHelper.logActivity(currentUser.getFullName(), "Updated Quiz Attempt Grade", "Quiz Attempt #" + attemptId);
        });

        redirectAttributes.addFlashAttribute("successMessage", "Quiz attempt score updated.");
        return "redirect:/instructor/grade-submissions";
    }
}
