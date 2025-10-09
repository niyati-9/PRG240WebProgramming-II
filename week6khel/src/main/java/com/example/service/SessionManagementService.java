package com.example.service;

import com.example.model.User;
import com.example.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session Management Service
 * Handles session lifecycle, security, and user session tracking
 */
@Service
public class SessionManagementService {

    private static final Logger logger = LoggerFactory.getLogger(SessionManagementService.class);

    @Autowired
    private JwtUtil jwtUtil;

    // Track user sessions for security monitoring
    private final Map<String, UserSessionInfo> activeUserSessions = new ConcurrentHashMap<>();

    /**
     * User session information for tracking
     */
    public static class UserSessionInfo {
        private final String username;
        private final String sessionId;
        private final Long userId;
        private final String userType;
        private final Long loginTime;
        private Long lastActivity;
        private String ipAddress;
        private String userAgent;

        public UserSessionInfo(String username, String sessionId, Long userId, String userType, Long loginTime) {
            this.username = username;
            this.sessionId = sessionId;
            this.userId = userId;
            this.userType = userType;
            this.loginTime = loginTime;
            this.lastActivity = loginTime;
        }

        // Getters and setters
        public String getUsername() { return username; }
        public String getSessionId() { return sessionId; }
        public Long getUserId() { return userId; }
        public String getUserType() { return userType; }
        public Long getLoginTime() { return loginTime; }
        public Long getLastActivity() { return lastActivity; }
        public void setLastActivity(Long lastActivity) { this.lastActivity = lastActivity; }
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    }

    /**
     * Create secure session for authenticated user
     */
    public void createUserSession(HttpSession session, User user, String jwtToken, String ipAddress, String userAgent) {
        logger.info("Creating secure session for user: {}", user.getUsername());

        // Set session attributes
        session.setAttribute("currentUser", user);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("userType", user.getUserType());
        session.setAttribute("email", user.getEmail());
        session.setAttribute("fullName", user.getFullName());
        session.setAttribute("jwtToken", jwtToken);
        session.setAttribute("authenticated", true);
        session.setAttribute("loginTime", System.currentTimeMillis());
        session.setAttribute("lastActivity", System.currentTimeMillis());

        // Set session timeout based on JWT expiration
        int sessionTimeout = (int) (jwtUtil.getExpirationMs() / 1000);
        session.setMaxInactiveInterval(sessionTimeout);

        // Track user session for security monitoring
        UserSessionInfo sessionInfo = new UserSessionInfo(
                user.getUsername(),
                session.getId(),
                user.getUserId(),
                user.getUserType().getValue(),
                System.currentTimeMillis()
        );
        sessionInfo.setIpAddress(ipAddress);
        sessionInfo.setUserAgent(userAgent);

        activeUserSessions.put(session.getId(), sessionInfo);

        logger.info("Secure session created for user: {} with session ID: {}", user.getUsername(), session.getId());
    }

    /**
     * Update session activity timestamp
     */
    public void updateSessionActivity(HttpSession session) {
        if (session != null) {
            Long currentTime = System.currentTimeMillis();
            session.setAttribute("lastActivity", currentTime);

            UserSessionInfo sessionInfo = activeUserSessions.get(session.getId());
            if (sessionInfo != null) {
                sessionInfo.setLastActivity(currentTime);
            }
        }
    }

    /**
     * Validate session security and JWT token synchronization
     */
    public boolean validateSessionSecurity(HttpSession session, String jwtToken) {
        if (session == null) {
            logger.warn("Session validation failed: No session found");
            return false;
        }

        try {
            // Check if session is authenticated
            Boolean authenticated = (Boolean) session.getAttribute("authenticated");
            if (authenticated == null || !authenticated) {
                logger.warn("Session validation failed: Session not authenticated");
                return false;
            }

            // Verify JWT token matches session
            String sessionToken = (String) session.getAttribute("jwtToken");
            if (sessionToken == null || !sessionToken.equals(jwtToken)) {
                logger.warn("Session validation failed: JWT token mismatch");
                invalidateSession(session);
                return false;
            }

            // Validate JWT token expiration
            String username = (String) session.getAttribute("username");
            if (!jwtUtil.validateToken(jwtToken, username)) {
                logger.warn("Session validation failed: JWT token invalid for user: {}", username);
                invalidateSession(session);
                return false;
            }

            // Update session activity
            updateSessionActivity(session);

            return true;

        } catch (Exception e) {
            logger.error("Session validation error: {}", e.getMessage());
            invalidateSession(session);
            return false;
        }
    }

