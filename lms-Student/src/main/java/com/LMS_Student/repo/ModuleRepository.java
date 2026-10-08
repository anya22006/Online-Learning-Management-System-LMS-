package com.LMS_Student.repo;

import com.LMS_Student.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Integer> {

    List<Module> findByCourseIdOrderByModuleOrderAsc(Integer courseId);
}