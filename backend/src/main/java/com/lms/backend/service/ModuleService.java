package com.lms.backend.service;

import com.lms.backend.entity.Module;
import com.lms.backend.repository.ModuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;

    public ModuleService(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    public List<Module> getAllModules() {
        return moduleRepository.findAll();
    }

    public Optional<Module> getModuleById(Integer id) {
        return moduleRepository.findById(id);
    }

    public List<Module> getModulesByCourse(Integer courseId) {
        return moduleRepository.findByCourseId(courseId);
    }

    public Module saveModule(Module module) {
        return moduleRepository.save(module);
    }

    public void deleteModule(Integer id) {
        moduleRepository.deleteById(id);
    }
}