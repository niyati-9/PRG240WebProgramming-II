package com.example.dto;

/**
 * JWT Response DTO for authentication responses
 * Contains token information and user details
 */
public class JwtResponse {

    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private String username;
    private String email;
    private String fullName;
    private String userType;
    private Long userId;
    private Long expiresIn;

    // Default constructor
    public JwtResponse() {
    }

    // Constructor with token and user info
    public JwtResponse(String token, String refreshToken, String username, String email,
                       String fullName, String userType, Long userId, Long expiresIn) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.userType = userType;
        this.userId = userId;
        this.expiresIn = expiresIn;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    @Override
    public String toString() {
        return "JwtResponse{" +
                "token='[HIDDEN]'" +
                ", type='" + type + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", userType='" + userType + '\'' +
                ", userId=" + userId +
                ", expiresIn=" + expiresIn +
                '}';
    }
}