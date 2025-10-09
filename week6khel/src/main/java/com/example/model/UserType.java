package com.example.model;

public enum UserType {
    PLAYER("player", "Player", "Play sports and join games"),
    COACH("coach", "Coach", "Train players and manage teams"),
    ADMIN("admin", "Administrator", "System administration");

    private final String value;
    private final String displayName;
    private final String description;

    UserType(String value, String displayName, String description) {
        this.value = value;
        this.displayName = displayName;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    // Static method to get UserType from string value
    public static UserType fromValue(String value) {
        if (value == null || value.isEmpty()) {
            return PLAYER; // Default to player
        }

        for (UserType userType : UserType.values()) {
            if (userType.value.equalsIgnoreCase(value)) {
                return userType;
            }
        }

        return PLAYER; // Default fallback
    }

    // Static method to check if value is valid
    public static boolean isValid(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        for (UserType userType : UserType.values()) {
            if (userType.value.equalsIgnoreCase(value)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
