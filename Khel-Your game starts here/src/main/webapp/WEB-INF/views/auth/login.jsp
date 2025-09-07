<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Khel Sports Venue Booking</title>
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
                    <a href="${pageContext.request.contextPath}/login" class="btn-login active">Login</a>
                    <a href="${pageContext.request.contextPath}/signup" class="btn-signup">Sign Up</a>
                </div>
            </div>
        </nav>
    </header>

    <!-- Main Content -->
    <main class="auth-main">
        <div class="auth-container">
            <div class="auth-card">
                <div class="auth-header">
                    <div class="auth-icon">🎯</div>
                    <h2>Welcome Back</h2>
                    <p>Sign in to access your account and book amazing sports venues</p>
                </div>

                <!-- Success Message -->
                <c:if test="${param.registered == 'true'}">
                    <div class="alert alert-success">
                        <span>✅</span>
                        Registration successful! Please login with your credentials.
                    </div>
                </c:if>

                <!-- Error Message -->
                <c:if test="${not empty error}">
                    <div class="alert alert-error">
                        <span>⚠️</span>
                        ${error}
                    </div>
                </c:if>

                <form class="auth-form" action="${pageContext.request.contextPath}/login" method="post">
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
                        <label for="password">Password</label>
                        <div class="input-wrapper">
                            <span class="input-icon">🔒</span>
                            <input type="password" id="password" name="password" required
                                   placeholder="Enter your password">
                            <button type="button" class="toggle-password" onclick="togglePassword('password')">
                                <span id="password-eye">👁️</span>
                            </button>
                        </div>
                    </div>

                    <div class="form-options">
                        <label class="checkbox-wrapper">
                            <input type="checkbox" name="remember">
                            <span class="checkmark"></span>
                            Remember me
                        </label>
                        <a href="#" class="forgot-password">Forgot Password?</a>
                    </div>

                    <button type="submit" class="btn-auth-primary">
                        <span>Sign In</span>
                        <span class="btn-icon">🚀</span>
                    </button>
                </form>

                <div class="auth-divider">
                    <span>or continue with</span>
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
                    <p>Don't have an account? <a href="${pageContext.request.contextPath}/signup">Create one here</a></p>
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
            // Placeholder for social login functionality
            console.log('Social login with:', provider);
            alert('Social login with ' + provider + ' is not implemented yet.');
        }

        // Enhanced form interactions
        document.addEventListener('DOMContentLoaded', function() {
            const form = document.querySelector('.auth-form');
            const inputs = document.querySelectorAll('.auth-form input');
            const submitButton = document.querySelector('.btn-auth-primary');

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

                // Add real-time validation feedback
                input.addEventListener('input', function() {
                    this.classList.remove('error');
                    if (this.checkValidity()) {
                        this.classList.add('valid');
                    } else {
                        this.classList.remove('valid');
                    }
                });
            });

            // Enhanced form validation
            form.addEventListener('submit', function(e) {
                const email = document.getElementById('email');
                const password = document.getElementById('password');
                let isValid = true;

                // Remove previous error states
                inputs.forEach(input => input.classList.remove('error'));

                // Validate email
                if (!email.value || !email.checkValidity()) {
                    email.classList.add('error');
                    isValid = false;
                }

                // Validate password
                if (!password.value || password.value.length < 1) {
                    password.classList.add('error');
                    isValid = false;
                }

                if (!isValid) {
                    e.preventDefault();
                    showErrorMessage('Please fill in all fields correctly.');
                    return;
                }

                // Show loading state
                submitButton.disabled = true;
                submitButton.innerHTML = '<span>Signing In...</span><span class="btn-icon">⏳</span>';
            });

            function showErrorMessage(message) {
                // Remove existing error messages
                const existingAlert = document.querySelector('.alert-error');
                if (existingAlert && !existingAlert.textContent.includes('${error}')) {
                    existingAlert.remove();
                }

                // Create new error message if none exists
                if (!document.querySelector('.alert-error')) {
                    const alertDiv = document.createElement('div');
                    alertDiv.className = 'alert alert-error';
                    alertDiv.innerHTML = '<span>⚠️</span>' + message;

                    const form = document.querySelector('.auth-form');
                    form.parentNode.insertBefore(alertDiv, form);

                    // Auto-remove after 5 seconds
                    setTimeout(() => {
                        if (alertDiv.parentNode) {
                            alertDiv.remove();
                        }
                    }, 5000);
                }
            }

            // Add smooth animations
            const authCard = document.querySelector('.auth-card');
            authCard.style.opacity = '0';
            authCard.style.transform = 'translateY(20px)';

            setTimeout(() => {
                authCard.style.transition = 'all 0.6s ease-out';
                authCard.style.opacity = '1';
                authCard.style.transform = 'translateY(0)';
            }, 100);

            // Add keyboard navigation
            document.addEventListener('keydown', function(e) {
                if (e.key === 'Enter' && e.target.tagName !== 'BUTTON') {
                    const nextInput = getNextInput(e.target);
                    if (nextInput) {
                        nextInput.focus();
                    } else {
                        form.dispatchEvent(new Event('submit'));
                    }
                }
            });

            function getNextInput(currentInput) {
                const inputs = Array.from(document.querySelectorAll('input[type="email"], input[type="password"]'));
                const currentIndex = inputs.indexOf(currentInput);
                return inputs[currentIndex + 1] || null;
            }
        });

        // Add ripple effect to buttons
        document.querySelectorAll('.btn-auth-primary, .btn-social').forEach(button => {
            button.addEventListener('click', function(e) {
                const ripple = document.createElement('span');
                const rect = this.getBoundingClientRect();
                const size = Math.max(rect.width, rect.height);
                const x = e.clientX - rect.left - size / 2;
                const y = e.clientY - rect.top - size / 2;

                ripple.style.cssText = `
                    position: absolute;
                    width: ${size}px;
                    height: ${size}px;
                    background: rgba(255, 255, 255, 0.3);
                    border-radius: 50%;
                    left: ${x}px;
                    top: ${y}px;
                    transform: scale(0);
                    animation: ripple 0.6s ease-out;
                    pointer-events: none;
                `;

                this.style.position = 'relative';
                this.style.overflow = 'hidden';
                this.appendChild(ripple);

                setTimeout(() => ripple.remove(), 600);
            });
        });

        // Add CSS for ripple animation
        const style = document.createElement('style');
        style.textContent = `
            @keyframes ripple {
                to {
                    transform: scale(2);
                    opacity: 0;
                }
            }

            .auth-form input.error {
                border-color: #ef4444 !important;
                background: #fef2f2 !important;
                animation: shake 0.3s ease-in-out;
            }

            .auth-form input.valid {
                border-color: #10b981 !important;
                background: #f0fdf4 !important;
            }

            @keyframes shake {
                0%, 100% { transform: translateX(0); }
                25% { transform: translateX(-5px); }
                75% { transform: translateX(5px); }
            }
        `;
        document.head.appendChild(style);
    </script>
</body>
</html>