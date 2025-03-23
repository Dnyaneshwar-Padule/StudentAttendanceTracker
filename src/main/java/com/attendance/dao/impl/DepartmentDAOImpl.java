package com.attendance.dao.impl;

import com.attendance.dao.DepartmentDAO;
import com.attendance.models.Department;
import com.attendance.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of DepartmentDAO interface
 */
public class DepartmentDAOImpl implements DepartmentDAO {
    
    private static final Logger LOGGER = Logger.getLogger(DepartmentDAOImpl.class.getName());
    
    /**
     * Create a new department in the database
     */
    @Override
    public int createDepartment(Department department) {
        String sql = "INSERT INTO departments (name, code, hod_id, description, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, department.getName());
            pstmt.setString(2, department.getCode());
            pstmt.setInt(3, department.getHodId());
            pstmt.setString(4, department.getDescription());
            pstmt.setTimestamp(5, Timestamp.valueOf(department.getCreatedAt() != null ? 
                    department.getCreatedAt() : LocalDateTime.now()));
            pstmt.setTimestamp(6, Timestamp.valueOf(department.getUpdatedAt() != null ? 
                    department.getUpdatedAt() : LocalDateTime.now()));
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating department failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating department failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating department", e);
        }
        return -1;
    }
    
    /**
     * Get a department by ID
     */
    @Override
    public Department getDepartmentById(int departmentId) {
        String sql = "SELECT * FROM departments WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, departmentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToDepartment(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving department by ID", e);
        }
        return null;
    }
    
    /**
     * Get a department by name
     */
    @Override
    public Department getDepartmentByName(String name) {
        String sql = "SELECT * FROM departments WHERE name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToDepartment(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving department by name", e);
        }
        return null;
    }
    
    /**
     * Update an existing department
     */
    @Override
    public boolean updateDepartment(Department department) {
        String sql = "UPDATE departments SET name = ?, code = ?, hod_id = ?, description = ?, updated_at = ? " +
                     "WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, department.getName());
            pstmt.setString(2, department.getCode());
            pstmt.setInt(3, department.getHodId());
            pstmt.setString(4, department.getDescription());
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(6, department.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating department", e);
            return false;
        }
    }
    
    /**
     * Delete a department by ID
     */
    @Override
    public boolean deleteDepartment(int departmentId) {
        String sql = "DELETE FROM departments WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, departmentId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting department", e);
            return false;
        }
    }
    
    /**
     * Get all departments
     */
    @Override
    public List<Department> getAllDepartments() {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM departments ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                departments.add(mapResultSetToDepartment(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all departments", e);
        }
        return departments;
    }
    
    /**
     * Get departments by HOD ID
     */
    @Override
    public List<Department> getDepartmentsByHodId(int hodId) {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM departments WHERE hod_id = ? ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, hodId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                departments.add(mapResultSetToDepartment(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving departments by HOD ID", e);
        }
        return departments;
    }
    
    /**
     * Helper method to map a ResultSet to a Department object
     */
    private Department mapResultSetToDepartment(ResultSet rs) throws SQLException {
        Department department = new Department();
        department.setId(rs.getInt("id"));
        department.setName(rs.getString("name"));
        department.setCode(rs.getString("code"));
        department.setHodId(rs.getInt("hod_id"));
        department.setDescription(rs.getString("description"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        
        if (createdAt != null) {
            department.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        if (updatedAt != null) {
            department.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return department;
    }
}