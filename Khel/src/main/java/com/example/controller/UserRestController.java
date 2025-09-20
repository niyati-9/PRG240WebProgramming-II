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
            // Check if userService is properly injected
            if (userService == null) {
                logger.error("UserService is null - dependency injection failed");
                response.put("success", false);
                response.put("message", "Internal server error: UserService not available");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            List<User> users = userService.findAllUsers();

            // Convert User objects to UserDTO objects to avoid serialization issues
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
     * REST API endpoint to register user with JSON data
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
            return ResponseEntity.ok(response);

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