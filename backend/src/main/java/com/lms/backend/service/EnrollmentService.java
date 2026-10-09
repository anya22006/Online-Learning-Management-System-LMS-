package com.lms.backend.service;

import com.lms.backend.entity.Enrollment;
import com.lms.backend.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll(); 
    }

    public Optional<Enrollment> getEnrollmentById(Integer id) {
        return enrollmentRepository.findById(id);
    }

    public List<Enrollment> getEnrollmentsByStudent(Integer studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    public List<Enrollment> getEnrollmentsByCourse(Integer courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    public Optional<Enrollment> getEnrollment(
            Integer studentId,
            Integer courseId) {

        return enrollmentRepository.findByStudentIdAndCourseId(
                studentId,
                courseId
        );
    }

   public Enrollment saveEnrollment(Enrollment enrollment) {

    Optional<Enrollment> existingEnrollment =
            enrollmentRepository.findByStudentIdAndCourseId(
                    enrollment.getStudentId(),
                    enrollment.getCourseId()
            );

    if (existingEnrollment.isPresent()) {
        throw new RuntimeException(
                "Student is already enrolled in this course"
        );
    }

    return enrollmentRepository.save(enrollment);
}

    public void deleteEnrollment(Integer id) {
        enrollmentRepository.deleteById(id);
    }
}