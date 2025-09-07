package com.example.model;

import java.time.LocalDateTime;

public class User {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private UserType userType;
    private boolean isActive;
    private boolean emailVerified;
    private boolean phoneVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    // Profile information
    private String profilePicture;
    private String city;
    private String address;
    private String bio;

    // Preferences
    private boolean receiveNotifications;
    private boolean receiveNewsletter;

    // Default constructor
    public User() {
        this.isActive = true;
        this.emailVerified = false;
        this.phoneVerified = false;
        this.receiveNotifications = true;
        this.receiveNewsletter = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor with essential fields
    public User(String fullName, String email, String phone, String password, UserType userType) {
        this();
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.userType = userType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.emailVerified = false; // Reset verification when email changes
        this.updatedAt = LocalDateTime.now();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        this.phoneVerified = false; // Reset verification when phone changes
        this.updatedAt = LocalDateTime.now();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
        this.updatedAt = LocalDateTime.now();
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
        this.updatedAt = LocalDateTime.now();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        this.updatedAt = LocalDateTime.now();
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isReceiveNotifications() {
        return receiveNotifications;
    }

    public void setReceiveNotifications(boolean receiveNotifications) {
        this.receiveNotifications = receiveNotifications;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isReceiveNewsletter() {
        return receiveNewsletter;
    }

    public void setReceiveNewsletter(boolean receiveNewsletter) {
        this.receiveNewsletter = receiveNewsletter;
        this.updatedAt = LocalDateTime.now();
    }

    // Utility methods
    public String getFirstName() {
        if (fullName == null || fullName.isEmpty()) {
            return "";
        }
        return fullName.split(" ")[0];
    }

    public String getLastName() {
        if (fullName == null || fullName.isEmpty() || !fullName.contains(" ")) {
            return "";
        }
        String[] parts = fullName.split(" ");
        return parts[parts.length - 1];
    }

    public boolean isPlayer() {
        return userType == UserType.PLAYER;
    }

    public boolean isVenueOwner() {
        return userType == UserType.VENUE_OWNER;
    }

    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }

    public boolean canLogin() {
        return isActive && emailVerified;
    }

    // toString method
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", userType=" + userType +
                ", isActive=" + isActive +
                ", emailVerified=" + emailVerified +
                ", phoneVerified=" + phoneVerified +
                ", createdAt=" + createdAt +
                ", city='" + city + '\'' +
                '}';
    }

    // equals and hashCode based on email (unique identifier)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return email != null && email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return email != null ? email.hashCode() : 0;
    }
}