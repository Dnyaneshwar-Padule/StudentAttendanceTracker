package com.attendance.dao;

import com.attendance.models.User;
import java.util.List;

/**
 * Data Access Object interface for User entity
 */
public interface UserDAO {
    
    /**
     * Create a new user in the database
     * 
     * @param user The user to create
     * @return The ID of the created user
     */
    int createUser(User user);
    
    /**
     * Get a user by ID
     * 
     * @param userId The ID of the user
     * @return The user, or null if not found
     */
    User getUserById(int userId);
    
    /**
     * Get a user by username
     * 
     * @param username The username to search for
     * @return The user, or null if not found
     */
    User getUserByUsername(String username);
    
    /**
     * Get a user by email
     * 
     * @param email The email to search for
     * @return The user, or null if not found
     */
    User getUserByEmail(String email);
    
    /**
     * Update an existing user
     * 
     * @param user The user with updated information
     * @return true if successful, false otherwise
     */
    boolean updateUser(User user);
    
    /**
     * Delete a user by ID
     * 
     * @param userId The ID of the user to delete
     * @return true if successful, false otherwise
     */
    boolean deleteUser(int userId);
    
    /**
     * Get all users
     * 
     * @return List of all users
     */
    List<User> getAllUsers();
    
    /**
     * Get users by role
     * 
     * @param role The role to filter by
     * @return List of users with the specified role
     */
    List<User> getUsersByRole(String role);
    
    /**
     * Authenticate a user with username and password
     * 
     * @param username The username
     * @param password The password (should be hashed before comparing)
     * @return The authenticated user, or null if authentication failed
     */
    User authenticateUser(String username, String password);
}