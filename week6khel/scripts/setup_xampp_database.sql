-- XAMPP MySQL Database Setup for Khel App
-- Run this script in phpMyAdmin or MySQL command line

-- Create the database
CREATE DATABASE IF NOT EXISTS khel_app
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Use the database
USE khel_app;

-- Create users table with proper structure for authentication
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    user_type ENUM('PLAYER', 'COACH', 'ADMIN') NOT NULL DEFAULT 'PLAYER',
    phone VARCHAR(20),
    date_of_birth DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_user_type (user_type)
);

-- Insert sample admin user (password: admin123)
INSERT INTO users (username, email, password, full_name, user_type) VALUES
('admin', 'admin@khel.com', '$2a$10$rZ8R0eRhLz8R0eRhLz8R0eRhLz8R0eRhLz8R0eRhLz8R0eRhLz8R0e', 'System Administrator', 'ADMIN');

-- Insert sample coach user (password: coach123)
INSERT INTO users (username, email, password, full_name, user_type) VALUES
('coach1', 'coach@khel.com', '$2a$10$rZ8R0eRhLz8R0eRhLz8R0eRhLz8R0eRhLz8R0eRhLz8R0eRhLz8R0e', 'John Coach', 'COACH');

-- Insert sample player user (password: player123)
INSERT INTO users (username, email, password, full_name, user_type) VALUES
('player1', 'player@khel.com', '$2a$10$rZ8R0eRhLz8R0eRhLz8R0eRhLz8R0eRhLz8R0eRhLz8R0eRhLz8R0e', 'Mike Player', 'PLAYER');

-- Show created tables
SHOW TABLES;

-- Show users table structure
DESCRIBE users;

-- Show sample data
SELECT id, username, email, full_name, user_type, is_active, created_at FROM users;
