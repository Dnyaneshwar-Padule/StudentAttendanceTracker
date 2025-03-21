package com.attendance.dao.impl;

import com.attendance.dao.DepartmentDao;
import com.attendance.models.Department;
import com.attendance.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of DepartmentDao interface for database operations
 */
public class DepartmentDaoImpl implements DepartmentDao {
    private static final Logger LOGGER = Logger.getLogger(DepartmentDaoImpl.class.getName());

    @Override
    public Department findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM Departments WHERE department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDepartment(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding department by ID: " + id, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public List<Department> findAll() throws SQLException {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM Departments";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                departments.add(mapResultSetToDepartment(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all departments", e);
            throw e;
        }
        
        return departments;
    }

    @Override
    public Department save(Department department) throws SQLException {
        String sql = "INSERT INTO Departments (department_name, hod_id) VALUES (?, ?) RETURNING department_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, department.getDepartmentName());
            
            if (department.getHodId() > 0) {
                stmt.setInt(2, department.getHodId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    department.setDepartmentId(rs.getInt(1));
                    return department;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving department: " + department, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public Department update(Department department) throws SQLException {
        String sql = "UPDATE Departments SET department_name = ?, hod_id = ? WHERE department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, department.getDepartmentName());
            
            if (department.getHodId() > 0) {
                stmt.setInt(2, department.getHodId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            
            stmt.setInt(3, department.getDepartmentId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return department;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating department: " + department, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM Departments WHERE department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting department with ID: " + id, e);
            throw e;
        }
    }

    @Override
    public Department findByName(String name) throws SQLException {
        String sql = "SELECT * FROM Departments WHERE department_name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDepartment(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding department by name: " + name, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public List<Department> findByHod(int hodId) throws SQLException {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM Departments WHERE hod_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, hodId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    departments.add(mapResultSetToDepartment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding departments by HOD ID: " + hodId, e);
            throw e;
        }
        
        return departments;
    }
    
    /**
     * Maps a database result set to a Department object
     * @param rs The result set positioned at the current row
     * @return A populated Department object
     * @throws SQLException If a database error occurs
     */
    private Department mapResultSetToDepartment(ResultSet rs) throws SQLException {
        Department department = new Department();
        department.setDepartmentId(rs.getInt("department_id"));
        department.setDepartmentName(rs.getString("department_name"));
        
        // Handle null hod_id
        int hodId = rs.getInt("hod_id");
        if (!rs.wasNull()) {
            department.setHodId(hodId);
        }
        
        return department;
    }
}