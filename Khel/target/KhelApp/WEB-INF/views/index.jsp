<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Khel - Sports Made Simple</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>
    <div class="hero-container">
        <!-- Navigation -->
        <nav class="nav">
            <div class="nav-content">
                <div class="nav-brand">
                    <i class="fas fa-running brand-icon"></i>
                    <span class="brand-text">Khel</span>
                </div>
                <div class="nav-actions">
                    <a href="${pageContext.request.contextPath}/login" class="btn-secondary">Log in</a>
                    <a href="${pageContext.request.contextPath}/register" class="btn-primary">Sign up</a>
                </div>
            </div>
        </nav>

        <!-- Hero Section -->
        <main class="hero-main">
            <div class="hero-content">
                <div class="hero-text">
                    <h1 class="hero-title">
                        Connect with your
                        <span class="highlight">sports community</span>
                        like never before
                    </h1>
                    <p class="hero-description">
                        Join thousands of athletes, coaches, and sports enthusiasts. 
                        Find games, book venues, and build your sports network with Khel.
                    </p>
                    <div class="hero-actions">
                        <a href="${pageContext.request.contextPath}/register" class="btn-hero-primary">
                            Get started—it's free
                        </a>
                    </div>
                </div>
                
                <div class="hero-visual">
                    <div class="stats-card">
                        <div class="stats-header">
                            <span class="stats-title">Active Players</span>
                            <span class="stats-growth">+150%</span>
                        </div>
                        <div class="stats-chart">
                            <div class="chart-bar" style="height: 20%"></div>
                            <div class="chart-bar" style="height: 40%"></div>
                            <div class="chart-bar" style="height: 60%"></div>
                            <div class="chart-bar" style="height: 80%"></div>
                            <div class="chart-bar" style="height: 100%"></div>
                            <div class="chart-bar" style="height: 75%"></div>
                        </div>
                        <div class="stats-number">2,500</div>
                        <div class="play-button">
                            <i class="fas fa-play"></i>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <!-- Features Section -->
    <section class="features">
        <div class="features-content">
            <h2 class="features-title">Everything you need to manage your sports life</h2>
            <div class="features-grid">
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fas fa-users"></i>
                    </div>
                    <h3>Find Players</h3>
                    <p>Connect with players in your area and skill level</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fas fa-map-marker-alt"></i>
                    </div>
                    <h3>Book Venues</h3>
                    <p>Reserve courts, fields, and facilities with ease</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fas fa-calendar"></i>
                    </div>
                    <h3>Schedule Games</h3>
                    <p>Organize matches and tournaments effortlessly</p>
                </div>
            </div>
        </div>
    </section>
</body>
</html>
