package com.attendance.dao;

import com.attendance.models.Subject;
import com.attendance.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Subject-related database operations
 */
public class SubjectDAO {

    /**
     * Create a new subject
     * @param subject The subject to create
     * @return true if successful, false otherwise
     */
    public boolean createSubject(Subject subject) {
        String sql = "INSERT INTO Subject (subject_code, subject_name) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, subject.getSubjectCode());
            pstmt.setString(2, subject.getSubjectName());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get a subject by code
     * @param subjectCode The subject code
     * @return Subject object if found, null otherwise
     */
    public Subject getSubjectByCode(String subjectCode) {
        String sql = "SELECT * FROM Subject WHERE subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, subjectCode);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Subject subject = new Subject();
                    subject.setSubjectCode(rs.getString("subject_code"));
                    subject.setSubjectName(rs.getString("subject_name"));
                    return subject;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Get all subjects
     * @return List of all subjects
     */
    public List<Subject> getAllSubjects() {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT * FROM Subject";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Subject subject = new Subject();
                subject.setSubjectCode(rs.getString("subject_code"));
                subject.setSubjectName(rs.getString("subject_name"));
                subjects.add(subject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return subjects;
    }

    /**
     * Update a subject
     * @param subject The subject to update
     * @return true if successful, false otherwise
     */
    public boolean updateSubject(Subject subject) {
        String sql = "UPDATE Subject SET subject_name = ? WHERE subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, subject.getSubjectName());
            pstmt.setString(2, subject.getSubjectCode());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a subject
     * @param subjectCode The code of the subject to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteSubject(String subjectCode) {
        String sql = "DELETE FROM Subject WHERE subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, subjectCode);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Assign a subject to a department and class
     * @param departmentId The department ID
     * @param classId The class ID
     * @param subjectCode The subject code
     * @return true if successful, false otherwise
     */
    public boolean assignSubjectToDepartmentClass(int departmentId, int classId, String subjectCode) {
        String sql = "INSERT INTO Department_Subject (department_id, class_id, subject_code) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, departmentId);
            pstmt.setInt(2, classId);
            pstmt.setString(3, subjectCode);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get subjects assigned to a specific department and class
     * @param departmentId The department ID
     * @param classId The class ID
     * @return List of subjects
     */
    public List<Subject> getSubjectsByDepartmentAndClass(int departmentId, int classId) {
        List<Subject> subjects = new ArrayList<>();
        
        String sql = "SELECT s.* FROM Subject s " +
                    "JOIN Department_Subject ds ON s.subject_code = ds.subject_code " +
                    "WHERE ds.department_id = ? AND ds.class_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, departmentId);
            pstmt.setInt(2, classId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Subject subject = new Subject();
                    subject.setSubjectCode(rs.getString("subject_code"));
                    subject.setSubjectName(rs.getString("subject_name"));
                    subjects.add(subject);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return subjects;
    }

    /**
     * Remove a subject assignment from a department and class
     * @param departmentId The department ID
     * @param classId The class ID
     * @param subjectCode The subject code
     * @return true if successful, false otherwise
     */
    public boolean removeSubjectFromDepartmentClass(int departmentId, int classId, String subjectCode) {
        String sql = "DELETE FROM Department_Subject WHERE department_id = ? AND class_id = ? AND subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, departmentId);
            pstmt.setInt(2, classId);
            pstmt.setString(3, subjectCode);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if a subject exists by code
     * @param subjectCode The subject code to check
     * @return true if subject exists, false otherwise
     */
    public boolean subjectExistsByCode(String subjectCode) {
        String sql = "SELECT 1 FROM Subject WHERE subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, subjectCode);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
