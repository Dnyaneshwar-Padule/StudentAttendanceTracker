package com.attendance.dao.impl;

import com.attendance.dao.TeacherAssignmentDao;
import com.attendance.models.TeacherAssignment;
import com.attendance.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of TeacherAssignmentDao interface for database operations
 */
public class TeacherAssignmentDaoImpl implements TeacherAssignmentDao {
    private static final Logger LOGGER = Logger.getLogger(TeacherAssignmentDaoImpl.class.getName());

    @Override
    public TeacherAssignment findByTeacherSubjectAndClass(int teacherId, String subjectCode, int classId) throws SQLException {
        String sql = "SELECT * FROM TeacherAssignments WHERE teacher_id = ? AND subject_code = ? AND class_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, teacherId);
            stmt.setString(2, subjectCode);
            stmt.setInt(3, classId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTeacherAssignment(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding teacher assignment by teacher, subject, and class", e);
            throw e;
        }
        
        return null;
    }

    @Override
    public List<TeacherAssignment> findAll() throws SQLException {
        List<TeacherAssignment> teacherAssignments = new ArrayList<>();
        String sql = "SELECT * FROM TeacherAssignments";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                teacherAssignments.add(mapResultSetToTeacherAssignment(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all teacher assignments", e);
            throw e;
        }
        
        return teacherAssignments;
    }

    @Override
    public List<TeacherAssignment> findByTeacher(int teacherId) throws SQLException {
        List<TeacherAssignment> teacherAssignments = new ArrayList<>();
        String sql = "SELECT * FROM TeacherAssignments WHERE teacher_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, teacherId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    teacherAssignments.add(mapResultSetToTeacherAssignment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding teacher assignments by teacher ID: " + teacherId, e);
            throw e;
        }
        
        return teacherAssignments;
    }

    @Override
    public List<TeacherAssignment> findBySubject(String subjectCode) throws SQLException {
        List<TeacherAssignment> teacherAssignments = new ArrayList<>();
        String sql = "SELECT * FROM TeacherAssignments WHERE subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, subjectCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    teacherAssignments.add(mapResultSetToTeacherAssignment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding teacher assignments by subject code: " + subjectCode, e);
            throw e;
        }
        
        return teacherAssignments;
    }

    @Override
    public List<TeacherAssignment> findByClass(int classId) throws SQLException {
        List<TeacherAssignment> teacherAssignments = new ArrayList<>();
        String sql = "SELECT * FROM TeacherAssignments WHERE class_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    teacherAssignments.add(mapResultSetToTeacherAssignment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding teacher assignments by class ID: " + classId, e);
            throw e;
        }
        
        return teacherAssignments;
    }

    @Override
    public List<TeacherAssignment> findByClassAndSubject(int classId, String subjectCode) throws SQLException {
        List<TeacherAssignment> teacherAssignments = new ArrayList<>();
        String sql = "SELECT * FROM TeacherAssignments WHERE class_id = ? AND subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId);
            stmt.setString(2, subjectCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    teacherAssignments.add(mapResultSetToTeacherAssignment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding teacher assignments by class ID and subject code", e);
            throw e;
        }
        
        return teacherAssignments;
    }

    @Override
    public TeacherAssignment save(TeacherAssignment teacherAssignment) throws SQLException {
        String sql = "INSERT INTO TeacherAssignments (teacher_id, subject_code, class_id, assignment_type) " +
                     "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, teacherAssignment.getTeacherId());
            stmt.setString(2, teacherAssignment.getSubjectCode());
            stmt.setInt(3, teacherAssignment.getClassId());
            stmt.setString(4, teacherAssignment.getAssignmentType());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return teacherAssignment;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving teacher assignment: " + teacherAssignment, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public TeacherAssignment update(TeacherAssignment teacherAssignment) throws SQLException {
        String sql = "UPDATE TeacherAssignments SET assignment_type = ? " +
                     "WHERE teacher_id = ? AND subject_code = ? AND class_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, teacherAssignment.getAssignmentType());
            stmt.setInt(2, teacherAssignment.getTeacherId());
            stmt.setString(3, teacherAssignment.getSubjectCode());
            stmt.setInt(4, teacherAssignment.getClassId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return teacherAssignment;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating teacher assignment: " + teacherAssignment, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public boolean delete(int teacherId, String subjectCode, int classId) throws SQLException {
        String sql = "DELETE FROM TeacherAssignments WHERE teacher_id = ? AND subject_code = ? AND class_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, teacherId);
            stmt.setString(2, subjectCode);
            stmt.setInt(3, classId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting teacher assignment", e);
            throw e;
        }
    }
    
    /**
     * Maps a database result set to a TeacherAssignment object
     * @param rs The result set positioned at the current row
     * @return A populated TeacherAssignment object
     * @throws SQLException If a database error occurs
     */
    private TeacherAssignment mapResultSetToTeacherAssignment(ResultSet rs) throws SQLException {
        TeacherAssignment teacherAssignment = new TeacherAssignment();
        teacherAssignment.setTeacherId(rs.getInt("teacher_id"));
        teacherAssignment.setSubjectCode(rs.getString("subject_code"));
        teacherAssignment.setClassId(rs.getInt("class_id"));
        teacherAssignment.setAssignmentType(rs.getString("assignment_type"));
        return teacherAssignment;
    }
}