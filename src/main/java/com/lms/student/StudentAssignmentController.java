package com.lms.student;

import com.lms.assignment.Assignment;
import com.lms.assignment.AssignmentRepository;
import com.lms.course.Course;
import com.lms.course.CourseRepository;
import com.lms.grading.Grade;
import com.lms.grading.GradeRepository;
import com.lms.grading.Submission;
import com.lms.grading.SubmissionRepository;
import com.lms.student.dto.StudentAssignmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentAssignmentController {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @GetMapping({"/assignments", "/assignments/{studentId}"})
    public ResponseEntity<List<StudentAssignmentDTO>> getStudentAssignments(
            @PathVariable(required = false) Long studentId) {
        Long targetStudentId = (studentId != null) ? studentId : 1L;
        List<Assignment> assignments = assignmentRepository.findAll();
        List<StudentAssignmentDTO> result = new ArrayList<>();

        for (Assignment a : assignments) {
            StudentAssignmentDTO dto = new StudentAssignmentDTO();
            dto.setAssignmentId(a.getAssignmentId());
            dto.setCourseId(a.getCourseId());
            dto.setTitle(a.getTitle());
            dto.setDescription(a.getInstructions());
            dto.setDueDate(a.getDueDate());
            dto.setMaxMarks(a.getMaxScore());

            if (a.getCourseId() != null) {
                Optional<Course> courseOpt = courseRepository.findById(a.getCourseId());
                courseOpt.ifPresent(c -> dto.setCourseTitle(c.getTitle()));
            }

            Optional<Submission> submissionOpt = submissionRepository.findByStudentIdAndAssignmentId(targetStudentId, a.getAssignmentId());
            if (submissionOpt.isPresent()) {
                Submission sub = submissionOpt.get();
                dto.setSubmittedAt(sub.getSubmittedAt());

                Optional<Grade> gradeOpt = gradeRepository.findBySubmissionId(sub.getSubmissionId());
                if (gradeOpt.isPresent()) {
                    Grade g = gradeOpt.get();
                    dto.setMarks(g.getScore());
                    dto.setFeedback(g.getFeedback());
                    dto.setSubmissionStatus("Graded");
                } else {
                    dto.setSubmissionStatus("Submitted");
                }
            } else {
                dto.setSubmissionStatus("Not submitted");
            }
            result.add(dto);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/submissions")
    public ResponseEntity<Submission> submitAssignment(@RequestBody Map<String, Object> payload) {
        Long assignmentId = Long.valueOf(payload.get("assignmentId").toString());
        Long studentId = payload.get("studentId") != null ? Long.valueOf(payload.get("studentId").toString()) : 1L;
        String content = payload.get("content") != null ? payload.get("content").toString() :
                        (payload.get("submissionFile") != null ? payload.get("submissionFile").toString() : "");
        String studentName = payload.get("studentName") != null ? payload.get("studentName").toString() : "Alex Johnson";

        Optional<Submission> existing = submissionRepository.findByStudentIdAndAssignmentId(studentId, assignmentId);
        Submission submission;
        if (existing.isPresent()) {
            submission = existing.get();
            submission.setContent(content);
            submission.setSubmittedAt(LocalDateTime.now());
        } else {
            submission = new Submission(assignmentId, studentId, studentName, content);
        }

        Submission saved = submissionRepository.save(submission);
        return ResponseEntity.ok(saved);
    }
}
