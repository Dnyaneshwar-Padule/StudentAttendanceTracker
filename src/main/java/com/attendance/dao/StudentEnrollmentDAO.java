package com.attendance.dao;

import com.attendance.models.StudentEnrollment;
import com.attendance.models.User;
import com.attendance.models.Class;
import com.attendance.utils.DatabaseConnection;
import com.attendance.dao.impl.UserDaoImpl;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for StudentEnrollment-related database operations
 */
public class StudentEnrollmentDAO {

    private UserDao userDAO = new UserDaoImpl();
    private ClassDAO classDAO = new ClassDAO();

    /**
     * Create a new student enrollment
     * @param enrollment The student enrollment to create
     * @return true if successful, false otherwise
     */
    public boolean createEnrollment(StudentEnrollment enrollment) {
        String sql = "INSERT INTO StudentEnrollment (enrollment_id, user_id, class_id, academic_year, enrollment_status) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, enrollment.getEnrollmentId());
            pstmt.setInt(2, enrollment.getUserId());
            pstmt.setInt(3, enrollment.getClassId());
            pstmt.setString(4, enrollment.getAcademicYear());
            pstmt.setString(5, enrollment.getEnrollmentStatus());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get a student enrollment by enrollment ID
     * @param enrollmentId The enrollment ID
     * @return StudentEnrollment object if found, null otherwise
     */
    public StudentEnrollment getEnrollmentById(String enrollmentId) {
        String sql = "SELECT * FROM StudentEnrollment WHERE enrollment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, enrollmentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    StudentEnrollment enrollment = new StudentEnrollment();
                    enrollment.setEnrollmentId(rs.getString("enrollment_id"));
                    enrollment.setUserId(rs.getInt("user_id"));
                    enrollment.setClassId(rs.getInt("class_id"));
                    enrollment.setAcademicYear(rs.getString("academic_year"));
                    enrollment.setEnrollmentStatus(rs.getString("enrollment_status"));
                    
                    // Load related objects
                    try {
                        enrollment.setUser(userDAO.findById(enrollment.getUserId()));
                        enrollment.setClassObj(classDAO.getClassById(enrollment.getClassId()));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    
                    return enrollment;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Get student enrollment by user ID
     * @param userId The user ID
     * @return StudentEnrollment object if found, null otherwise
     */
    public StudentEnrollment getEnrollmentByUserId(int userId) {
        String sql = "SELECT * FROM StudentEnrollment WHERE user_id = ? AND enrollment_status = 'Active'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    StudentEnrollment enrollment = new StudentEnrollment();
                    enrollment.setEnrollmentId(rs.getString("enrollment_id"));
                    enrollment.setUserId(rs.getInt("user_id"));
                    enrollment.setClassId(rs.getInt("class_id"));
                    enrollment.setAcademicYear(rs.getString("academic_year"));
                    enrollment.setEnrollmentStatus(rs.getString("enrollment_status"));
                    
                    // Load related objects
                    try {
                        enrollment.setUser(userDAO.findById(enrollment.getUserId()));
                        enrollment.setClassObj(classDAO.getClassById(enrollment.getClassId()));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    
                    return enrollment;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Get all student enrollments by class ID
     * @param classId The class ID
     * @param academicYear The academic year (optional, can be null)
     * @return List of student enrollments
     */
    public List<StudentEnrollment> getEnrollmentsByClass(int classId, String academicYear) {
        List<StudentEnrollment> enrollments = new ArrayList<>();
        
        String sql = "SELECT * FROM StudentEnrollment WHERE class_id = ?";
        
        if (academicYear != null && !academicYear.isEmpty()) {
            sql += " AND academic_year = ?";
        }
        
        sql += " AND enrollment_status = 'Active'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, classId);
            
            if (academicYear != null && !academicYear.isEmpty()) {
                pstmt.setString(2, academicYear);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    StudentEnrollment enrollment = new StudentEnrollment();
                    enrollment.setEnrollmentId(rs.getString("enrollment_id"));
                    enrollment.setUserId(rs.getInt("user_id"));
                    enrollment.setClassId(rs.getInt("class_id"));
                    enrollment.setAcademicYear(rs.getString("academic_year"));
                    enrollment.setEnrollmentStatus(rs.getString("enrollment_status"));
                    
                    // Load user info
                    try {
                        enrollment.setUser(userDAO.findById(enrollment.getUserId()));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    
                    enrollments.add(enrollment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return enrollments;
    }

    /**
     * Update student enrollment status
     * @param enrollmentId The enrollment ID
     * @param status The new status
     * @return true if successful, false otherwise
     */
    public boolean updateEnrollmentStatus(String enrollmentId, String status) {
        String sql = "UPDATE StudentEnrollment SET enrollment_status = ? WHERE enrollment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setString(2, enrollmentId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update student class (for promotion)
     * @param enrollmentId The enrollment ID
     * @param newClassId The new class ID
     * @param newAcademicYear The new academic year
     * @return true if successful, false otherwise
     */
    public boolean updateStudentClass(String enrollmentId, int newClassId, String newAcademicYear) {
        String sql = "UPDATE StudentEnrollment SET class_id = ?, academic_year = ? WHERE enrollment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, newClassId);
            pstmt.setString(2, newAcademicYear);
            pstmt.setString(3, enrollmentId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all student enrollments for a department
     * @param departmentId The department ID
     * @param academicYear The academic year (optional, can be null)
     * @return List of student enrollments
     */
    public List<StudentEnrollment> getEnrollmentsByDepartment(int departmentId, String academicYear) {
        List<StudentEnrollment> enrollments = new ArrayList<>();
        
        String sql = "SELECT se.* FROM StudentEnrollment se " +
                    "JOIN Classes c ON se.class_id = c.class_id " +
                    "WHERE c.department_id = ? AND se.enrollment_status = 'Active'";
        
        if (academicYear != null && !academicYear.isEmpty()) {
            sql += " AND se.academic_year = ?";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, departmentId);
            
            if (academicYear != null && !academicYear.isEmpty()) {
                pstmt.setString(2, academicYear);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    StudentEnrollment enrollment = new StudentEnrollment();
                    enrollment.setEnrollmentId(rs.getString("enrollment_id"));
                    enrollment.setUserId(rs.getInt("user_id"));
                    enrollment.setClassId(rs.getInt("class_id"));
                    enrollment.setAcademicYear(rs.getString("academic_year"));
                    enrollment.setEnrollmentStatus(rs.getString("enrollment_status"));
                    
                    // Load related objects
                    try {
                        enrollment.setUser(userDAO.findById(enrollment.getUserId()));
                        enrollment.setClassObj(classDAO.getClassById(enrollment.getClassId()));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    
                    enrollments.add(enrollment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return enrollments;
    }
}