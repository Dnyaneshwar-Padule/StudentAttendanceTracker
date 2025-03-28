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
        String sql = "INSERT INTO Department (department_name) VALUES (?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, department.getName());
            
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
        String sql = "SELECT * FROM Department WHERE department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, departmentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Department department = new Department();
                department.setId(rs.getInt("department_id"));
                department.setName(rs.getString("department_name"));
                return department;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving department by ID: " + departmentId, e);
        }
        return null;
    }
    
    /**
     * Get a department by name
     */
    @Override
    public Department getDepartmentByName(String name) {
        String sql = "SELECT * FROM Department WHERE department_name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Department department = new Department();
                department.setId(rs.getInt("department_id"));
                department.setName(rs.getString("department_name"));
                return department;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving department by name: " + name, e);
        }
        return null;
    }
    
    /**
     * Update an existing department
     */
    @Override
    public boolean updateDepartment(Department department) {
        String sql = "UPDATE Department SET department_name = ? WHERE department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, department.getName());
            pstmt.setInt(2, department.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating department: " + department.getId(), e);
            return false;
        }
    }
    
    /**
     * Delete a department by ID
     */
    @Override
    public boolean deleteDepartment(int departmentId) {
        String sql = "DELETE FROM Department WHERE department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, departmentId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting department: " + departmentId, e);
            return false;
        }
    }
    
    /**
     * Get all departments
     */
    @Override
    public List<Department> getAllDepartments() {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM Department ORDER BY department_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Department department = new Department();
                department.setId(rs.getInt("department_id"));
                department.setName(rs.getString("department_name"));
                departments.add(department);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all departments", e);
        }
        return departments;
    }
    
    /**
     * Get departments by HOD ID
     * Note: In the new schema, we need to join with Users table to find HOD-department associations
     */
    @Override
    public List<Department> getDepartmentsByHodId(int hodId) {
        List<Department> departments = new ArrayList<>();
        // This is an adapted query since our new schema doesn't have HOD link directly in Department
        String sql = "SELECT d.* FROM Department d JOIN Users u ON d.department_id = u.department_id " +
                     "WHERE u.user_id = ? AND u.role = 'HOD' ORDER BY d.department_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, hodId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Department department = new Department();
                department.setId(rs.getInt("department_id"));
                department.setName(rs.getString("department_name"));
                departments.add(department);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving departments by HOD ID: " + hodId, e);
        }
        return departments;
    }
    
    // Helper method removed since we're now directly mapping result sets in each method
}