package com.attendance.dao.impl;

import com.attendance.dao.UserDAO;
import com.attendance.models.User;
import com.attendance.utils.PasswordUtils;
import com.attendance.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of UserDAO interface
 */
public class UserDAOImpl implements UserDAO {
    private static final Logger LOGGER = Logger.getLogger(UserDAOImpl.class.getName());
    
    /**
     * Create a new user
     * 
     * @param user the user to create
     * @return the created user with generated ID
     * @throws Exception if an error occurs
     */
    @Override
    public User create(User user) throws Exception {
        String sql = "INSERT INTO Users (full_name, email, password, phone, address, role, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING user_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());  // In production, this would be hashed
            stmt.setString(4, user.getPhone());
            stmt.setString(5, user.getAddress());
            stmt.setString(6, user.getRole());
            stmt.setString(7, user.getStatus() != null ? user.getStatus() : "Active");
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                user.setUserId(rs.getInt("user_id"));
                return user;
            } else {
                throw new SQLException("Failed to create user, no ID generated");
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating user", e);
            throw e;
        }
    }
    
    /**
     * Get a user by ID
     * 
     * @param userId the user ID
     * @return an Optional containing the user if found
     * @throws Exception if an error occurs
     */
    @Override
    public Optional<User> getById(int userId) throws Exception {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                return Optional.of(user);
            } else {
                return Optional.empty();
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user by ID", e);
            throw e;
        }
    }
    
    /**
     * Get a user by email
     * 
     * @param email the user email
     * @return an Optional containing the user if found
     * @throws Exception if an error occurs
     */
    @Override
    public Optional<User> getByEmail(String email) throws Exception {
        String sql = "SELECT * FROM Users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                return Optional.of(user);
            } else {
                return Optional.empty();
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user by email", e);
            throw e;
        }
    }
    
    /**
     * Update an existing user
     * 
     * @param user the user to update
     * @return the updated user
     * @throws Exception if an error occurs
     */
    @Override
    public User update(User user) throws Exception {
        String sql = "UPDATE Users SET full_name = ?, email = ?, phone = ?, " +
                   "address = ?, role = ?, status = ?, updated_at = CURRENT_TIMESTAMP " +
                   "WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getAddress());
            stmt.setString(5, user.getRole());
            stmt.setString(6, user.getStatus());
            stmt.setInt(7, user.getUserId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                return user;
            } else {
                throw new SQLException("Failed to update user, no rows affected");
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user", e);
            throw e;
        }
    }
    
    /**
     * Delete a user
     * 
     * @param userId the ID of the user to delete
     * @return true if deletion was successful
     * @throws Exception if an error occurs
     */
    @Override
    public boolean delete(int userId) throws Exception {
        String sql = "DELETE FROM Users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user", e);
            throw e;
        }
    }
    
    /**
     * Get all users
     * 
     * @return list of all users
     * @throws Exception if an error occurs
     */
    @Override
    public List<User> getAll() throws Exception {
        String sql = "SELECT * FROM Users ORDER BY user_id";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                users.add(user);
            }
            
            return users;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all users", e);
            throw e;
        }
    }
    
    /**
     * Get users by role
     * 
     * @param role the role to filter by
     * @return list of users with the specified role
     * @throws Exception if an error occurs
     */
    @Override
    public List<User> getByRole(String role) throws Exception {
        String sql = "SELECT * FROM Users WHERE role = ? ORDER BY user_id";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                users.add(user);
            }
            
            return users;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting users by role", e);
            throw e;
        }
    }
    
    /**
     * Change user password
     * 
     * @param userId the user ID
     * @param newPassword the new password
     * @return true if password was changed successfully
     * @throws Exception if an error occurs
     */
    @Override
    public boolean changePassword(int userId, String newPassword) throws Exception {
        String sql = "UPDATE Users SET password = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newPassword);  // In production, this would be hashed
            stmt.setInt(2, userId);
            
            int rowsAffected = stmt.executeUpdate();
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error changing user password", e);
            throw e;
        }
    }
    
    /**
     * Update user status
     * 
     * @param userId the user ID
     * @param newStatus the new status
     * @return true if status was updated successfully
     * @throws Exception if an error occurs
     */
    @Override
    public boolean updateStatus(int userId, String newStatus) throws Exception {
        String sql = "UPDATE Users SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newStatus);
            stmt.setInt(2, userId);
            
            int rowsAffected = stmt.executeUpdate();
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user status", e);
            throw e;
        }
    }
    
    /**
     * Authenticate a user
     * 
     * @param email the user email
     * @param password the user password
     * @return an Optional containing the user if authentication is successful
     * @throws Exception if an error occurs
     */
    @Override
    public Optional<User> authenticate(String email, String password) throws Exception {
        Optional<User> userOpt = getByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // In a real application, we would use password hashing
            // For now, we'll do a simple comparison
            if (password.equals(user.getPassword())) {
                return Optional.of(user);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Get a user by ID (direct)
     * 
     * @param userId the user ID
     * @return the user if found, null otherwise
     * @throws Exception if an error occurs
     */
    @Override
    public User getUserById(int userId) throws Exception {
        Optional<User> userOpt = getById(userId);
        return userOpt.orElse(null);
    }
    
    /**
     * Get users by role
     * 
     * @param role the role to filter by
     * @return list of users with the specified role
     * @throws Exception if an error occurs
     */
    @Override
    public List<User> getUsersByRole(String role) throws Exception {
        return getByRole(role);
    }
    
    /**
     * Get users by role and department
     * 
     * @param role the role to filter by
     * @param departmentId the department ID to filter by
     * @return list of users with the specified role and department
     * @throws Exception if an error occurs
     */
    @Override
    public List<User> getUsersByRoleAndDepartment(String role, int departmentId) throws Exception {
        String sql = "SELECT * FROM Users WHERE role = ? AND department_id = ? ORDER BY user_id";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role);
            stmt.setInt(2, departmentId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                users.add(user);
            }
            
            return users;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting users by role and department", e);
            throw e;
        }
    }
    
    /**
     * Find users by status
     * 
     * @param status the status to filter by
     * @return list of users with the specified status
     * @throws Exception if an error occurs
     */
    @Override
    public List<User> findByStatus(String status) throws Exception {
        String sql = "SELECT * FROM Users WHERE status = ? ORDER BY user_id";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                users.add(user);
            }
            
            return users;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding users by status", e);
            throw e;
        }
    }
    
    /**
     * Search for users by a query string
     * 
     * @param query the search query
     * @return list of users matching the search query
     * @throws Exception if an error occurs
     */
    @Override
    public List<User> searchUsers(String query) throws Exception {
        String sql = "SELECT * FROM Users WHERE " +
                     "LOWER(full_name) LIKE LOWER(?) OR " +
                     "LOWER(email) LIKE LOWER(?) OR " +
                     "CAST(user_id AS VARCHAR) LIKE ? " +
                     "ORDER BY user_id";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchParam = "%" + query + "%";
            stmt.setString(1, searchParam);
            stmt.setString(2, searchParam);
            stmt.setString(3, searchParam);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                users.add(user);
            }
            
            return users;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching users", e);
            throw e;
        }
    }
    
    /**
     * Map a ResultSet to a User object
     * 
     * @param rs the ResultSet
     * @return the User object
     * @throws SQLException if an error occurs
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setPhone(rs.getString("phone"));
        user.setAddress(rs.getString("address"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }
}