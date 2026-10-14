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
import com.lms.student.Enrollment;
import com.lms.student.EnrollmentRepository;
import com.lms.student.StudentProfileRepository;
import com.lms.user.User;
import com.lms.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/student")
public class StudentWebController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

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
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityHelper securityHelper;

    private User getAuthenticatedStudent(HttpSession session) {
        if (!securityHelper.hasAnyRole(session, "STUDENT", "ADMIN")) {
            return null;
        }
        return securityHelper.getCurrentUser(session);
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User currentUser = getAuthenticatedStudent(session);
        if (currentUser == null) return "redirect:/login";

        Long studentId = currentUser.getId();
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<Course> enrolledCourses = new ArrayList<>();
        Set<Long> enrolledCourseIds = new HashSet<>();

        for (Enrollment e : enrollments) {
            enrolledCourseIds.add(e.getCourseId());
            courseRepository.findById(e.getCourseId()).ifPresent(enrolledCourses::add);
        }

        List<Assignment> allAssignments = assignmentRepository.findAll();
        List<Submission> studentSubmissions = submissionRepository.findByStudentId(studentId);
        Set<Long> submittedAssignmentIds = new HashSet<>();
        for (Submission s : studentSubmissions) {
            submittedAssignmentIds.add(s.getAssignmentId());
        }

        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentId(studentId);

        double totalPct = 0;
        int gradeCount = 0;
        for (QuizAttempt qa : attempts) {
            if (qa.getPercentage() != null) {
                totalPct += qa.getPercentage();
                gradeCount++;
            }
        }
        for (Submission s : studentSubmissions) {
            Optional<Grade> g = gradeRepository.findBySubmissionId(s.getSubmissionId());
            if (g.isPresent() && g.get().getMaxScore() > 0) {
                totalPct += ((double) g.get().getScore() / g.get().getMaxScore()) * 100.0;
                gradeCount++;
            }
        }
        double avgGrade = gradeCount > 0 ? Math.round((totalPct / gradeCount) * 10.0) / 10.0 : 100.0;

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("enrolledCount", enrollments.size());
        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("assignments", allAssignments);
        model.addAttribute("submittedAssignmentIds", submittedAssignmentIds);
        model.addAttribute("attempts", attempts);
        model.addAttribute("avgGrade", avgGrade);

        return "student-dashboard";
    }

    @GetMapping("/my-courses")
    public String myCourses(HttpSession session, Model model) {
        User currentUser = getAuthenticatedStudent(session);
        if (currentUser == null) return "redirect:/login";

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(currentUser.getId());
        List<Course> enrolledCourses = new ArrayList<>();
        for (Enrollment e : enrollments) {
            courseRepository.findById(e.getCourseId()).ifPresent(enrolledCourses::add);
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("enrolledCourses", enrolledCourses);
        return "my-courses";
    }

    @GetMapping("/catalog")
    public String courseCatalog(HttpSession session, Model model) {
        User currentUser = getAuthenticatedStudent(session);
        if (currentUser == null) return "redirect:/login";

        List<Course> publishedCourses = courseRepository.findByStatus(CourseStatus.PUBLISHED);
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(currentUser.getId());
        Set<Long> enrolledCourseIds = new HashSet<>();
        for (Enrollment e : enrollments) {
            enrolledCourseIds.add(e.getCourseId());
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("courses", publishedCourses);
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);
        return "course-catalog";
    }

    @PostMapping("/enroll")
    public String enrollInCourse(@RequestParam Long courseId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedStudent(session);
        if (currentUser == null) return "redirect:/login";

        if (!enrollmentRepository.existsByStudentIdAndCourseId(currentUser.getId(), courseId)) {
            enrollmentRepository.save(new Enrollment(currentUser.getId(), courseId));
            String courseTitle = courseRepository.findById(courseId).map(Course::getTitle).orElse("Course #" + courseId);
            securityHelper.logActivity(currentUser.getFullName(), "Course Enrollment", "Enrolled in " + courseTitle);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully enrolled in " + courseTitle + "!");
        } else {
            redirectAttributes.addFlashAttribute("infoMessage", "You are already enrolled in this course.");
        }

        return "redirect:/student/my-courses";
    }

    @GetMapping("/course/{courseId}")
    public String courseDetails(@PathVariable Long courseId, HttpSession session, Model model) {
        User currentUser = getAuthenticatedStudent(session);
        if (currentUser == null) return "redirect:/login";

        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return "redirect:/student/my-courses";

        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByLessonOrderAsc(courseId);
        boolean isEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(currentUser.getId(), courseId);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        model.addAttribute("isEnrolled", isEnrolled);
        return "course-details";
    }

    @GetMapping("/my-assignments")
    public String myAssignments(HttpSession session, Model model) {
        User currentUser = getAuthenticatedStudent(session);
        if (currentUser == null) return "redirect:/login";

        List<Assignment> assignments = assignmentRepository.findAll();
        List<Submission> submissions = submissionRepository.findByStudentId(currentUser.getId());
        Map<Long, Submission> submissionMap = new HashMap<>();
        Map<Long, Grade> gradeMap = new HashMap<>();

        for (Submission s : submissions) {
            submissionMap.put(s.getAssignmentId(), s);
            gradeRepository.findBySubmissionId(s.getSubmissionId()).ifPresent(g -> gradeMap.put(s.getAssignmentId(), g));
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("assignments", assignments);
        model.addAttribute("submissionMap", submissionMap);
        model.addAttribute("gradeMap", gradeMap);
        return "my-assignments";
    }

    @PostMapping("/submit-assignment")
    public String submitAssignment(@RequestParam Long assignmentId,
                                   @RequestParam String content,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedStudent(session);
        if (currentUser == null) return "redirect:/login";

        Optional<Submission> existing = submissionRepository.findByStudentIdAndAssignmentId(currentUser.getId(), assignmentId);
        Submission submission;
        if (existing.isPresent()) {
            submission = existing.get();
            submission.setContent(content);
            submission.setSubmittedAt(LocalDateTime.now());
        } else {
            submission = new Submission(assignmentId, currentUser.getId(), currentUser.getFullName(), content);
        }
        submissionRepository.save(submission);

        securityHelper.logActivity(currentUser.getFullName(), "Assignment Submission", "Submitted Assignment #" + assignmentId);
        redirectAttributes.addFlashAttribute("successMessage", "Assignment submitted successfully!");
        return "redirect:/student/my-assignments";
    }

    @GetMapping("/my-quizzes")
    public String myQuizzes(HttpSession session, Model model) {
        User currentUser = getAuthenticatedStudent(session);
        if (currentUser == null) return "redirect:/login";

        List<Quiz> quizzes = quizRepository.findAll();
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentId(currentUser.getId());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("attempts", attempts);
        return "my-quizzes";
    }

    @GetMapping("/take-quiz/{quizId}")
    public String takeQuiz(@PathVariable Long quizId, HttpSession session, Model model) {
        User currentUser = getAuthenticatedStudent(session);
        if (currentUser == null) return "redirect:/login";

        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quizId);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        return "take-quiz";
    }

    @PostMapping("/submit-quiz")
    public String submitQuiz(@RequestParam Long quizId,
                             @RequestParam Map<String, String> allParams,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedStudent(session);
        if (currentUser == null) return "redirect:/login";

        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quizId);

        int totalQuestions = questions.size();
        int totalPoints = 0;
        int earnedPoints = 0;

        for (QuizQuestion q : questions) {
            int qMarks = q.getMarks() != null ? q.getMarks() : 5;
            totalPoints += qMarks;

            String ansParam = allParams.get("q_" + q.getQuestionId());
            if (ansParam != null && q.getCorrectOption() != null && ansParam.trim().equalsIgnoreCase(q.getCorrectOption().trim())) {
                earnedPoints += qMarks;
            }
        }

        double percentage = totalPoints > 0 ? ((double) earnedPoints / totalPoints) * 100.0 : 100.0;
        double roundedPercentage = Math.round(percentage * 10.0) / 10.0;

        Long courseId = quizRepository.findById(quizId).map(Quiz::getCourseId).orElse(null);

        QuizAttempt attempt = new QuizAttempt(quizId, currentUser.getId(), currentUser.getFullName(), courseId, earnedPoints, totalQuestions, roundedPercentage);
        attempt.setTotalMarks(totalPoints > 0 ? totalPoints : 100);
        quizAttemptRepository.save(attempt);

        securityHelper.logActivity(currentUser.getFullName(), "Quiz Completed", "Scored " + earnedPoints + "/" + totalPoints + " (" + roundedPercentage + "%)");
        redirectAttributes.addFlashAttribute("quizResult", "Quiz submitted! You scored " + earnedPoints + " out of " + totalPoints + " (" + roundedPercentage + "%).");

        return "redirect:/student/my-quizzes";
    }

    @GetMapping("/my-grades")
    public String myGrades(HttpSession session, Model model) {
        User currentUser = getAuthenticatedStudent(session);
        if (currentUser == null) return "redirect:/login";

        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentId(currentUser.getId());
        List<Submission> submissions = submissionRepository.findByStudentId(currentUser.getId());

        List<Map<String, Object>> assignmentGradeRows = new ArrayList<>();
        for (Submission s : submissions) {
            Map<String, Object> row = new HashMap<>();
            row.put("submission", s);
            assignmentRepository.findById(s.getAssignmentId()).ifPresent(a -> row.put("assignment", a));
            Optional<Grade> gradeOpt = gradeRepository.findBySubmissionId(s.getSubmissionId());
            gradeOpt.ifPresent(grade -> row.put("grade", grade));
            assignmentGradeRows.add(row);
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("attempts", attempts);
        model.addAttribute("assignmentGradeRows", assignmentGradeRows);
        return "my-grades";
    }

    @GetMapping("/my-progress")
    public String myProgress(HttpSession session, Model model) {
        User currentUser = getAuthenticatedStudent(session);
        if (currentUser == null) return "redirect:/login";

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(currentUser.getId());
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentId(currentUser.getId());
        List<Submission> submissions = submissionRepository.findByStudentId(currentUser.getId());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("enrolledCount", enrollments.size());
        model.addAttribute("quizAttemptsCount", attempts.size());
        model.addAttribute("submissionsCount", submissions.size());
        return "my-progress";
    }

    @GetMapping("/settings")
    public String settings(HttpSession session, Model model) {
        User currentUser = getAuthenticatedStudent(session);
        if (currentUser == null) return "redirect:/login";

        model.addAttribute("currentUser", currentUser);
        return "settings";
    }

    @PostMapping("/settings")
    public String updateSettings(@RequestParam String fullName,
                                 @RequestParam(required = false) String department,
                                 @RequestParam(required = false) String password,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedStudent(session);
        if (currentUser == null) return "redirect:/login";

        currentUser.setFullName(fullName);
        if (department != null && !department.trim().isEmpty()) {
            currentUser.setDepartment(department.trim());
        }
        if (password != null && !password.trim().isEmpty()) {
            currentUser.setPassword(password.trim());
        }

        userRepository.save(currentUser);
        session.setAttribute("currentUser", currentUser);

        studentProfileRepository.findById(currentUser.getId()).ifPresent(sp -> {
            sp.setName(fullName);
            studentProfileRepository.save(sp);
        });

        redirectAttributes.addFlashAttribute("successMessage", "Profile settings updated successfully in SQLite!");
        return "redirect:/student/settings";
    }
}
