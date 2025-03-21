package com.attendance.dao;

import com.attendance.models.Department;
import com.attendance.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Department-related database operations
 */
public class DepartmentDAO {

    /**
     * Create a new department
     * @param department The department to create
     * @return The department ID if successful, -1 otherwise
     */
    public int createDepartment(Department department) {
        String sql = "INSERT INTO Department (department_name) VALUES (?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, department.getDepartmentName());
            
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
     * Get a department by ID
     * @param departmentId The department ID
     * @return Department object if found, null otherwise
     */
    public Department getDepartmentById(int departmentId) {
        String sql = "SELECT * FROM Department WHERE department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, departmentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Department department = new Department();
                    department.setDepartmentId(rs.getInt("department_id"));
                    department.setDepartmentName(rs.getString("department_name"));
                    return department;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Get all departments
     * @return List of all departments
     */
    public List<Department> getAllDepartments() {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM Department";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Department department = new Department();
                department.setDepartmentId(rs.getInt("department_id"));
                department.setDepartmentName(rs.getString("department_name"));
                departments.add(department);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return departments;
    }

    /**
     * Update a department
     * @param department The department to update
     * @return true if successful, false otherwise
     */
    public boolean updateDepartment(Department department) {
        String sql = "UPDATE Department SET department_name = ? WHERE department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, department.getDepartmentName());
            pstmt.setInt(2, department.getDepartmentId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a department
     * @param departmentId The ID of the department to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteDepartment(int departmentId) {
        String sql = "DELETE FROM Department WHERE department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, departmentId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if a department exists by name
     * @param departmentName The name to check
     * @return true if department exists, false otherwise
     */
    public boolean departmentExistsByName(String departmentName) {
        String sql = "SELECT 1 FROM Department WHERE department_name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, departmentName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
