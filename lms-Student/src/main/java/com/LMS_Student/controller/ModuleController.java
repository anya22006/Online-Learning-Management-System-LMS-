package com.LMS_Student.controller;

import com.LMS_Student.entity.Module;
import com.LMS_Student.service.ModuleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/modules")
@CrossOrigin
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping("/course/{courseId}")
    public List<Module> getModulesByCourse(
            @PathVariable Integer courseId) {

        return moduleService.getModulesByCourse(courseId);
    }
}