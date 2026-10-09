package com.LMS_Student.repo;

import com.LMS_Student.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {

    List<Assignment> findByCourseIdInOrderByDueDateAsc(List<Integer> courseIds);
}
