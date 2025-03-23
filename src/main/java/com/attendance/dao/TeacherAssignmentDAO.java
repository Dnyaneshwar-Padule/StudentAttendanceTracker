package com.attendance.dao;

import com.attendance.models.TeacherAssignment;
import com.attendance.models.User;
import com.attendance.models.Subject;
import com.attendance.models.Class;
import com.attendance.utils.DatabaseConnection;
import com.attendance.dao.impl.UserDaoImpl;
import com.attendance.dao.impl.SubjectDAOImpl;
import com.attendance.dao.impl.ClassDAOImpl;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for TeacherAssignment-related database operations
 */
public class TeacherAssignmentDAO {

    private UserDao userDAO = new UserDaoImpl();
    private SubjectDAO subjectDAO = new SubjectDAOImpl();
    private ClassDAO classDAO = new ClassDAOImpl();

    /**
     * Assign a teacher to a subject and class
     * @param assignment The teacher assignment
     * @return true if successful, false otherwise
     */
    public boolean assignTeacher(TeacherAssignment assignment) {
        String sql = "INSERT INTO TeacherAssignment (teacher_id, subject_code, class_id, assignment_type) " +
                    "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, assignment.getTeacherId());
            pstmt.setString(2, assignment.getSubjectCode());
            pstmt.setInt(3, assignment.getClassId());
            pstmt.setString(4, assignment.getAssignmentType());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all teacher assignments
     * @return List of all teacher assignments
     */
    public List<TeacherAssignment> getAllAssignments() {
        List<TeacherAssignment> assignments = new ArrayList<>();
        String sql = "SELECT * FROM TeacherAssignment";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                TeacherAssignment assignment = new TeacherAssignment();
                assignment.setTeacherId(rs.getInt("teacher_id"));
                assignment.setSubjectCode(rs.getString("subject_code"));
                assignment.setClassId(rs.getInt("class_id"));
                assignment.setAssignmentType(rs.getString("assignment_type"));
                
                // Load related objects
                try {
                    assignment.setTeacher(userDAO.findById(assignment.getTeacherId()));
                    assignment.setSubject(subjectDAO.getSubjectByCode(assignment.getSubjectCode()));
                    assignment.setClassObj(classDAO.getClassById(assignment.getClassId()));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                
                assignments.add(assignment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return assignments;
    }

    /**
     * Get teacher assignments by teacher ID
     * @param teacherId The teacher ID
     * @return List of assignments for the teacher
     */
    public List<TeacherAssignment> getAssignmentsByTeacher(int teacherId) {
        List<TeacherAssignment> assignments = new ArrayList<>();
        String sql = "SELECT * FROM TeacherAssignment WHERE teacher_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, teacherId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TeacherAssignment assignment = new TeacherAssignment();
                    assignment.setTeacherId(rs.getInt("teacher_id"));
                    assignment.setSubjectCode(rs.getString("subject_code"));
                    assignment.setClassId(rs.getInt("class_id"));
                    assignment.setAssignmentType(rs.getString("assignment_type"));
                    
                    // Load related objects
                    assignment.setSubject(subjectDAO.getSubjectByCode(assignment.getSubjectCode()));
                    assignment.setClassObj(classDAO.getClassById(assignment.getClassId()));
                    
                    assignments.add(assignment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return assignments;
    }

    /**
     * Get teacher assignments by class ID
     * @param classId The class ID
     * @return List of assignments for the class
     */
    public List<TeacherAssignment> getAssignmentsByClass(int classId) {
        List<TeacherAssignment> assignments = new ArrayList<>();
        String sql = "SELECT * FROM TeacherAssignment WHERE class_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, classId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TeacherAssignment assignment = new TeacherAssignment();
                    assignment.setTeacherId(rs.getInt("teacher_id"));
                    assignment.setSubjectCode(rs.getString("subject_code"));
                    assignment.setClassId(rs.getInt("class_id"));
                    assignment.setAssignmentType(rs.getString("assignment_type"));
                    
                    // Load related objects
                    try {
                        assignment.setTeacher(userDAO.findById(assignment.getTeacherId()));
                        assignment.setSubject(subjectDAO.getSubjectByCode(assignment.getSubjectCode()));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    
                    assignments.add(assignment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return assignments;
    }

    /**
     * Get teacher assignments by subject code
     * @param subjectCode The subject code
     * @return List of assignments for the subject
     */
    public List<TeacherAssignment> getAssignmentsBySubject(String subjectCode) {
        List<TeacherAssignment> assignments = new ArrayList<>();
        String sql = "SELECT * FROM TeacherAssignment WHERE subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, subjectCode);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TeacherAssignment assignment = new TeacherAssignment();
                    assignment.setTeacherId(rs.getInt("teacher_id"));
                    assignment.setSubjectCode(rs.getString("subject_code"));
                    assignment.setClassId(rs.getInt("class_id"));
                    assignment.setAssignmentType(rs.getString("assignment_type"));
                    
                    // Load related objects
                    try {
                        assignment.setTeacher(userDAO.findById(assignment.getTeacherId()));
                        assignment.setClassObj(classDAO.getClassById(assignment.getClassId()));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    
                    assignments.add(assignment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return assignments;
    }

    /**
     * Update a teacher assignment
     * @param assignment The assignment to update
     * @return true if successful, false otherwise
     */
    public boolean updateAssignment(TeacherAssignment assignment) {
        String sql = "UPDATE TeacherAssignment SET assignment_type = ? " +
                    "WHERE teacher_id = ? AND subject_code = ? AND class_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, assignment.getAssignmentType());
            pstmt.setInt(2, assignment.getTeacherId());
            pstmt.setString(3, assignment.getSubjectCode());
            pstmt.setInt(4, assignment.getClassId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Remove a teacher assignment
     * @param teacherId The teacher ID
     * @param subjectCode The subject code
     * @param classId The class ID
     * @return true if successful, false otherwise
     */
    public boolean removeAssignment(int teacherId, String subjectCode, int classId) {
        String sql = "DELETE FROM TeacherAssignment " +
                    "WHERE teacher_id = ? AND subject_code = ? AND class_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, teacherId);
            pstmt.setString(2, subjectCode);
            pstmt.setInt(3, classId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if a teacher is assigned to a class as a class teacher
     * @param teacherId The teacher ID
     * @param classId The class ID
     * @return true if assigned, false otherwise
     */
    public boolean isClassTeacher(int teacherId, int classId) {
        String sql = "SELECT 1 FROM TeacherAssignment " +
                    "WHERE teacher_id = ? AND class_id = ? AND assignment_type = 'Class Teacher'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, teacherId);
            pstmt.setInt(2, classId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get class teacher for a specific class
     * @param classId The class ID
     * @return User object of the class teacher if found, null otherwise
     */
    public User getClassTeacher(int classId) {
        String sql = "SELECT teacher_id FROM TeacherAssignment " +
                    "WHERE class_id = ? AND assignment_type = 'Class Teacher'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, classId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int teacherId = rs.getInt("teacher_id");
                    try {
                        return userDAO.findById(teacherId);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
}