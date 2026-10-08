package com.LMS_Student.service;

import com.LMS_Student.entity.Enrollment;
import com.LMS_Student.repo.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    public Enrollment enrollStudent(Integer studentId, Integer courseId) {

        // Check if student is already enrolled
        if (enrollmentRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .isPresent()) {

            throw new RuntimeException("Student is already enrolled in this course.");
        }

        Enrollment enrollment = new Enrollment();

        enrollment.setStudentId(studentId);
        enrollment.setCourseId(courseId);
        enrollment.setProgress(0);
        enrollment.setStatus("active");

        return enrollmentRepository.save(enrollment);
    }

    public List<Enrollment> getStudentEnrollments(Integer studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }
}