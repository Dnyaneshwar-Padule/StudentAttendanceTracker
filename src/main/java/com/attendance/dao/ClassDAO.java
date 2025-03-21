package com.attendance.dao;

import com.attendance.models.Class;
import com.attendance.models.Department;
import com.attendance.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Class-related database operations
 */
public class ClassDAO {

    private DepartmentDAO departmentDAO = new DepartmentDAO();

    /**
     * Create a new class
     * @param classObj The class object to create
     * @return The class ID if successful, -1 otherwise
     */
    public int createClass(Class classObj) {
        String sql = "INSERT INTO Classes (class_name, department_id) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, classObj.getClassName());
            pstmt.setInt(2, classObj.getDepartmentId());
            
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
     * Get a class by ID
     * @param classId The class ID
     * @return Class object if found, null otherwise
     */
    public Class getClassById(int classId) {
        String sql = "SELECT * FROM Classes WHERE class_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, classId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Class classObj = new Class();
                    classObj.setClassId(rs.getInt("class_id"));
                    classObj.setClassName(rs.getString("class_name"));
                    classObj.setDepartmentId(rs.getInt("department_id"));
                    
                    // Fetch the department details
                    Department department = departmentDAO.getDepartmentById(classObj.getDepartmentId());
                    classObj.setDepartment(department);
                    
                    return classObj;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Get all classes
     * @return List of all classes with their departments
     */
    public List<Class> getAllClasses() {
        List<Class> classes = new ArrayList<>();
        String sql = "SELECT c.*, d.department_name FROM Classes c " +
                     "JOIN Department d ON c.department_id = d.department_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Class classObj = new Class();
                classObj.setClassId(rs.getInt("class_id"));
                classObj.setClassName(rs.getString("class_name"));
                classObj.setDepartmentId(rs.getInt("department_id"));
                
                // Set department details
                Department department = new Department();
                department.setDepartmentId(rs.getInt("department_id"));
                department.setDepartmentName(rs.getString("department_name"));
                classObj.setDepartment(department);
                
                classes.add(classObj);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return classes;
    }

    /**
     * Get classes by department ID
     * @param departmentId The department ID
     * @return List of classes in the department
     */
    public List<Class> getClassesByDepartment(int departmentId) {
        List<Class> classes = new ArrayList<>();
        String sql = "SELECT * FROM Classes WHERE department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, departmentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Class classObj = new Class();
                    classObj.setClassId(rs.getInt("class_id"));
                    classObj.setClassName(rs.getString("class_name"));
                    classObj.setDepartmentId(rs.getInt("department_id"));
                    
                    classes.add(classObj);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return classes;
    }

    /**
     * Update a class
     * @param classObj The class to update
     * @return true if successful, false otherwise
     */
    public boolean updateClass(Class classObj) {
        String sql = "UPDATE Classes SET class_name = ?, department_id = ? WHERE class_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, classObj.getClassName());
            pstmt.setInt(2, classObj.getDepartmentId());
            pstmt.setInt(3, classObj.getClassId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a class
     * @param classId The ID of the class to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteClass(int classId) {
        String sql = "DELETE FROM Classes WHERE class_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, classId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if a class exists for a department
     * @param className The class name (FY, SY, TY)
     * @param departmentId The department ID
     * @return true if class exists, false otherwise
     */
    public boolean classExistsForDepartment(String className, int departmentId) {
        String sql = "SELECT 1 FROM Classes WHERE class_name = ? AND department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, className);
            pstmt.setInt(2, departmentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
