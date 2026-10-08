package com.lms.student;

import com.lms.assignment.Assignment;
import com.lms.assignment.AssignmentRepository;
import com.lms.course.Course;
import com.lms.course.CourseRepository;
import com.lms.grading.Grade;
import com.lms.grading.GradeRepository;
import com.lms.grading.Submission;
import com.lms.grading.SubmissionRepository;
import com.lms.student.dto.StudentGradeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/student/grades")
@CrossOrigin(origins = "*")
public class StudentGradeController {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @GetMapping({"/{studentId}", ""})
    public ResponseEntity<List<StudentGradeDTO>> getStudentGrades(
            @PathVariable(required = false) Long studentId) {
        Long targetStudentId = (studentId != null) ? studentId : 1L;
        List<Submission> submissions = submissionRepository.findByStudentId(targetStudentId);
        List<StudentGradeDTO> result = new ArrayList<>();

        for (Submission sub : submissions) {
            StudentGradeDTO dto = new StudentGradeDTO();
            dto.setSubmissionId(sub.getSubmissionId());
            dto.setSubmittedAt(sub.getSubmittedAt());

            Optional<Assignment> assignmentOpt = assignmentRepository.findById(sub.getAssignmentId());
            if (assignmentOpt.isPresent()) {
                Assignment a = assignmentOpt.get();
                dto.setAssignmentTitle(a.getTitle());
                dto.setCourseId(a.getCourseId());

                if (a.getCourseId() != null) {
                    Optional<Course> courseOpt = courseRepository.findById(a.getCourseId());
                    courseOpt.ifPresent(c -> dto.setCourseTitle(c.getTitle()));
                }
            }

            Optional<Grade> gradeOpt = gradeRepository.findBySubmissionId(sub.getSubmissionId());
            if (gradeOpt.isPresent()) {
                Grade g = gradeOpt.get();
                dto.setMarks(g.getScore());
                dto.setFeedback(g.getFeedback());
                dto.setSubmissionStatus("Graded");
            } else {
                dto.setSubmissionStatus("Submitted");
                dto.setFeedback("No instructor feedback yet.");
            }

            result.add(dto);
        }

        return ResponseEntity.ok(result);
    }
}
