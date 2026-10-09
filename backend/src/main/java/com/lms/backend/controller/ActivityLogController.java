package com.lms.backend.controller;

import com.lms.backend.entity.ActivityLog;
import com.lms.backend.service.ActivityLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity-logs")
@CrossOrigin(origins = "http://localhost:5173")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(
            ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public List<ActivityLog> getAllLogs() {
        return activityLogService.getAllLogs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityLog> getLogById(
            @PathVariable Integer id) {

        return activityLogService.getLogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<ActivityLog> getLogsByUser(
            @PathVariable Integer userId) {

        return activityLogService.getLogsByUser(userId);
    }

    @GetMapping("/action/{action}")
    public List<ActivityLog> getLogsByAction(
            @PathVariable String action) {

        return activityLogService.getLogsByAction(action);
    }

    @GetMapping("/entity-type/{entityType}")
    public List<ActivityLog> getLogsByEntityType(
            @PathVariable String entityType) {

        return activityLogService
                .getLogsByEntityType(entityType);
    }

    @GetMapping("/entity/{entityId}")
    public List<ActivityLog> getLogsByEntityId(
            @PathVariable Integer entityId) {

        return activityLogService
                .getLogsByEntityId(entityId);
    }

    @PostMapping
    public ActivityLog createLog(
            @RequestBody ActivityLog log) {

        return activityLogService.saveLog(log);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(
            @PathVariable Integer id) {

        if (activityLogService.getLogById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        activityLogService.deleteLog(id);

        return ResponseEntity.noContent().build();
    }
}