package com.example.controller;

import com.example.dto.UserDTO;
import com.example.model.User;
import com.example.model.UserType;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     * Health check endpoint
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> health() {
        logger.info("Health check requested");
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Khel App User API is running");
        return ResponseEntity.ok(response);
    }

    /**
     * Get all users
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        logger.info("API: Fetching all users");

        Map<String, Object> response = new HashMap<>();

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
     * Register user (POST)
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
     * Get user by ID
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable("id") String id) {
        logger.info("API: Getting user by ID: {}", id);

        Map<String, Object> response = new HashMap<>();

        try {
            if (userService == null) {
                logger.error("UserService is null");
                response.put("success", false);
                response.put("message", "Internal server error: UserService not available");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            Long userId = Long.parseLong(id);
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
     * Update user (PUT)
     */
    @PutMapping(value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable("id") String id, @RequestBody UserDTO userDTO) {
        logger.info("API: Updating user with ID: {}", id);

        Map<String, Object> response = new HashMap<>();

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
     * Delete user (DELETE)
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable("id") String id) {
        logger.info("API: Deleting user with ID: {}", id);

        Map<String, Object> response = new HashMap<>();

        try {
            if (userService == null) {
                logger.error("UserService is null");
                response.put("success", false);
                response.put("message", "Internal server error: UserService not available");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            Long userId = Long.parseLong(id);

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
     * Login endpoint
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
     * Login with email endpoint
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
     * Change password endpoint
     */
    @PostMapping(value = "/{id}/change-password",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> changePassword(@PathVariable("id") String id,
                                                              @RequestBody Map<String, String> passwordData) {
        logger.info("API: Password change attempt for user ID: {}", id);

        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = Long.parseLong(id);
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

        logger.info("Debug endpoint called - UserService is {}",
                userService != null ? "available" : "null");

        return ResponseEntity.ok(response);
    }
}