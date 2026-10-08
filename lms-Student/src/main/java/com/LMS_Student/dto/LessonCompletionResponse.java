package com.LMS_Student.dto;

import com.LMS_Student.entity.StudentProgress;

public class LessonCompletionResponse {

    private final StudentProgress progress;
    private final boolean alreadyCompleted;

    public LessonCompletionResponse(
            StudentProgress progress,
            boolean alreadyCompleted
    ) {
        this.progress = progress;
        this.alreadyCompleted = alreadyCompleted;
    }

    public StudentProgress getProgress() {
        return progress;
    }

    public boolean isAlreadyCompleted() {
        return alreadyCompleted;
    }
}
