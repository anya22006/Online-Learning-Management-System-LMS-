package com.lms.backend.controller;

import com.lms.backend.entity.Module;
import com.lms.backend.service.ModuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@CrossOrigin(origins = "http://localhost:5173")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping
    public List<Module> getAllModules() {
        return moduleService.getAllModules();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Module> getModuleById(
            @PathVariable Integer id) {

        return moduleService.getModuleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/course/{courseId}")
    public List<Module> getModulesByCourse(
            @PathVariable Integer courseId) {

        return moduleService.getModulesByCourse(courseId);
    }

    @PostMapping
    public Module createModule(
            @RequestBody Module module) {

        return moduleService.saveModule(module);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Module> updateModule(
            @PathVariable Integer id,
            @RequestBody Module module) {

        return moduleService.getModuleById(id)
                .map(existingModule -> {

                    existingModule.setCourseId(module.getCourseId());
                    existingModule.setTitle(module.getTitle());
                    existingModule.setDescription(module.getDescription());
                    existingModule.setModuleOrder(module.getModuleOrder());

                    return ResponseEntity.ok(
                            moduleService.saveModule(existingModule)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(
            @PathVariable Integer id) {

        if (moduleService.getModuleById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        moduleService.deleteModule(id);

        return ResponseEntity.noContent().build();
    }
}