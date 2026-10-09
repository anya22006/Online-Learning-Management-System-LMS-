package com.LMS_Student.dto;

public class StudentSubmissionRequest {

    private Integer assignmentId;
    private Integer studentId;
    private String submissionFile;

    public Integer getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Integer assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getSubmissionFile() {
        return submissionFile;
    }

    public void setSubmissionFile(String submissionFile) {
        this.submissionFile = submissionFile;
    }
}
