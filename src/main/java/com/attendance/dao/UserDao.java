package com.attendance.dao;

import com.attendance.models.User;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for User entities
 */
public interface UserDao extends BaseDao<User, Integer> {
    
    /**
     * Find a user by email address
     * @param email The email address
     * @return The user or null if not found
     * @throws SQLException If a database error occurs
     */
    User findByEmail(String email) throws SQLException;
    
    /**
     * Find a user by phone number
     * @param phoneNo The phone number
     * @return The user or null if not found
     * @throws SQLException If a database error occurs
     */
    User findByPhoneNo(String phoneNo) throws SQLException;
    
    /**
     * Find users by role
     * @param role The role to search for
     * @return List of users with the specified role
     * @throws SQLException If a database error occurs
     */
    List<User> findByRole(String role) throws SQLException;
    
    /**
     * Find users by department
     * @param departmentId The department ID
     * @return List of users in the specified department
     * @throws SQLException If a database error occurs
     */
    List<User> findByDepartment(int departmentId) throws SQLException;
    
    /**
     * Authenticate a user with email and password
     * @param email The email address
     * @param password The plain text password
     * @return The authenticated user or null if authentication fails
     * @throws SQLException If a database error occurs
     */
    User authenticate(String email, String password) throws SQLException;
}