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
    
    /**
     * Find students by department
     * @param departmentId The department ID
     * @return List of students in the specified department
     * @throws SQLException If a database error occurs
     */
    List<User> findStudentsByDepartment(int departmentId) throws SQLException;
    
    /**
     * Find students by class
     * @param classId The class ID
     * @return List of students in the specified class
     * @throws SQLException If a database error occurs
     */
    List<User> findStudentsByClass(int classId) throws SQLException;
    
    /**
     * Find students by academic year
     * @param academicYear The academic year
     * @return List of students in the specified academic year
     * @throws SQLException If a database error occurs
     */
    List<User> findStudentsByAcademicYear(String academicYear) throws SQLException;
    
    /**
     * Find users by role and status
     * @param role The role to search for
     * @param status The status to search for
     * @return List of users with the specified role and status
     * @throws SQLException If a database error occurs
     */
    List<User> findUsersByRoleAndStatus(String role, String status) throws SQLException;
    
    /**
     * Find users by role (alias for findByRole to maintain API consistency)
     * @param role The role to search for
     * @return List of users with the specified role
     * @throws SQLException If a database error occurs
     */
    default List<User> findUsersByRole(String role) throws SQLException {
        return findByRole(role);
    }
    
    /**
     * Search for students by name, email, or ID
     * @param query The search query
     * @return List of matching students
     * @throws SQLException If a database error occurs
     */
    List<User> searchStudents(String query) throws SQLException;
    
    /**
     * Register a new user
     * @param user The user to register
     * @return The registered user with ID populated
     * @throws SQLException If a database error occurs
     */
    User registerUser(User user) throws SQLException;
}