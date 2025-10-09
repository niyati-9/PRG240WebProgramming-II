-- Database setup script for Khel App
-- Run this in your XAMPP MySQL to create the database and tables

CREATE DATABASE IF NOT EXISTS khel_app;
USE khel_app;

-- Users table for authentication
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    user_type ENUM('PLAYER', 'COACH', 'ADMIN') DEFAULT 'PLAYER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_user_type (user_type)
);

-- Insert default admin user (password: admin123)
INSERT INTO users (username, email, password, full_name, user_type) VALUES 
('admin', 'admin@khelapp.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'System Administrator', 'ADMIN');
