package com.lms.admin;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String affectedResource;

    public ActivityLog() {}

    public ActivityLog(String userName, String action, String affectedResource) {
        this.timestamp = LocalDateTime.now();
        this.userName = userName;
        this.action = action;
        this.affectedResource = affectedResource;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getAffectedResource() { return affectedResource; }
    public void setAffectedResource(String affectedResource) { this.affectedResource = affectedResource; }
}
