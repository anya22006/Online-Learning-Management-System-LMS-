package com.lms.backend.repository;

import com.lms.backend.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository
        extends JpaRepository<ActivityLog, Integer> {

    List<ActivityLog> findByUserId(Integer userId);

    List<ActivityLog> findByAction(String action);

    List<ActivityLog> findByEntityType(String entityType);

    List<ActivityLog> findByEntityId(Integer entityId);
}