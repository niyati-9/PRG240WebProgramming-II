<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Khel</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/css/style.css">
</head>
<body>
    <header>
        <nav class="navbar">
            <div class="container">
                <div class="logo">
                    <div class="logo-icon">&#x1F3C6;</div>
                    <h1>Khel</h1>
                </div>
                <ul class="nav-links">
                    <li><a href="${pageContext.request.contextPath}/">Home</a></li>
                    <li><a href="#sports">Sports</a></li>
                    <li><a href="#about">About</a></li>
                    <li><a href="#contact">Contact</a></li>
                </ul>
                <div class="auth-buttons">
                    <span>Welcome, ${user.fullName}</span>
                    <a href="${pageContext.request.contextPath}/logout" class="btn-login">Logout</a>
                </div>
            </div>
        </nav>
    </header>

    <!-- Welcome Section -->
    <section class="welcome-section">
        <div class="container">
            <h2>Welcome, ${user.fullName}!
                <span class="user-type-badge">
                    <c:choose>
                        <c:when test="${isAdmin}">Admin</c:when>
                        <c:when test="${isCoach}">Coach</c:when>
                        <c:when test="${isPlayer}">Player</c:when>
                        <c:otherwise>User</c:otherwise>
                    </c:choose>
                </span>
            </h2>
            <p>Ready to book your next game or manage your venues?</p>
        </div>
    </section>

    <main>
        <div class="container">
            <div class="dashboard-content">

                <!-- Admin Dashboard -->
                <c:if test="${isAdmin}">
                    <section class="quick-actions">
                        <h3>Admin Dashboard</h3>
                        <div class="action-card">
                            <div class="action-icon">&#x1F4CA;</div>
                            <h3>User Management</h3>
                            <p>Manage all users, players, and venue owners</p>
                            <a href="#users" class="btn-action">Manage Users</a>
                        </div>
                        <div class="action-card">
                            <div class="action-icon">&#x1F3DF;&#xFE0F;</div>
                            <h3>Venue Oversight</h3>
                            <p>Monitor and manage all registered venues</p>
                            <a href="#venues" class="btn-action">View Venues</a>
                        </div>
                        <div class="action-card">
                            <div class="action-icon">&#x1F4C8;</div>
                            <h3>Analytics</h3>
                            <p>View platform statistics and reports</p>
                            <a href="#analytics" class="btn-action">View Reports</a>
                        </div>
                        <div class="action-card">
                            <div class="action-icon">&#x2699;&#xFE0F;</div>
                            <h3>System Settings</h3>
                            <p>Configure platform settings and preferences</p>
                            <a href="#settings" class="btn-action">Settings</a>
                        </div>
                    </section>

                    <!-- Admin Stats -->
                    <section class="admin-stats">
                        <h4>System Statistics</h4>
                        <div class="stats-grid">
                            <div class="stat-card">
                                <h4>Total Users</h4>
                                <span class="stat-number">${totalUsers}</span>
                            </div>
                            <div class="stat-card">
                                <h4>Active Sessions</h4>
                                <span class="stat-number">-</span>
                            </div>
                        </div>
                    </section>
                </c:if>

                <!-- Coach Dashboard -->
                <c:if test="${isCoach}">
                    <section class="quick-actions">
                        <h3>Coach Dashboard</h3>
                        <div class="action-card">
                            <div class="action-icon">&#x1F3DF;&#xFE0F;</div>
                            <h3>My Venues</h3>
                            <p>Manage your registered sports venues</p>
                            <a href="#my-venues" class="btn-action">Manage Venues</a>
                        </div>
                        <div class="action-card">
                            <div class="action-icon">&#x1F4C5;</div>
                            <h3>Bookings</h3>
                            <p>View and manage venue bookings</p>
                            <a href="#bookings" class="btn-action">View Bookings</a>
                        </div>
                        <div class="action-card">
                            <div class="action-icon">&#x1F4B0;</div>
                            <h3>Earnings</h3>
                            <p>Track your venue earnings and payments</p>
                            <a href="#earnings" class="btn-action">View Earnings</a>
                        </div>
                        <div class="action-card">
                            <div class="action-icon">&#x2795;</div>
                            <h3>Add New Venue</h3>
                            <p>Register a new sports venue</p>
                            <a href="#add-venue" class="btn-action">Add Venue</a>
                        </div>
                    </section>
                </c:if>

                <!-- Player Dashboard -->
                <c:if test="${isPlayer}">
                    <section class="quick-actions">
                        <h3>Player Dashboard</h3>
                        <div class="action-card">
                            <div class="action-icon">&#x1F50D;</div>
                            <h3>Find Venues</h3>
                            <p>Search and discover sports venues near you</p>
                            <a href="#search" class="btn-action">Search Venues</a>
                        </div>
                        <div class="action-card">
                            <div class="action-icon">&#x1F4C5;</div>
                            <h3>My Bookings</h3>
                            <p>View your upcoming and past bookings</p>
                            <a href="#my-bookings" class="btn-action">My Bookings</a>
                        </div>
                        <div class="action-card">
                            <div class="action-icon">&#x2764;&#xFE0F;</div>
                            <h3>Favorites</h3>
                            <p>Access your favorite venues quickly</p>
                            <a href="#favorites" class="btn-action">Favorites</a>
                        </div>
                        <div class="action-card">
                            <div class="action-icon">&#x1F4B3;</div>
                            <h3>Payment History</h3>
                            <p>View your booking payments and receipts</p>
                            <a href="#payments" class="btn-action">Payments</a>
                        </div>
                    </section>
                </c:if>

                <!-- Sports Section (common for all users) -->
                <section id="sports" class="sports-section">
                    <div class="container">
                        <h2>Sports We Cover</h2>
                        <div class="sports-grid">
                            <div class="sport-card">
                                <div class="sport-icon">&#x26BD;</div>
                                <h3>Futsal</h3>
                                <p>Indoor football venues across Nepal</p>
                            </div>
                            <div class="sport-card">
                                <div class="sport-icon">&#x1F3CF;</div>
                                <h3>Cricket</h3>
                                <p>Cricket grounds and practice nets</p>
                            </div>
                            <div class="sport-card">
                                <div class="sport-icon">&#x1F3F8;</div>
                                <h3>Badminton</h3>
                                <p>Indoor badminton courts</p>
                            </div>
                            <div class="sport-card">
                                <div class="sport-icon">&#x1F3C0;</div>
                                <h3>Basketball</h3>
                                <p>Basketball courts and facilities</p>
                            </div>
                            <div class="sport-card">
                                <div class="sport-icon">&#x1F3CA;</div>
                                <h3>Swimming</h3>
                                <p>Swimming pools and aquatic centers</p>
                            </div>
                            <div class="sport-card">
                                <div class="sport-icon">&#x1F3D0;</div>
                                <h3>Volleyball</h3>
                                <p>Indoor and beach volleyball courts</p>
                            </div>
                            <div class="sport-card">
                                <div class="sport-icon">&#x1F3BE;</div>
                                <h3>Tennis</h3>
                                <p>Tennis courts and training facilities</p>
                            </div>
                            <div class="sport-card">
                                <div class="sport-icon">&#x1F3D3;</div>
                                <h3>Table Tennis</h3>
                                <p>Indoor table tennis halls</p>
                            </div>
                        </div>
                    </div>
                </section>
            </div>
        </div>
    </main>

    <footer>
        <div class="container">
            <div class="footer-content">
                <div class="footer-section">
                    <h3>Khel</h3>
                    <p>Digitizing sports booking across Nepal. Making it easier for athletes and sports enthusiasts to find and book quality venues while helping venue owners maximize their facility utilization.</p>
                </div>
                <div class="footer-section">
                    <h4>Quick Links</h4>
                    <ul>
                        <li><a href="${pageContext.request.contextPath}/">Home</a></li>
                        <li><a href="#sports">Sports</a></li>
                        <li><a href="#about">About</a></li>
                        <li><a href="#contact">Contact</a></li>
                        <li><a href="#faq">FAQ</a></li>
                        <li><a href="#terms">Terms & Conditions</a></li>
                    </ul>
                </div>
                <div class="footer-section">
                    <h4>For Venues</h4>
                    <ul>
                        <li><a href="#list-venue">List Your Venue</a></li>
                        <li><a href="#venue-dashboard">Venue Dashboard</a></li>
                        <li><a href="#pricing">Pricing Plans</a></li>
                        <li><a href="#support">Venue Support</a></li>
                    </ul>
                </div>
                <div class="footer-section">
                    <h4>Contact Info</h4>
                    <p><strong>Email:</strong> info@khel.com.np</p>
                    <p><strong>Phone:</strong> +977-1-7654321</p>
                    <p><strong>Address:</strong> Kathmandu, Nepal</p>
                    <p><strong>Business Hours:</strong> 9 AM - 6 PM (Mon-Fri)</p>
                </div>
            </div>
            <div class="footer-bottom">
                <p>&copy; 2025 Khel. All rights reserved. | <a href="#privacy">Privacy Policy</a> | <a href="#terms">Terms of Service</a></p>
            </div>
        </div>
    </footer>
</body>
</html>