package com.lms.backend.repository;

import com.lms.backend.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Integer> {

    List<Module> findByCourseId(Integer courseId);

}