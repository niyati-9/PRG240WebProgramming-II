package com.example.service;

import com.example.model.User;
import com.example.model.UserType;
import java.util.List;
import java.util.Optional;

public interface UserService {

    /**
     * Authenticate user with username and password
     * @param username username
     * @param password user password (plain text)
     * @return Optional<User> if authentication successful
     */
    Optional<User> authenticateUser(String username, String password);

    /**
     * Authenticate user with email and password
     * @param email user email
     * @param password user password (plain text)
     * @return Optional<User> if authentication successful
     */
    Optional<User> authenticateUserByEmail(String email, String password);

    /**
     * Register a new user
     * @param username unique username
     * @param fullName user's full name
     * @param email user's email
     * @param phoneNumber user's phone number
     * @param password user's password (plain text)
     * @param userType type of user (PLAYER, COACH, ADMIN)
     * @return User object if registration successful
     * @throws RuntimeException if user already exists or validation fails
     */
    User registerUser(String username, String fullName, String email, String phoneNumber,
                      String password, UserType userType);

    /**
     * Change user password
     * @param userId user ID
     * @param currentPassword current password (plain text)
     * @param newPassword new password (plain text)
     * @return true if password changed successfully
     */
    boolean changePassword(Long userId, String currentPassword, String newPassword);

    // User management methods
    /**
     * Find user by ID
     * @param id user ID
     * @return Optional<User>
     */
    Optional<User> findById(Long id);

    /**
     * Find user by username
     * @param username username
     * @return Optional<User>
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     * @param email user email
     * @return Optional<User>
     */
    Optional<User> findByEmail(String email);

    /**
     * Get all users
     * @return List<User>
     */
    List<User> findAllUsers();

    /**
     * Get users by type
     * @param userType user type filter
     * @return List<User>
     */
    List<User> findByUserType(UserType userType);

    /**
     * Update user profile
     * @param user updated user object
     * @return updated User
     */
    User updateUser(User user);

    /**
     * Delete user by ID
     * @param userId user ID
     * @return true if deleted successfully
     */
    boolean deleteUser(Long userId);

    // Validation methods
    /**
     * Check if username is already registered
     * @param username username to check
     * @return true if username exists
     */
    boolean isUsernameExists(String username);

    /**
     * Check if email is already registered
     * @param email email to check
     * @return true if email exists
     */
    boolean isEmailExists(String email);

    /**
     * Validate password strength
     * @param password password to validate
     * @return true if password meets requirements
     */
    boolean isValidPassword(String password);

    // Statistics
    /**
     * Get total number of users
     * @return total user count
     */
    long getTotalUserCount();
}
