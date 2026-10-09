package com.LMS_Student.repo;

import com.LMS_Student.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Integer> {

    boolean existsByUserIdAndActionAndDescription(
            Integer userId,
            String action,
            String description
    );

    long countByUserIdAndActionAndDescriptionStartingWith(
            Integer userId,
            String action,
            String descriptionPrefix
    );
}
