package com.example.controller;

import com.example.dto.JwtResponse;
import com.example.dto.UserDTO;
import com.example.model.User;
import com.example.model.UserType;
import com.example.service.UserService;
import com.example.service.SessionManagementService;
import com.example.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Enhanced JWT Authentication REST Controller with Session Management
 * Handles JWT-based authentication endpoints with security enhancements
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class JwtAuthController {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SessionManagementService sessionManagementService;

    /**
     * Enhanced JWT Login endpoint with session management
     */
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest,
                                                     HttpServletRequest request) {
        logger.info("=== JWT LOGIN ATTEMPT ===");

        String username = loginRequest.get("username");
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        logger.info("Login attempt for: {}", username != null ? username : email);

        Map<String, Object> response = new HashMap<>();

        try {
            if (password == null || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Password is required");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<User> userOpt;

            // Try authentication by email or username
            if (email != null && !email.trim().isEmpty()) {
                userOpt = userService.authenticateUserByEmail(email.trim(), password);
                logger.info("Authentication attempt by email: {}", email);
            } else if (username != null && !username.trim().isEmpty()) {
                userOpt = userService.authenticateUser(username.trim(), password);
                logger.info("Authentication attempt by username: {}", username);
            } else {
                response.put("success", false);
                response.put("message", "Username or email is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Check if user account is active
                if (!user.isActive()) {
                    logger.warn("Login attempt for inactive user: {}", user.getUsername());
                    response.put("success", false);
                    response.put("message", "Account is inactive");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }

                // Generate JWT tokens with enhanced session management
                String jwtToken = jwtUtil.generateToken(
                        user.getUsername(),
                        user.getUserId(),
                        user.getUserType().getValue()
                );

                String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

                // Create JWT response with additional session info
                JwtResponse jwtResponse = new JwtResponse(
                        jwtToken,
                        refreshToken,
                        user.getUsername(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getUserType().getValue(),
                        user.getUserId(),
                        jwtUtil.getExpirationMs()
                );

                // Add session management information
                response.put("success", true);
                response.put("message", "Authentication successful");
                response.put("data", jwtResponse);
                response.put("sessionInfo", Map.of(
                        "loginTime", System.currentTimeMillis(),
                        "expiresAt", System.currentTimeMillis() + jwtUtil.getExpirationMs(),
                        "tokenType", "Bearer",
                        "refreshTokenExpiresAt", System.currentTimeMillis() + jwtUtil.getRefreshExpirationMs()
                ));

                logger.info("=== JWT LOGIN SUCCESSFUL ===");
                logger.info("JWT tokens generated for user: {}", user.getUsername());

                return ResponseEntity.ok(response);

            } else {
                response.put("success", false);
                response.put("message", "Invalid credentials");

                logger.warn("=== JWT LOGIN FAILED ===");
                logger.warn("Invalid credentials for: {}", username != null ? username : email);

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (Exception e) {
            logger.error("=== JWT LOGIN ERROR ===");
            logger.error("Login error: {}", e.getMessage(), e);

            response.put("success", false);
            response.put("message", "Authentication failed: " + e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Enhanced JWT Registration endpoint
     */
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserDTO userDTO,
                                                        HttpServletRequest request) {
        logger.info("=== JWT REGISTRATION ATTEMPT ===");
        logger.info("Registration attempt for username: {}, email: {}",
                userDTO.getUsername(), userDTO.getEmail());

        Map<String, Object> response = new HashMap<>();

        try {
            // Convert userType string to enum
            UserType userType = UserType.fromValue(userDTO.getUserType());

            // Register user with enhanced validation
            User registeredUser = userService.registerUser(
                    userDTO.getUsername(),
                    userDTO.getFullName(),
                    userDTO.getEmail(),
                    userDTO.getPhoneNumber(),
                    userDTO.getPassword(),
                    userType
            );

            // Generate JWT tokens for newly registered user
            String jwtToken = jwtUtil.generateToken(
                    registeredUser.getUsername(),
                    registeredUser.getUserId(),
                    registeredUser.getUserType().getValue()
            );

            String refreshToken = jwtUtil.generateRefreshToken(registeredUser.getUsername());

            // Create JWT response with session info
            JwtResponse jwtResponse = new JwtResponse(
                    jwtToken,
                    refreshToken,
                    registeredUser.getUsername(),
                    registeredUser.getEmail(),
                    registeredUser.getFullName(),
                    registeredUser.getUserType().getValue(),
                    registeredUser.getUserId(),
                    jwtUtil.getExpirationMs()
            );

            response.put("success", true);
            response.put("message", "Registration successful. You are now logged in.");
            response.put("data", jwtResponse);
            response.put("sessionInfo", Map.of(
                    "registrationTime", System.currentTimeMillis(),
                    "expiresAt", System.currentTimeMillis() + jwtUtil.getExpirationMs(),
                    "autoLogin", true
            ));

            logger.info("=== JWT REGISTRATION SUCCESSFUL ===");
            logger.info("User registered and JWT tokens generated: {}", registeredUser.getUsername());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("=== JWT REGISTRATION ERROR ===");
            logger.error("Registration error: {}", e.getMessage(), e);

            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Enhanced Token refresh endpoint with security validation
     */
    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> refreshRequest,
                                                            HttpServletRequest request) {
        logger.info("=== JWT TOKEN REFRESH ATTEMPT ===");

        Map<String, Object> response = new HashMap<>();
        String refreshToken = refreshRequest.get("refreshToken");

        try {
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Refresh token is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if refresh token is blacklisted
            if (jwtUtil.isTokenBlacklisted(refreshToken)) {
                logger.warn("Refresh attempt with blacklisted token");
                response.put("success", false);
                response.put("message", "Refresh token is invalid");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String username = jwtUtil.extractUsername(refreshToken);

            if (username != null && jwtUtil.validateToken(refreshToken, username)) {
                // Find user details
                Optional<User> userOpt = userService.findByUsername(username);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    // Check if user is still active
                    if (!user.isActive()) {
                        logger.warn("Token refresh attempt for inactive user: {}", username);
                        response.put("success", false);
                        response.put("message", "Account is inactive");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                    }

                    // Generate new tokens
                    String newToken = jwtUtil.generateToken(
                            user.getUsername(),
                            user.getUserId(),
                            user.getUserType().getValue()
                    );

                    String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());

                    // Blacklist old refresh token
                    jwtUtil.blacklistToken(refreshToken);

                    JwtResponse jwtResponse = new JwtResponse(
                            newToken,
                            newRefreshToken,
                            user.getUsername(),
                            user.getEmail(),
                            user.getFullName(),
                            user.getUserType().getValue(),
                            user.getUserId(),
                            jwtUtil.getExpirationMs()
                    );

                    response.put("success", true);
                    response.put("message", "Token refreshed successfully");
                    response.put("data", jwtResponse);
                    response.put("refreshInfo", Map.of(
                            "refreshTime", System.currentTimeMillis(),
                            "newExpiresAt", System.currentTimeMillis() + jwtUtil.getExpirationMs(),
                            "oldTokenBlacklisted", true
                    ));

                    logger.info("=== JWT TOKEN REFRESH SUCCESSFUL ===");
                    logger.info("Tokens refreshed for user: {}", username);

                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", "User not found");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
            } else {
                response.put("success", false);
                response.put("message", "Invalid refresh token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (Exception e) {
            logger.error("=== JWT TOKEN REFRESH ERROR ===");
            logger.error("Token refresh error: {}", e.getMessage(), e);

            response.put("success", false);
            response.put("message", "Token refresh failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Enhanced Token validation endpoint with detailed validation
     */
    @PostMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> tokenRequest,
                                                             HttpServletRequest request) {
        logger.info("=== JWT TOKEN VALIDATION ===");

        Map<String, Object> response = new HashMap<>();
        String token = tokenRequest.get("token");

        try {
            if (token == null || token.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Token is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if token is blacklisted
            if (jwtUtil.isTokenBlacklisted(token)) {
                logger.warn("Validation attempt with blacklisted token");
                response.put("success", false);
                response.put("message", "Token is blacklisted");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String username = jwtUtil.extractUsername(token);

            if (username != null && jwtUtil.validateToken(token, username)) {
                // Get user details
                Optional<User> userOpt = userService.findByUsername(username);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    // Check if user is active
                    if (!user.isActive()) {
                        logger.warn("Token validation for inactive user: {}", username);
                        response.put("success", false);
                        response.put("message", "Account is inactive");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                    }

                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("userId", user.getUserId());
                    userInfo.put("username", user.getUsername());
                    userInfo.put("email", user.getEmail());
                    userInfo.put("fullName", user.getFullName());
                    userInfo.put("userType", user.getUserType().getValue());
                    userInfo.put("isActive", user.isActive());

                    // Add token information
                    Map<String, Object> tokenInfo = new HashMap<>();
                    tokenInfo.put("issuedAt", jwtUtil.extractClaim(token, claims -> claims.getIssuedAt().getTime()));
                    tokenInfo.put("expiresAt", jwtUtil.extractExpiration(token).getTime());
                    tokenInfo.put("sessionId", jwtUtil.extractSessionId(token));
                    tokenInfo.put("timeToExpiry", jwtUtil.extractExpiration(token).getTime() - System.currentTimeMillis());

                    // Format validation time
                    String validatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a d MMM yyyy"));

                    response.put("success", true);
                    response.put("message", "Token is valid");
                    response.put("user", userInfo);
                    response.put("tokenInfo", tokenInfo);
                    response.put("validationInfo", Map.of(
                            "validatedAt", validatedAt,
                            "timestamp", System.currentTimeMillis()
                    ));

                    logger.debug("Token validated for user: {}", username);
                    return ResponseEntity.ok(response);
                }
            }

            response.put("success", false);
            response.put("message", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

        } catch (Exception e) {
            logger.error("Token validation error: {}", e.getMessage(), e);

            response.put("success", false);
            response.put("message", "Token validation failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Enhanced Logout endpoint with session management
     */
    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> logout(@RequestBody(required = false) Map<String, String> logoutRequest,
                                                      HttpServletRequest request) {
        logger.info("=== JWT LOGOUT ===");

        Map<String, Object> response = new HashMap<>();

        try {
            String token = null;
            String refreshToken = null;

            // Extract tokens from request body or Authorization header
            if (logoutRequest != null) {
                token = logoutRequest.get("token");
                refreshToken = logoutRequest.get("refreshToken");
            }

            // Fallback to Authorization header
            if (token == null) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }

            String username = null;
            if (token != null) {
                try {
                    username = jwtUtil.extractUsername(token);
                    // Blacklist the access token
                    jwtUtil.blacklistToken(token);
                } catch (Exception e) {
                    logger.warn("Error extracting username from token during logout: {}", e.getMessage());
                }
            }

            // Blacklist refresh token if provided
            if (refreshToken != null) {
                try {
                    jwtUtil.blacklistToken(refreshToken);
                } catch (Exception e) {
                    logger.warn("Error blacklisting refresh token during logout: {}", e.getMessage());
                }
            }

            // Invalidate all user sessions if username is available
            if (username != null) {
                sessionManagementService.invalidateAllUserSessions(username);
                logger.info("All sessions invalidated for user: {}", username);
            }

            response.put("success", true);
            response.put("message", "Logged out successfully. All tokens have been invalidated.");
            response.put("logoutInfo", Map.of(
                    "logoutTime", System.currentTimeMillis(),
                    "tokensBlacklisted", (token != null || refreshToken != null),
                    "allSessionsInvalidated", username != null
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Logout error: {}", e.getMessage());

            response.put("success", true); // Still return success for logout
            response.put("message", "Logged out with warnings. Please remove tokens from client storage.");
            response.put("warnings", List.of("Error during logout process: " + e.getMessage()));

            return ResponseEntity.ok(response);
        }
    }

    /**
     * Session statistics endpoint (admin only)
     */
    @GetMapping(value = "/session-stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getSessionStatistics(HttpServletRequest request) {
        logger.info("Session statistics requested");

        try {
            // Extract and validate JWT token from Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Authorization header required"));
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            String userType = jwtUtil.extractUserType(token);

            // Validate token and check admin privileges
            if (!jwtUtil.validateToken(token, username) || !"admin".equalsIgnoreCase(userType)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Admin access required"));
            }

            Map<String, Object> stats = sessionManagementService.getSessionStatistics();
            stats.put("requestedBy", username);
            stats.put("requestTime", System.currentTimeMillis());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Session statistics retrieved successfully",
                    "data", stats
            ));

        } catch (Exception e) {
            logger.error("Error retrieving session statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Failed to retrieve statistics"));
        }
    }
}