    /**
     * Check if user has permission for write operations
     */
    public boolean validateWriteOperationPermission(HttpSession session, String jwtToken, Long targetUserId) {
        if (!validateSessionSecurity(session, jwtToken)) {
            return false;
        }

        try {
            Long sessionUserId = (Long) session.getAttribute("userId");
            String username = (String) session.getAttribute("username");
            String userType = session.getAttribute("userType").toString();

            // Admin users can perform write operations on any user
            if ("admin".equalsIgnoreCase(userType)) {
                logger.info("Admin user {} granted write operation permission", username);
                return true;
            }

            // Regular users can only modify their own data
            if (targetUserId != null && !sessionUserId.equals(targetUserId)) {
                logger.warn("Write operation denied: User {} attempting to modify user {}", sessionUserId, targetUserId);
                return false;
            }

            // Verify JWT token is not expired for write operations
            if (jwtUtil.isTokenExpired(jwtToken)) {
                logger.warn("Write operation denied: JWT token expired for user {}", username);
                return false;
            }

            logger.info("Write operation permission granted for user: {}", username);
            return true;

        } catch (Exception e) {
            logger.error("Write operation permission validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Invalidate user session securely
     */
    public void invalidateSession(HttpSession session) {
        if (session != null) {
            try {
                String sessionId = session.getId();
                String username = (String) session.getAttribute("username");
                String jwtToken = (String) session.getAttribute("jwtToken");

                // Blacklist JWT token
                if (jwtToken != null) {
                    jwtUtil.blacklistToken(jwtToken);
                }

                // Remove from active sessions
                activeUserSessions.remove(sessionId);

                // Invalidate session
                session.invalidate();

                logger.info("Session invalidated for user: {} - Session ID: {}", username, sessionId);

            } catch (Exception e) {
                logger.error("Error invalidating session: {}", e.getMessage());
            }
        }
    }

    /**
     * Invalidate all sessions for a specific user
     */
    public void invalidateAllUserSessions(String username) {
        logger.info("Invalidating all sessions for user: {}", username);

        activeUserSessions.entrySet().removeIf(entry -> {
            UserSessionInfo sessionInfo = entry.getValue();
            if (sessionInfo.getUsername().equals(username)) {
                logger.info("Invalidating session: {} for user: {}", entry.getKey(), username);
                return true;
            }
            return false;
        });

        // Invalidate all JWT tokens for user
        jwtUtil.invalidateAllUserSessions(username);

        logger.info("All sessions invalidated for user: {}", username);
    }

    /**
     * Check for suspicious session activity
     */
    public boolean detectSuspiciousActivity(HttpSession session, String currentIpAddress) {
        if (session == null) return false;

        try {
            UserSessionInfo sessionInfo = activeUserSessions.get(session.getId());
            if (sessionInfo == null) return false;

            // Check for IP address changes (simple check)
            if (sessionInfo.getIpAddress() != null && !sessionInfo.getIpAddress().equals(currentIpAddress)) {
                logger.warn("SUSPICIOUS ACTIVITY: IP address change detected for user: {} from {} to {}",
                        sessionInfo.getUsername(), sessionInfo.getIpAddress(), currentIpAddress);
                return true;
            }

            // Check for session timeout
            Long lastActivity = sessionInfo.getLastActivity();
            if (lastActivity != null) {
                Long inactiveTime = System.currentTimeMillis() - lastActivity;
                if (inactiveTime > jwtUtil.getExpirationMs()) {
                    logger.warn("SUSPICIOUS ACTIVITY: Session timeout exceeded for user: {}", sessionInfo.getUsername());
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            logger.error("Error detecting suspicious activity: {}", e.getMessage());
            return true; // Err on the side of caution
        }
    }

    /**
     * Get session statistics for monitoring
     */
    public Map<String, Object> getSessionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeUserSessions", activeUserSessions.size());
        stats.put("jwtStatistics", jwtUtil.getSessionStatistics());

        // Count sessions by user type
        Map<String, Long> sessionsByUserType = new HashMap<>();
        activeUserSessions.values().forEach(session -> {
            String userType = session.getUserType();
            sessionsByUserType.put(userType, sessionsByUserType.getOrDefault(userType, 0L) + 1);
        });
        stats.put("sessionsByUserType", sessionsByUserType);

        return stats;
    }

    /**
     * Cleanup expired sessions
     */
    public void cleanupExpiredSessions() {
        Long currentTime = System.currentTimeMillis();
        Long sessionTimeout = jwtUtil.getExpirationMs();

        activeUserSessions.entrySet().removeIf(entry -> {
            UserSessionInfo sessionInfo = entry.getValue();
            Long lastActivity = sessionInfo.getLastActivity();
            if (lastActivity != null && (currentTime - lastActivity) > sessionTimeout) {
                logger.debug("Removing expired session for user: {}", sessionInfo.getUsername());
                return true;
            }
            return false;
        });

        // Also cleanup JWT tokens
        jwtUtil.cleanupExpiredTokens();

        logger.debug("Session cleanup completed");
    }

    /**
     * Get user session information
     */
    public UserSessionInfo getUserSession(String sessionId) {
        return activeUserSessions.get(sessionId);
    }

    /**
     * Check if user has active sessions
     */
    public boolean hasActiveSessions(String username) {
        return activeUserSessions.values().stream()
                .anyMatch(session -> session.getUsername().equals(username));
    }

    /**
     * Get all active sessions for a user
     */
    public Map<String, UserSessionInfo> getActiveSessionsForUser(String username) {
        Map<String, UserSessionInfo> userSessions = new HashMap<>();
        activeUserSessions.entrySet().forEach(entry -> {
            if (entry.getValue().getUsername().equals(username)) {
                userSessions.put(entry.getKey(), entry.getValue());
            }
        });
        return userSessions;
    }
}