package com.lms.student;

import com.lms.assignment.Assignment;
import com.lms.assignment.AssignmentRepository;
import com.lms.common.SecurityHelper;
import com.lms.course.Course;
import com.lms.course.CourseRepository;
import com.lms.grading.Grade;
import com.lms.grading.GradeRepository;
import com.lms.grading.Submission;
import com.lms.grading.SubmissionRepository;
import com.lms.student.dto.StudentAssignmentDTO;
import com.lms.user.User;
import jakarta.servlet.http.HttpSession;
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

    @Autowired
    private SecurityHelper securityHelper;

    @GetMapping({"/assignments", "/assignments/{studentId}"})
    public ResponseEntity<List<StudentAssignmentDTO>> getStudentAssignments(
            @PathVariable(required = false) Long studentId,
            HttpSession session) {
        Long targetStudentId = studentId;
        if (targetStudentId == null) {
            User currentUser = securityHelper.getCurrentUser(session);
            targetStudentId = (currentUser != null) ? currentUser.getId() : 1L;
        }

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
    public ResponseEntity<Submission> submitAssignment(
            @RequestBody Map<String, Object> payload,
            HttpSession session) {
        Long assignmentId = Long.valueOf(payload.get("assignmentId").toString());
        User currentUser = securityHelper.getCurrentUser(session);

        Long studentId;
        if (payload.get("studentId") != null) {
            studentId = Long.valueOf(payload.get("studentId").toString());
        } else if (currentUser != null) {
            studentId = currentUser.getId();
        } else {
            studentId = 1L;
        }

        String studentName;
        if (payload.get("studentName") != null) {
            studentName = payload.get("studentName").toString();
        } else if (currentUser != null) {
            studentName = currentUser.getFullName();
        } else {
            studentName = "Student #" + studentId;
        }

        String content = payload.get("content") != null ? payload.get("content").toString() :
                        (payload.get("submissionFile") != null ? payload.get("submissionFile").toString() : "");

        Optional<Submission> existing = submissionRepository.findByStudentIdAndAssignmentId(studentId, assignmentId);
        Submission submission;
        if (existing.isPresent()) {
            submission = existing.get();
            submission.setContent(content);
            submission.setStudentName(studentName);
            submission.setSubmittedAt(LocalDateTime.now());
        } else {
            submission = new Submission(assignmentId, studentId, studentName, content);
        }

        Submission saved = submissionRepository.save(submission);
        return ResponseEntity.ok(saved);
    }
}
