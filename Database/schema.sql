-- MySQL Database Schema for LMS Instructor Module
CREATE DATABASE IF NOT EXISTS lms_db;
USE lms_db;

-- 1. Courses Table
CREATE TABLE IF NOT EXISTS courses (
    course_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instructor_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    description TEXT,
    syllabus TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Lessons Table
CREATE TABLE IF NOT EXISTS lessons (
    lesson_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content_url TEXT,
    lesson_order INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
);

-- 3. Assignments Table
CREATE TABLE IF NOT EXISTS assignments (
    assignment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    instructions TEXT,
    due_date DATETIME NOT NULL,
    max_score INT NOT NULL DEFAULT 100,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
);

-- 4. Quizzes Table
CREATE TABLE IF NOT EXISTS quizzes (
    quiz_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    instructions TEXT,
    time_limit_minutes INT DEFAULT 30,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
);

-- 5. Quiz Questions Table
CREATE TABLE IF NOT EXISTS quiz_questions (
    question_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255),
    option_d VARCHAR(255),
    correct_option VARCHAR(10) NOT NULL,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE
);

-- 6. Submissions Table (Student Module Interop)
CREATE TABLE IF NOT EXISTS submissions (
    submission_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    student_name VARCHAR(255),
    content TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. Grades Table
CREATE TABLE IF NOT EXISTS grades (
    grade_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id BIGINT NOT NULL UNIQUE,
    instructor_id BIGINT NOT NULL,
    score INT NOT NULL,
    max_score INT NOT NULL,
    feedback TEXT,
    graded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES submissions(submission_id) ON DELETE CASCADE
);
