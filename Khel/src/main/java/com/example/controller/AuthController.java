package com.example.controller;

import com.example.model.User;
import com.example.model.UserType;
import com.example.service.UserService;
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
import java.util.Optional;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

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

                // Store user in session
                session.setAttribute("currentUser", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("userType", user.getUserType());
                session.setAttribute("username", user.getUsername());

                // Set session timeout based on remember me
                if (remember) {
                    session.setMaxInactiveInterval(30 * 24 * 60 * 60); // 30 days
                } else {
                    session.setMaxInactiveInterval(2 * 60 * 60); // 2 hours
                }

                logger.info("=== KHEL APP LOGIN SUCCESSFUL ===");
                logger.info("User {} logged in successfully", user.getUsername());

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
                                RedirectAttributes redirectAttributes) {

        logger.info("=== KHEL APP SIGNUP ATTEMPT ===");
        logger.info("Signup attempt for username: {}, email: {}", username, email);
        logger.info("DEBUG - Received parameters: username={}, fullName={}, email={}, phoneNumber={}, userType={}",
                username, fullName, email, phoneNumber, userType);

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

            // Convert userType string to enum - handle both COACH and coach values
            UserType userTypeEnum;
            try {
                // Handle the mapping from form values to enum
                if ("COACH".equalsIgnoreCase(userType) || "coach".equalsIgnoreCase(userType)) {
                    userTypeEnum = UserType.COACH;
                } else if ("PLAYER".equalsIgnoreCase(userType) || "player".equalsIgnoreCase(userType)) {
                    userTypeEnum = UserType.PLAYER;
                } else {
                    userTypeEnum = UserType.fromValue(userType);
                }
                logger.info("DEBUG - UserType converted successfully: {}", userTypeEnum);
            } catch (Exception e) {
                logger.error("DEBUG - UserType conversion failed: {}", e.getMessage());
                model.addAttribute("error", "Invalid user type selected");
                return populateSignupForm(model, username, fullName, email, phoneNumber, userType);
            }

            // Check if user already exists
            logger.info("DEBUG - Checking if username exists: {}", username);
            if (userService.isUsernameExists(username)) {
                logger.info("DEBUG - Username already exists: {}", username);
                model.addAttribute("error", "Username is already taken");
                return populateSignupForm(model, username, fullName, email, phoneNumber, userType);
            }

            logger.info("DEBUG - Checking if email exists: {}", email);
            if (userService.isEmailExists(email)) {
                logger.info("DEBUG - Email already exists: {}", email);
                model.addAttribute("error", "An account with this email already exists");
                return populateSignupForm(model, username, fullName, email, phoneNumber, userType);
            }

            logger.info("DEBUG - About to register user with userService.registerUser()");
            logger.info("DEBUG - Parameters: username={}, fullName={}, email={}, phoneNumber={}, userType={}",
                    username.trim(), fullName.trim(), email.trim(),
                    phoneNumber != null ? phoneNumber.trim() : null, userTypeEnum);

            // Register user and save to database
            User newUser = userService.registerUser(
                    username.trim(),
                    fullName.trim(),
                    email.trim(),
                    phoneNumber != null ? phoneNumber.trim() : null,
                    password,
                    userTypeEnum
            );

            logger.info("DEBUG - User registration completed successfully");
            logger.info("DEBUG - New user object: ID={}, Username={}, Email={}",
                    newUser.getUserId(), newUser.getUsername(), newUser.getEmail());

            logger.info("=== KHEL APP SIGNUP SUCCESSFUL ===");
            logger.info("New user signed up successfully: {} ({})", newUser.getUsername(), newUser.getUserType());

            // Store user data in session for summary page
            session.setAttribute("newUser", newUser);
            session.setAttribute("signupSuccess", true);

            logger.info("DEBUG - User data stored in session, redirecting to summary");

            // Redirect to summary page
            return "redirect:/auth/summary";

        } catch (RuntimeException e) {
            logger.warn("=== KHEL APP SIGNUP FAILED ===");
            logger.warn("Signup failed for username {}: {}", username, e.getMessage());
            logger.error("DEBUG - Full exception details: ", e);
            model.addAttribute("error", e.getMessage());
            return populateSignupForm(model, username, fullName, email, phoneNumber, userType);

        } catch (Exception e) {
            logger.error("=== KHEL APP SIGNUP ERROR ===");
            logger.error("Unexpected error during signup for username {}: {}", username, e.getMessage());
            logger.error("DEBUG - Full exception details: ", e);
            model.addAttribute("error", "Signup failed. Please try again.");
            return populateSignupForm(model, username, fullName, email, phoneNumber, userType);
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            logger.info("=== KHEL APP LOGOUT ===");
            logger.info("User {} logged out", currentUser.getUsername());
        }

        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "You have been logged out successfully");
        return "redirect:/";
    }

    // ==================== DASHBOARD METHODS ====================

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String dashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            logger.warn("Unauthorized access attempt to dashboard");
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);

        // Fix the user type checks to use enum comparison
        model.addAttribute("isAdmin", currentUser.getUserType() == UserType.ADMIN);
        model.addAttribute("isCoach", currentUser.getUserType() == UserType.COACH);
        model.addAttribute("isPlayer", currentUser.getUserType() == UserType.PLAYER);

        // Set dashboard title and welcome message based on user type
        if (currentUser.getUserType() == UserType.ADMIN) {
            model.addAttribute("dashboardTitle", "Admin Dashboard");
            model.addAttribute("welcomeMessage", "Welcome Admin " + currentUser.getFullName() + "!");
            model.addAttribute("totalUsers", userService.getTotalUserCount());
        } else if (currentUser.getUserType() == UserType.COACH) {
            model.addAttribute("dashboardTitle", "Coach Dashboard");
            model.addAttribute("welcomeMessage", "Welcome Coach " + currentUser.getFullName() + "!");
        } else {
            model.addAttribute("dashboardTitle", "Player Dashboard");
            model.addAttribute("welcomeMessage", "Welcome to Khel App, " + currentUser.getFullName() + "!");
        }

        logger.info("Dashboard loaded for user: {} ({})", currentUser.getUsername(), currentUser.getUserType());
        return "dashboard";
    }

    @RequestMapping(value = "/auth/summary", method = RequestMethod.GET)
    public String summaryPage(HttpSession session, Model model) {
        User newUser = (User) session.getAttribute("newUser");
        Boolean signupSuccess = (Boolean) session.getAttribute("signupSuccess");

        if (newUser == null || signupSuccess == null || !signupSuccess) {
            logger.warn("Unauthorized access to summary page - redirecting to signup");
            return "redirect:/signup";
        }

        // Store the user for use in the summary page
        session.setAttribute("registeredUser", newUser);
        model.addAttribute("user", newUser);
        logger.info("Summary page loaded for new user: {}", newUser.getUsername());

        // Clear the temporary session attributes after displaying summary
        session.removeAttribute("newUser");
        session.removeAttribute("signupSuccess");

        return "auth/summary";
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
}