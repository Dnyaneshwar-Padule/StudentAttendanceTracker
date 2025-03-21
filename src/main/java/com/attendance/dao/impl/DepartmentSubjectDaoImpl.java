package com.attendance.dao.impl;

import com.attendance.dao.DepartmentSubjectDao;
import com.attendance.models.DepartmentSubject;
import com.attendance.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of DepartmentSubjectDao interface for database operations
 */
public class DepartmentSubjectDaoImpl implements DepartmentSubjectDao {
    private static final Logger LOGGER = Logger.getLogger(DepartmentSubjectDaoImpl.class.getName());

    @Override
    public DepartmentSubject findByDepartmentAndSubject(int departmentId, String subjectCode) throws SQLException {
        String sql = "SELECT * FROM DepartmentSubjects WHERE department_id = ? AND subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, departmentId);
            stmt.setString(2, subjectCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDepartmentSubject(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding department subject by department and subject", e);
            throw e;
        }
        
        return null;
    }

    @Override
    public List<DepartmentSubject> findAll() throws SQLException {
        List<DepartmentSubject> departmentSubjects = new ArrayList<>();
        String sql = "SELECT * FROM DepartmentSubjects";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                departmentSubjects.add(mapResultSetToDepartmentSubject(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all department subjects", e);
            throw e;
        }
        
        return departmentSubjects;
    }

    @Override
    public List<DepartmentSubject> findByDepartment(int departmentId) throws SQLException {
        List<DepartmentSubject> departmentSubjects = new ArrayList<>();
        String sql = "SELECT * FROM DepartmentSubjects WHERE department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, departmentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    departmentSubjects.add(mapResultSetToDepartmentSubject(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding department subjects by department ID: " + departmentId, e);
            throw e;
        }
        
        return departmentSubjects;
    }

    @Override
    public List<DepartmentSubject> findBySubject(String subjectCode) throws SQLException {
        List<DepartmentSubject> departmentSubjects = new ArrayList<>();
        String sql = "SELECT * FROM DepartmentSubjects WHERE subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, subjectCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    departmentSubjects.add(mapResultSetToDepartmentSubject(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding department subjects by subject code: " + subjectCode, e);
            throw e;
        }
        
        return departmentSubjects;
    }

    @Override
    public List<DepartmentSubject> findBySemester(String semester) throws SQLException {
        List<DepartmentSubject> departmentSubjects = new ArrayList<>();
        String sql = "SELECT * FROM DepartmentSubjects WHERE semester = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, semester);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    departmentSubjects.add(mapResultSetToDepartmentSubject(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding department subjects by semester: " + semester, e);
            throw e;
        }
        
        return departmentSubjects;
    }

    @Override
    public List<DepartmentSubject> findByDepartmentAndSemester(int departmentId, String semester) throws SQLException {
        List<DepartmentSubject> departmentSubjects = new ArrayList<>();
        String sql = "SELECT * FROM DepartmentSubjects WHERE department_id = ? AND semester = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, departmentId);
            stmt.setString(2, semester);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    departmentSubjects.add(mapResultSetToDepartmentSubject(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding department subjects by department and semester", e);
            throw e;
        }
        
        return departmentSubjects;
    }

    @Override
    public DepartmentSubject save(DepartmentSubject departmentSubject) throws SQLException {
        String sql = "INSERT INTO DepartmentSubjects (department_id, subject_code, semester, year_of_study) " +
                     "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, departmentSubject.getDepartmentId());
            stmt.setString(2, departmentSubject.getSubjectCode());
            stmt.setString(3, departmentSubject.getSemester());
            stmt.setString(4, departmentSubject.getYearOfStudy());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return departmentSubject;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving department subject: " + departmentSubject, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public DepartmentSubject update(DepartmentSubject departmentSubject) throws SQLException {
        String sql = "UPDATE DepartmentSubjects SET semester = ?, year_of_study = ? " +
                     "WHERE department_id = ? AND subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, departmentSubject.getSemester());
            stmt.setString(2, departmentSubject.getYearOfStudy());
            stmt.setInt(3, departmentSubject.getDepartmentId());
            stmt.setString(4, departmentSubject.getSubjectCode());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return departmentSubject;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating department subject: " + departmentSubject, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public boolean delete(int departmentId, String subjectCode) throws SQLException {
        String sql = "DELETE FROM DepartmentSubjects WHERE department_id = ? AND subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, departmentId);
            stmt.setString(2, subjectCode);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting department subject", e);
            throw e;
        }
    }
    
    /**
     * Maps a database result set to a DepartmentSubject object
     * @param rs The result set positioned at the current row
     * @return A populated DepartmentSubject object
     * @throws SQLException If a database error occurs
     */
    private DepartmentSubject mapResultSetToDepartmentSubject(ResultSet rs) throws SQLException {
        DepartmentSubject departmentSubject = new DepartmentSubject();
        departmentSubject.setDepartmentId(rs.getInt("department_id"));
        departmentSubject.setSubjectCode(rs.getString("subject_code"));
        departmentSubject.setSemester(rs.getString("semester"));
        departmentSubject.setYearOfStudy(rs.getString("year_of_study"));
        return departmentSubject;
    }
}