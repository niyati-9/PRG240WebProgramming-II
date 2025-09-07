package com.example.service;

import com.example.model.User;
import com.example.model.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    // In-memory storage for demo purposes
    private final Map<Long, User> userStorage = new ConcurrentHashMap<>();
    private final Map<String, User> emailIndex = new ConcurrentHashMap<>();
    private final Map<String, User> phoneIndex = new ConcurrentHashMap<>();
    private Long nextId = 1L;

    // Email validation pattern
    private static final java.util.regex.Pattern EMAIL_PATTERN =
            java.util.regex.Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Password requirements
    private static final int MIN_PASSWORD_LENGTH = 6;

    // Demo data initialization
    public UserServiceImpl() {
        initializeDemoData();
    }

    private void initializeDemoData() {
        // Create demo admin user
        User admin = new User("Niyati Admin User", "admin@khel.com.np", "1234567890",
                hashPassword("admin123"), UserType.ADMIN);
        admin.setId(nextId++);
        admin.setEmailVerified(true);
        admin.setPhoneVerified(true);
        admin.setCity("Kathmandu");
        userStorage.put(admin.getId(), admin);
        emailIndex.put(admin.getEmail().toLowerCase(), admin);
        phoneIndex.put(admin.getPhone(), admin);

        // Create demo player
        User player = new User("John Doe", "player@example.com", "9876543210",
                hashPassword("player123"), UserType.PLAYER);
        player.setId(nextId++);
        player.setEmailVerified(true);
        player.setCity("Pokhara");
        userStorage.put(player.getId(), player);
        emailIndex.put(player.getEmail().toLowerCase(), player);
        phoneIndex.put(player.getPhone(), player);

        // Create demo venue owner
        User owner = new User("Jane Smith", "owner@example.com", "9123456789",
                hashPassword("owner123"), UserType.VENUE_OWNER);
        owner.setId(nextId++);
        owner.setEmailVerified(true);
        owner.setCity("Lalitpur");
        userStorage.put(owner.getId(), owner);
        emailIndex.put(owner.getEmail().toLowerCase(), owner);
        phoneIndex.put(owner.getPhone(), owner);

        logger.info("Initialized demo data with {} users", userStorage.size());
    }

    @Override
    public Optional<User> authenticateUser(String email, String password) {
        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            logger.warn("Authentication failed: Missing email or password");
            return Optional.empty();
        }

        User user = emailIndex.get(email.toLowerCase());
        if (user == null) {
            logger.warn("Authentication failed: User not found for email {}", email);
            return Optional.empty();
        }

        if (!user.canLogin()) {
            logger.warn("Authentication failed: User {} cannot login (inactive or unverified)", email);
            return Optional.empty();
        }

        if (!verifyPassword(password, user.getPassword())) {
            logger.warn("Authentication failed: Invalid password for user {}", email);
            return Optional.empty();
        }

        user.setLastLoginAt(LocalDateTime.now());

        logger.info("User {} authenticated successfully", email);
        return Optional.of(user);
    }

    @Override
    public User registerUser(String fullName, String email, String phone, String password,
                             UserType userType, boolean receiveNewsletter) {

        // Validate input
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }
        if (!isValidEmail(email)) {
            throw new RuntimeException("Invalid email format");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new RuntimeException("Phone number is required");
        }
        if (!isValidPassword(password)) {
            throw new RuntimeException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (userType == null) {
            throw new RuntimeException("User type is required");
        }

        if (isEmailExists(email)) {
            throw new RuntimeException("Email is already registered");
        }
        if (isPhoneExists(phone)) {
            throw new RuntimeException("Phone number is already registered");
        }

        User newUser = new User(fullName.trim(), email.toLowerCase(), phone,
                hashPassword(password), userType);
        newUser.setId(nextId++);
        newUser.setReceiveNewsletter(receiveNewsletter);

        userStorage.put(newUser.getId(), newUser);
        emailIndex.put(newUser.getEmail(), newUser);
        phoneIndex.put(newUser.getPhone(), newUser);

        sendEmailVerification(newUser.getId());

        logger.info("New user registered: {} with email {}", fullName, email);
        return newUser;
    }

    @Override
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userStorage.get(userId);
        if (user == null) {
            logger.warn("Change password failed: User not found with ID {}", userId);
            return false;
        }

        if (!verifyPassword(currentPassword, user.getPassword())) {
            logger.warn("Change password failed: Current password incorrect for user {}", userId);
            return false;
        }

        if (!isValidPassword(newPassword)) {
            logger.warn("Change password failed: New password doesn't meet requirements for user {}", userId);
            return false;
        }

        user.setPassword(hashPassword(newPassword));
        logger.info("Password changed successfully for user {}", userId);
        return true;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userStorage.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return Optional.ofNullable(emailIndex.get(email.toLowerCase()));
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        if (phone == null) return Optional.empty();
        return Optional.ofNullable(phoneIndex.get(phone));
    }

    @Override
    public List<User> findAllUsers(int page, int size) {
        return userStorage.values().stream()
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByUserType(UserType userType) {
        return userStorage.values().stream()
                .filter(user -> user.getUserType() == userType)
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public User updateUser(User user) {
        if (user == null || user.getId() == null) {
            throw new RuntimeException("Invalid user for update");
        }

        User existingUser = userStorage.get(user.getId());
        if (existingUser == null) {
            throw new RuntimeException("User not found for update");
        }

        if (!existingUser.getEmail().equals(user.getEmail())) {
            emailIndex.remove(existingUser.getEmail());
            if (isEmailExists(user.getEmail())) {
                throw new RuntimeException("Email is already taken by another user");
            }
            emailIndex.put(user.getEmail().toLowerCase(), user);
        }

        if (!existingUser.getPhone().equals(user.getPhone())) {
            phoneIndex.remove(existingUser.getPhone());
            if (isPhoneExists(user.getPhone())) {
                throw new RuntimeException("Phone is already taken by another user");
            }
            phoneIndex.put(user.getPhone(), user);
        }

        user.setUpdatedAt(LocalDateTime.now());
        userStorage.put(user.getId(), user);

        logger.info("User {} updated successfully", user.getId());
        return user;
    }

    @Override
    public boolean setUserActive(Long userId, boolean active) {
        User user = userStorage.get(userId);
        if (user == null) {
            logger.warn("Set active failed: User not found with ID {}", userId);
            return false;
        }

        user.setActive(active);
        logger.info("User {} set to {}", userId, active ? "active" : "inactive");
        return true;
    }

    @Override
    public boolean deleteUser(Long userId) {
        User user = userStorage.get(userId);
        if (user == null) {
            logger.warn("Delete failed: User not found with ID {}", userId);
            return false;
        }

        userStorage.remove(userId);
        emailIndex.remove(user.getEmail());
        phoneIndex.remove(user.getPhone());

        logger.info("User {} deleted successfully", userId);
        return true;
    }

    @Override
    public boolean isEmailExists(String email) {
        if (email == null) return false;
        return emailIndex.containsKey(email.toLowerCase());
    }

    @Override
    public boolean isPhoneExists(String phone) {
        if (phone == null) return false;
        return phoneIndex.containsKey(phone);
    }

    @Override
    public boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    @Override
    public boolean isValidPhone(String phone) {
        return phone != null && !phone.isEmpty(); // Only checks presence
    }

    @Override
    public boolean isValidPassword(String password) {
        if (password == null) return false;
        return password.length() >= MIN_PASSWORD_LENGTH;
    }

    @Override
    public boolean sendEmailVerification(Long userId) {
        User user = userStorage.get(userId);
        if (user == null) {
            logger.warn("Send email verification failed: User not found with ID {}", userId);
            return false;
        }

        String verificationToken = generateVerificationToken();
        logger.info("Email verification sent to {} with token: {}", user.getEmail(), verificationToken);

        user.setEmailVerified(true); // auto-verify for demo
        return true;
    }

    @Override
    public boolean verifyEmail(Long userId, String token) {
        User user = userStorage.get(userId);
        if (user == null) {
            logger.warn("Email verification failed: User not found with ID {}", userId);
            return false;
        }

        user.setEmailVerified(true);
        logger.info("Email verified for user {}", userId);
        return true;
    }

    @Override
    public boolean sendPhoneVerification(Long userId) {
        User user = userStorage.get(userId);
        if (user == null) {
            logger.warn("Send phone verification failed: User not found with ID {}", userId);
            return false;
        }

        String otp = generateOTP();
        logger.info("Phone verification SMS sent to {} with OTP: {}", user.getPhone(), otp);
        return true;
    }

    @Override
    public boolean verifyPhone(Long userId, String otp) {
        User user = userStorage.get(userId);
        if (user == null) {
            logger.warn("Phone verification failed: User not found with ID {}", userId);
            return false;
        }

        user.setPhoneVerified(true);
        logger.info("Phone verified for user {}", userId);
        return true;
    }

    @Override
    public boolean sendPasswordResetEmail(String email) {
        if (!isEmailExists(email)) {
            logger.warn("Password reset failed: Email {} not found", email);
            return false;
        }

        String resetToken = generateResetToken();
        logger.info("Password reset email sent to {} with token: {}", email, resetToken);
        return true;
    }

    @Override
    public boolean resetPassword(String email, String token, String newPassword) {
        User user = emailIndex.get(email.toLowerCase());
        if (user == null) {
            logger.warn("Password reset failed: User not found with email {}", email);
            return false;
        }

        if (!isValidPassword(newPassword)) {
            logger.warn("Password reset failed: New password doesn't meet requirements");
            return false;
        }

        user.setPassword(hashPassword(newPassword));
        logger.info("Password reset successful for user {}", email);
        return true;
    }

    @Override
    public long getTotalUserCount() {
        return userStorage.size();
    }

    @Override
    public long getUserCountByType(UserType userType) {
        return userStorage.values().stream()
                .filter(user -> user.getUserType() == userType)
                .count();
    }

    @Override
    public long getActiveUserCount() {
        return userStorage.values().stream()
                .filter(User::isActive)
                .count();
    }

    @Override
    public List<User> getRecentlyRegisteredUsers(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return userStorage.values().stream()
                .filter(user -> user.getCreatedAt().isAfter(cutoffDate))
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // Utility methods

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private boolean verifyPassword(String password, String hash) {
        return hashPassword(password).equals(hash);
    }

    private String generateVerificationToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String generateOTP() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }

    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", getTotalUserCount());
        stats.put("activeUsers", getActiveUserCount());
        stats.put("players", getUserCountByType(UserType.PLAYER));
        stats.put("venueOwners", getUserCountByType(UserType.VENUE_OWNER));
        stats.put("admins", getUserCountByType(UserType.ADMIN));
        stats.put("recentRegistrations", getRecentlyRegisteredUsers(7).size());

        long verifiedEmails = userStorage.values().stream()
                .filter(User::isEmailVerified)
                .count();
        stats.put("verifiedEmails", verifiedEmails);

        long verifiedPhones = userStorage.values().stream()
                .filter(User::isPhoneVerified)
                .count();
        stats.put("verifiedPhones", verifiedPhones);

        return stats;
    }

    public List<User> searchUsers(String query, int page, int size) {
        if (query == null || query.trim().isEmpty()) {
            return findAllUsers(page, size);
        }

        String searchQuery = query.toLowerCase().trim();
        return userStorage.values().stream()
                .filter(user ->
                        user.getFullName().toLowerCase().contains(searchQuery) ||
                                user.getEmail().toLowerCase().contains(searchQuery)
                )
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    public List<User> getUsersByCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return userStorage.values().stream()
                .filter(user -> city.equalsIgnoreCase(user.getCity()))
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public List<User> getNewsletterSubscribers() {
        return userStorage.values().stream()
                .filter(User::isReceiveNewsletter)
                .filter(User::isEmailVerified)
                .filter(User::isActive)
                .collect(Collectors.toList());
    }

    public int bulkUpdateUserStatus(List<Long> userIds, boolean active) {
        int updatedCount = 0;
        for (Long userId : userIds) {
            if (setUserActive(userId, active)) {
                updatedCount++;
            }
        }
        logger.info("Bulk updated {} users to {} status", updatedCount, active ? "active" : "inactive");
        return updatedCount;
    }

    public String generateUserReport() {
        StringBuilder report = new StringBuilder();
        Map<String, Object> stats = getUserStatistics();

        report.append("=== KHEL USER REPORT ===\n");
        report.append("Generated at: ").append(LocalDateTime.now()).append("\n\n");

        report.append("OVERVIEW:\n");
        report.append("- Total Users: ").append(stats.get("totalUsers")).append("\n");
        report.append("- Active Users: ").append(stats.get("activeUsers")).append("\n");
        report.append("- Recent Registrations (7 days): ").append(stats.get("recentRegistrations")).append("\n\n");

        report.append("BY USER TYPE:\n");
        report.append("- Players: ").append(stats.get("players")).append("\n");
        report.append("- Venue Owners: ").append(stats.get("venueOwners")).append("\n");
        report.append("- Administrators: ").append(stats.get("admins")).append("\n\n");

        report.append("VERIFICATION STATUS:\n");
        report.append("- Verified Emails: ").append(stats.get("verifiedEmails")).append("\n");
        report.append("- Verified Phones: ").append(stats.get("verifiedPhones")).append("\n\n");

        Map<String, Long> cityBreakdown = userStorage.values().stream()
                .filter(user -> user.getCity() != null && !user.getCity().isEmpty())
                .collect(Collectors.groupingBy(User::getCity, Collectors.counting()));

        if (!cityBreakdown.isEmpty()) {
            report.append("BY CITY:\n");
            cityBreakdown.forEach((city, count) ->
                    report.append("- ").append(city).append(": ").append(count).append("\n"));
        }

        return report.toString();
    }
}
