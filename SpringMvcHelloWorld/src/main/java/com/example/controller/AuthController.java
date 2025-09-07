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

            // Authenticate user
            Optional<User> userOptional = userService.authenticateUser(email.trim(), password);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Store user in session
                session.setAttribute("currentUser", user);
                session.setAttribute("userId", user.getId());
                session.setAttribute("userType", user.getUserType());

                // Set session timeout based on remember me
                if (remember) {
                    session.setMaxInactiveInterval(30 * 24 * 60 * 60); // 30 days
                } else {
                    session.setMaxInactiveInterval(2 * 60 * 60); // 2 hours
                }

                logger.info("User {} logged in successfully", user.getEmail());

                // Redirect all users to the same dashboard
                return "redirect:/dashboard";

            } else {
                model.addAttribute("error", "Invalid email or password");
                model.addAttribute("email", email); // Preserve email in form
                return "auth/login";
            }

        } catch (Exception e) {
            logger.error("Login error for email {}: {}", email, e.getMessage());
            model.addAttribute("error", "Login failed. Please try again.");
            model.addAttribute("email", email);
            return "auth/login";
        }
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String processSignup(@RequestParam("fullName") String fullName,
                                @RequestParam("email") String email,
                                @RequestParam("phone") String phone,
                                @RequestParam("password") String password,
                                @RequestParam("confirmPassword") String confirmPassword,
                                @RequestParam("userType") String userType,
                                @RequestParam(value = "newsletter", required = false) boolean newsletter,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        logger.info("Signup attempt for email: {}", email);

        try {
            // Validate input
            if (fullName == null || fullName.trim().isEmpty()) {
                model.addAttribute("error", "Full name is required");
                return populateSignupForm(model, fullName, email, phone, userType, newsletter);
            }

            if (email == null || email.trim().isEmpty()) {
                model.addAttribute("error", "Email is required");
                return populateSignupForm(model, fullName, email, phone, userType, newsletter);
            }

            if (phone == null || phone.trim().isEmpty()) {
                model.addAttribute("error", "Phone number is required");
                return populateSignupForm(model, fullName, email, phone, userType, newsletter);
            }

            if (password == null || password.trim().isEmpty()) {
                model.addAttribute("error", "Password is required");
                return populateSignupForm(model, fullName, email, phone, userType, newsletter);
            }

            if (confirmPassword == null || !password.equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match");
                return populateSignupForm(model, fullName, email, phone, userType, newsletter);
            }

            // Additional validations
            if (!userService.isValidEmail(email)) {
                model.addAttribute("error", "Please enter a valid email address");
                return populateSignupForm(model, fullName, email, phone, userType, newsletter);
            }

            if (!userService.isValidPhone(phone)) {
                model.addAttribute("error", "Please enter a valid phone number");
                return populateSignupForm(model, fullName, email, phone, userType, newsletter);
            }

            if (!userService.isValidPassword(password)) {
                model.addAttribute("error", "Password must be at least 6 characters long");
                return populateSignupForm(model, fullName, email, phone, userType, newsletter);
            }

            // Convert userType string to enum
            UserType userTypeEnum;
            try {
                userTypeEnum = UserType.fromValue(userType);
            } catch (Exception e) {
                model.addAttribute("error", "Invalid user type selected");
                return populateSignupForm(model, fullName, email, phone, userType, newsletter);
            }

            // Check if user already exists
            if (userService.isEmailExists(email)) {
                model.addAttribute("error", "An account with this email already exists");
                return populateSignupForm(model, fullName, email, phone, userType, newsletter);
            }

            if (userService.isPhoneExists(phone)) {
                model.addAttribute("error", "An account with this phone number already exists");
                return populateSignupForm(model, fullName, email, phone, userType, newsletter);
            }

            // Register user
            User newUser = userService.registerUser(
                    fullName.trim(),
                    email.trim(),
                    phone.trim(),
                    password,
                    userTypeEnum,
                    newsletter
            );

            logger.info("New user registered successfully: {} ({})", newUser.getEmail(), newUser.getUserType());

            // Redirect to summary page with user data
            String redirectUrl = String.format(
                    "redirect:/signup-summary?fullName=%s&email=%s&phone=%s&userType=%s&newsletter=%s",
                    java.net.URLEncoder.encode(fullName, "UTF-8"),
                    java.net.URLEncoder.encode(email, "UTF-8"),
                    java.net.URLEncoder.encode(phone, "UTF-8"),
                    userType,
                    newsletter
            );

            return redirectUrl;

        } catch (RuntimeException e) {
            logger.warn("Registration failed for email {}: {}", email, e.getMessage());
            model.addAttribute("error", e.getMessage());
            return populateSignupForm(model, fullName, email, phone, userType, newsletter);

        } catch (Exception e) {
            logger.error("Unexpected error during registration for email {}: {}", email, e.getMessage());
            model.addAttribute("error", "Registration failed. Please try again.");
            return populateSignupForm(model, fullName, email, phone, userType, newsletter);
        }
    }

    @RequestMapping(value = "/signup-summary", method = RequestMethod.GET)
    public String signupSummary(@RequestParam(value = "fullName", required = false) String fullName,
                                @RequestParam(value = "email", required = false) String email,
                                @RequestParam(value = "phone", required = false) String phone,
                                @RequestParam(value = "userType", required = false) String userType,
                                @RequestParam(value = "newsletter", required = false) boolean newsletter,
                                Model model) {

        logger.info("Signup summary page requested for email: {}", email);

        // Add all parameters to model for JSP access
        model.addAttribute("fullName", fullName);
        model.addAttribute("email", email);
        model.addAttribute("phone", phone);
        model.addAttribute("userType", userType);
        model.addAttribute("newsletter", newsletter);

        return "auth/summary";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            logger.info("User {} logged out", currentUser.getEmail());
        }

        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "You have been logged out successfully");
        return "redirect:/";
    }

    @RequestMapping(value = "/forgot-password", method = RequestMethod.GET)
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @RequestMapping(value = "/forgot-password", method = RequestMethod.POST)
    public String processForgotPassword(@RequestParam("email") String email,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        try {
            if (email == null || email.trim().isEmpty()) {
                model.addAttribute("error", "Email is required");
                return "auth/forgot-password";
            }

            boolean emailSent = userService.sendPasswordResetEmail(email.trim());

            if (emailSent) {
                redirectAttributes.addFlashAttribute("success",
                        "If an account with this email exists, you will receive password reset instructions.");
            } else {
                redirectAttributes.addFlashAttribute("info",
                        "If an account with this email exists, you will receive password reset instructions.");
            }

            return "redirect:/login";

        } catch (Exception e) {
            logger.error("Forgot password error for email {}: {}", email, e.getMessage());
            model.addAttribute("error", "Failed to send reset email. Please try again.");
            return "auth/forgot-password";
        }
    }

    // ==================== DASHBOARD METHODS ====================

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String dashboard(HttpSession session, Model model) {
        // Check if user is logged in
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            logger.warn("Unauthorized access attempt to dashboard");
            return "redirect:/login";
        }

        try {
            // Get fresh user data from database
            Optional<User> userOptional = userService.findById(currentUser.getId());
            if (!userOptional.isPresent()) {
                logger.warn("User not found in database: {}", currentUser.getId());
                session.invalidate();
                return "redirect:/login";
            }

            User user = userOptional.get();

            // Update session with fresh user data
            session.setAttribute("currentUser", user);

            // Add user data to model
            model.addAttribute("user", user);
            model.addAttribute("userType", user.getUserType());
            model.addAttribute("isAdmin", user.isAdmin());
            model.addAttribute("isVenueOwner", user.isVenueOwner());
            model.addAttribute("isPlayer", user.isPlayer());

            // Add role-specific data based on user type
            if (user.isAdmin()) {
                addAdminDashboardData(model);
                logger.info("Admin dashboard loaded for user: {}", user.getEmail());
            } else if (user.isVenueOwner()) {
                addVenueOwnerDashboardData(model, user);
                logger.info("Venue owner dashboard loaded for user: {}", user.getEmail());
            } else if (user.isPlayer()) {
                addPlayerDashboardData(model, user);
                logger.info("Player dashboard loaded for user: {}", user.getEmail());
            } else {
                logger.warn("Unknown user type for user: {} ({})", user.getEmail(), user.getUserType());
            }

            return "dashboard";

        } catch (Exception e) {
            logger.error("Error loading dashboard for user {}: {}", currentUser.getEmail(), e.getMessage());
            model.addAttribute("error", "Unable to load dashboard. Please try again.");
            return "dashboard";
        }
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String profilePage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Get fresh user data
        Optional<User> userOptional = userService.findById(currentUser.getId());
        if (userOptional.isPresent()) {
            model.addAttribute("user", userOptional.get());
            return "user/profile";
        } else {
            return "redirect:/login";
        }
    }

    // ==================== HELPER METHODS ====================

    // Helper method to populate signup form with previous values
    private String populateSignupForm(Model model, String fullName, String email, String phone,
                                      String userType, boolean newsletter) {
        model.addAttribute("fullName", fullName);
        model.addAttribute("email", email);
        model.addAttribute("phone", phone);
        model.addAttribute("userType", userType);
        model.addAttribute("newsletter", newsletter);
        return "auth/signup";
    }

    // Add admin-specific dashboard data
    private void addAdminDashboardData(Model model) {
        try {
            // You can implement these methods in your UserService
            // model.addAttribute("totalUsers", userService.getTotalUserCount());
            // model.addAttribute("totalVenueOwners", userService.getUserCountByType(UserType.VENUE_OWNER));
            // model.addAttribute("totalPlayers", userService.getUserCountByType(UserType.PLAYER));
            // model.addAttribute("recentUsers", userService.getRecentUsers(5));

            // For now, add placeholder data
            model.addAttribute("dashboardTitle", "Admin Dashboard");
            model.addAttribute("welcomeMessage", "Welcome to the Admin Dashboard");

        } catch (Exception e) {
            logger.error("Error loading admin dashboard data: {}", e.getMessage());
        }
    }

    // Add venue owner-specific dashboard data
    private void addVenueOwnerDashboardData(Model model, User user) {
        try {
            // You can implement these methods in your UserService
            // model.addAttribute("userVenues", userService.getVenuesByOwner(user.getId()));
            // model.addAttribute("totalBookings", userService.getTotalBookingsByOwner(user.getId()));
            // model.addAttribute("recentBookings", userService.getRecentBookingsByOwner(user.getId(), 5));
            // model.addAttribute("monthlyRevenue", userService.getMonthlyRevenue(user.getId()));

            // For now, add placeholder data
            model.addAttribute("dashboardTitle", "Venue Owner Dashboard");
            model.addAttribute("welcomeMessage", "Welcome to your Venue Management Dashboard");

        } catch (Exception e) {
            logger.error("Error loading venue owner dashboard data: {}", e.getMessage());
        }
    }

    // Add player-specific dashboard data
    private void addPlayerDashboardData(Model model, User user) {
        try {
            // You can implement these methods in your UserService
            // model.addAttribute("userBookings", userService.getBookingsByPlayer(user.getId()));
            // model.addAttribute("upcomingGames", userService.getUpcomingGamesByPlayer(user.getId()));
            // model.addAttribute("favoriteVenues", userService.getFavoriteVenues(user.getId()));
            // model.addAttribute("gameHistory", userService.getGameHistoryByPlayer(user.getId(), 10));

            // For now, add placeholder data
            model.addAttribute("dashboardTitle", "Player Dashboard");
            model.addAttribute("welcomeMessage", "Welcome to your Player Dashboard");

        } catch (Exception e) {
            logger.error("Error loading player dashboard data: {}", e.getMessage());
        }
    }
}