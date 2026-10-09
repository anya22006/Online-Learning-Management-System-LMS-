package com.lms.instructor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstructorProfileRepository extends JpaRepository<InstructorProfile, Long> {
}
