<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registration Success - Khel Sports Venue Booking</title>
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
                    <a href="${pageContext.request.contextPath}/signup" class="btn-signup">Sign Up</a>
                </div>
            </div>
        </nav>
    </header>

    <!-- Main Content -->
    <main class="auth-main">
        <div class="auth-container">
            <div class="auth-card summary-card">
                <div class="auth-header">
                    <div class="auth-icon success-icon">✅</div>
                    <h2>Registration Successful!</h2>
                    <p>Welcome to Khel! Your account has been created successfully.</p>
                </div>

                <!-- Success Message -->
                <div class="alert alert-success">
                    <span>🎉</span>
                    Your account registration is complete! You can now start booking amazing sports venues.
                </div>

                <!-- User Summary -->
                <div class="user-summary">
                    <h3 class="summary-title">Account Summary</h3>

                    <div class="summary-grid">
                        <div class="summary-item">
                            <div class="summary-icon">👤</div>
                            <div class="summary-content">
                                <label>Username</label>
                                <span class="summary-value">${user.username}</span>
                            </div>
                        </div>

                        <div class="summary-item">
                            <div class="summary-icon">👤</div>
                            <div class="summary-content">
                                <label>Full Name</label>
                                <span class="summary-value">${user.fullName}</span>
                            </div>
                        </div>

                        <div class="summary-item">
                            <div class="summary-icon">📧</div>
                            <div class="summary-content">
                                <label>Email Address</label>
                                <span class="summary-value">${user.email}</span>
                            </div>
                        </div>

                        <div class="summary-item">
                            <div class="summary-icon">📱</div>
                            <div class="summary-content">
                                <label>Phone Number</label>
                                <span class="summary-value">${user.phoneNumber}</span>
                            </div>
                        </div>

                        <div class="summary-item">
                            <div class="summary-icon">
                                <c:choose>
                                    <c:when test="${user.userType == 'COACH'}">🏟️</c:when>
                                    <c:when test="${user.userType == 'ADMIN'}">⚙️</c:when>
                                    <c:otherwise>⚽</c:otherwise>
                                </c:choose>
                            </div>
                            <div class="summary-content">
                                <label>Account Type</label>
                                <span class="summary-value">
                                    <c:choose>
                                        <c:when test="${user.userType == 'COACH'}">Coach</c:when>
                                        <c:when test="${user.userType == 'ADMIN'}">Administrator</c:when>
                                        <c:otherwise>Player</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                        </div>

                        <div class="summary-item">
                            <div class="summary-icon">📅</div>
                            <div class="summary-content">
                                <label>Registration Date</label>
                                <span class="summary-value">
                                    <script>
                                        document.write(new Date().toLocaleDateString('en-US', {
                                            year: 'numeric',
                                            month: 'long',
                                            day: 'numeric'
                                        }));
                                    </script>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Action Buttons -->
                <div class="summary-actions">
                    <a href="${pageContext.request.contextPath}/login" class="btn-auth-primary">
                        <span>Sign In to Your Account</span>
                        <span class="btn-icon">🚀</span>
                    </a>

                    <div class="secondary-actions">
                        <a href="${pageContext.request.contextPath}/" class="btn-secondary">
                            <span class="btn-icon">🏠</span>
                            Back to Home
                        </a>
                        <a href="${pageContext.request.contextPath}/#sports" class="btn-secondary">
                            <span class="btn-icon">⚽</span>
                            Browse Sports
                        </a>
                    </div>
                </div>

                <!-- Additional Information -->
                <div class="additional-info">
                    <div class="info-card">
                        <div class="info-icon">🛡️</div>
                        <div class="info-content">
                            <h4>Your Data is Secure</h4>
                            <p>We use industry-standard encryption to protect your personal information and ensure your privacy.</p>
                        </div>
                    </div>

                    <div class="info-card">
                        <div class="info-icon">💬</div>
                        <div class="info-content">
                            <h4>Need Help?</h4>
                            <p>Our support team is here 24/7. Contact us at <a href="mailto:support@khel.com">support@khel.com</a></p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </main>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            // Smooth entrance animation
            const authCard = document.querySelector('.auth-card');
            authCard.style.opacity = '0';
            authCard.style.transform = 'translateY(30px)';

            setTimeout(() => {
                authCard.style.transition = 'all 0.8s ease-out';
                authCard.style.opacity = '1';
                authCard.style.transform = 'translateY(0)';
            }, 100);

            // Animate summary items
            const summaryItems = document.querySelectorAll('.summary-item, .step-item, .info-card');
            summaryItems.forEach((item, index) => {
                item.style.opacity = '0';
                item.style.transform = 'translateY(20px)';

                setTimeout(() => {
                    item.style.transition = 'all 0.5s ease-out';
                    item.style.opacity = '1';
                    item.style.transform = 'translateY(0)';
                }, 200 + (index * 100));
            });

            // Auto-scroll to important information
            setTimeout(() => {
                const summarySection = document.querySelector('.user-summary');
                if (summarySection) {
                    summarySection.scrollIntoView({
                        behavior: 'smooth',
                        block: 'center'
                    });
                }
            }, 1500);

            // Add celebration effect
            setTimeout(() => {
                createCelebration();
            }, 800);
        });

        function createCelebration() {
            const colors = ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#10b981'];
            const emojis = ['🎉', '✨', '🎊', '🏆', '⭐'];

            for (let i = 0; i < 20; i++) {
                setTimeout(() => {
                    createConfetti();
                }, i * 100);
            }
        }

        function createConfetti() {
            const confetti = document.createElement('div');
            const isEmoji = Math.random() > 0.7;

            if (isEmoji) {
                confetti.textContent = ['🎉', '✨', '🎊', '🏆', '⭐'][Math.floor(Math.random() * 5)];
                confetti.style.fontSize = '20px';
            } else {
                confetti.style.width = '8px';
                confetti.style.height = '8px';
                confetti.style.backgroundColor = ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#10b981'][Math.floor(Math.random() * 5)];
            }

            confetti.style.position = 'fixed';
            confetti.style.left = Math.random() * 100 + 'vw';
            confetti.style.top = '-10px';
            confetti.style.pointerEvents = 'none';
            confetti.style.zIndex = '9999';
            confetti.style.borderRadius = '50%';

            document.body.appendChild(confetti);

            const animation = confetti.animate([
                {
                    transform: `translateY(0px) rotateZ(0deg)`,
                    opacity: 1
                },
                {
                    transform: `translateY(100vh) rotateZ(360deg)`,
                    opacity: 0
                }
            ], {
                duration: 3000 + Math.random() * 2000,
                easing: 'cubic-bezier(0.25, 0.46, 0.45, 0.94)'
            });

            animation.addEventListener('finish', () => {
                confetti.remove();
            });
        }
    </script>
</body>
</html>