package com.attendance.dao;

import com.attendance.models.User;
import com.attendance.utils.DatabaseConnection;
import com.attendance.utils.PasswordHashing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User-related database operations
 */
public class UserDAO {

    /**
     * Register a new user
     * @param user The user to register
     * @return The user ID if successful, -1 otherwise
     */
    public int registerUser(User user) {
        String sql = "INSERT INTO Users (name, phone_no, email, password, role, department_id) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Hash the password before storing
            String hashedPassword = PasswordHashing.createPasswordHash(user.getPassword());
            
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getPhoneNo());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, hashedPassword);
            pstmt.setString(5, user.getRole());
            
            // Department ID can be null for some roles
            if (user.getDepartmentId() > 0) {
                pstmt.setInt(6, user.getDepartmentId());
            } else {
                pstmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }

    /**
     * Authenticate a user with email and password
     * @param email User's email
     * @param password User's password
     * @return User object if authenticated, null otherwise
     */
    public User authenticateUser(String email, String password) {
        String sql = "SELECT * FROM Users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    
                    // Verify the password
                    if (PasswordHashing.verifyPassword(password, storedPassword)) {
                        User user = new User();
                        user.setUserId(rs.getInt("user_id"));
                        user.setName(rs.getString("name"));
                        user.setPhoneNo(rs.getString("phone_no"));
                        user.setEmail(rs.getString("email"));
                        user.setRole(rs.getString("role"));
                        
                        // Department ID might be null
                        int departmentId = rs.getInt("department_id");
                        if (!rs.wasNull()) {
                            user.setDepartmentId(departmentId);
                        }
                        
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Get a user by ID
     * @param userId The user ID
     * @return User object if found, null otherwise
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setPhoneNo(rs.getString("phone_no"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    
                    // Department ID might be null
                    int departmentId = rs.getInt("department_id");
                    if (!rs.wasNull()) {
                        user.setDepartmentId(departmentId);
                    }
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Update user information
     * @param user The user to update
     * @return true if successful, false otherwise
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE Users SET name = ?, phone_no = ?, email = ?, role = ?, department_id = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getPhoneNo());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getRole());
            
            if (user.getDepartmentId() > 0) {
                pstmt.setInt(5, user.getDepartmentId());
            } else {
                pstmt.setNull(5, java.sql.Types.INTEGER);
            }
            
            pstmt.setInt(6, user.getUserId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Change a user's password
     * @param userId The user ID
     * @param newPassword The new password
     * @return true if successful, false otherwise
     */
    public boolean changePassword(int userId, String newPassword) {
        String sql = "UPDATE Users SET password = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String hashedPassword = PasswordHashing.createPasswordHash(newPassword);
            
            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all users with a specific role and department
     * @param role The role to filter by
     * @param departmentId The department ID to filter by
     * @return List of users
     */
    public List<User> getUsersByRoleAndDepartment(String role, int departmentId) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE role = ? AND department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, role);
            pstmt.setInt(2, departmentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setPhoneNo(rs.getString("phone_no"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    user.setDepartmentId(rs.getInt("department_id"));
                    
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return users;
    }

    /**
     * Get all users with a specific role
     * @param role The role to filter by
     * @return List of users
     */
    public List<User> getUsersByRole(String role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE role = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, role);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setPhoneNo(rs.getString("phone_no"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    
                    // Department ID might be null
                    int departmentId = rs.getInt("department_id");
                    if (!rs.wasNull()) {
                        user.setDepartmentId(departmentId);
                    }
                    
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return users;
    }

    /**
     * Check if a user exists by email
     * @param email The email to check
     * @return true if user exists, false otherwise
     */
    public boolean userExistsByEmail(String email) {
        String sql = "SELECT 1 FROM Users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
