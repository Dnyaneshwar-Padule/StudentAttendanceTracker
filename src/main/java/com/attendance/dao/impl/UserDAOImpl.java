package com.attendance.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.attendance.dao.UserDAO;
import com.attendance.models.User;
import com.attendance.utils.DatabaseConnection;

/**
 * Implementation of the UserDAO interface
 */
public class UserDAOImpl implements UserDAO {
    
    private static final Logger LOGGER = Logger.getLogger(UserDAOImpl.class.getName());
    
    @Override
    public User findById(int userId) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by ID: " + userId, e);
        }
        
        return null;
    }
    
    @Override
    public User findByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by email: " + email, e);
        }
        
        return null;
    }
    
    @Override
    public User authenticate(String email, String password) {
        String sql = "SELECT * FROM Users WHERE email = ? AND password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error authenticating user: " + email, e);
        }
        
        return null;
    }
    
    @Override
    public int insert(User user) {
        String sql = "INSERT INTO Users (name, phone_no, email, password, role, department_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING user_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getPhoneNo());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getRole());
            
            if (user.getDepartmentId() != null) {
                stmt.setInt(6, user.getDepartmentId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting user: " + user, e);
        }
        
        return -1;
    }
    
    @Override
    public boolean update(User user) {
        String sql = "UPDATE Users SET name = ?, phone_no = ?, email = ?, " +
                     "password = ?, role = ?, department_id = ? " +
                     "WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getPhoneNo());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getRole());
            
            if (user.getDepartmentId() != null) {
                stmt.setInt(6, user.getDepartmentId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            stmt.setInt(7, user.getUserId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user: " + user, e);
            return false;
        }
    }
    
    @Override
    public boolean delete(int userId) {
        String sql = "DELETE FROM Users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user with ID: " + userId, e);
            return false;
        }
    }
    
    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM Users";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all users", e);
        }
        
        return users;
    }
    
    @Override
    public List<User> findByRole(String role) {
        String sql = "SELECT * FROM Users WHERE role = ?";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(extractUserFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding users by role: " + role, e);
        }
        
        return users;
    }
    
    @Override
    public List<User> findByDepartment(int departmentId) {
        String sql = "SELECT * FROM Users WHERE department_id = ?";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, departmentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(extractUserFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding users by department ID: " + departmentId, e);
        }
        
        return users;
    }
    
    /**
     * Helper method to extract a User from a ResultSet
     * @param rs The ResultSet containing user data
     * @return A User object
     * @throws SQLException If there's an error accessing the ResultSet
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setPhoneNo(rs.getString("phone_no"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        
        int departmentId = rs.getInt("department_id");
        if (!rs.wasNull()) {
            user.setDepartmentId(departmentId);
        }
        
        return user;
    }
}