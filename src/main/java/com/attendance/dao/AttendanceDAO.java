package com.attendance.dao;

import com.attendance.models.Attendance;
import com.attendance.models.User;
import com.attendance.models.Subject;
import com.attendance.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Attendance-related database operations
 */
public class AttendanceDAO {

    private UserDAO userDAO = new UserDAO();
    private SubjectDAO subjectDAO = new SubjectDAO();

    /**
     * Record attendance for a student
     * @param attendance The attendance record to save
     * @return The attendance ID if successful, -1 otherwise
     */
    public int recordAttendance(Attendance attendance) {
        String sql = "INSERT INTO Attendance (attendance_date, subject_code, student_id, semester, academic_year, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setDate(1, attendance.getAttendanceDate());
            pstmt.setString(2, attendance.getSubjectCode());
            pstmt.setInt(3, attendance.getStudentId());
            pstmt.setString(4, attendance.getSemester());
            pstmt.setString(5, attendance.getAcademicYear());
            pstmt.setString(6, attendance.getStatus());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }

    /**
     * Update an existing attendance record
     * @param attendance The attendance record to update
     * @return true if successful, false otherwise
     */
    public boolean updateAttendance(Attendance attendance) {
        String sql = "UPDATE Attendance SET status = ? WHERE attendance_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, attendance.getStatus());
            pstmt.setInt(2, attendance.getAttendanceId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get attendance records for a student by subject and date range
     * @param studentId The student ID
     * @param subjectCode The subject code
     * @param startDate The start date
     * @param endDate The end date
     * @return List of attendance records
     */
    public List<Attendance> getStudentAttendance(int studentId, String subjectCode, Date startDate, Date endDate) {
        List<Attendance> attendanceList = new ArrayList<>();
        
        String sql = "SELECT * FROM Attendance WHERE student_id = ?";
        
        if (subjectCode != null && !subjectCode.isEmpty()) {
            sql += " AND subject_code = ?";
        }
        
        if (startDate != null && endDate != null) {
            sql += " AND attendance_date BETWEEN ? AND ?";
        }
        
        sql += " ORDER BY attendance_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            pstmt.setInt(paramIndex++, studentId);
            
            if (subjectCode != null && !subjectCode.isEmpty()) {
                pstmt.setString(paramIndex++, subjectCode);
            }
            
            if (startDate != null && endDate != null) {
                pstmt.setDate(paramIndex++, startDate);
                pstmt.setDate(paramIndex++, endDate);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Attendance attendance = new Attendance();
                    attendance.setAttendanceId(rs.getInt("attendance_id"));
                    attendance.setAttendanceDate(rs.getDate("attendance_date"));
                    attendance.setSubjectCode(rs.getString("subject_code"));
                    attendance.setStudentId(rs.getInt("student_id"));
                    attendance.setSemester(rs.getString("semester"));
                    attendance.setAcademicYear(rs.getString("academic_year"));
                    attendance.setStatus(rs.getString("status"));
                    
                    // Load related objects
                    attendance.setSubject(subjectDAO.getSubjectByCode(attendance.getSubjectCode()));
                    
                    attendanceList.add(attendance);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return attendanceList;
    }

    /**
     * Get attendance records for a class, subject, and date
     * @param classId The class ID
     * @param subjectCode The subject code
     * @param date The attendance date
     * @param semester The semester
     * @return List of attendance records
     */
    public List<Attendance> getClassAttendance(int classId, String subjectCode, Date date, String semester) {
        List<Attendance> attendanceList = new ArrayList<>();
        
        String sql = "SELECT a.* FROM Attendance a " +
                    "JOIN StudentEnrollment se ON a.student_id = se.user_id " +
                    "WHERE se.class_id = ? AND a.subject_code = ? AND a.attendance_date = ? AND a.semester = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, classId);
            pstmt.setString(2, subjectCode);
            pstmt.setDate(3, date);
            pstmt.setString(4, semester);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Attendance attendance = new Attendance();
                    attendance.setAttendanceId(rs.getInt("attendance_id"));
                    attendance.setAttendanceDate(rs.getDate("attendance_date"));
                    attendance.setSubjectCode(rs.getString("subject_code"));
                    attendance.setStudentId(rs.getInt("student_id"));
                    attendance.setSemester(rs.getString("semester"));
                    attendance.setAcademicYear(rs.getString("academic_year"));
                    attendance.setStatus(rs.getString("status"));
                    
                    // Load related objects
                    attendance.setStudent(userDAO.getUserById(attendance.getStudentId()));
                    attendance.setSubject(subjectDAO.getSubjectByCode(attendance.getSubjectCode()));
                    
                    attendanceList.add(attendance);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return attendanceList;
    }

    /**
     * Check if attendance exists for a student on a specific date and subject
     * @param studentId The student ID
     * @param subjectCode The subject code
     * @param date The attendance date
     * @return true if attendance record exists, false otherwise
     */
    public boolean attendanceExists(int studentId, String subjectCode, Date date) {
        String sql = "SELECT 1 FROM Attendance WHERE student_id = ? AND subject_code = ? AND attendance_date = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setString(2, subjectCode);
            pstmt.setDate(3, date);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get attendance records for bulk update (all students in a class for a subject and date)
     * @param classId The class ID
     * @param subjectCode The subject code
     * @param date The attendance date
     * @param semester The semester
     * @param academicYear The academic year
     * @return Map of student IDs to their attendance records (or null if no record exists)
     */
    public Map<Integer, Attendance> getAttendanceForBulkUpdate(int classId, String subjectCode, Date date, 
                                                              String semester, String academicYear) {
        Map<Integer, Attendance> attendanceMap = new HashMap<>();
        
        // First get all students in the class
        String sqlStudents = "SELECT user_id FROM StudentEnrollment WHERE class_id = ? AND academic_year = ? AND enrollment_status = 'Active'";
        
        // Then check existing attendance records
        String sqlAttendance = "SELECT * FROM Attendance WHERE student_id = ? AND subject_code = ? AND attendance_date = ? AND semester = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmtStudents = conn.prepareStatement(sqlStudents);
             PreparedStatement pstmtAttendance = conn.prepareStatement(sqlAttendance)) {
            
            pstmtStudents.setInt(1, classId);
            pstmtStudents.setString(2, academicYear);
            
            try (ResultSet rsStudents = pstmtStudents.executeQuery()) {
                while (rsStudents.next()) {
                    int studentId = rsStudents.getInt("user_id");
                    
                    // Check if this student already has attendance record
                    pstmtAttendance.setInt(1, studentId);
                    pstmtAttendance.setString(2, subjectCode);
                    pstmtAttendance.setDate(3, date);
                    pstmtAttendance.setString(4, semester);
                    
                    try (ResultSet rsAttendance = pstmtAttendance.executeQuery()) {
                        if (rsAttendance.next()) {
                            // Existing attendance record
                            Attendance attendance = new Attendance();
                            attendance.setAttendanceId(rsAttendance.getInt("attendance_id"));
                            attendance.setAttendanceDate(rsAttendance.getDate("attendance_date"));
                            attendance.setSubjectCode(rsAttendance.getString("subject_code"));
                            attendance.setStudentId(rsAttendance.getInt("student_id"));
                            attendance.setSemester(rsAttendance.getString("semester"));
                            attendance.setAcademicYear(rsAttendance.getString("academic_year"));
                            attendance.setStatus(rsAttendance.getString("status"));
                            
                            attendanceMap.put(studentId, attendance);
                        } else {
                            // No attendance record yet
                            attendanceMap.put(studentId, null);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return attendanceMap;
    }

    /**
     * Get attendance summary for a student by subject
     * @param studentId The student ID
     * @param subjectCode The subject code
     * @param semester The semester
     * @param academicYear The academic year
     * @return Map with attendance counts (present, absent, leave)
     */
    public Map<String, Integer> getAttendanceSummary(int studentId, String subjectCode, String semester, String academicYear) {
        Map<String, Integer> summary = new HashMap<>();
        summary.put("Present", 0);
        summary.put("Absent", 0);
        summary.put("Leave", 0);
        summary.put("Total", 0);
        
        String sql = "SELECT status, COUNT(*) as count FROM Attendance " +
                    "WHERE student_id = ? AND subject_code = ? AND semester = ? AND academic_year = ? " +
                    "GROUP BY status";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setString(2, subjectCode);
            pstmt.setString(3, semester);
            pstmt.setString(4, academicYear);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    int count = rs.getInt("count");
                    
                    summary.put(status, count);
                    summary.put("Total", summary.get("Total") + count);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return summary;
    }

    /**
     * Get overall attendance percentage for a student across all subjects
     * @param studentId The student ID
     * @param semester The semester
     * @param academicYear The academic year
     * @return Attendance percentage
     */
    public double getOverallAttendancePercentage(int studentId, String semester, String academicYear) {
        String sql = "SELECT " +
                    "SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) as present_count, " +
                    "COUNT(*) as total_count " +
                    "FROM Attendance " +
                    "WHERE student_id = ? AND semester = ? AND academic_year = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setString(2, semester);
            pstmt.setString(3, academicYear);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int presentCount = rs.getInt("present_count");
                    int totalCount = rs.getInt("total_count");
                    
                    if (totalCount > 0) {
                        return (double) presentCount / totalCount * 100;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0.0;
    }
}
