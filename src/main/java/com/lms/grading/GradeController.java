package com.lms.grading;

import com.lms.grading.dto.GradeRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instructor")
@CrossOrigin(origins = "*")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @GetMapping("/submissions")
    public ResponseEntity<List<Submission>> getSubmissions() {
        return ResponseEntity.ok(gradeService.getAllSubmissions());
    }

    @PostMapping("/submissions")
    public ResponseEntity<Submission> createSampleSubmission(@RequestBody Submission submission) {
        return ResponseEntity.ok(gradeService.createSampleSubmission(submission));
    }

    @PostMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<Grade> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long instructorId,
            @RequestBody GradeRequestDTO dto) {
        dto.setSubmissionId(submissionId);
        return ResponseEntity.ok(gradeService.gradeSubmission(instructorId, dto));
    }
}
