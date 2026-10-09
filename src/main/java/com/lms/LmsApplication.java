package com.lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(LmsApplication.class, args);
        System.out.println("=================================================");
        System.out.println("  LMS Instructor Module Backend is Running!     ");
        System.out.println("  Access Dashboard: http://localhost:8080        ");
        System.out.println("  Access H2 Console: http://localhost:8080/h2-console");
        System.out.println("=================================================");
    }
}
