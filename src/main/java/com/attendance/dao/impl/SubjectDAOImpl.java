package com.attendance.dao.impl;

import com.attendance.dao.SubjectDAO;
import com.attendance.models.Subject;
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
 * Implementation of SubjectDAO interface
 */
public class SubjectDAOImpl implements SubjectDAO {
    
    private static final Logger LOGGER = Logger.getLogger(SubjectDAOImpl.class.getName());
    
    /**
     * Create a new subject in the database
     */
    @Override
    public int createSubject(Subject subject) {
        String sql = "INSERT INTO subjects (name, code, description, department_id, class_id, teacher_id, " +
                     "semester, credits, academic_year, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, subject.getName());
            pstmt.setString(2, subject.getCode());
            pstmt.setString(3, subject.getDescription());
            pstmt.setInt(4, subject.getDepartmentId());
            pstmt.setInt(5, subject.getClassId());
            pstmt.setInt(6, subject.getTeacherId());
            pstmt.setInt(7, subject.getSemester());
            pstmt.setInt(8, subject.getCredits());
            pstmt.setString(9, subject.getAcademicYear());
            pstmt.setTimestamp(10, Timestamp.valueOf(subject.getCreatedAt() != null ? 
                    subject.getCreatedAt() : LocalDateTime.now()));
            pstmt.setTimestamp(11, Timestamp.valueOf(subject.getUpdatedAt() != null ? 
                    subject.getUpdatedAt() : LocalDateTime.now()));
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating subject failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating subject failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating subject", e);
        }
        return -1;
    }
    
    /**
     * Get a subject by ID
     */
    @Override
    public Subject getSubjectById(int subjectId) {
        String sql = "SELECT * FROM subjects WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, subjectId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToSubject(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving subject by ID", e);
        }
        return null;
    }
    
    /**
     * Get subjects by department ID
     */
    @Override
    public List<Subject> getSubjectsByDepartmentId(int departmentId) {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT * FROM subjects WHERE department_id = ? ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, departmentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                subjects.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving subjects by department ID", e);
        }
        return subjects;
    }
    
    /**
     * Get subjects by class ID
     */
    @Override
    public List<Subject> getSubjectsByClassId(int classId) {
        return getSubjectsByClass(classId); // Use the alias method
    }
    
    /**
     * Alias for getSubjectsByClassId()
     */
    @Override
    public List<Subject> getSubjectsByClass(int classId) {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT * FROM subjects WHERE class_id = ? ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, classId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                subjects.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving subjects by class ID", e);
        }
        return subjects;
    }
    
    /**
     * Get subjects by teacher ID
     */
    @Override
    public List<Subject> getSubjectsByTeacherId(int teacherId) {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT * FROM subjects WHERE teacher_id = ? ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                subjects.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving subjects by teacher ID", e);
        }
        return subjects;
    }
    
    /**
     * Get subjects by semester
     */
    @Override
    public List<Subject> getSubjectsBySemester(int semester) {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT * FROM subjects WHERE semester = ? ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, semester);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                subjects.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving subjects by semester", e);
        }
        return subjects;
    }
    
    /**
     * Update an existing subject
     */
    @Override
    public boolean updateSubject(Subject subject) {
        String sql = "UPDATE subjects SET name = ?, code = ?, description = ?, department_id = ?, " +
                     "class_id = ?, teacher_id = ?, semester = ?, credits = ?, academic_year = ?, " +
                     "updated_at = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, subject.getName());
            pstmt.setString(2, subject.getCode());
            pstmt.setString(3, subject.getDescription());
            pstmt.setInt(4, subject.getDepartmentId());
            pstmt.setInt(5, subject.getClassId());
            pstmt.setInt(6, subject.getTeacherId());
            pstmt.setInt(7, subject.getSemester());
            pstmt.setInt(8, subject.getCredits());
            pstmt.setString(9, subject.getAcademicYear());
            pstmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(11, subject.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating subject", e);
            return false;
        }
    }
    
    /**
     * Delete a subject by ID
     */
    @Override
    public boolean deleteSubject(int subjectId) {
        String sql = "DELETE FROM subjects WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, subjectId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting subject", e);
            return false;
        }
    }
    
    /**
     * Get all subjects
     */
    @Override
    public List<Subject> getAllSubjects() {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT * FROM subjects ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                subjects.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all subjects", e);
        }
        return subjects;
    }
    
    /**
     * Get subjects by teacher ID and class ID
     */
    @Override
    public List<Subject> getSubjectsByTeacherIdAndClassId(int teacherId, int classId) {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT * FROM subjects WHERE teacher_id = ? AND class_id = ? ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, teacherId);
            pstmt.setInt(2, classId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                subjects.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving subjects by teacher ID and class ID", e);
        }
        return subjects;
    }
    
    /**
     * Assign a teacher to a subject
     */
    @Override
    public boolean assignTeacherToSubject(int subjectId, int teacherId) {
        String sql = "UPDATE subjects SET teacher_id = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, teacherId);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(3, subjectId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error assigning teacher to subject", e);
            return false;
        }
    }
    
    /**
     * Get a subject by code
     */
    @Override
    public Subject getSubjectByCode(String code) {
        String sql = "SELECT * FROM subjects WHERE code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToSubject(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving subject by code", e);
        }
        return null;
    }
    
    /**
     * Check if a subject with the given code exists
     */
    @Override
    public boolean subjectExistsByCode(String code) {
        String sql = "SELECT COUNT(*) FROM subjects WHERE code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if subject exists by code", e);
        }
        return false;
    }
    
    /**
     * Assign a subject to a department and class
     */
    @Override
    public boolean assignSubjectToDepartmentClass(int departmentId, int classId, String academicYear) {
        // This would typically update all subjects matching the criteria
        // For now, just return true as a placeholder
        return true;
    }
    
    /**
     * Remove a subject from a department and class
     */
    @Override
    public boolean removeSubjectFromDepartmentClass(int departmentId, int classId, String academicYear) {
        // This would typically update all subjects matching the criteria
        // For now, just return true as a placeholder
        return true;
    }
    
    /**
     * Helper method to map a ResultSet to a Subject object
     */
    private Subject mapResultSetToSubject(ResultSet rs) throws SQLException {
        Subject subject = new Subject();
        subject.setId(rs.getInt("id"));
        subject.setName(rs.getString("name"));
        subject.setCode(rs.getString("code"));
        subject.setDescription(rs.getString("description"));
        subject.setDepartmentId(rs.getInt("department_id"));
        subject.setClassId(rs.getInt("class_id"));
        subject.setTeacherId(rs.getInt("teacher_id"));
        subject.setSemester(rs.getInt("semester"));
        subject.setCredits(rs.getInt("credits"));
        subject.setAcademicYear(rs.getString("academic_year"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        
        if (createdAt != null) {
            subject.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        if (updatedAt != null) {
            subject.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return subject;
    }
}