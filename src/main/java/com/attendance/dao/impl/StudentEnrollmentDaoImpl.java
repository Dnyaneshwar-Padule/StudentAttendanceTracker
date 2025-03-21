package com.attendance.dao.impl;

import com.attendance.dao.StudentEnrollmentDao;
import com.attendance.models.StudentEnrollment;
import com.attendance.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of StudentEnrollmentDao interface for database operations
 */
public class StudentEnrollmentDaoImpl implements StudentEnrollmentDao {
    private static final Logger LOGGER = Logger.getLogger(StudentEnrollmentDaoImpl.class.getName());

    @Override
    public StudentEnrollment findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM StudentEnrollments WHERE enrollment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudentEnrollment(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding student enrollment by ID: " + id, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public List<StudentEnrollment> findAll() throws SQLException {
        List<StudentEnrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM StudentEnrollments";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                enrollments.add(mapResultSetToStudentEnrollment(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all student enrollments", e);
            throw e;
        }
        
        return enrollments;
    }

    @Override
    public StudentEnrollment save(StudentEnrollment enrollment) throws SQLException {
        String sql = "INSERT INTO StudentEnrollments (student_id, class_id, academic_year, enrollment_date, status) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING enrollment_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, enrollment.getStudentId());
            stmt.setInt(2, enrollment.getClassId());
            stmt.setString(3, enrollment.getAcademicYear());
            stmt.setDate(4, enrollment.getEnrollmentDate());
            stmt.setString(5, enrollment.getStatus());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    enrollment.setEnrollmentId(rs.getInt(1));
                    return enrollment;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving student enrollment: " + enrollment, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public StudentEnrollment update(StudentEnrollment enrollment) throws SQLException {
        String sql = "UPDATE StudentEnrollments SET student_id = ?, class_id = ?, academic_year = ?, " +
                     "enrollment_date = ?, status = ? WHERE enrollment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, enrollment.getStudentId());
            stmt.setInt(2, enrollment.getClassId());
            stmt.setString(3, enrollment.getAcademicYear());
            stmt.setDate(4, enrollment.getEnrollmentDate());
            stmt.setString(5, enrollment.getStatus());
            stmt.setInt(6, enrollment.getEnrollmentId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return enrollment;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating student enrollment: " + enrollment, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM StudentEnrollments WHERE enrollment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting student enrollment with ID: " + id, e);
            throw e;
        }
    }

    @Override
    public List<StudentEnrollment> findByStudent(int studentId) throws SQLException {
        List<StudentEnrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM StudentEnrollments WHERE student_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapResultSetToStudentEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding enrollments by student ID: " + studentId, e);
            throw e;
        }
        
        return enrollments;
    }

    @Override
    public List<StudentEnrollment> findByClass(int classId) throws SQLException {
        List<StudentEnrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM StudentEnrollments WHERE class_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapResultSetToStudentEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding enrollments by class ID: " + classId, e);
            throw e;
        }
        
        return enrollments;
    }

    @Override
    public List<StudentEnrollment> findByAcademicYear(String academicYear) throws SQLException {
        List<StudentEnrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM StudentEnrollments WHERE academic_year = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, academicYear);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapResultSetToStudentEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding enrollments by academic year: " + academicYear, e);
            throw e;
        }
        
        return enrollments;
    }

    @Override
    public StudentEnrollment findByStudentClassAndYear(int studentId, int classId, String academicYear) throws SQLException {
        String sql = "SELECT * FROM StudentEnrollments WHERE student_id = ? AND class_id = ? AND academic_year = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setInt(2, classId);
            stmt.setString(3, academicYear);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudentEnrollment(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding enrollment by student, class, and year", e);
            throw e;
        }
        
        return null;
    }

    @Override
    public StudentEnrollment findCurrentEnrollment(int studentId) throws SQLException {
        String sql = "SELECT * FROM StudentEnrollments WHERE student_id = ? " +
                     "ORDER BY academic_year DESC, enrollment_date DESC LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudentEnrollment(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding current enrollment for student ID: " + studentId, e);
            throw e;
        }
        
        return null;
    }
    
    /**
     * Maps a database result set to a StudentEnrollment object
     * @param rs The result set positioned at the current row
     * @return A populated StudentEnrollment object
     * @throws SQLException If a database error occurs
     */
    private StudentEnrollment mapResultSetToStudentEnrollment(ResultSet rs) throws SQLException {
        StudentEnrollment enrollment = new StudentEnrollment();
        enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
        enrollment.setStudentId(rs.getInt("student_id"));
        enrollment.setClassId(rs.getInt("class_id"));
        enrollment.setAcademicYear(rs.getString("academic_year"));
        enrollment.setEnrollmentDate(rs.getDate("enrollment_date"));
        enrollment.setStatus(rs.getString("status"));
        return enrollment;
    }
}