package com.example.service;

import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.model.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Password requirements
    private static final int MIN_PASSWORD_LENGTH = 6;

    @Override
    public Optional<User> authenticateUser(String username, String password) {
        logger.info("=== USER AUTHENTICATION ATTEMPT ===");
        logger.info("Attempting authentication for username: {}", username);

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            logger.warn("Authentication failed: Missing username or password");
            return Optional.empty();
        }

        Optional<User> userOpt = userDAO.findByUsername(username);
        if (userOpt.isEmpty()) {
            logger.warn("Authentication failed: User not found for username {}", username);
            return Optional.empty();
        }

        User user = userOpt.get();
        if (!user.isActive()) {
            logger.warn("Authentication failed: User {} is inactive", username);
            return Optional.empty();
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Authentication failed: Invalid password for user {}", username);
            return Optional.empty();
        }

        logger.info("=== USER AUTHENTICATION SUCCESSFUL ===");
        logger.info("User {} authenticated successfully", username);
        return Optional.of(user);
    }

    @Override
    public Optional<User> authenticateUserByEmail(String email, String password) {
        logger.info("=== USER EMAIL AUTHENTICATION ATTEMPT ===");
        logger.info("Attempting authentication for email: {}", email);

        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            logger.warn("Authentication failed: Missing email or password");
            return Optional.empty();
        }

        Optional<User> userOpt = userDAO.findByEmail(email.toLowerCase());
        if (userOpt.isEmpty()) {
            logger.warn("Authentication failed: User not found for email {}", email);
            return Optional.empty();
        }

        User user = userOpt.get();
        if (!user.isActive()) {
            logger.warn("Authentication failed: User {} is inactive", email);
            return Optional.empty();
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Authentication failed: Invalid password for user {}", email);
            return Optional.empty();
        }

        logger.info("=== USER EMAIL AUTHENTICATION SUCCESSFUL ===");
        logger.info("User {} authenticated successfully via email", user.getUsername());
        return Optional.of(user);
    }

    @Override
    public User registerUser(String username, String fullName, String email, String phoneNumber,
                             String password, UserType userType) {

        logger.info("=== USER REGISTRATION ATTEMPT ===");
        logger.info("Attempting to register user: Username={}, Email={}, UserType={}",
                username, email, userType);

        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }
        if (!isValidEmail(email)) {
            throw new RuntimeException("Invalid email format");
        }
        if (!isValidPassword(password)) {
            throw new RuntimeException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (userType == null) {
            userType = UserType.PLAYER; // Default to player
        }

        // Check for existing users
        if (isUsernameExists(username)) {
            throw new RuntimeException("Username is already taken");
        }
        if (isEmailExists(email)) {
            throw new RuntimeException("Email is already registered");
        }

        // Create new user
        User newUser = new User(username.trim(), fullName.trim(), email.toLowerCase(),
                phoneNumber, passwordEncoder.encode(password), userType);

        // Save to database
        User savedUser = userDAO.save(newUser);

        logger.info("=== USER REGISTRATION SUCCESSFUL ===");
        logger.info("New user registered: {} with username {}", fullName, username);
        return savedUser;
    }

    @Override
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        logger.info("Attempting to change password for user ID: {}", userId);

        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            logger.warn("Change password failed: User not found with ID {}", userId);
            return false;
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            logger.warn("Change password failed: Current password incorrect for user {}", userId);
            return false;
        }

        if (!isValidPassword(newPassword)) {
            logger.warn("Change password failed: New password doesn't meet requirements for user {}", userId);
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userDAO.update(user);

        logger.info("Password changed successfully for user {}", userId);
        return true;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userDAO.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return userDAO.findByEmail(email.toLowerCase());
    }

    @Override
    public List<User> findAllUsers() {
        return userDAO.findAll();
    }

    @Override
    public List<User> findByUserType(UserType userType) {
        return userDAO.findAll().stream()
                .filter(user -> user.getUserType() == userType)
                .toList();
    }

    @Override
    public User updateUser(User user) {
        if (user == null || user.getUserId() == null) {
            throw new RuntimeException("Invalid user for update");
        }

        Optional<User> existingUserOpt = userDAO.findById(user.getUserId());
        if (existingUserOpt.isEmpty()) {
            throw new RuntimeException("User not found for update");
        }

        User existingUser = existingUserOpt.get();

        // Check if username is being changed and if it's available
        if (!existingUser.getUsername().equals(user.getUsername())) {
            if (isUsernameExists(user.getUsername())) {
                throw new RuntimeException("Username is already taken by another user");
            }
        }

        // Check if email is being changed and if it's available
        if (!existingUser.getEmail().equals(user.getEmail())) {
            if (isEmailExists(user.getEmail())) {
                throw new RuntimeException("Email is already taken by another user");
            }
        }

        User updatedUser = userDAO.update(user);
        logger.info("User {} updated successfully", user.getUserId());
        return updatedUser;
    }

    @Override
    public boolean deleteUser(Long userId) {
        logger.info("Attempting to delete user with ID: {}", userId);

        boolean deleted = userDAO.deleteById(userId);
        if (deleted) {
            logger.info("User {} deleted successfully", userId);
        } else {
            logger.warn("Delete failed: User not found with ID {}", userId);
        }
        return deleted;
    }

    @Override
    public boolean isUsernameExists(String username) {
        if (username == null) return false;
        return userDAO.existsByUsername(username);
    }

    @Override
    public boolean isEmailExists(String email) {
        if (email == null) return false;
        return userDAO.existsByEmail(email.toLowerCase());
    }

    @Override
    public boolean isValidPassword(String password) {
        if (password == null) return false;
        return password.length() >= MIN_PASSWORD_LENGTH;
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    @Override
    public long getTotalUserCount() {
        return userDAO.findAll().size();
    }
}
