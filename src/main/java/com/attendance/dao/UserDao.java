package com.attendance.dao;

import com.attendance.models.User;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
     * Find users by role and department
     * @param role The role to search for
     * @param departmentId The department ID
     * @return List of users with the specified role and department
     * @throws SQLException If a database error occurs
     */
    List<User> findByRoleAndDepartment(String role, int departmentId) throws SQLException;
    
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
    
    /**
     * Search for users by name, email, or ID
     * @param query The search query
     * @return List of matching users
     * @throws SQLException If a database error occurs
     */
    List<User> searchUsers(String query) throws SQLException;
    
    /**
     * Find users by status
     * @param status The status to search for
     * @return List of users with the specified status
     * @throws SQLException If a database error occurs
     */
    List<User> findByStatus(String status) throws SQLException;
    
    /**
     * Count the total number of users in the system
     * @return Total number of users
     * @throws SQLException If a database error occurs
     */
    int countUsers() throws SQLException;
    
    /**
     * Count the number of users with a specific status
     * @param status The status to count
     * @return Number of users with the specified status
     * @throws SQLException If a database error occurs
     */
    int countUsersByStatus(String status) throws SQLException;
    
    /**
     * Authenticate a user with email and password, returning an empty optional if not found
     * @param email The email address
     * @param password The plain text password
     * @return Optional containing the authenticated user, empty if authentication fails
     * @throws SQLException If a database error occurs
     */
    default Optional<User> authenticateOptional(String email, String password) throws SQLException {
        User user = authenticate(email, password);
        return Optional.ofNullable(user);
    }
    
    /**
     * Find a user by email address (alias for findByEmail to maintain compatibility)
     * @param email The email address
     * @return The user or null if not found
     * @throws SQLException If a database error occurs
     */
    default User getByEmail(String email) throws SQLException {
        return findByEmail(email);
    }
    
    /**
     * Create a new user (alias for registerUser to maintain compatibility)
     * @param user The user to create
     * @return The created user with ID populated
     * @throws SQLException If a database error occurs
     */
    default User create(User user) throws SQLException {
        return registerUser(user);
    }
    
    /**
     * Find a user by ID (alias for findById to maintain compatibility)
     * @param id The user ID
     * @return The user or null if not found
     * @throws SQLException If a database error occurs
     */
    default User getById(int id) throws SQLException {
        return findById(id);
    }
    
    /**
     * Get users by role (alias for findByRole to maintain compatibility)
     * @param role The role to search for
     * @return List of users with the specified role
     * @throws SQLException If a database error occurs
     */
    default List<User> getByRole(String role) throws SQLException {
        return findByRole(role);
    }
    
    // No need for duplicate methods as authenticate is already defined at line 61
}