package com.example.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Enhanced JWT Token Utility with Session Management
 * Provides methods for token generation, validation, and session tracking
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // JWT secret key - in production, this should be loaded from environment variables
    @Value("${jwt.secret:khelAppSecretKeyForJWTTokenGenerationAndValidation2024}")
    private String jwtSecret;

    // JWT expiration time in milliseconds (24 hours)
    @Value("${jwt.expiration:86400000}")
    private Long jwtExpirationMs;

    // Refresh token expiration time in milliseconds (7 days)
    @Value("${jwt.refresh.expiration:604800000}")
    private Long refreshExpirationMs;

    // Token blacklist for logout functionality
    private final Map<String, Long> tokenBlacklist = new ConcurrentHashMap<>();

    // Active sessions tracking
    private final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();

    /**
     * Session information for tracking active sessions
     */
    public static class SessionInfo {
        private final String username;
        private final Long userId;
        private final String userType;
        private final Long issuedAt;
        private final Long expiresAt;
        private String sessionId;

        public SessionInfo(String username, Long userId, String userType, Long issuedAt, Long expiresAt) {
            this.username = username;
            this.userId = userId;
            this.userType = userType;
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
        }

        // Getters
        public String getUsername() { return username; }
        public Long getUserId() { return userId; }
        public String getUserType() { return userType; }
        public Long getIssuedAt() { return issuedAt; }
        public Long getExpiresAt() { return expiresAt; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }

    /**
     * Generate JWT token for user with enhanced session tracking
     */
    public String generateToken(String username, Long userId, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        claims.put("sessionId", generateSessionId(username));

        String token = createToken(claims, username);

        // Track active session
        Long issuedAt = System.currentTimeMillis();
        Long expiresAt = issuedAt + jwtExpirationMs;
        SessionInfo sessionInfo = new SessionInfo(username, userId, userType, issuedAt, expiresAt);
        activeSessions.put(token, sessionInfo);

        logger.info("JWT token generated for user: {} with session tracking", username);
        return token;
    }

    /**
     * Generate refresh token with session management
     */
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("sessionId", generateSessionId(username));

        String refreshToken = Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(getSigningKey())
                .compact();

        logger.info("Refresh token generated for user: {}", username);
        return refreshToken;
    }

    /**
     * Create token with claims and enhanced security
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Get signing key for JWT
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate unique session ID
     */
    private String generateSessionId(String username) {
        return username + "_" + System.currentTimeMillis() + "_" + Math.random();
    }

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extract user type from token
     */
    public String extractUserType(String token) {
        return extractClaim(token, claims -> claims.get("userType", String.class));
    }

    /**
     * Extract session ID from token
     */
    public String extractSessionId(String token) {
        return extractClaim(token, claims -> claims.get("sessionId", String.class));
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract claim from token with enhanced error handling
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (ExpiredJwtException e) {
            logger.warn("Attempting to extract claim from expired token");
            throw e;
        } catch (Exception e) {
            logger.error("Error extracting claim from token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.error("Error extracting claims from token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Check if token is expired with detailed logging
     */
    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            boolean expired = expiration.before(new Date());

            if (expired) {
                logger.warn("JWT token expired at: {}", expiration);
                // Remove from active sessions if expired
                activeSessions.remove(token);
            }

            return expired;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}", e.getMessage());
            activeSessions.remove(token);
            return true;
        } catch (Exception e) {
            logger.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Enhanced token validation with session management
     */
    public Boolean validateToken(String token, String username) {
        try {
            // Check if token is blacklisted
            if (isTokenBlacklisted(token)) {
                logger.warn("JWT token is blacklisted for user: {}", username);
                return false;
            }

            // Extract username and validate
            final String extractedUsername = extractUsername(token);
            boolean isValid = extractedUsername.equals(username) && !isTokenExpired(token);

            if (isValid) {
                // Update session info
                SessionInfo sessionInfo = activeSessions.get(token);
                if (sessionInfo != null) {
                    logger.debug("Token validated for active session: {}", sessionInfo.getUsername());
                }
            } else {
                logger.warn("Token validation failed for user: {}", username);
                activeSessions.remove(token);
            }

            return isValid;

        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}", e.getMessage());
            activeSessions.remove(token);
            return false;
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        Long blacklistTime = tokenBlacklist.get(token);
        if (blacklistTime != null) {
            // Check if blacklist entry has expired (cleanup)
            if (System.currentTimeMillis() > blacklistTime + jwtExpirationMs) {
                tokenBlacklist.remove(token);
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Blacklist a token (for logout functionality)
     */
    public void blacklistToken(String token) {
        if (token != null && !token.isEmpty()) {
            tokenBlacklist.put(token, System.currentTimeMillis());
            activeSessions.remove(token);
            logger.info("Token blacklisted and session removed");
        }
    }

    /**
     * Check if token can be refreshed
     */
    public Boolean canTokenBeRefreshed(String token) {
        try {
            return !isTokenExpired(token) && !isTokenBlacklisted(token);
        } catch (Exception e) {
            logger.error("Error checking if token can be refreshed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Refresh token with session management
     */
    public String refreshToken(String token) {
        try {
            final Claims claims = extractAllClaims(token);
            String username = claims.getSubject();
            Long userId = claims.get("userId", Long.class);
            String userType = claims.get("userType", String.class);

            // Blacklist old token
            blacklistToken(token);

            // Generate new token
            String newToken = generateToken(username, userId, userType);

            logger.info("Token refreshed for user: {}", username);
            return newToken;

        } catch (Exception e) {
            logger.error("Error refreshing token: {}", e.getMessage());
            throw new RuntimeException("Token refresh failed", e);
        }
    }

    /**
     * Get active session info for a token
     */
    public SessionInfo getSessionInfo(String token) {
        return activeSessions.get(token);
    }

    /**
     * Get all active sessions for a user
     */
    public Map<String, SessionInfo> getActiveSessionsForUser(String username) {
        Map<String, SessionInfo> userSessions = new HashMap<>();
        for (Map.Entry<String, SessionInfo> entry : activeSessions.entrySet()) {
            if (entry.getValue().getUsername().equals(username)) {
                userSessions.put(entry.getKey(), entry.getValue());
            }
        }
        return userSessions;
    }

    /**
     * Invalidate all sessions for a user
     */
    public void invalidateAllUserSessions(String username) {
        activeSessions.entrySet().removeIf(entry -> {
            if (entry.getValue().getUsername().equals(username)) {
                blacklistToken(entry.getKey());
                return true;
            }
            return false;
        });
        logger.info("All sessions invalidated for user: {}", username);
    }

    /**
     * Cleanup expired tokens and sessions
     */
    public void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();

        // Clean expired blacklisted tokens
        tokenBlacklist.entrySet().removeIf(entry ->
                currentTime > entry.getValue() + jwtExpirationMs);

        // Clean expired active sessions
        activeSessions.entrySet().removeIf(entry ->
                currentTime > entry.getValue().getExpiresAt());

        logger.debug("Expired tokens and sessions cleaned up");
    }

    /**
     * Get session statistics
     */
    public Map<String, Object> getSessionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeSessions", activeSessions.size());
        stats.put("blacklistedTokens", tokenBlacklist.size());
        stats.put("jwtExpirationMs", jwtExpirationMs);
        stats.put("refreshExpirationMs", refreshExpirationMs);
        return stats;
    }

    /**
     * Get expiration time in milliseconds
     */
    public Long getExpirationMs() {
        return jwtExpirationMs;
    }

    /**
     * Get refresh token expiration time
     */
    public Long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }
}