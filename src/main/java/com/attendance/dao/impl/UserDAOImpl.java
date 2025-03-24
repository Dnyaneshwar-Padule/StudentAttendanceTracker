package com.attendance.dao.impl;

import com.attendance.dao.UserDAO;
import com.attendance.models.User;
import com.attendance.utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the UserDAO interface
 */
public class UserDAOImpl implements UserDAO {
    
    private static final Logger LOGGER = Logger.getLogger(UserDAOImpl.class.getName());
    
    // SQL queries
    private static final String SQL_CREATE_USER = 
            "INSERT INTO users (username, password, first_name, last_name, email, phone_number, " +
            "role, active, created_at, updated_at, profile_picture, department) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
    
    private static final String SQL_GET_USER_BY_ID = 
            "SELECT * FROM users WHERE id = ?";
    
    private static final String SQL_GET_USER_BY_USERNAME = 
            "SELECT * FROM users WHERE username = ?";
    
    private static final String SQL_GET_USER_BY_EMAIL = 
            "SELECT * FROM users WHERE email = ?";
    
    private static final String SQL_UPDATE_USER = 
            "UPDATE users SET username = ?, password = ?, first_name = ?, last_name = ?, " +
            "email = ?, phone_number = ?, role = ?, active = ?, updated_at = ?, " +
            "profile_picture = ?, department = ? WHERE id = ?";
    
    private static final String SQL_DELETE_USER = 
            "DELETE FROM users WHERE id = ?";
    
    private static final String SQL_GET_ALL_USERS = 
            "SELECT * FROM users ORDER BY id";
    
    private static final String SQL_GET_USERS_BY_ROLE = 
            "SELECT * FROM users WHERE role = ? ORDER BY id";
    
    @Override
    public int createUser(User user) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int generatedId = -1;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_CREATE_USER, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFirstName());
            pstmt.setString(4, user.getLastName());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getPhoneNumber());
            pstmt.setString(7, user.getRole());
            pstmt.setBoolean(8, user.isActive());
            pstmt.setTimestamp(9, Timestamp.valueOf(user.getCreatedAt()));
            pstmt.setTimestamp(10, Timestamp.valueOf(user.getUpdatedAt()));
            pstmt.setString(11, user.getProfilePicture());
            pstmt.setString(12, user.getDepartment());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                rs = pstmt.getResultSet();
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                    user.setId(generatedId); // Update the user object with the new ID
                }
            }
            
            LOGGER.log(Level.INFO, "Created user with ID: {0}", generatedId);
            return generatedId;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating user", e);
            return -1;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    @Override
    public User getUserById(int userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_USER_BY_ID);
            pstmt.setInt(1, userId);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
            
            return null;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user by ID", e);
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    @Override
    public User getUserByUsername(String username) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_USER_BY_USERNAME);
            pstmt.setString(1, username);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
            
            return null;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user by username", e);
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    @Override
    public User getUserByEmail(String email) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_USER_BY_EMAIL);
            pstmt.setString(1, email);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
            
            return null;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user by email", e);
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    @Override
    public boolean updateUser(User user) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_UPDATE_USER);
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFirstName());
            pstmt.setString(4, user.getLastName());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getPhoneNumber());
            pstmt.setString(7, user.getRole());
            pstmt.setBoolean(8, user.isActive());
            pstmt.setTimestamp(9, Timestamp.valueOf(user.getUpdatedAt()));
            pstmt.setString(10, user.getProfilePicture());
            pstmt.setString(11, user.getDepartment());
            pstmt.setInt(12, user.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user", e);
            return false;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    @Override
    public boolean deleteUser(int userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_DELETE_USER);
            pstmt.setInt(1, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user", e);
            return false;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    @Override
    public List<User> getAllUsers() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_ALL_USERS);
            
            rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
            
            return users;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all users", e);
            return users;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    @Override
    public List<User> getUsersByRole(String role) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_USERS_BY_ROLE);
            pstmt.setString(1, role);
            
            rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
            
            return users;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting users by role", e);
            return users;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    @Override
    public User authenticateUser(String username, String password) {
        User user = getUserByUsername(username);
        
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        
        return null;
    }
    
    /**
     * Extract a User object from a ResultSet
     * 
     * @param rs The ResultSet containing user data
     * @return The User object
     * @throws SQLException if an error occurs
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("active"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        user.setProfilePicture(rs.getString("profile_picture"));
        user.setDepartment(rs.getString("department"));
        
        return user;
    }
    
    /**
     * Close database resources
     * 
     * @param conn The database connection
     * @param stmt The prepared statement
     * @param rs The result set
     */
    private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing ResultSet", e);
            }
        }
        
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing Statement", e);
            }
        }
        
        DatabaseConnection.closeConnection(conn);
    }
}