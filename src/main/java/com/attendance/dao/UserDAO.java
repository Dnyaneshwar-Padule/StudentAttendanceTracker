package com.attendance.dao;

import com.attendance.models.User;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object Interface for User
 */
public interface UserDAO {
    
    /**
     * Create a new user
     * 
     * @param user the user to create
     * @return the created user with generated ID
     * @throws Exception if an error occurs
     */
    User create(User user) throws Exception;
    
    /**
     * Get a user by ID
     * 
     * @param userId the user ID
     * @return an Optional containing the user if found
     * @throws Exception if an error occurs
     */
    Optional<User> getById(int userId) throws Exception;
    
    /**
     * Get a user by email
     * 
     * @param email the user email
     * @return an Optional containing the user if found
     * @throws Exception if an error occurs
     */
    Optional<User> getByEmail(String email) throws Exception;
    
    /**
     * Update an existing user
     * 
     * @param user the user to update
     * @return the updated user
     * @throws Exception if an error occurs
     */
    User update(User user) throws Exception;
    
    /**
     * Delete a user
     * 
     * @param userId the ID of the user to delete
     * @return true if deletion was successful
     * @throws Exception if an error occurs
     */
    boolean delete(int userId) throws Exception;
    
    /**
     * Get all users
     * 
     * @return list of all users
     * @throws Exception if an error occurs
     */
    List<User> getAll() throws Exception;
    
    /**
     * Get users by role
     * 
     * @param role the role to filter by
     * @return list of users with the specified role
     * @throws Exception if an error occurs
     */
    List<User> getByRole(String role) throws Exception;
    
    /**
     * Change user password
     * 
     * @param userId the user ID
     * @param newPassword the new password
     * @return true if password was changed successfully
     * @throws Exception if an error occurs
     */
    boolean changePassword(int userId, String newPassword) throws Exception;
    
    /**
     * Update user status
     * 
     * @param userId the user ID
     * @param newStatus the new status
     * @return true if status was updated successfully
     * @throws Exception if an error occurs
     */
    boolean updateStatus(int userId, String newStatus) throws Exception;
    
    /**
     * Authenticate a user
     * 
     * @param email the user email
     * @param password the user password
     * @return an Optional containing the user if authentication is successful
     * @throws Exception if an error occurs
     */
    Optional<User> authenticate(String email, String password) throws Exception;
}