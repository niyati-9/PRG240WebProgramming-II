<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign Up - Khel Sports Venue Booking</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/css/style.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/css/auth.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
</head>
<body class="auth-body">
    <!-- Navigation -->
    <header>
        <nav class="navbar">
            <div class="container">
                <div class="logo">
                    <div class="logo-icon">🏆</div>
                    <h1><a href="${pageContext.request.contextPath}/">Khel</a></h1>
                </div>
                <ul class="nav-links">
                    <li><a href="${pageContext.request.contextPath}/">Home</a></li>
                    <li><a href="${pageContext.request.contextPath}/#sports">Sports</a></li>
                    <li><a href="${pageContext.request.contextPath}/#about">About</a></li>
                    <li><a href="${pageContext.request.contextPath}/#contact">Contact</a></li>
                </ul>
                <div class="auth-buttons">
                    <a href="${pageContext.request.contextPath}/login" class="btn-login">Login</a>
                    <a href="${pageContext.request.contextPath}/signup" class="btn-signup active">Sign Up</a>
                </div>
            </div>
        </nav>
    </header>

    <!-- Main Content -->
    <main class="auth-main">
        <div class="auth-container">
            <div class="auth-card signup-card">
                <div class="auth-header">
                    <div class="auth-icon">🎉</div>
                    <h2>Join Khel Today</h2>
                    <p>Create your account and start booking amazing sports venues in minutes</p>
                </div>

                <!-- Error Message -->
                <c:if test="${not empty error}">
                    <div class="alert alert-error">
                        <span>⚠️</span>
                        ${error}
                    </div>
                </c:if>

                <form class="auth-form" action="${pageContext.request.contextPath}/signup" method="post">
                    <div class="form-row">
                        <div class="form-group">
                            <label for="fullName">Full Name</label>
                            <div class="input-wrapper">
                                <span class="input-icon">👤</span>
                                <input type="text" id="fullName" name="fullName" required
                                       placeholder="Enter your full name"
                                       value="${param.fullName}">
                            </div>
                        </div>
                    </div>

                    <div class="form-row two-cols">
                        <div class="form-group">
                            <label for="email">Email Address</label>
                            <div class="input-wrapper">
                                <span class="input-icon">📧</span>
                                <input type="email" id="email" name="email" required
                                       placeholder="Enter your email address"
                                       value="${param.email}">
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="phone">Phone Number</label>
                            <div class="input-wrapper">
                                <span class="input-icon">📱</span>
                                <input type="tel" id="phone" name="phone" required
                                       placeholder="Enter your phone number"
                                       value="${param.phone}">
                            </div>
                        </div>
                    </div>

                    <div class="form-row two-cols">
                        <div class="form-group">
                            <label for="password">Password</label>
                            <div class="input-wrapper">
                                <span class="input-icon">🔒</span>
                                <input type="password" id="password" name="password" required
                                       placeholder="Create a strong password"
                                       minlength="8">
                                <button type="button" class="toggle-password" onclick="togglePassword('password')">
                                    <span id="password-eye">👁️</span>
                                </button>
                            </div>
                            <div class="password-strength" id="password-strength" style="display: none;">
                                <div class="strength-bar">
                                    <div class="strength-fill" id="strength-fill"></div>
                                </div>
                                <span class="strength-text" id="strength-text"></span>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="confirmPassword">Confirm Password</label>
                            <div class="input-wrapper">
                                <span class="input-icon">🔐</span>
                                <input type="password" id="confirmPassword" name="confirmPassword" required
                                       placeholder="Confirm your password">
                                <button type="button" class="toggle-password" onclick="togglePassword('confirmPassword')">
                                    <span id="confirmPassword-eye">👁️</span>
                                </button>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="userType">Account Type</label>
                        <div class="user-type-selection">
                            <label class="user-type-option">
                                <input type="radio" name="userType" value="player" checked>
                                <div class="user-type-card">
                                    <span class="user-type-icon">⚽</span>
                                    <h4>Book Venues</h4>
                                    <p>Find and book sports venues for playing</p>
                                </div>
                            </label>

                            <label class="user-type-option">
                                <input type="radio" name="userType" value="owner">
                                <div class="user-type-card">
                                    <span class="user-type-icon">🏟️</span>
                                    <h4>List My Venue</h4>
                                    <p>Manage and rent out my sports facility</p>
                                </div>
                            </label>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="checkbox-wrapper">
                            <input type="checkbox" name="terms" required>
                            <span class="checkmark"></span>
                            I agree to the <a href="#" target="_blank">Terms & Conditions</a> and <a href="#" target="_blank">Privacy Policy</a>
                        </label>
                    </div>

                    <div class="form-group">
                        <label class="checkbox-wrapper">
                            <input type="checkbox" name="newsletter">
                            <span class="checkmark"></span>
                            Send me updates about new venues and special offers
                        </label>
                    </div>

                    <button type="submit" class="btn-auth-primary">
                        <span>Create Account</span>
                        <span class="btn-icon">✨</span>
                    </button>
                </form>

                <div class="auth-divider">
                    <span>or sign up with</span>
                </div>

                <div class="social-login">
                    <button class="btn-social google" onclick="socialLogin('google')">
                        <span class="social-icon">🌐</span>
                        Google
                    </button>
                    <button class="btn-social facebook" onclick="socialLogin('facebook')">
                        <span class="social-icon">📱</span>
                        Facebook
                    </button>
                </div>

                <div class="auth-footer">
                    <p>Already have an account? <a href="${pageContext.request.contextPath}/login">Sign in here</a></p>
                </div>
            </div>
        </div>
    </main>

    <script>
        function togglePassword(inputId) {
            const input = document.getElementById(inputId);
            const eye = document.getElementById(inputId + '-eye');

            if (input.type === 'password') {
                input.type = 'text';
                eye.textContent = '🙈';
            } else {
                input.type = 'password';
                eye.textContent = '👁️';
            }
        }

        function socialLogin(provider) {
            console.log('Social login with:', provider);
            alert('Social login with ' + provider + ' is not implemented yet.');
        }

        // Password strength checker
        function checkPasswordStrength(password) {
            let score = 0;
            let feedback = [];

            // Length check
            if (password.length >= 8) score += 1;
            else feedback.push('at least 8 characters');

            // Character variety checks
            if (/[a-z]/.test(password)) score += 1;
            else feedback.push('lowercase letters');

            if (/[A-Z]/.test(password)) score += 1;
            else feedback.push('uppercase letters');

            if (/[0-9]/.test(password)) score += 1;
            else feedback.push('numbers');

            if (/[^A-Za-z0-9]/.test(password)) score += 1;
            else feedback.push('special characters');

            return { score, feedback };
        }

        function updatePasswordStrength(password) {
            const strengthIndicator = document.getElementById('password-strength');
            const strengthFill = document.getElementById('strength-fill');
            const strengthText = document.getElementById('strength-text');

            if (!password) {
                strengthIndicator.style.display = 'none';
                return;
            }

            strengthIndicator.style.display = 'block';
            const { score, feedback } = checkPasswordStrength(password);

            // Update strength bar
            const percentage = (score / 5) * 100;
            strengthFill.style.width = percentage + '%';

            // Update strength level and color
            let level, className;
            if (score <= 2) {
                level = 'Weak';
                className = 'strength-weak';
            } else if (score === 3) {
                level = 'Fair';
                className = 'strength-fair';
            } else if (score === 4) {
                level = 'Good';
                className = 'strength-good';
            } else {
                level = 'Strong';
                className = 'strength-strong';
            }

            strengthFill.className = 'strength-fill ' + className;
            strengthText.textContent = level;

            if (feedback.length > 0 && score < 4) {
                strengthText.textContent += ' - Add: ' + feedback.slice(0, 2).join(', ');
            }
        }

        document.addEventListener('DOMContentLoaded', function() {
            const form = document.querySelector('.auth-form');
            const inputs = document.querySelectorAll('.auth-form input');
            const submitButton = document.querySelector('.btn-auth-primary');
            const passwordInput = document.getElementById('password');
            const confirmPasswordInput = document.getElementById('confirmPassword');

            // Add floating label effect
            inputs.forEach(input => {
                input.addEventListener('focus', function() {
                    this.parentElement.parentElement.classList.add('focused');
                });

                input.addEventListener('blur', function() {
                    if (this.value === '') {
                        this.parentElement.parentElement.classList.remove('focused');
                    }
                });

                // Check if input has value on load
                if (input.value !== '') {
                    input.parentElement.parentElement.classList.add('focused');
                }
            });

            // Password strength indicator
            passwordInput.addEventListener('input', function() {
                updatePasswordStrength(this.value);
                validatePasswordMatch();
            });

            // Password confirmation validation
            confirmPasswordInput.addEventListener('input', validatePasswordMatch);

            function validatePasswordMatch() {
                const password = passwordInput.value;
                const confirmPassword = confirmPasswordInput.value;

                if (confirmPassword && password !== confirmPassword) {
                    confirmPasswordInput.setCustomValidity('Passwords do not match');
                    confirmPasswordInput.classList.add('error');
                } else {
                    confirmPasswordInput.setCustomValidity('');
                    confirmPasswordInput.classList.remove('error');
                }
            }

            // Phone number formatting
            const phoneInput = document.getElementById('phone');
            phoneInput.addEventListener('input', function() {
                // Remove all non-digit characters
                let value = this.value.replace(/\D/g, '');

                // Format as needed (simple formatting)
                if (value.length >= 10) {
                    value = value.replace(/(\d{3})(\d{3})(\d{4})/, '($1) $2-$3');
                }

                this.value = value;
            });

            // User type selection
            document.querySelectorAll('input[name="userType"]').forEach(radio => {
                radio.addEventListener('change', function() {
                    document.querySelectorAll('.user-type-card').forEach(card => {
                        card.classList.remove('selected');
                    });
                    this.nextElementSibling.classList.add('selected');
                });
            });

            // Set initial user type selection
            const checkedRadio = document.querySelector('input[name="userType"]:checked');
            if (checkedRadio) {
                checkedRadio.nextElementSibling.classList.add('selected');
            }

            // Form validation
            form.addEventListener('submit', function(e) {
                let isValid = true;
                const errors = [];

                // Remove previous error states
                inputs.forEach(input => input.classList.remove('error'));

                // Validate all required fields
                inputs.forEach(input => {
                    if (input.required && !input.value.trim()) {
                        input.classList.add('error');
                        isValid = false;
                    }
                });

                // Specific validations
                const password = passwordInput.value;
                const confirmPassword = confirmPasswordInput.value;
                const email = document.getElementById('email').value;
                const terms = document.querySelector('input[name="terms"]').checked;

                // Email validation
                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (email && !emailRegex.test(email)) {
                    document.getElementById('email').classList.add('error');
                    errors.push('Please enter a valid email address');
                    isValid = false;
                }

                // Password strength validation
                const { score } = checkPasswordStrength(password);
                if (password && score < 3) {
                    passwordInput.classList.add('error');
                    errors.push('Password is too weak. Please create a stronger password');
                    isValid = false;
                }

                // Password match validation
                if (password !== confirmPassword) {
                    confirmPasswordInput.classList.add('error');
                    errors.push('Passwords do not match');
                    isValid = false;
                }

                // Terms validation
                if (!terms) {
                    errors.push('Please agree to the Terms & Conditions');
                    isValid = false;
                }

                if (!isValid) {
                    e.preventDefault();
                    showErrorMessage(errors[0] || 'Please correct the highlighted fields');
                    return;
                }

                // Show loading state
                submitButton.disabled = true;
                submitButton.innerHTML = '<span>Creating Account...</span><span class="btn-icon">⏳</span>';
            });

            function showErrorMessage(message) {
                const existingAlert = document.querySelector('.alert-error');
                if (existingAlert && !existingAlert.textContent.includes('${error}')) {
                    existingAlert.remove();
                }

                if (!document.querySelector('.alert-error')) {
                    const alertDiv = document.createElement('div');
                    alertDiv.className = 'alert alert-error';
                    alertDiv.innerHTML = '<span>⚠️</span>' + message;

                    form.parentNode.insertBefore(alertDiv, form);

                    setTimeout(() => {
                        if (alertDiv.parentNode) {
                            alertDiv.remove();
                        }
                    }, 5000);
                }
            }

            // Smooth entrance animation
            const authCard = document.querySelector('.auth-card');
            authCard.style.opacity = '0';
            authCard.style.transform = 'translateY(30px)';

            setTimeout(() => {
                authCard.style.transition = 'all 0.8s ease-out';
                authCard.style.opacity = '1';
                authCard.style.transform = 'translateY(0)';
            }, 100);

            // Real-time validation feedback
            inputs.forEach(input => {
                input.addEventListener('input', function() {
                    this.classList.remove('error');
                    if (this.checkValidity() && this.value.trim()) {
                        this.classList.add('valid');
                    } else {
                        this.classList.remove('valid');
                    }
                });
            });
        });
    </script>
</body>
</html>