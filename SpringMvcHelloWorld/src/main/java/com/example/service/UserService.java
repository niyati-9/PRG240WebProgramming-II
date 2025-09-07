package com.example.service;

import com.example.model.User;
import com.example.model.UserType;
import java.util.List;
import java.util.Optional;

public interface UserService {

    // Authentication methods
    /**
     * Authenticate user with email and password
     * @param email user email
     * @param password user password (plain text)
     * @return Optional<User> if authentication successful
     */
    Optional<User> authenticateUser(String email, String password);

    /**
     * Register a new user
     * @param fullName user's full name
     * @param email user's email
     * @param phone user's phone number
     * @param password user's password (plain text)
     * @param userType type of user (PLAYER, VENUE_OWNER)
     * @param receiveNewsletter whether user wants newsletter
     * @return User object if registration successful
     * @throws RuntimeException if user already exists or validation fails
     */
    User registerUser(String fullName, String email, String phone, String password,
                      UserType userType, boolean receiveNewsletter);

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
     * Find user by email
     * @param email user email
     * @return Optional<User>
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by phone number
     * @param phone user phone
     * @return Optional<User>
     */
    Optional<User> findByPhone(String phone);

    /**
     * Get all users with pagination
     * @param page page number (0-based)
     * @param size page size
     * @return List<User>
     */
    List<User> findAllUsers(int page, int size);

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
     * Activate or deactivate user
     * @param userId user ID
     * @param active true to activate, false to deactivate
     * @return true if successful
     */
    boolean setUserActive(Long userId, boolean active);

    /**
     * Delete user by ID
     * @param userId user ID
     * @return true if deleted successfully
     */
    boolean deleteUser(Long userId);

    // Validation methods
    /**
     * Check if email is already registered
     * @param email email to check
     * @return true if email exists
     */
    boolean isEmailExists(String email);

    /**
     * Check if phone is already registered
     * @param phone phone to check
     * @return true if phone exists
     */
    boolean isPhoneExists(String phone);

    /**
     * Validate email format
     * @param email email to validate
     * @return true if valid format
     */
    boolean isValidEmail(String email);

    /**
     * Validate phone format
     * @param phone phone to validate
     * @return true if valid format
     */
    boolean isValidPhone(String phone);

    /**
     * Validate password strength
     * @param password password to validate
     * @return true if password meets requirements
     */
    boolean isValidPassword(String password);

    // Email and phone verification
    /**
     * Send email verification
     * @param userId user ID
     * @return true if verification email sent
     */
    boolean sendEmailVerification(Long userId);

    /**
     * Verify email with token
     * @param userId user ID
     * @param token verification token
     * @return true if email verified successfully
     */
    boolean verifyEmail(Long userId, String token);

    /**
     * Send phone verification SMS
     * @param userId user ID
     * @return true if verification SMS sent
     */
    boolean sendPhoneVerification(Long userId);

    /**
     * Verify phone with OTP
     * @param userId user ID
     * @param otp verification OTP
     * @return true if phone verified successfully
     */
    boolean verifyPhone(Long userId, String otp);

    // Password reset
    /**
     * Send password reset email
     * @param email user email
     * @return true if reset email sent
     */
    boolean sendPasswordResetEmail(String email);

    /**
     * Reset password with token
     * @param email user email
     * @param token reset token
     * @param newPassword new password
     * @return true if password reset successfully
     */
    boolean resetPassword(String email, String token, String newPassword);

    // Statistics and reporting
    /**
     * Get total number of users
     * @return total user count
     */
    long getTotalUserCount();

    /**
     * Get number of users by type
     * @param userType user type
     * @return count of users
     */
    long getUserCountByType(UserType userType);

    /**
     * Get number of active users
     * @return count of active users
     */
    long getActiveUserCount();

    /**
     * Get recently registered users
     * @param days number of days back
     * @return List<User>
     */
    List<User> getRecentlyRegisteredUsers(int days);
}