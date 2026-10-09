package com.lms.backend.service;

import com.lms.backend.entity.ActivityLog;
import com.lms.backend.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(
            ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public List<ActivityLog> getAllLogs() {
        return activityLogRepository.findAll();
    }

    public Optional<ActivityLog> getLogById(Integer id) {
        return activityLogRepository.findById(id);
    }

    public List<ActivityLog> getLogsByUser(Integer userId) {
        return activityLogRepository.findByUserId(userId);
    }

    public List<ActivityLog> getLogsByAction(String action) {
        return activityLogRepository.findByAction(action);
    }

    public List<ActivityLog> getLogsByEntityType(
            String entityType) {

        return activityLogRepository.findByEntityType(entityType);
    }

    public List<ActivityLog> getLogsByEntityId(Integer entityId) {
        return activityLogRepository.findByEntityId(entityId);
    }

    public ActivityLog saveLog(ActivityLog log) {
        return activityLogRepository.save(log);
    }

    public void deleteLog(Integer id) {
        activityLogRepository.deleteById(id);
    }
}