package com.lms.course.dto;

public class CourseRequestDTO {
    private String title;
    private String category;
    private String description;
    private String syllabus;

    public CourseRequestDTO() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSyllabus() { return syllabus; }
    public void setSyllabus(String syllabus) { this.syllabus = syllabus; }
}
