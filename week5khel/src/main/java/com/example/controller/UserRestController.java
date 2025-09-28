package com.example.controller;

import com.example.dto.UserDTO;
import com.example.model.User;
import com.example.model.UserType;
import com.example.service.UserService;
import com.example.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserRestController {

    private static final Logger logger = LoggerFactory.getLogger(UserRestController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Validate JWT token and extract user info
     */
    private Map<String, Object> validateToken(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            result.put("valid", false);
            result.put("error", "Authorization header required");
            return result;
        }

        String token = authHeader.substring(7);

        try {
            if (jwtUtil.isTokenBlacklisted(token)) {
                result.put("valid", false);
                result.put("error", "Token is blacklisted");
                return result;
            }

            String username = jwtUtil.extractUsername(token);
            if (!jwtUtil.validateToken(token, username)) {
                result.put("valid", false);
                result.put("error", "Invalid token");
                return result;
            }

            Long userId = jwtUtil.extractUserId(token);
            String userType = jwtUtil.extractUserType(token);

            result.put("valid", true);
            result.put("username", username);
            result.put("userId", userId);
            result.put("userType", userType);
            result.put("token", token);

            return result;
        } catch (Exception e) {
            logger.error("Token validation error: {}", e.getMessage());
            result.put("valid", false);
            result.put("error", "Token validation failed");
            return result;
        }
    }

    /**
     * Check if user has permission for the operation
     */
    private boolean hasPermission(Long tokenUserId, String tokenUserType, Long targetUserId, String operation) {
        // Admin users can perform any operation
        if ("admin".equalsIgnoreCase(tokenUserType)) {
            return true;
        }

        // Users can only modify their own data
        if ("PUT".equals(operation) || "DELETE".equals(operation)) {
            return tokenUserId.equals(targetUserId);
        }

        // GET operations allowed for all authenticated users
        return true;
    }

    /**
     * Health check endpoint
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> health() {
        logger.info("Health check requested");
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Khel App User API is running");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a d MMM yyyy")));
        return ResponseEntity.ok(response);
    }

    /**
     * Get all users (requires valid JWT token)
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getAllUsers(HttpServletRequest request) {
        logger.info("API: Fetching all users");

        Map<String, Object> response = new HashMap<>();

        // Validate JWT token
        Map<String, Object> tokenValidation = validateToken(request);
        if (!(boolean) tokenValidation.get("valid")) {
            response.put("success", false);
            response.put("message", tokenValidation.get("error"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            if (userService == null) {
                logger.error("UserService is null - dependency injection failed");
                response.put("success", false);
                response.put("message", "Internal server error: UserService not available");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            List<User> users = userService.findAllUsers();

            List<UserDTO> userDTOs = new ArrayList<>();
            if (users != null) {
                for (User user : users) {
                    UserDTO userDTO = new UserDTO(
                            user.getUserId(),
                            user.getUsername(),
                            user.getFullName(),
                            user.getEmail(),
                            user.getPhoneNumber(),
                            user.getUserType().getValue()
                    );
                    userDTOs.add(userDTO);
                }
            }

            response.put("success", true);
            response.put("message", "Users retrieved successfully");
            response.put("count", userDTOs.size());
            response.put("users", userDTOs);
            response.put("retrievedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a d MMM yyyy")));

            logger.info("API: Retrieved {} users", userDTOs.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("API: Error fetching users: {}", e.getMessage(), e);

            response.put("success", false);
            response.put("message", "Error fetching users: " + e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Register user (POST) - No token required for registration
     */
    @PostMapping(value = "",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody UserDTO userDTO) {
        logger.info("API: Processing user registration for: {}",
                userDTO != null ? userDTO.getUsername() : "null");

        Map<String, Object> response = new HashMap<>();

        try {
            if (userDTO == null) {
                response.put("success", false);
                response.put("message", "User data is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (userService == null) {
                logger.error("UserService is null during registration");
                response.put("success", false);
                response.put("message", "Internal server error: UserService not available");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            UserType userType = UserType.fromValue(userDTO.getUserType());

            User registeredUser = userService.registerUser(
                    userDTO.getUsername(),
                    userDTO.getFullName(),
                    userDTO.getEmail(),
                    userDTO.getPhoneNumber(),
                    userDTO.getPassword(),
                    userType
            );

            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("user", new UserDTO(
                    registeredUser.getUserId(),
                    registeredUser.getUsername(),
                    registeredUser.getFullName(),
                    registeredUser.getEmail(),
                    registeredUser.getPhoneNumber(),
                    registeredUser.getUserType().getValue()
            ));
            response.put("registeredAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a d MMM yyyy")));

            logger.info("API: User registered successfully: {}", registeredUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("API: Error registering user: {}", e.getMessage(), e);

            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get user by ID (requires valid JWT token)
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable("id") String id, HttpServletRequest request) {
        logger.info("API: Getting user by ID: {}", id);

        Map<String, Object> response = new HashMap<>();

        // Validate JWT token
        Map<String, Object> tokenValidation = validateToken(request);
        if (!(boolean) tokenValidation.get("valid")) {
            response.put("success", false);
            response.put("message", tokenValidation.get("error"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            if (userService == null) {
                logger.error("UserService is null");
                response.put("success", false);
                response.put("message", "Internal server error: UserService not available");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            Long userId = Long.parseLong(id);
            Long tokenUserId = (Long) tokenValidation.get("userId");
            String tokenUserType = (String) tokenValidation.get("userType");

            // Check permission
            if (!hasPermission(tokenUserId, tokenUserType, userId, "GET")) {
                response.put("success", false);
                response.put("message", "Not authorized to view this user");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            Optional<User> userOpt = userService.findById(userId);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                response.put("success", true);
                response.put("message", "User found");
                response.put("user", new UserDTO(
                        user.getUserId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getUserType().getValue()
                ));
                response.put("retrievedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a d MMM yyyy")));

                logger.info("API: User found: {}", user.getUsername());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "User not found with ID: " + id);

                logger.info("API: User not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (NumberFormatException e) {
            logger.error("API: Invalid user ID format: {}", id);
            response.put("success", false);
            response.put("message", "Invalid user ID format: " + id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            logger.error("API: Error getting user by ID {}: {}", id, e.getMessage(), e);

            response.put("success", false);
            response.put("message", "Error getting user: " + e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update user (PUT) - Requires valid JWT token and proper authorization
     */
    @PutMapping(value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable("id") String id,
                                                          @RequestBody UserDTO userDTO,
                                                          HttpServletRequest request) {
        logger.info("API: Updating user with ID: {}", id);

        Map<String, Object> response = new HashMap<>();

        // Validate JWT token first
        Map<String, Object> tokenValidation = validateToken(request);
        if (!(boolean) tokenValidation.get("valid")) {
            response.put("success", false);
            response.put("message", "Authentication required: " + tokenValidation.get("error"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            if (userService == null) {
                logger.error("UserService is null");
                response.put("success", false);
                response.put("message", "Internal server error: UserService not available");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            if (userDTO == null) {
                response.put("success", false);
                response.put("message", "User data is required");
                return ResponseEntity.badRequest().body(response);
            }

            Long userId = Long.parseLong(id);
            Long tokenUserId = (Long) tokenValidation.get("userId");
            String tokenUserType = (String) tokenValidation.get("userType");

            // Check authorization for update operation
            if (!hasPermission(tokenUserId, tokenUserType, userId, "PUT")) {
                logger.warn("Unauthorized update attempt: User {} trying to update user {}", tokenUserId, userId);
                response.put("success", false);
                response.put("message", "Not authorized to update this user");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Check if user exists
            Optional<User> existingUserOpt = userService.findById(userId);
            if (existingUserOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User existingUser = existingUserOpt.get();

            // Update user fields
            existingUser.setUsername(userDTO.getUsername());
            existingUser.setFullName(userDTO.getFullName());
            existingUser.setEmail(userDTO.getEmail());
            existingUser.setPhoneNumber(userDTO.getPhoneNumber());
            existingUser.setUserType(UserType.fromValue(userDTO.getUserType()));

            // Don't update password through PUT - handle separately

            User updatedUser = userService.updateUser(existingUser);

            response.put("success", true);
            response.put("message", "User updated successfully");
            response.put("user", new UserDTO(
                    updatedUser.getUserId(),
                    updatedUser.getUsername(),
                    updatedUser.getFullName(),
                    updatedUser.getEmail(),
                    updatedUser.getPhoneNumber(),
                    updatedUser.getUserType().getValue()
            ));
            response.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a d MMM yyyy")));

            logger.info("API: User updated successfully: {}", updatedUser.getUsername());
            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            logger.error("API: Invalid user ID format: {}", id);
            response.put("success", false);
            response.put("message", "Invalid user ID format: " + id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            logger.error("API: Error updating user {}: {}", id, e.getMessage(), e);

            response.put("success", false);
            response.put("message", "Update failed: " + e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Delete user (DELETE) - Requires valid JWT token and proper authorization
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable("id") String id, HttpServletRequest request) {
        logger.info("API: Deleting user with ID: {}", id);

        Map<String, Object> response = new HashMap<>();

        // Validate JWT token first
        Map<String, Object> tokenValidation = validateToken(request);
        if (!(boolean) tokenValidation.get("valid")) {
            response.put("success", false);
            response.put("message", "Authentication required: " + tokenValidation.get("error"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            if (userService == null) {
                logger.error("UserService is null");
                response.put("success", false);
                response.put("message", "Internal server error: UserService not available");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            Long userId = Long.parseLong(id);
            Long tokenUserId = (Long) tokenValidation.get("userId");
            String tokenUserType = (String) tokenValidation.get("userType");

            // Check authorization for delete operation
            if (!hasPermission(tokenUserId, tokenUserType, userId, "DELETE")) {
                logger.warn("Unauthorized delete attempt: User {} trying to delete user {}", tokenUserId, userId);
                response.put("success", false);
                response.put("message", "Not authorized to delete this user");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Check if user exists before deletion
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User userToDelete = userOpt.get();
            boolean deleted = userService.deleteUser(userId);

            if (deleted) {
                response.put("success", true);
                response.put("message", "User deleted successfully");
                response.put("deletedUser", new UserDTO(
                        userToDelete.getUserId(),
                        userToDelete.getUsername(),
                        userToDelete.getFullName(),
                        userToDelete.getEmail(),
                        userToDelete.getPhoneNumber(),
                        userToDelete.getUserType().getValue()
                ));
                response.put("deletedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a d MMM yyyy")));

                logger.info("API: User deleted successfully: {}", userToDelete.getUsername());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to delete user with ID: " + id);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (NumberFormatException e) {
            logger.error("API: Invalid user ID format: {}", id);
            response.put("success", false);
            response.put("message", "Invalid user ID format: " + id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            logger.error("API: Error deleting user {}: {}", id, e.getMessage(), e);

            response.put("success", false);
            response.put("message", "Delete failed: " + e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Login endpoint - No token required
     */
    @PostMapping(value = "/login",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData != null ? loginData.get("username") : null;
        String password = loginData != null ? loginData.get("password") : null;

        logger.info("API: Login attempt for username: {}", username);

        Map<String, Object> response = new HashMap<>();

        try {
            if (username == null || password == null) {
                response.put("success", false);
                response.put("message", "Username and password are required");
                return ResponseEntity.badRequest().body(response);
            }

            if (userService == null) {
                logger.error("UserService is null during login");
                response.put("success", false);
                response.put("message", "Internal server error: UserService not available");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            Optional<User> userOpt = userService.authenticateUser(username, password);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("user", new UserDTO(
                        user.getUserId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getUserType().getValue()
                ));
                response.put("loginTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a d MMM yyyy")));

                logger.info("API: Login successful for user: {}", username);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid username or password");

                logger.warn("API: Login failed for username: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (Exception e) {
            logger.error("API: Login error for username {}: {}", username, e.getMessage(), e);

            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Login with email endpoint - No token required
     */
    @PostMapping(value = "/login-email",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> loginWithEmail(@RequestBody Map<String, String> loginData) {
        String email = loginData != null ? loginData.get("email") : null;
        String password = loginData != null ? loginData.get("password") : null;

        logger.info("API: Login attempt for email: {}", email);

        Map<String, Object> response = new HashMap<>();

        try {
            if (email == null || password == null) {
                response.put("success", false);
                response.put("message", "Email and password are required");
                return ResponseEntity.badRequest().body(response);
            }

            if (userService == null) {
                logger.error("UserService is null during email login");
                response.put("success", false);
                response.put("message", "Internal server error: UserService not available");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            Optional<User> userOpt = userService.authenticateUserByEmail(email, password);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("user", new UserDTO(
                        user.getUserId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getUserType().getValue()
                ));
                response.put("loginTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a d MMM yyyy")));

                logger.info("API: Email login successful for user: {}", user.getUsername());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid email or password");

                logger.warn("API: Email login failed for email: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (Exception e) {
            logger.error("API: Email login error for email {}: {}", email, e.getMessage(), e);

            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Change password endpoint - Requires valid JWT token and authorization
     */
    @PostMapping(value = "/{id}/change-password",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> changePassword(@PathVariable("id") String id,
                                                              @RequestBody Map<String, String> passwordData,
                                                              HttpServletRequest request) {
        logger.info("API: Password change attempt for user ID: {}", id);

        Map<String, Object> response = new HashMap<>();

        // Validate JWT token first
        Map<String, Object> tokenValidation = validateToken(request);
        if (!(boolean) tokenValidation.get("valid")) {
            response.put("success", false);
            response.put("message", "Authentication required: " + tokenValidation.get("error"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            Long userId = Long.parseLong(id);
            Long tokenUserId = (Long) tokenValidation.get("userId");
            String tokenUserType = (String) tokenValidation.get("userType");

            // Check authorization
            if (!hasPermission(tokenUserId, tokenUserType, userId, "PUT")) {
                logger.warn("Unauthorized password change attempt: User {} trying to change password for user {}", tokenUserId, userId);
                response.put("success", false);
                response.put("message", "Not authorized to change password for this user");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            String currentPassword = passwordData != null ? passwordData.get("currentPassword") : null;
            String newPassword = passwordData != null ? passwordData.get("newPassword") : null;

            if (currentPassword == null || newPassword == null) {
                response.put("success", false);
                response.put("message", "Current password and new password are required");
                return ResponseEntity.badRequest().body(response);
            }

            boolean changed = userService.changePassword(userId, currentPassword, newPassword);

            if (changed) {
                response.put("success", true);
                response.put("message", "Password changed successfully");
                response.put("changedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a d MMM yyyy")));
                logger.info("API: Password changed successfully for user ID: {}", userId);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to change password. Check your current password.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (NumberFormatException e) {
            logger.error("API: Invalid user ID format: {}", id);
            response.put("success", false);
            response.put("message", "Invalid user ID format: " + id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            logger.error("API: Password change error for user {}: {}", id, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Password change failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Debug endpoint to check service availability
     */
    @GetMapping(value = "/debug", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> debug() {
        Map<String, Object> response = new HashMap<>();
        response.put("controller", "UserRestController is working");
        response.put("userService", userService != null ? "Available" : "NULL - Check configuration");
        response.put("timestamp", System.currentTimeMillis());
        response.put("debugTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a d MMM yyyy")));

        logger.info("Debug endpoint called - UserService is {}",
                userService != null ? "available" : "null");

        return ResponseEntity.ok(response);
    }
}