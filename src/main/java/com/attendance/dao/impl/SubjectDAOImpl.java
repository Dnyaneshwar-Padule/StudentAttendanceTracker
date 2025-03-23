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
        String sql = "INSERT INTO Subject (subject_name, subject_code) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, subject.getName());
            pstmt.setString(2, subject.getCode());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating subject failed, no rows affected.");
            }
            
            // Since we're using subject_code as the primary key, return 1 to indicate success
            // The actual implementation might need to be adjusted based on the application logic
            return 1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating subject", e);
        }
        return -1;
    }
    
    /**
     * Get a subject by ID
     * Note: In the new schema, subjects are identified by their code rather than an ID
     */
    @Override
    public Subject getSubjectById(int subjectId) {
        // In the new database schema, subjects don't have numeric IDs
        // This method is implemented for backward compatibility
        // It will attempt to find a subject with a code that matches the ID as a string
        
        String sql = "SELECT * FROM Subject WHERE subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Convert the ID to string since subject_code is a string in the new schema
            pstmt.setString(1, String.valueOf(subjectId));
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Subject subject = new Subject();
                subject.setCode(rs.getString("subject_code"));
                subject.setName(rs.getString("subject_name"));
                return subject;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving subject by ID converted to code", e);
        }
        return null;
    }
    
    /**
     * Get subjects by department ID
     * Note: In the new schema, department-subject relationships are in Department_Subject table
     */
    @Override
    public List<Subject> getSubjectsByDepartmentId(int departmentId) {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT s.* FROM Subject s " +
                     "JOIN Department_Subject ds ON s.subject_code = ds.subject_code " +
                     "WHERE ds.department_id = ? " +
                     "ORDER BY s.subject_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, departmentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Subject subject = new Subject();
                subject.setCode(rs.getString("subject_code"));
                subject.setName(rs.getString("subject_name"));
                subjects.add(subject);
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
     * Note: In the new schema, class-subject relationships are in Department_Subject table
     */
    @Override
    public List<Subject> getSubjectsByClass(int classId) {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT s.* FROM Subject s " +
                     "JOIN Department_Subject ds ON s.subject_code = ds.subject_code " +
                     "WHERE ds.class_id = ? " +
                     "ORDER BY s.subject_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, classId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Subject subject = new Subject();
                subject.setCode(rs.getString("subject_code"));
                subject.setName(rs.getString("subject_name"));
                subjects.add(subject);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving subjects by class ID", e);
        }
        return subjects;
    }
    
    /**
     * Get subjects by teacher ID
     * Note: In the new schema, teacher-subject assignments are in TeacherAssignment table
     */
    @Override
    public List<Subject> getSubjectsByTeacherId(int teacherId) {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT s.* FROM Subject s " +
                     "JOIN TeacherAssignment ta ON s.subject_code = ta.subject_code " +
                     "WHERE ta.teacher_id = ? " +
                     "ORDER BY s.subject_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Subject subject = new Subject();
                subject.setCode(rs.getString("subject_code"));
                subject.setName(rs.getString("subject_name"));
                subjects.add(subject);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving subjects by teacher ID", e);
        }
        return subjects;
    }
    
    /**
     * Get subjects by semester
     * Note: In the current schema, semester information is not directly in the Subject table
     * but could be in an association table or derived from class info
     */
    @Override
    public List<Subject> getSubjectsBySemester(int semester) {
        List<Subject> subjects = new ArrayList<>();
        // In the current schema, we would need join queries with Classes table
        // but for now, let's retrieve all subjects and then filter in the application if needed
        String sql = "SELECT * FROM Subject ORDER BY subject_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Subject subject = new Subject();
                subject.setCode(rs.getString("subject_code"));
                subject.setName(rs.getString("subject_name"));
                // We're not filtering by semester here since it's not directly in the schema
                // Application code will need to handle this
                subjects.add(subject);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving subjects by semester", e);
        }
        return subjects;
    }
    
    /**
     * Update an existing subject
     * Note: In the new schema, only subject name can be updated in the base Subject table
     */
    @Override
    public boolean updateSubject(Subject subject) {
        String sql = "UPDATE Subject SET subject_name = ? WHERE subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, subject.getName());
            pstmt.setString(2, subject.getCode());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating subject", e);
            return false;
        }
    }
    
    /**
     * Delete a subject by ID
     * Note: In the new schema, we need to convert ID to code since subject_code is the primary key
     */
    @Override
    public boolean deleteSubject(int subjectId) {
        // First, find the subject code using the ID
        String findCodeSql = "SELECT subject_code FROM Subject WHERE subject_code = ?";
        String deleteRelationsSql = "DELETE FROM Department_Subject WHERE subject_code = ?";
        String deleteTeacherSql = "DELETE FROM TeacherAssignment WHERE subject_code = ?";
        String deleteAttendanceSql = "DELETE FROM Attendance WHERE subject_code = ?";
        String deleteSubjectSql = "DELETE FROM Subject WHERE subject_code = ?";
        
        Connection conn = null;
        String subjectCode = String.valueOf(subjectId); // Default value if we can't find it
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // First try to find the subject code
            try (PreparedStatement pstmt = conn.prepareStatement(findCodeSql)) {
                pstmt.setString(1, String.valueOf(subjectId));
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    subjectCode = rs.getString("subject_code");
                }
            }
            
            // Delete relationships first
            try (PreparedStatement pstmt = conn.prepareStatement(deleteRelationsSql)) {
                pstmt.setString(1, subjectCode);
                pstmt.executeUpdate();
            }
            
            // Delete teacher assignments
            try (PreparedStatement pstmt = conn.prepareStatement(deleteTeacherSql)) {
                pstmt.setString(1, subjectCode);
                pstmt.executeUpdate();
            }
            
            // Delete attendance records
            try (PreparedStatement pstmt = conn.prepareStatement(deleteAttendanceSql)) {
                pstmt.setString(1, subjectCode);
                pstmt.executeUpdate();
            }
            
            // Finally delete the subject
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSubjectSql)) {
                pstmt.setString(1, subjectCode);
                int affectedRows = pstmt.executeUpdate();
                conn.commit();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting subject with ID: " + subjectId, e);
            // Rollback transaction in case of error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e2) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", e2);
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection", e);
                }
            }
        }
    }
    
    /**
     * Get all subjects
     */
    @Override
    public List<Subject> getAllSubjects() {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT * FROM Subject ORDER BY subject_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Subject subject = new Subject();
                subject.setCode(rs.getString("subject_code"));
                subject.setName(rs.getString("subject_name"));
                subjects.add(subject);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all subjects", e);
        }
        return subjects;
    }
    
    /**
     * Get subjects by teacher ID and class ID
     * Note: In the new schema, these associations are in TeacherAssignment and Department_Subject tables
     */
    @Override
    public List<Subject> getSubjectsByTeacherIdAndClassId(int teacherId, int classId) {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT s.* FROM Subject s " +
                     "JOIN TeacherAssignment ta ON s.subject_code = ta.subject_code " +
                     "WHERE ta.teacher_id = ? AND ta.class_id = ? " +
                     "ORDER BY s.subject_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, teacherId);
            pstmt.setInt(2, classId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Subject subject = new Subject();
                subject.setCode(rs.getString("subject_code"));
                subject.setName(rs.getString("subject_name"));
                subjects.add(subject);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving subjects by teacher ID and class ID", e);
        }
        return subjects;
    }
    
    /**
     * Assign a teacher to a subject
     * Note: In the new schema, teacher-subject assignments are in TeacherAssignment table
     */
    @Override
    public boolean assignTeacherToSubject(int subjectId, int teacherId) {
        // Find the subject code from the ID or use the ID as string if it's a direct code
        String findCodeSql = "SELECT subject_code FROM Subject WHERE subject_code = ?";
        String checkExistingSql = "SELECT COUNT(*) FROM TeacherAssignment WHERE teacher_id = ? AND subject_code = ?";
        String insertSql = "INSERT INTO TeacherAssignment (teacher_id, subject_code, class_id, assignment_type) VALUES (?, ?, ?, ?)";
        String updateSql = "UPDATE TeacherAssignment SET class_id = ?, assignment_type = ? WHERE teacher_id = ? AND subject_code = ?";
        
        String subjectCode = String.valueOf(subjectId); // Default value
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // First try to find the subject code
            try (PreparedStatement pstmt = conn.prepareStatement(findCodeSql)) {
                pstmt.setString(1, String.valueOf(subjectId));
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    subjectCode = rs.getString("subject_code");
                }
            }
            
            // Check if the assignment already exists
            boolean assignmentExists = false;
            try (PreparedStatement pstmt = conn.prepareStatement(checkExistingSql)) {
                pstmt.setInt(1, teacherId);
                pstmt.setString(2, subjectCode);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    assignmentExists = rs.getInt(1) > 0;
                }
            }
            
            PreparedStatement pstmt;
            if (assignmentExists) {
                // Update existing assignment
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setInt(1, 0); // Default class_id as 0, can be updated later
                pstmt.setString(2, "Primary"); // Default assignment type
                pstmt.setInt(3, teacherId);
                pstmt.setString(4, subjectCode);
            } else {
                // Insert new assignment
                pstmt = conn.prepareStatement(insertSql);
                pstmt.setInt(1, teacherId);
                pstmt.setString(2, subjectCode);
                pstmt.setInt(3, 0); // Default class_id as 0, can be updated later
                pstmt.setString(4, "Primary"); // Default assignment type
            }
            
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
        String sql = "SELECT * FROM Subject WHERE subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Subject subject = new Subject();
                subject.setCode(rs.getString("subject_code"));
                subject.setName(rs.getString("subject_name"));
                return subject;
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
        String sql = "SELECT COUNT(*) FROM Subject WHERE subject_code = ?";
        
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
     * Note: In the new schema, these assignments are in Department_Subject table
     */
    @Override
    public boolean assignSubjectToDepartmentClass(int departmentId, int classId, String academicYear) {
        // We need to assign this for all subjects that should belong to this department-class combination
        // For now, we'll assume we're assigning a specific subject which is indicated by the subjectId parameter
        // that's converted from departmentId (since the method signature can't be changed)
        
        String findSubjectCodeSql = "SELECT subject_code FROM Subject WHERE subject_code = ?";
        String checkExistingSql = "SELECT COUNT(*) FROM Department_Subject WHERE department_id = ? AND class_id = ? AND subject_code = ?";
        String insertSql = "INSERT INTO Department_Subject (department_id, class_id, subject_code) VALUES (?, ?, ?)";
        
        String subjectCode = String.valueOf(departmentId); // Using departmentId as a placeholder for subjectId
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // First try to find the subject code
            try (PreparedStatement pstmt = conn.prepareStatement(findSubjectCodeSql)) {
                pstmt.setString(1, subjectCode);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    subjectCode = rs.getString("subject_code");
                } else {
                    // If we can't find the subject with this code, we can't proceed
                    LOGGER.warning("Subject with code " + subjectCode + " not found");
                    return false;
                }
            }
            
            // Check if the assignment already exists
            boolean assignmentExists = false;
            try (PreparedStatement pstmt = conn.prepareStatement(checkExistingSql)) {
                pstmt.setInt(1, departmentId);
                pstmt.setInt(2, classId);
                pstmt.setString(3, subjectCode);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    assignmentExists = rs.getInt(1) > 0;
                }
            }
            
            // Only insert if it doesn't exist
            if (!assignmentExists) {
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setInt(1, departmentId);
                    pstmt.setInt(2, classId);
                    pstmt.setString(3, subjectCode);
                    
                    int affectedRows = pstmt.executeUpdate();
                    return affectedRows > 0;
                }
            } else {
                // Already exists, consider it success
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error assigning subject to department and class", e);
            return false;
        }
    }
    
    /**
     * Remove a subject from a department and class
     * Note: In the new schema, these assignments are in Department_Subject table
     */
    @Override
    public boolean removeSubjectFromDepartmentClass(int departmentId, int classId, String academicYear) {
        // Similar to assignSubjectToDepartmentClass, we're using departmentId as a placeholder for subjectId
        String findSubjectCodeSql = "SELECT subject_code FROM Subject WHERE subject_code = ?";
        String deleteSql = "DELETE FROM Department_Subject WHERE department_id = ? AND class_id = ? AND subject_code = ?";
        
        String subjectCode = String.valueOf(departmentId); // Using departmentId as a placeholder for subjectId
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // First try to find the subject code
            try (PreparedStatement pstmt = conn.prepareStatement(findSubjectCodeSql)) {
                pstmt.setString(1, subjectCode);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    subjectCode = rs.getString("subject_code");
                } else {
                    // If we can't find the subject with this code, we assume it's already not assigned
                    LOGGER.warning("Subject with code " + subjectCode + " not found, assuming it's already removed");
                    return true;
                }
            }
            
            // Delete from Department_Subject
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setInt(1, departmentId);
                pstmt.setInt(2, classId);
                pstmt.setString(3, subjectCode);
                
                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing subject from department and class", e);
            return false;
        }
    }
    
    // Helper method removed since we're now directly mapping result sets in each method
}