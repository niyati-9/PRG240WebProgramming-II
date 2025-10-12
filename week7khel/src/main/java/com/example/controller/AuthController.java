package com.example.controller;

import com.example.model.User;
import com.example.model.UserType;
import com.example.service.UserService;
import com.example.service.SessionManagementService;
import com.example.util.JwtUtil;
import com.example.dto.JwtResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;  // ADD THIS IMPORT
import java.util.Optional;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SessionManagementService sessionManagementService;

    // ==================== AUTHENTICATION METHODS ====================

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String homePage() {
        logger.info("Home page requested");
        return "home";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage(Model model) {
        logger.info("Login page requested");
        return "auth/login";
    }

    @RequestMapping(value = "/signup", method = RequestMethod.GET)
    public String signupPage(Model model) {
        logger.info("Signup page requested");
        return "auth/signup";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String processLogin(@RequestParam("email") String email,
                               @RequestParam("password") String password,
                               @RequestParam(value = "remember", required = false) boolean remember,
                               Model model,
                               HttpSession session,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {

        logger.info("=== KHEL APP LOGIN ATTEMPT ===");
        logger.info("Login attempt for email: {}", email);

        try {
            // Validate input
            if (email == null || email.trim().isEmpty()) {
                model.addAttribute("error", "Email is required");
                return "auth/login";
            }

            if (password == null || password.trim().isEmpty()) {
                model.addAttribute("error", "Password is required");
                return "auth/login";
            }

            // Authenticate user using email instead of username
            Optional<User> userOptional = userService.authenticateUserByEmail(email.trim(), password);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Generate JWT tokens for session-based login as well
                String jwtToken = jwtUtil.generateToken(
                        user.getUsername(),
                        user.getUserId(),
                        user.getUserType().getValue()
                );

                String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

                // Get client information for session security
                String ipAddress = getClientIpAddress(request);
                String userAgent = request.getHeader("User-Agent");

                // Create secure session with enhanced security
                sessionManagementService.createUserSession(session, user, jwtToken, ipAddress, userAgent);

                // Set additional JWT tokens in session
                session.setAttribute("refreshToken", refreshToken);

                // Set session timeout based on remember me
                if (remember) {
                    session.setMaxInactiveInterval(30 * 24 * 60 * 60); // 30 days
                } else {
                    session.setMaxInactiveInterval(2 * 60 * 60); // 2 hours
                }

                logger.info("=== KHEL APP LOGIN SUCCESSFUL ===");
                logger.info("User {} logged in successfully with JWT tokens and secure session", user.getUsername());
                logger.info("Session ID: {}, IP: {}", session.getId(), ipAddress);

                // Redirect to dashboard for all user types
                return "redirect:/dashboard";

            } else {
                model.addAttribute("error", "Invalid email or password");
                model.addAttribute("email", email);
                return "auth/login";
            }

        } catch (Exception e) {
            logger.error("=== KHEL APP LOGIN ERROR ===");
            logger.error("Login error for email {}: {}", email, e.getMessage());
            model.addAttribute("error", "Login failed. Please try again.");
            model.addAttribute("email", email);
            return "auth/login";
        }
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String processSignup(@RequestParam("username") String username,
                                @RequestParam("fullName") String fullName,
                                @RequestParam("email") String email,
                                @RequestParam("phoneNumber") String phoneNumber,
                                @RequestParam("password") String password,
                                @RequestParam("confirmPassword") String confirmPassword,
                                @RequestParam("userType") String userType,
                                Model model,
                                HttpSession session,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {

        logger.info("=== KHEL APP SIGNUP ATTEMPT ===");
        logger.info("Signup attempt for username: {}, email: {}", username, email);

        try {
            // Validate input
            if (username == null || username.trim().isEmpty()) {
                model.addAttribute("error", "Username is required");
                return populateSignupForm(model, username, fullName, email, phoneNumber, userType);
            }

            if (fullName == null || fullName.trim().isEmpty()) {
                model.addAttribute("error", "Full name is required");
                return populateSignupForm(model, username, fullName, email, phoneNumber, userType);
            }

            if (email == null || email.trim().isEmpty()) {
                model.addAttribute("error", "Email is required");
                return populateSignupForm(model, username, fullName, email, phoneNumber, userType);
            }

            if (password == null || password.trim().isEmpty()) {
                model.addAttribute("error", "Password is required");
                return populateSignupForm(model, username, fullName, email, phoneNumber, userType);
            }

            if (confirmPassword == null || !password.equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match");
                return populateSignupForm(model, username, fullName, email, phoneNumber, userType);
            }

            // Additional validations
            if (!userService.isValidPassword(password)) {
                model.addAttribute("error", "Password must be at least 6 characters long");
                return populateSignupForm(model, username, fullName, email, phoneNumber, userType);
            }

            // Convert userType string to enum
            UserType userTypeEnum;
            try {
                if ("COACH".equalsIgnoreCase(userType) || "coach".equalsIgnoreCase(userType)) {
                    userTypeEnum = UserType.COACH;
                } else if ("PLAYER".equalsIgnoreCase(userType) || "player".equalsIgnoreCase(userType)) {
                    userTypeEnum = UserType.PLAYER;
                } else {
                    userTypeEnum = UserType.fromValue(userType);
                }
            } catch (Exception e) {
                model.addAttribute("error", "Invalid user type selected");
                return populateSignupForm(model, username, fullName, email, phoneNumber, userType);
            }

            // Check if user already exists
            if (userService.isUsernameExists(username)) {
                model.addAttribute("error", "Username is already taken");
                return populateSignupForm(model, username, fullName, email, phoneNumber, userType);
            }

            if (userService.isEmailExists(email)) {
                model.addAttribute("error", "An account with this email already exists");
                return populateSignupForm(model, username, fullName, email, phoneNumber, userType);
            }

            // Register user and save to database
            User newUser = userService.registerUser(
                    username.trim(),
                    fullName.trim(),
                    email.trim(),
                    phoneNumber != null ? phoneNumber.trim() : null,
                    password,
                    userTypeEnum
            );

            // Generate JWT tokens for new user
            String jwtToken = jwtUtil.generateToken(
                    newUser.getUsername(),
                    newUser.getUserId(),
                    newUser.getUserType().getValue()
            );

            String refreshToken = jwtUtil.generateRefreshToken(newUser.getUsername());

            // Get client information for session security
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            logger.info("=== KHEL APP SIGNUP SUCCESSFUL ===");
            logger.info("New user signed up successfully: {} ({}) with JWT tokens",
                    newUser.getUsername(), newUser.getUserType());

            // Create secure session for new user
            sessionManagementService.createUserSession(session, newUser, jwtToken, ipAddress, userAgent);

            // Store additional data in session for summary page
            session.setAttribute("newUser", newUser);
            session.setAttribute("signupSuccess", true);
            session.setAttribute("refreshToken", refreshToken);

            // Redirect to summary page
            return "redirect:/auth/summary";

        } catch (RuntimeException e) {
            logger.warn("=== KHEL APP SIGNUP FAILED ===");
            logger.warn("Signup failed for username {}: {}", username, e.getMessage());
            model.addAttribute("error", e.getMessage());
            return populateSignupForm(model, username, fullName, email, phoneNumber, userType);

        } catch (Exception e) {
            logger.error("=== KHEL APP SIGNUP ERROR ===");
            logger.error("Unexpected error during signup for username {}: {}", username, e.getMessage());
            model.addAttribute("error", "Signup failed. Please try again.");
            return populateSignupForm(model, username, fullName, email, phoneNumber, userType);
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            logger.info("=== KHEL APP LOGOUT ===");
            logger.info("User {} logging out with session invalidation", currentUser.getUsername());

            // Securely invalidate session and JWT tokens
            sessionManagementService.invalidateSession(session);
        }

        redirectAttributes.addFlashAttribute("message", "You have been logged out successfully");
        return "redirect:/";
    }

    // ==================== DASHBOARD METHODS ====================

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String dashboard(HttpSession session, HttpServletRequest request, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        String jwtToken = (String) session.getAttribute("jwtToken");

        if (currentUser == null) {
            logger.warn("Unauthorized access attempt to dashboard");
            return "redirect:/login";
        }

        // Validate session security and JWT token
        if (!sessionManagementService.validateSessionSecurity(session, jwtToken)) {
            logger.warn("Session security validation failed for user: {}", currentUser.getUsername());
            return "redirect:/login";
        }

        // Check for suspicious activity
        String currentIpAddress = getClientIpAddress(request);
        if (sessionManagementService.detectSuspiciousActivity(session, currentIpAddress)) {
            logger.warn("Suspicious activity detected for user: {}", currentUser.getUsername());
            sessionManagementService.invalidateSession(session);
            model.addAttribute("error", "Suspicious activity detected. Please log in again.");
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);

        // Add JWT token info to model for potential client-side use
        String refreshToken = (String) session.getAttribute("refreshToken");

        if (jwtToken != null) {
            model.addAttribute("hasJwtToken", true);
            // Check if token is close to expiration (within 1 hour)
            if (jwtUtil.isTokenExpired(jwtToken) || isTokenNearExpiry(jwtToken)) {
                model.addAttribute("tokenNearExpiry", true);
            }
        }

        // Fix the user type checks to use enum comparison
        model.addAttribute("isAdmin", currentUser.getUserType() == UserType.ADMIN);
        model.addAttribute("isCoach", currentUser.getUserType() == UserType.COACH);
        model.addAttribute("isPlayer", currentUser.getUserType() == UserType.PLAYER);

        // Set dashboard title and welcome message based on user type
        if (currentUser.getUserType() == UserType.ADMIN) {
            model.addAttribute("dashboardTitle", "Admin Dashboard");
            model.addAttribute("welcomeMessage", "Welcome Admin " + currentUser.getFullName() + "!");
            model.addAttribute("totalUsers", userService.getTotalUserCount());
            model.addAttribute("sessionStats", sessionManagementService.getSessionStatistics());
        } else if (currentUser.getUserType() == UserType.COACH) {
            model.addAttribute("dashboardTitle", "Coach Dashboard");
            model.addAttribute("welcomeMessage", "Welcome Coach " + currentUser.getFullName() + "!");
        } else {
            model.addAttribute("dashboardTitle", "Player Dashboard");
            model.addAttribute("welcomeMessage", "Welcome to Khel App, " + currentUser.getFullName() + "!");
        }

        logger.info("Dashboard loaded for user: {} ({}) with secure session validation",
                currentUser.getUsername(), currentUser.getUserType());
        return "dashboard";
    }

    @RequestMapping(value = "/auth/summary", method = RequestMethod.GET)
    public String summaryPage(HttpSession session, HttpServletRequest request, Model model) {
        User newUser = (User) session.getAttribute("newUser");
        Boolean signupSuccess = (Boolean) session.getAttribute("signupSuccess");
        String jwtToken = (String) session.getAttribute("jwtToken");

        if (newUser == null || signupSuccess == null || !signupSuccess) {
            logger.warn("Unauthorized access to summary page - redirecting to signup");
            return "redirect:/signup";
        }

        // Validate session security for new user
        if (!sessionManagementService.validateSessionSecurity(session, jwtToken)) {
            logger.warn("Session security validation failed for new user: {}", newUser.getUsername());
            return "redirect:/signup";
        }

        model.addAttribute("user", newUser);

        // Add JWT token availability info
        if (jwtToken != null) {
            model.addAttribute("jwtTokenGenerated", true);
        }

        logger.info("Summary page loaded for new user: {} with secure session", newUser.getUsername());

        // Clear the temporary session attributes after displaying summary
        session.removeAttribute("newUser");
        session.removeAttribute("signupSuccess");

        return "auth/summary";
    }

    // ==================== SESSION MANAGEMENT ENDPOINTS ====================

    @RequestMapping(value = "/session/refresh", method = RequestMethod.POST)
    public String refreshSession(HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) session.getAttribute("currentUser");
            String jwtToken = (String) session.getAttribute("jwtToken");

            if (currentUser == null || jwtToken == null) {
                redirectAttributes.addFlashAttribute("error", "No active session to refresh");
                return "redirect:/login";
            }

            // Check if token can be refreshed
            if (!jwtUtil.canTokenBeRefreshed(jwtToken)) {
                redirectAttributes.addFlashAttribute("error", "Session cannot be refreshed. Please log in again.");
                sessionManagementService.invalidateSession(session);
                return "redirect:/login";
            }

            // Refresh JWT token
            String newJwtToken = jwtUtil.refreshToken(jwtToken);
            String newRefreshToken = jwtUtil.generateRefreshToken(currentUser.getUsername());

            // Update session with new tokens
            session.setAttribute("jwtToken", newJwtToken);
            session.setAttribute("refreshToken", newRefreshToken);
            session.setAttribute("tokenRefreshTime", System.currentTimeMillis());

            logger.info("Session refreshed for user: {}", currentUser.getUsername());
            redirectAttributes.addFlashAttribute("message", "Session refreshed successfully");

            return "redirect:/dashboard";

        } catch (Exception e) {
            logger.error("Session refresh error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to refresh session");
            return "redirect:/login";
        }
    }

    // ==================== HELPER METHODS ====================

    // Helper method for signup form population
    private String populateSignupForm(Model model, String username, String fullName, String email,
                                      String phoneNumber, String userType) {
        model.addAttribute("username", username);
        model.addAttribute("fullName", fullName);
        model.addAttribute("email", email);
        model.addAttribute("phoneNumber", phoneNumber);
        model.addAttribute("userType", userType);
        return "auth/signup";
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Check if JWT token is near expiry (within 1 hour)
     */
    private boolean isTokenNearExpiry(String token) {
        try {
            Date expiration = jwtUtil.extractExpiration(token);
            long currentTime = System.currentTimeMillis();
            long expirationTime = expiration.getTime();
            long timeUntilExpiration = expirationTime - currentTime;

            // Check if token expires within 1 hour (3600000 milliseconds)
            return timeUntilExpiration < 3600000 && timeUntilExpiration > 0;
        } catch (Exception e) {
            logger.error("Error checking token expiry: {}", e.getMessage());
            return true; // Assume near expiry on error for safety
        }
    }
}