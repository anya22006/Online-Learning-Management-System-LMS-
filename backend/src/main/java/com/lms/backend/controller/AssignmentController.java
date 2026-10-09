package com.lms.backend.controller;

import com.lms.backend.entity.Assignment;
import com.lms.backend.service.AssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@CrossOrigin(origins = "http://localhost:5173")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public List<Assignment> getAllAssignments() {
        return assignmentService.getAllAssignments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignmentById(
            @PathVariable Integer id) {

        return assignmentService.getAssignmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/course/{courseId}")
    public List<Assignment> getAssignmentsByCourse(
            @PathVariable Integer courseId) {

        return assignmentService.getAssignmentsByCourse(courseId);
    }

    @PostMapping
    public Assignment createAssignment(
            @RequestBody Assignment assignment) {

        return assignmentService.saveAssignment(assignment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Assignment> updateAssignment(
            @PathVariable Integer id,
            @RequestBody Assignment assignment) {

        return assignmentService.getAssignmentById(id)
                .map(existingAssignment -> {

                    existingAssignment.setCourseId(
                            assignment.getCourseId());

                    existingAssignment.setTitle(
                            assignment.getTitle());

                    existingAssignment.setDescription(
                            assignment.getDescription());

                    existingAssignment.setDueDate(
                            assignment.getDueDate());

                    existingAssignment.setMaxMarks(
                            assignment.getMaxMarks());

                    return ResponseEntity.ok(
                            assignmentService.saveAssignment(
                                    existingAssignment)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Integer id) {

        if (assignmentService.getAssignmentById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        assignmentService.deleteAssignment(id);

        return ResponseEntity.noContent().build();
    }
}