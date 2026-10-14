package com.lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LmsApplication.class, args);
        System.out.println("=================================================");
        System.out.println("  LMS Server Active with SQLite (lms.db)         ");
        System.out.println("  Universal Login Portal: http://localhost:8080/login");
        System.out.println("  Student Dashboard:      http://localhost:8080/student/dashboard");
        System.out.println("  Instructor Dashboard:   http://localhost:8080/instructor/dashboard");
        System.out.println("  Admin Command Center:   http://localhost:8080/admin/dashboard");
        System.out.println("=================================================");
    }
}
