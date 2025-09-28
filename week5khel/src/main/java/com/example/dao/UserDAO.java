package com.example.dao;

import com.example.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User operations
 * Defines contract for database operations
 */
public interface UserDAO {
    
    /**
     * Save a new user to the database
     * @param user the user to save
     * @return the saved user with generated ID
     */
    User save(User user);
    
    /**
     * Find a user by ID
     * @param id the user ID
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findById(Long id);
    
    /**
     * Find a user by username
     * @param username the username
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find a user by email
     * @param email the email address
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if username exists
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Find all users
     * @return list of all users
     */
    List<User> findAll();
    
    /**
     * Update an existing user
     * @param user the user with updated data
     * @return the updated user
     */
    User update(User user);
    
    /**
     * Delete a user by ID
     * @param id the user ID
     * @return true if user was deleted, false if not found
     */
    boolean deleteById(Long id);
}
