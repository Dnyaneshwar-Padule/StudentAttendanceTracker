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
    public Subject findByCode(String code) throws SQLException {
        // This method is equivalent to findById since the ID for a subject is its code
        return findById(code);
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
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);  // Start transaction
            
            // First insert into Subjects table
            String sqlSubject = "INSERT INTO Subjects (subject_code, subject_name, semester, credits) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(sqlSubject);
            stmt.setString(1, subject.getSubjectCode());
            stmt.setString(2, subject.getSubjectName());
            stmt.setString(3, subject.getSemester());
            stmt.setInt(4, subject.getCredits());
            
            int rowsAffected = stmt.executeUpdate();
            
            // If department ID is set, also insert into DepartmentSubjects table
            if (rowsAffected > 0 && subject.getDepartmentId() > 0) {
                stmt.close();
                
                String sqlDeptSubject = "INSERT INTO DepartmentSubjects (department_id, subject_code) VALUES (?, ?)";
                stmt = conn.prepareStatement(sqlDeptSubject);
                stmt.setInt(1, subject.getDepartmentId());
                stmt.setString(2, subject.getSubjectCode());
                
                stmt.executeUpdate();
            }
            
            conn.commit();  // Commit transaction
            return subject;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();  // Rollback on error
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
            LOGGER.log(Level.SEVERE, "Error saving subject: " + subject, e);
            throw e;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error closing statement", e);
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);  // Reset auto commit
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error closing connection", e);
                }
            }
        }
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
        String sql = "SELECT s.*, ds.department_id FROM Subjects s " +
                     "JOIN DepartmentSubjects ds ON s.subject_code = ds.subject_code " +
                     "WHERE ds.department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, departmentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Subject subject = mapResultSetToSubject(rs);
                    subject.setDepartmentId(departmentId); // Explicitly set department ID
                    subjects.add(subject);
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
    
    @Override
    public List<Subject> findByDepartmentAndClass(int departmentId, int classId) throws SQLException {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT s.* FROM Subjects s " +
                     "JOIN DepartmentSubjects ds ON s.subject_code = ds.subject_code " +
                     "JOIN Classes c ON s.semester = c.semester " +
                     "WHERE ds.department_id = ? AND c.class_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, departmentId);
            stmt.setInt(2, classId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    subjects.add(mapResultSetToSubject(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding subjects by department ID and class ID", e);
            throw e;
        }
        
        return subjects;
    }
    
    @Override
    public List<Subject> findByClassId(int classId) throws SQLException {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT s.* FROM Subjects s " +
                     "JOIN Classes c ON s.semester = c.semester " +
                     "WHERE c.class_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    subjects.add(mapResultSetToSubject(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding subjects by class ID: " + classId, e);
            throw e;
        }
        
        return subjects;
    }
    
    @Override
    public List<Subject> searchSubjects(String query) throws SQLException {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT * FROM Subjects WHERE subject_code LIKE ? OR subject_name LIKE ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String likePattern = "%" + query + "%";
            stmt.setString(1, likePattern);
            stmt.setString(2, likePattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    subjects.add(mapResultSetToSubject(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching subjects with query: " + query, e);
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
        
        // Try to map departmentId if the column exists
        try {
            subject.setDepartmentId(rs.getInt("department_id"));
        } catch (SQLException e) {
            // Column may not exist in some queries, that's ok
        }
        
        return subject;
    }
    
    @Override
    public List<Subject> findAllByStudentAndSemester(int studentId, String semester, String academicYear) throws SQLException {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT s.* FROM Subjects s " +
                     "JOIN StudentEnrollment se ON s.class_id = se.class_id " +
                     "WHERE se.student_id = ? AND s.semester = ? AND se.academic_year = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setString(2, semester);
            stmt.setString(3, academicYear);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    subjects.add(mapResultSetToSubject(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding subjects for student and semester", e);
            throw e;
        }
        
        return subjects;
    }
}