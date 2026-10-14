package com.lms.common;

import com.lms.admin.ActivityLog;
import com.lms.admin.ActivityLogRepository;
import com.lms.user.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class SecurityHelper {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    public User getCurrentUser(HttpSession session) {
        if (session == null) return null;
        return (User) session.getAttribute("currentUser");
    }

    public boolean isAuthenticated(HttpSession session) {
        return getCurrentUser(session) != null;
    }

    public boolean hasAnyRole(HttpSession session, String... roles) {
        User user = getCurrentUser(session);
        if (user == null || user.getRole() == null) return false;
        return Arrays.stream(roles).anyMatch(r -> r.equalsIgnoreCase(user.getRole()));
    }

    public void logActivity(String userName, String action, String details) {
        activityLogRepository.save(new ActivityLog(userName != null ? userName : "System", action, details));
    }
}
