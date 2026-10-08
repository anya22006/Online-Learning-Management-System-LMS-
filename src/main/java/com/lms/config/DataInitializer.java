package com.lms.config;

import com.lms.course.Course;
import com.lms.course.CourseRepository;
import com.lms.course.CourseStatus;
import com.lms.grading.Submission;
import com.lms.grading.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Override
    public void run(String... args) throws Exception {
        if (courseRepository.count() == 0) {
            Course c1 = new Course(2L, "Full Stack Web Development with Java", "Web Dev", "Master Java, Spring Boot, MySQL, and Frontend technologies.", "Week 1: Java Basics\nWeek 2: Spring Boot REST APIs\nWeek 3: Database & JPA\nWeek 4: Frontend Integration", CourseStatus.PUBLISHED);
            Course c2 = new Course(2L, "Data Structures & Algorithms in Java", "Computer Science", "Deep dive into Arrays, Trees, Graphs, and Dynamic Programming.", "Module 1: Linear Data Structures\nModule 2: Non-Linear Data Structures\nModule 3: Algorithm Optimization", CourseStatus.DRAFT);
            courseRepository.save(c1);
            courseRepository.save(c2);
            System.out.println("Initialized sample courses.");
        }

        if (submissionRepository.count() == 0) {
            Submission s1 = new Submission(101L, 301L, "Alex Johnson", "Here is my submission for Assignment 1: Java Spring Boot REST API setup with Controllers and Services.");
            Submission s2 = new Submission(101L, 302L, "Sophia Martinez", "Completed Assignment 1. Source code hosted on GitHub repo. All endpoints verified with Postman.");
            submissionRepository.save(s1);
            submissionRepository.save(s2);
            System.out.println("Initialized sample student submissions.");
        }
    }
}
