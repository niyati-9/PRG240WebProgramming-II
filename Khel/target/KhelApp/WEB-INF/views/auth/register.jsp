<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register - Khel Sports Venue Booking</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/css/style.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/css/auth.css">
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
                    <div class="auth-icon">🏅</div>
                    <h2>Create Your Account</h2>
                    <p>Join Khel and start booking amazing sports venues</p>
                </div>

                <!-- Error Message -->
                <c:if test="${not empty error}">
                    <div class="alert alert-error">
                        <span>⚠️</span>
                        ${error}
                    </div>
                </c:if>

                <form class="auth-form" action="${pageContext.request.contextPath}/register" method="post">
                    <div class="form-row">
                        <div class="form-group">
                            <label for="username">Username</label>
                            <div class="input-wrapper">
                                <span class="input-icon">👤</span>
                                <input type="text" id="username" name="username" required
                                       placeholder="Choose a unique username"
                                       value="${param.username}">
                            </div>
                        </div>
                    </div>

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
                            <label for="phoneNumber">Phone Number</label>
                            <div class="input-wrapper">
                                <span class="input-icon">📱</span>
                                <input type="tel" id="phoneNumber" name="phoneNumber"
                                       placeholder="Enter your phone number"
                                       value="${param.phoneNumber}">
                            </div>
                        </div>
                    </div>

                    <div class="form-row two-cols">
                        <div class="form-group">
                            <label for="password">Password</label>
                            <div class="input-wrapper">
                                <span class="input-icon">🔒</span>
                                <input type="password" id="password" name="password" required
                                       placeholder="Create a password (min 6 characters)"
                                       minlength="6">
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="confirmPassword">Confirm Password</label>
                            <div class="input-wrapper">
                                <span class="input-icon">🔐</span>
                                <input type="password" id="confirmPassword" name="confirmPassword" required
                                       placeholder="Confirm your password">
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="userType">Account Type</label>
                        <div class="user-type-selection">
                            <label class="user-type-option">
                                <input type="radio" name="userType" value="PLAYER" checked>
                                <div class="user-type-card">
                                    <span class="user-type-icon">⚽</span>
                                    <h4>Player</h4>
                                    <p>Book venues for playing sports</p>
                                </div>
                            </label>

                            <label class="user-type-option">
                                <input type="radio" name="userType" value="COACH">
                                <div class="user-type-card">
                                    <span class="user-type-icon">🏟️</span>
                                    <h4>Venue Owner</h4>
                                    <p>List and manage sports venues</p>
                                </div>
                            </label>
                        </div>
                    </div>

                    <button type="submit" class="btn-auth-primary">
                        <span>Create Account</span>
                    </button>
                </form>

                <div class="auth-footer">
                    <p>Already have an account? <a href="${pageContext.request.contextPath}/login">Sign in here</a></p>
                </div>
            </div>
        </div>
    </main>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const form = document.querySelector('.auth-form');
            const passwordInput = document.getElementById('password');
            const confirmPasswordInput = document.getElementById('confirmPassword');

            // Password confirmation validation
            function validatePasswordMatch() {
                const password = passwordInput.value;
                const confirmPassword = confirmPasswordInput.value;

                if (confirmPassword && password !== confirmPassword) {
                    confirmPasswordInput.setCustomValidity('Passwords do not match');
                } else {
                    confirmPasswordInput.setCustomValidity('');
                }
            }

            passwordInput.addEventListener('input', validatePasswordMatch);
            confirmPasswordInput.addEventListener('input', validatePasswordMatch);

            // User type selection
            document.querySelectorAll('input[name="userType"]').forEach(radio => {
                radio.addEventListener('change', function() {
                    document.querySelectorAll('.user-type-card').forEach(card => {
                        card.classList.remove('selected');
                    });
                    this.nextElementSibling.classList.add('selected');
                });
            });

            // Set initial selection
            const checkedRadio = document.querySelector('input[name="userType"]:checked');
            if (checkedRadio) {
                checkedRadio.nextElementSibling.classList.add('selected');
            }
        });
    </script>
</body>
</html>
