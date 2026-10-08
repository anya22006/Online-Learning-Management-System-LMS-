# 🚀 LMS Backend Architecture & API Guide

This directory contains the comprehensive documentation, architecture overview, and REST API specification for the **Online Learning Management System (LMS) - Instructor Module** backend.

---

## 🛠️ Technology Stack

- **Framework**: Spring Boot `3.2.3`
- **Language**: Java 17
- **Database**: MySQL (`lms_db` on `localhost:3306`)
- **ORM / Data Access**: Spring Data JPA & Hibernate
- **Build Tool**: Apache Maven (`pom.xml`)
- **Port**: `8080`

---

## 📁 Source Code Organization (`src/main/java/com/lms`)

```
com.lms
├── LmsApplication.java           # Spring Boot Main Entry Point
├── assignment                    # Assignment Module
│   ├── Assignment.java           # Entity Model
│   ├── AssignmentController.java # REST Controller
│   ├── AssignmentRepository.java # JPA Repository
│   ├── AssignmentService.java    # Business Logic
│   └── dto/
│       └── AssignmentRequestDTO.java
├── course                        # Course Module
│   ├── Course.java               # Entity Model
│   ├── CourseController.java     # REST Controller
│   ├── CourseRepository.java     # JPA Repository
│   ├── CourseService.java        # Business Logic
│   ├── CourseStatus.java         # Enum (DRAFT, PUBLISHED)
│   └── dto/
│       └── CourseRequestDTO.java
├── quiz                          # Quiz Module
│   ├── Quiz.java                 # Quiz Entity
│   ├── QuizQuestion.java         # Quiz Question Entity
│   ├── QuizController.java       # REST Controller
│   ├── QuizRepository.java       # JPA Quiz Repository
│   ├── QuizQuestionRepository.java # JPA Question Repository
│   ├── QuizService.java          # Business Logic
│   └── dto/
│       ├── QuizRequestDTO.java
│       └── QuizQuestionDTO.java
├── grading                       # Submissions & Grading Module
│   ├── Submission.java           # Submission Entity
│   ├── GradeController.java      # REST Controller
│   ├── GradeRepository.java      # JPA Grade Repository
│   ├── SubmissionRepository.java # JPA Submission Repository
│   ├── GradeService.java         # Business Logic
│   └── dto/
│       └── GradeRequestDTO.java
├── lesson                        # Lesson Module
│   ├── Lesson.java               # Entity Model
│   └── LessonRepository.java     # JPA Repository
└── config                        # Global Configurations
    ├── DataInitializer.java      # Sample DB Data Seeder
    └── WebConfig.java            # CORS Configuration
```

---

## 📡 REST API Endpoint Reference

Base API Path: `/api/instructor`  
Default Instructor Header: `X-User-Id: 2`

### 1. Course Endpoints (`/api/instructor/courses`)

| Method | Endpoint | Description | Request Body / Parameters |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/instructor/courses` | Create a new course | `CourseRequestDTO` (title, category, description, syllabus) |
| `GET` | `/api/instructor/courses` | Fetch all courses created by instructor | None |
| `PATCH` | `/api/instructor/courses/{id}/status` | Change course status (`DRAFT` ↔ `PUBLISHED`) | Query Param: `status` (`DRAFT` / `PUBLISHED`) |
| `DELETE`| `/api/instructor/courses/{id}` | Delete a course | None |

### 2. Assignment Endpoints (`/api/instructor/assignments`)

| Method | Endpoint | Description | Request Body / Parameters |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/instructor/assignments` | Create a new assignment for a course | `AssignmentRequestDTO` (courseId, title, description, maxPoints, dueDate) |
| `GET` | `/api/instructor/assignments` | Fetch all assignments | None |

### 3. Quiz Endpoints (`/api/instructor/quizzes`)

| Method | Endpoint | Description | Request Body / Parameters |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/instructor/quizzes` | Create a quiz with multiple questions | `QuizRequestDTO` (courseId, title, instructions, timeLimitMinutes, questions: [...]) |
| `GET` | `/api/instructor/quizzes` | Fetch all quizzes | None |

### 4. Grading & Submissions Endpoints (`/api/instructor/submissions`)

| Method | Endpoint | Description | Request Body / Parameters |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/instructor/submissions` | Fetch student submissions for grading | None |
| `POST` | `/api/instructor/submissions/{id}/grade` | Submit grade & feedback for a submission | `GradeRequestDTO` (score, feedback) |

---

## ⚙️ Backend Configuration Summary (`application.properties`)

```properties
server.port=8080

# Database Connection
spring.datasource.url=jdbc:mysql://localhost:3306/lms_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=Sneha@89

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

## 🏃 How to Run the Backend

1. Ensure MySQL is running on `localhost:3306` with database credentials matching `application.properties`.
2. Open the project in VS Code.
3. Navigate to `src/main/java/com/lms/LmsApplication.java`.
4. Right-click and choose **Run Java** (or use the Spring Boot Dashboard extension).
