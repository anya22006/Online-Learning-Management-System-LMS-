package com.LMS_Student.service;

import com.LMS_Student.entity.Module;
import com.LMS_Student.repo.ModuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;

    public ModuleService(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    public List<Module> getModulesByCourse(Integer courseId) {
        return moduleRepository.findByCourseIdOrderByModuleOrderAsc(courseId);
    }
}