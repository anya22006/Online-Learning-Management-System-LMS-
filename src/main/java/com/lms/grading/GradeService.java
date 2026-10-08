package com.lms.grading;

import com.lms.grading.dto.GradeRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GradeService {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    public Grade gradeSubmission(Long instructorId, GradeRequestDTO dto) {
        if (dto.getScore() < 0 || (dto.getMaxScore() != null && dto.getScore() > dto.getMaxScore())) {
            throw new IllegalArgumentException("Score (" + dto.getScore() + ") must be between 0 and max score (" + dto.getMaxScore() + ")");
        }

        Grade grade = gradeRepository.findBySubmissionId(dto.getSubmissionId())
                .orElseGet(() -> new Grade());

        grade.setSubmissionId(dto.getSubmissionId());
        grade.setInstructorId(instructorId);
        grade.setScore(dto.getScore());
        grade.setMaxScore(dto.getMaxScore() != null ? dto.getMaxScore() : 100);
        grade.setFeedback(dto.getFeedback());

        return gradeRepository.save(grade);
    }

    public List<Submission> getAllSubmissions() {
        return submissionRepository.findAll();
    }

    public Submission createSampleSubmission(Submission submission) {
        return submissionRepository.save(submission);
    }
}
