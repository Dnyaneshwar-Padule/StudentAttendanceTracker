package com.attendance.dao.impl;

import com.attendance.dao.UserDao;
import com.attendance.models.User;
import com.attendance.utils.DatabaseConnection;
import com.attendance.utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of UserDao interface for database operations
 */
public class UserDaoImpl implements UserDao {
    private static final Logger LOGGER = Logger.getLogger(UserDaoImpl.class.getName());

    @Override
    public User findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by ID: " + id, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all users", e);
            throw e;
        }
        
        return users;
    }

    @Override
    public User save(User user) throws SQLException {
        String sql = "INSERT INTO Users (name, phone_no, email, password, role, department_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING user_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getPhoneNo());
            stmt.setString(3, user.getEmail());
            
            // Generate secure password hash
            String securePassword = PasswordUtils.generateSecurePassword(user.getPassword());
            stmt.setString(4, securePassword);
            
            stmt.setString(5, user.getRole());
            
            if (user.getDepartmentId() > 0) {
                stmt.setInt(6, user.getDepartmentId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user.setUserId(rs.getInt(1));
                    return user;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving user: " + user, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public User update(User user) throws SQLException {
        String sql = "UPDATE Users SET name = ?, phone_no = ?, email = ?, role = ?, department_id = ? " +
                     "WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getPhoneNo());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getRole());
            
            if (user.getDepartmentId() > 0) {
                stmt.setInt(5, user.getDepartmentId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            
            stmt.setInt(6, user.getUserId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return user;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user: " + user, e);
            throw e;
        }
        
        return null;
    }
    
    /**
     * Update user's password
     * @param userId User ID
     * @param newPassword New password (plain text)
     * @return true if password was updated, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updatePassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE Users SET password = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Generate secure password hash
            String securePassword = PasswordUtils.generateSecurePassword(newPassword);
            stmt.setString(1, securePassword);
            stmt.setInt(2, userId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating password for user ID: " + userId, e);
            throw e;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM Users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user with ID: " + id, e);
            throw e;
        }
    }

    @Override
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM Users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by email: " + email, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public User findByPhoneNo(String phoneNo) throws SQLException {
        String sql = "SELECT * FROM Users WHERE phone_no = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, phoneNo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by phone number: " + phoneNo, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public List<User> findByRole(String role) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE role = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding users by role: " + role, e);
            throw e;
        }
        
        return users;
    }

    @Override
    public List<User> findByDepartment(int departmentId) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, departmentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding users by department ID: " + departmentId, e);
            throw e;
        }
        
        return users;
    }

    @Override
    public User authenticate(String email, String password) throws SQLException {
        String sql = "SELECT * FROM Users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    String storedSecurePassword = user.getPassword();
                    
                    // Verify password matches
                    if (PasswordUtils.verifySecurePassword(password, storedSecurePassword)) {
                        // Don't return the password in the user object for security reasons
                        user.setPassword(null);
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error authenticating user with email: " + email, e);
            throw e;
        }
        
        return null;
    }
    
    /**
     * Maps a database result set to a User object
     * @param rs The result set positioned at the current row
     * @return A populated User object
     * @throws SQLException If a database error occurs
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setPhoneNo(rs.getString("phone_no"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        
        // Handle null department_id
        int departmentId = rs.getInt("department_id");
        if (!rs.wasNull()) {
            user.setDepartmentId(departmentId);
        }
        
        return user;
    }
}