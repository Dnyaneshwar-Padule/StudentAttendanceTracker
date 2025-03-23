package com.attendance.dao;

import java.util.List;
import java.util.Optional;
import com.attendance.models.User;

/**
 * Data Access Object interface for User entity
 */
public interface UserDAO {
    
    /**
     * Find a user by their ID
     * @param userId The user ID
     * @return The user if found, null otherwise
     */
    User findById(int userId);
    
    /**
     * Find a user by their ID with Optional
     * @param userId The user ID
     * @return Optional containing the user if found, empty Optional otherwise
     */
    default Optional<User> findOptionalById(int userId) {
        return Optional.ofNullable(findById(userId));
    }
    
    /**
     * Find a user by their ID (alternative name)
     * @param userId The user ID
     * @return Optional containing the user if found, empty Optional otherwise
     */
    default Optional<User> getById(int userId) {
        return findOptionalById(userId);
    }
    
    /**
     * Find a user by their email
     * @param email The user's email
     * @return The user if found, null otherwise
     */
    User findByEmail(String email);
    
    /**
     * Find a user by their email with Optional
     * @param email The user's email
     * @return Optional containing the user if found, empty Optional otherwise
     */
    default Optional<User> findOptionalByEmail(String email) {
        return Optional.ofNullable(findByEmail(email));
    }
    
    /**
     * Find a user by their email (alternative name)
     * @param email The user's email
     * @return Optional containing the user if found, empty Optional otherwise
     */
    default Optional<User> getByEmail(String email) {
        return findOptionalByEmail(email);
    }
    
    /**
     * Authenticate a user with email and password
     * @param email The user's email
     * @param password The user's password
     * @return The authenticated user if credentials are valid, null otherwise
     */
    User authenticate(String email, String password);
    
    /**
     * Authenticate a user with email and password returning Optional
     * @param email The user's email
     * @param password The user's password
     * @return Optional containing the authenticated user if credentials are valid, empty Optional otherwise
     */
    default Optional<User> authenticateOptional(String email, String password) {
        return Optional.ofNullable(authenticate(email, password));
    }
    
    /**
     * Insert a new user into the database
     * @param user The user to insert
     * @return The ID of the newly inserted user, or -1 if the operation failed
     */
    int insert(User user);
    
    /**
     * Create a new user and return the ID
     * @param user The user to create
     * @return The ID of the newly created user, or -1 if the operation failed
     */
    default int createAndGetId(User user) {
        return insert(user);
    }
    
    /**
     * Create a new user and return the created user object
     * @param user The user to create
     * @return The created user object with ID set
     */
    default User create(User user) {
        int userId = insert(user);
        if (userId > 0) {
            // Set the ID on the user object
            user.setUserId(userId);
            return user;
        }
        return null;
    }
    
    /**
     * Update an existing user
     * @param user The user to update
     * @return true if the update was successful, false otherwise
     */
    boolean update(User user);
    
    /**
     * Delete a user
     * @param userId The ID of the user to delete
     * @return true if the deletion was successful, false otherwise
     */
    boolean delete(int userId);
    
    /**
     * Get all users
     * @return A list of all users
     */
    List<User> findAll();
    
    /**
     * Find users by role
     * @param role The role to search for
     * @return A list of users with the specified role
     */
    List<User> findByRole(String role);
    
    /**
     * Find users by role (alternative name)
     * @param role The role to search for
     * @return A list of users with the specified role
     */
    default List<User> getByRole(String role) {
        return findByRole(role);
    }
    
    /**
     * Find users by department
     * @param departmentId The department ID
     * @return A list of users in the specified department
     */
    List<User> findByDepartment(int departmentId);
}