package com.lms;

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
import com.lms.student.Enrollment;
import com.lms.student.EnrollmentRepository;
import com.lms.student.StudentProfile;
import com.lms.student.StudentProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class LmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LmsApplication.class, args);
        System.out.println("=================================================");
        System.out.println("  LMS Backend Server is Running!                 ");
        System.out.println("  Admin Dashboard:      http://localhost:8080/admin-dashboard.html");
        System.out.println("  Instructor Dashboard: http://localhost:8080/instructor-dashboard.html");
        System.out.println("  Student Dashboard:    http://localhost:8080/student-dashboard.html");
        System.out.println("=================================================");
    }

    @Bean
    public CommandLineRunner initDatabase(
            CourseRepository courseRepository,
            StudentProfileRepository studentProfileRepository,
            InstructorProfileRepository instructorProfileRepository,
            EnrollmentRepository enrollmentRepository,
            AssignmentRepository assignmentRepository,
            SubmissionRepository submissionRepository,
            GradeRepository gradeRepository
    ) {
        return args -> {
            // Seed Instructor
            if (!instructorProfileRepository.existsById(2L)) {
                instructorProfileRepository.save(new InstructorProfile(2L, "Dr. Sarah Jenkins", "sarah.jenkins@lms.com", "Computer Science", "Instructor", "Active"));
            }

            // Seed Students
            if (!studentProfileRepository.existsById(1L)) {
                studentProfileRepository.save(new StudentProfile(1L, "Alex Johnson", "alex.johnson@student.lms.com", "Student", "Active"));
            }
            if (!studentProfileRepository.existsById(2L)) {
                studentProfileRepository.save(new StudentProfile(2L, "Emily Davis", "emily.davis@student.lms.com", "Student", "Active"));
            }

            // Seed Courses if empty
            if (courseRepository.count() == 0) {
                Course c1 = new Course(2L, "Advanced Java Spring Boot & Microservices", "Web Development", "Master enterprise Spring Boot architecture.", "Module 1: REST API design", CourseStatus.PUBLISHED);
                Course c2 = new Course(2L, "Full-Stack Web Development with React", "Computer Science", "Build modern full-stack web applications.", "Module 1: React Fundamentals", CourseStatus.PUBLISHED);
                Course c3 = new Course(2L, "Python for Data Science & Machine Learning", "Data Science", "Learn Python, Pandas, and AI algorithms.", "Module 1: Data Analysis", CourseStatus.PUBLISHED);
                
                courseRepository.save(c1);
                courseRepository.save(c2);
                courseRepository.save(c3);

                // Seed Initial Enrollments
                if (c1.getCourseId() != null) {
                    enrollmentRepository.save(new Enrollment(1L, c1.getCourseId()));
                    enrollmentRepository.save(new Enrollment(2L, c1.getCourseId()));
                }
                if (c2.getCourseId() != null) {
                    enrollmentRepository.save(new Enrollment(1L, c2.getCourseId()));
                }

                // Seed Assignment
                if (c1.getCourseId() != null) {
                    Assignment assignment = new Assignment(c1.getCourseId(), "Build REST API for E-Commerce", "Implement Spring Data JPA and REST controllers.", LocalDateTime.now().plusDays(7), 100);
                    assignmentRepository.save(assignment);

                    // Seed Submission & Grade
                    Submission sub = new Submission(assignment.getAssignmentId(), 1L, "Alex Johnson", "https://github.com/alex/ecommerce-api");
                    submissionRepository.save(sub);

                    Grade grade = new Grade(sub.getSubmissionId(), 2L, 95, 100, "Excellent REST controller design and clean structure!");
                    gradeRepository.save(grade);
                }
            }
        };
    }
}
