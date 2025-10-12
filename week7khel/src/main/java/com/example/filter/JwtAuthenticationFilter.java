package com.example.filter;

import com.example.util.JwtUtil;
import com.example.service.UserService;
import com.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Enhanced JWT Authentication Filter with Session Management
 * Validates JWT tokens and manages user sessions with security enhancements
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/users/health",
            "/api/users/login",
            "/api/users/login-email",
            "/api/users", // POST for registration
            "/api/debug/status",
            "/api/test/hello",
            "/",
            "/login",
            "/signup",
            "/home",
            "/test"
    );

    // Static resources that don't require authentication
    private static final List<String> STATIC_RESOURCES = Arrays.asList(
            "/resources/", "/css/", "/js/", "/images/", ".css", ".js", ".png", ".jpg", ".ico"
    );

    // Protected endpoints that require valid JWT token for UPDATE/DELETE operations
    private static final List<String> PROTECTED_WRITE_OPERATIONS = Arrays.asList(
            "/api/users", // PUT, DELETE operations
            "/api/auth/refresh",
            "/api/auth/validate"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        HttpSession session = request.getSession(false);

        logger.debug("JWT Filter processing: {} {} - Session ID: {}",
                method, requestPath, session != null ? session.getId() : "null");

        // Skip authentication for public endpoints and static resources
        if (shouldSkipAuthentication(requestPath, method)) {
            logger.debug("Skipping JWT authentication for public endpoint: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // PROTECTED ENDPOINT - JWT TOKEN REQUIRED
        logger.info("PROTECTED ENDPOINT ACCESSED: {} {} - JWT TOKEN VALIDATION REQUIRED", method, requestPath);

        // Extract JWT token from request
        String token = extractTokenFromRequest(request);

        if (token == null || token.isEmpty()) {
            logger.warn("SECURITY VIOLATION: No JWT token provided for protected endpoint: {} {}", method, requestPath);
            handleAuthenticationFailure(response, "Authentication required - Missing JWT token", requestPath);
            return;
        }

        try {
            // Validate JWT token and check expiration
            if (!validateJwtTokenAndSession(token, request, response)) {
                return; // Error already handled in validation method
            }

            // Additional validation for write operations (UPDATE/DELETE)
            if (isWriteOperation(method) && isProtectedWriteEndpoint(requestPath)) {
                if (!validateWriteOperationPermissions(token, request, response)) {
                    return; // Error already handled in validation method
                }
            }

            // Token is valid, continue to endpoint
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("JWT token processing error for {}: {}", requestPath, e.getMessage());
            handleAuthenticationFailure(response, "Token processing error", requestPath);
        }
    }

    /**
     * Validate JWT token and manage session
     */
    private boolean validateJwtTokenAndSession(String token, HttpServletRequest request,
                                               HttpServletResponse response) throws IOException {
        try {
            String username = jwtUtil.extractUsername(token);

            if (username == null) {
                logger.warn("Invalid JWT token: Unable to extract username");
                handleAuthenticationFailure(response, "Invalid token format", request.getRequestURI());
                return false;
            }

            // Check if token is expired
            if (jwtUtil.isTokenExpired(token)) {
                logger.warn("JWT token expired for user: {}", username);
                handleTokenExpired(response, username, request.getRequestURI());
                return false;
            }

            // Validate token signature and claims
            if (!jwtUtil.validateToken(token, username)) {
                logger.warn("JWT token validation failed for user: {}", username);
                handleAuthenticationFailure(response, "Invalid token signature", request.getRequestURI());
                return false;
            }

            // Get user details and validate user status
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                logger.warn("JWT token valid but user not found: {}", username);
                handleAuthenticationFailure(response, "User not found", request.getRequestURI());
                return false;
            }

            User user = userOpt.get();
            if (!user.isActive()) {
                logger.warn("User account is inactive: {}", username);
                handleAuthenticationFailure(response, "Account is inactive", request.getRequestURI());
                return false;
            }

            // Create or update session with user information
            HttpSession session = request.getSession(true);
            updateUserSession(session, user, token);

            // Set user information in request attributes for controllers
            setRequestAttributes(request, user, token);

            logger.info("JWT authentication successful for user: {} - Session: {}",
                    username, session.getId());
            return true;

        } catch (Exception e) {
            logger.error("JWT token validation error: {}", e.getMessage());
            handleAuthenticationFailure(response, "Token validation failed", request.getRequestURI());
            return false;
        }
    }

    /**
     * Additional validation for write operations (UPDATE/DELETE)
     */
    private boolean validateWriteOperationPermissions(String token, HttpServletRequest request,
                                                      HttpServletResponse response) throws IOException {
        try {
            String username = jwtUtil.extractUsername(token);
            Long tokenUserId = jwtUtil.extractUserId(token);
            String requestPath = request.getRequestURI();

            logger.info("Validating write operation permissions for user: {} on path: {}", username, requestPath);

            // Check token freshness for write operations (must not be expired)
            if (jwtUtil.isTokenExpired(token)) {
                logger.warn("Token expired during write operation validation for user: {}", username);
                handleTokenExpired(response, username, requestPath);
                return false;
            }

            // For user-specific endpoints, ensure user can only modify their own data
            if (requestPath.matches(".*/api/users/\\d+.*")) {
                String pathUserId = extractUserIdFromPath(requestPath);
                String tokenUserType = jwtUtil.extractUserType(token);

                // Admin users can modify any user
                if (!"admin".equalsIgnoreCase(tokenUserType) &&
                        pathUserId != null && !pathUserId.equals(tokenUserId.toString())) {
                    logger.warn("AUTHORIZATION VIOLATION: User {} attempting to modify user {}", tokenUserId, pathUserId);
                    handleAuthorizationFailure(response, "Not authorized to modify this user", requestPath);
                    return false;
                }
            }

            // Additional session validation for write operations
            HttpSession session = request.getSession(false);
            if (session == null) {
                logger.warn("No session found for write operation by user: {}", username);
                handleAuthenticationFailure(response, "Session required for write operations", requestPath);
                return false;
            }

            // Verify session user matches token user
            String sessionUsername = (String) session.getAttribute("username");
            if (sessionUsername == null || !sessionUsername.equals(username)) {
                logger.warn("Session mismatch for write operation: session={}, token={}", sessionUsername, username);
                handleAuthenticationFailure(response, "Session validation failed", requestPath);
                return false;
            }

            logger.info("Write operation permissions validated for user: {}", username);
            return true;

        } catch (Exception e) {
            logger.error("Write operation permission validation error: {}", e.getMessage());
            handleAuthenticationFailure(response, "Permission validation failed", request.getRequestURI());
            return false;
        }
    }

    /**
     * Update user session with current information
     */
    private void updateUserSession(HttpSession session, User user, String token) {
        // Set session timeout based on token expiration
        int sessionTimeout = (int) (jwtUtil.getExpirationMs() / 1000); // Convert to seconds
        session.setMaxInactiveInterval(sessionTimeout);

        // Store user information in session
        session.setAttribute("currentUser", user);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("userType", user.getUserType());
        session.setAttribute("email", user.getEmail());
        session.setAttribute("fullName", user.getFullName());
        session.setAttribute("jwtToken", token);
        session.setAttribute("tokenRefreshTime", System.currentTimeMillis());
        session.setAttribute("authenticated", true);

        logger.debug("Session updated for user: {} - Session ID: {}", user.getUsername(), session.getId());
    }

    /**
     * Set request attributes for controller access
     */
    private void setRequestAttributes(HttpServletRequest request, User user, String token) {
        request.setAttribute("currentUser", user);
        request.setAttribute("userId", user.getUserId());
        request.setAttribute("username", user.getUsername());
        request.setAttribute("userType", user.getUserType().getValue());
        request.setAttribute("email", user.getEmail());
        request.setAttribute("authenticated", true);
        request.setAttribute("jwtToken", token);
    }

    /**
     * Check if authentication should be skipped for this path
     */
    private boolean shouldSkipAuthentication(String requestPath, String method) {
        // Allow OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // Allow POST to /api/users for registration
        if ("POST".equals(method) && "/api/users".equals(requestPath)) {
            return true;
        }

        // Check public endpoints
        for (String publicEndpoint : PUBLIC_ENDPOINTS) {
            if (requestPath.equals(publicEndpoint) || requestPath.startsWith(publicEndpoint)) {
                return true;
            }
        }

        // Check static resources
        for (String staticResource : STATIC_RESOURCES) {
            if (requestPath.contains(staticResource) || requestPath.endsWith(staticResource)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if this is a write operation
     */
    private boolean isWriteOperation(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method);
    }

    /**
     * Check if this endpoint requires additional validation for write operations
     */
    private boolean isProtectedWriteEndpoint(String requestPath) {
        for (String protectedEndpoint : PROTECTED_WRITE_OPERATIONS) {
            if (requestPath.startsWith(protectedEndpoint)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extract user ID from URL path
     */
    private String extractUserIdFromPath(String requestPath) {
        try {
            String[] pathSegments = requestPath.split("/");
            for (int i = 0; i < pathSegments.length; i++) {
                if ("users".equals(pathSegments[i]) && i + 1 < pathSegments.length) {
                    String userId = pathSegments[i + 1];
                    if (userId.matches("\\d+")) {
                        return userId;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting user ID from path: {}", requestPath);
        }
        return null;
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Fallback: check for token in request parameter (less secure)
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            logger.warn("JWT token provided as query parameter - use Authorization header instead");
            return tokenParam;
        }

        return null;
    }

    /**
     * Handle token expiration
     */
    private void handleTokenExpired(HttpServletResponse response, String username, String requestPath)
            throws IOException {
        logger.error("=== JWT TOKEN EXPIRED ===");
        logger.error("User: {}", username);
        logger.error("Path: {}", requestPath);
        logger.error("Time: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a d MMM yyyy")));
        logger.error("=========================");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"success\": false, \"message\": \"JWT token has expired. Please refresh your token.\", " +
                        "\"error_code\": \"TOKEN_EXPIRED\", \"path\": \"%s\", \"username\": \"%s\", " +
                        "\"timestamp\": \"%s\"}",
                requestPath, username, LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a d MMM yyyy"))
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    /**
     * Handle authorization failure
     */
    private void handleAuthorizationFailure(HttpServletResponse response, String message, String requestPath)
            throws IOException {
        logger.error("=== AUTHORIZATION FAILURE ===");
        logger.error("Path: {}", requestPath);
        logger.error("Reason: {}", message);
        logger.error("Time: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a d MMM yyyy")));
        logger.error("=============================");

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"success\": false, \"message\": \"%s\", \"error_code\": \"FORBIDDEN\", " +
                        "\"path\": \"%s\", \"timestamp\": \"%s\"}",
                message, requestPath, LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a d MMM yyyy"))
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    /**
     * Handle general authentication failure
     */
    private void handleAuthenticationFailure(HttpServletResponse response, String message, String requestPath)
            throws IOException {
        logger.error("=== AUTHENTICATION FAILURE ===");
        logger.error("Path: {}", requestPath);
        logger.error("Reason: {}", message);
        logger.error("Time: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a d MMM yyyy")));
        logger.error("===============================");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"success\": false, \"message\": \"%s\", \"error_code\": \"AUTHENTICATION_REQUIRED\", " +
                        "\"path\": \"%s\", \"timestamp\": \"%s\"}",
                message, requestPath, LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a d MMM yyyy"))
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}