package com.lms.config;

import com.lms.admin.ActivityLog;
import com.lms.admin.ActivityLogRepository;
import com.lms.admin.PlatformSetting;
import com.lms.admin.PlatformSettingRepository;
import com.lms.assignment.Assignment;
import com.lms.assignment.AssignmentRepository;
import com.lms.course.Course;
import com.lms.course.CourseRepository;
import com.lms.course.CourseStatus;
import com.lms.grading.Grade;
import com.lms.grading.GradeRepository;
import com.lms.grading.Submission;
import com.lms.grading.SubmissionRepository;
import com.lms.instructor.InstructorProfile;
import com.lms.instructor.InstructorProfileRepository;
import com.lms.lesson.Lesson;
import com.lms.lesson.LessonRepository;
import com.lms.quiz.Quiz;
import com.lms.quiz.QuizAttempt;
import com.lms.quiz.QuizAttemptRepository;
import com.lms.quiz.QuizQuestion;
import com.lms.quiz.QuizQuestionRepository;
import com.lms.quiz.QuizRepository;
import com.lms.student.Enrollment;
import com.lms.student.EnrollmentRepository;
import com.lms.student.StudentProfile;
import com.lms.student.StudentProfileRepository;
import com.lms.user.User;
import com.lms.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private InstructorProfileRepository instructorProfileRepository;

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
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private PlatformSettingRepository platformSettingRepository;

    @Override
    public void run(String... args) throws Exception {
        // 1. Initialize Default Users if not present
        User studentUser;
        User instructorUser;
        User adminUser;

        if (userRepository.count() == 0) {
            studentUser = userRepository.save(new User("student@lms.com", "student123", "Alex Johnson", "STUDENT", "Computer Science"));
            instructorUser = userRepository.save(new User("instructor@lms.com", "instructor123", "Dr. Sarah Jenkins", "INSTRUCTOR", "Computer Science & Engineering"));
            adminUser = userRepository.save(new User("admin@lms.com", "admin123", "System Administrator", "ADMIN", "Administration"));
            System.out.println("Initialized SQLite default users: student@lms.com, instructor@lms.com, admin@lms.com");
        } else {
            studentUser = userRepository.findByEmail("student@lms.com").orElse(null);
            instructorUser = userRepository.findByEmail("instructor@lms.com").orElse(null);
            adminUser = userRepository.findByEmail("admin@lms.com").orElse(null);
        }

        Long studentId = studentUser != null ? studentUser.getId() : 1L;
        Long instructorId = instructorUser != null ? instructorUser.getId() : 2L;

        // 2. Sync Profiles
        if (studentProfileRepository.count() == 0) {
            studentProfileRepository.save(new StudentProfile(studentId, "Alex Johnson", "student@lms.com", "Student", "Active"));
        }
        if (instructorProfileRepository.count() == 0) {
            instructorProfileRepository.save(new InstructorProfile(instructorId, "Dr. Sarah Jenkins", "instructor@lms.com", "Computer Science & Engineering", "Instructor", "Active"));
        }

        // 2b. Initialize Second Student (Emily Davis) for distinct student dashboard demonstration
        User emilyUser = userRepository.findByEmail("emily@lms.com").orElse(null);
        if (emilyUser == null) {
            emilyUser = userRepository.save(new User("emily@lms.com", "student123", "Emily Davis", "STUDENT", "Information Technology"));
            studentProfileRepository.save(new StudentProfile(emilyUser.getId(), "Emily Davis", "emily@lms.com", "Student", "Active"));
            System.out.println("Initialized second student: emily@lms.com");
        }

        // 3. Initialize Courses
        Course c1;
        Course c2;
        Course c3;
        if (courseRepository.count() == 0) {
            c1 = courseRepository.save(new Course(instructorId, "Full Stack Web Development with Java", "Web Dev",
                    "Master modern Java, Spring Boot, SQLite, and responsive full-stack dynamic architecture.",
                    "Week 1: Java Basics\nWeek 2: Spring Boot REST APIs\nWeek 3: Database & SQLite JPA\nWeek 4: Frontend UI & Security",
                    CourseStatus.PUBLISHED));

            c2 = courseRepository.save(new Course(instructorId, "Data Structures & Algorithms in Java", "Computer Science",
                    "Deep dive into Arrays, Trees, Graphs, Sorting algorithms, and Dynamic Programming.",
                    "Module 1: Linear Data Structures\nModule 2: Trees & Graphs\nModule 3: Algorithm Optimization",
                    CourseStatus.PUBLISHED));

            c3 = courseRepository.save(new Course(instructorId, "Database Systems & SQL Mastery", "Databases",
                    "Understand relational modeling, SQLite internal structures, indexing, and transactional isolation.",
                    "Part 1: Relational Algebra & SQL\nPart 2: Transactions & ACID\nPart 3: SQLite Query Optimization",
                    CourseStatus.PUBLISHED));
            System.out.println("Initialized sample courses.");
        } else {
            c1 = courseRepository.findAll().get(0);
            c2 = courseRepository.findAll().size() > 1 ? courseRepository.findAll().get(1) : c1;
            c3 = courseRepository.findAll().size() > 2 ? courseRepository.findAll().get(2) : c1;
        }

        // 4. Initialize Lessons
        if (lessonRepository.count() == 0 && c1 != null) {
            lessonRepository.save(new Lesson(c1.getCourseId(), "Introduction to Spring Boot 3", "https://spring.io/projects/spring-boot", 1));
            lessonRepository.save(new Lesson(c1.getCourseId(), "Building RESTful Web Services", "https://spring.io/guides/gs/rest-service/", 2));
            lessonRepository.save(new Lesson(c1.getCourseId(), "Connecting SQLite via JPA and Hibernate", "https://sqlite.org/docs.html", 3));
        }

        // 5. Initialize Enrollments
        if (enrollmentRepository.count() == 0 && c1 != null && c2 != null) {
            enrollmentRepository.save(new Enrollment(studentId, c1.getCourseId()));
            enrollmentRepository.save(new Enrollment(studentId, c2.getCourseId()));
        }
        if (emilyUser != null && c3 != null && !enrollmentRepository.existsByStudentIdAndCourseId(emilyUser.getId(), c3.getCourseId())) {
            enrollmentRepository.save(new Enrollment(emilyUser.getId(), c3.getCourseId()));
        }

        // 6. Initialize Assignments
        Assignment a1;
        if (assignmentRepository.count() == 0 && c1 != null) {
            a1 = assignmentRepository.save(new Assignment(c1.getCourseId(), "Spring Boot REST API Implementation",
                    "Build a RESTful controller with endpoints for managing student submissions. Verify with SQLite database persistence.",
                    LocalDateTime.now().plusDays(7), 100));

            assignmentRepository.save(new Assignment(c2.getCourseId(), "Binary Search Tree Traversal Project",
                    "Implement in-order, pre-order, and post-order depth-first traversal of a binary search tree in Java.",
                    LocalDateTime.now().plusDays(10), 100));
        } else {
            a1 = assignmentRepository.findAll().isEmpty() ? null : assignmentRepository.findAll().get(0);
        }

        // 7. Initialize Quizzes & Questions
        if (quizRepository.count() == 0 && c1 != null) {
            Quiz q1 = quizRepository.save(new Quiz(c1.getCourseId(), "Java & Spring Boot Core Quiz",
                    "Answer all multiple choice questions to test your knowledge of Spring Boot & SQLite architecture.", 20, 100));

            quizQuestionRepository.save(new QuizQuestion(q1.getQuizId(),
                    "What does the @RestController annotation combine in Spring Boot?",
                    "@Controller and @ResponseBody",
                    "@Service and @Repository",
                    "@Component and @Configuration",
                    "@Bean and @Autowired",
                    "A", 25, "MULTIPLE_CHOICE"));

            quizQuestionRepository.save(new QuizQuestion(q1.getQuizId(),
                    "Which database engine is embedded, serverless, and stores the database in a single disk file?",
                    "MySQL Server",
                    "PostgreSQL",
                    "SQLite",
                    "Oracle Database",
                    "C", 25, "MULTIPLE_CHOICE"));

            quizQuestionRepository.save(new QuizQuestion(q1.getQuizId(),
                    "Which Spring Data JPA interface provides built-in CRUD operations and pagination?",
                    "JpaRepository",
                    "DataRepositoryHelper",
                    "JdbcTemplate",
                    "EntityManagerFactory",
                    "A", 25, "MULTIPLE_CHOICE"));

            quizQuestionRepository.save(new QuizQuestion(q1.getQuizId(),
                    "What is Thymeleaf primarily used for in Spring Boot applications?",
                    "Server-Side HTML Template Rendering",
                    "NoSQL Document Storage",
                    "Message Queuing and Streaming",
                    "JVM Garbage Collection Profiling",
                    "A", 25, "MULTIPLE_CHOICE"));

            // Sample completed quiz attempt for Alex Johnson
            quizAttemptRepository.save(new QuizAttempt(q1.getQuizId(), studentId, "Alex Johnson", c1.getCourseId(), 100, 4, 100.0));
        }

        // 8. Initialize Submissions & Grades
        if (submissionRepository.count() == 0 && a1 != null) {
            Submission s1 = submissionRepository.save(new Submission(a1.getAssignmentId(), studentId, "Alex Johnson",
                    "Here is my completed submission: Configured SQLite JDBC datasource, built REST controllers with JPA repositories, and verified full CRUD operations."));

            gradeRepository.save(new Grade(s1.getSubmissionId(), instructorId, 95, 100, "Excellent submission! Clean architecture, proper separation of concerns, and SQLite runs seamlessly."));
        }

        // 9. Initialize Platform Settings & Activity Logs
        if (platformSettingRepository.count() == 0) {
            platformSettingRepository.save(new PlatformSetting("SITE_NAME", "Modern LMS Platform"));
            platformSettingRepository.save(new PlatformSetting("ALLOW_REGISTRATION", "true"));
            platformSettingRepository.save(new PlatformSetting("ACTIVE_DATABASE", "SQLite (lms.db)"));
        }

        if (activityLogRepository.count() == 0) {
            activityLogRepository.save(new ActivityLog("System", "System Startup", "SQLite database initialized with default seed data"));
            activityLogRepository.save(new ActivityLog("Admin", "Platform Configured", "Unified login enabled for Student, Instructor, and Admin"));
        }
    }
}
