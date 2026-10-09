package com.LMS_Student.controller;

import com.LMS_Student.dto.StudentAssignmentResponse;
import com.LMS_Student.service.AssignmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/assignments")
@CrossOrigin
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/{studentId}")
    public List<StudentAssignmentResponse> getStudentAssignments(
            @PathVariable Integer studentId
    ) {
        return assignmentService.getAssignmentsForStudent(studentId);
    }
}
