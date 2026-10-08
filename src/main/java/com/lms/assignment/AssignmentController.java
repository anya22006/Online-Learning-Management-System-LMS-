package com.lms.assignment;

import com.lms.assignment.dto.AssignmentRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instructor/assignments")
@CrossOrigin(origins = "*")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @PostMapping
    public ResponseEntity<Assignment> createAssignment(
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long instructorId,
            @RequestBody AssignmentRequestDTO dto) {
        return ResponseEntity.ok(assignmentService.createAssignment(instructorId, dto));
    }

    @GetMapping
    public ResponseEntity<List<Assignment>> getAllAssignments() {
        return ResponseEntity.ok(assignmentService.getAllAssignments());
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Assignment>> getAssignmentsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByCourse(courseId));
    }

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long assignmentId,
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long instructorId) {
        assignmentService.deleteAssignment(assignmentId, instructorId);
        return ResponseEntity.noContent().build();
    }
}
