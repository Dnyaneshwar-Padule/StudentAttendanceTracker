package com.attendance.dao.impl;

import com.attendance.dao.SubjectDao;
import com.attendance.models.Subject;
import com.attendance.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of SubjectDao interface for database operations
 */
public class SubjectDaoImpl implements SubjectDao {
    private static final Logger LOGGER = Logger.getLogger(SubjectDaoImpl.class.getName());

    @Override
    public Subject findById(String id) throws SQLException {
        String sql = "SELECT * FROM Subjects WHERE subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSubject(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding subject by code: " + id, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public List<Subject> findAll() throws SQLException {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT * FROM Subjects";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                subjects.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all subjects", e);
            throw e;
        }
        
        return subjects;
    }

    @Override
    public Subject save(Subject subject) throws SQLException {
        String sql = "INSERT INTO Subjects (subject_code, subject_name, semester, credits) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, subject.getSubjectCode());
            stmt.setString(2, subject.getSubjectName());
            stmt.setString(3, subject.getSemester());
            stmt.setInt(4, subject.getCredits());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return subject;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving subject: " + subject, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public Subject update(Subject subject) throws SQLException {
        String sql = "UPDATE Subjects SET subject_name = ?, semester = ?, credits = ? WHERE subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, subject.getSubjectName());
            stmt.setString(2, subject.getSemester());
            stmt.setInt(3, subject.getCredits());
            stmt.setString(4, subject.getSubjectCode());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return subject;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating subject: " + subject, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM Subjects WHERE subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting subject with code: " + id, e);
            throw e;
        }
    }

    @Override
    public List<Subject> findByDepartment(int departmentId) throws SQLException {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT s.* FROM Subjects s " +
                     "JOIN DepartmentSubjects ds ON s.subject_code = ds.subject_code " +
                     "WHERE ds.department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, departmentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    subjects.add(mapResultSetToSubject(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding subjects by department ID: " + departmentId, e);
            throw e;
        }
        
        return subjects;
    }

    @Override
    public List<Subject> findBySemester(String semester) throws SQLException {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT * FROM Subjects WHERE semester = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, semester);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    subjects.add(mapResultSetToSubject(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding subjects by semester: " + semester, e);
            throw e;
        }
        
        return subjects;
    }

    @Override
    public List<Subject> findByDepartmentAndSemester(int departmentId, String semester) throws SQLException {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT s.* FROM Subjects s " +
                     "JOIN DepartmentSubjects ds ON s.subject_code = ds.subject_code " +
                     "WHERE ds.department_id = ? AND s.semester = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, departmentId);
            stmt.setString(2, semester);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    subjects.add(mapResultSetToSubject(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding subjects by department ID and semester", e);
            throw e;
        }
        
        return subjects;
    }
    
    /**
     * Maps a database result set to a Subject object
     * @param rs The result set positioned at the current row
     * @return A populated Subject object
     * @throws SQLException If a database error occurs
     */
    private Subject mapResultSetToSubject(ResultSet rs) throws SQLException {
        Subject subject = new Subject();
        subject.setSubjectCode(rs.getString("subject_code"));
        subject.setSubjectName(rs.getString("subject_name"));
        subject.setSemester(rs.getString("semester"));
        subject.setCredits(rs.getInt("credits"));
        return subject;
    }
}