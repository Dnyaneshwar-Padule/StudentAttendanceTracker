package com.attendance.dao.impl;

import com.attendance.dao.AttendanceDAO;
import com.attendance.models.Attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of AttendanceDAO interface
 */
public class AttendanceDAOImpl implements AttendanceDAO {
    private static final Logger LOGGER = Logger.getLogger(AttendanceDAOImpl.class.getName());
    
    /**
     * Create a new attendance record
     * 
     * @param attendance the attendance record to create
     * @return the created attendance record with generated ID
     * @throws Exception if an error occurs
     */
    @Override
    public Attendance create(Attendance attendance) throws Exception {
        String sql = "INSERT INTO Attendance (student_id, subject_code, attendance_date, status, " +
                    "semester, academic_year, marked_by, remarks) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING attendance_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, attendance.getStudentId());
            stmt.setString(2, attendance.getSubjectCode());
            stmt.setDate(3, attendance.getAttendanceDate());
            stmt.setString(4, attendance.getStatus());
            stmt.setString(5, attendance.getSemester());
            stmt.setString(6, attendance.getAcademicYear());
            stmt.setString(7, attendance.getMarkedBy());
            stmt.setString(8, attendance.getRemarks());
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                attendance.setAttendanceId(rs.getInt("attendance_id"));
                return attendance;
            } else {
                throw new SQLException("Failed to create attendance record, no ID generated");
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating attendance record", e);
            throw e;
        }
    }
    
    /**
     * Get an attendance record by ID
     * 
     * @param attendanceId the attendance ID
     * @return an Optional containing the attendance record if found
     * @throws Exception if an error occurs
     */
    @Override
    public Optional<Attendance> getById(int attendanceId) throws Exception {
        String sql = "SELECT * FROM Attendance WHERE attendance_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, attendanceId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Attendance attendance = mapResultSetToAttendance(rs);
                return Optional.of(attendance);
            } else {
                return Optional.empty();
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance by ID", e);
            throw e;
        }
    }
    
    /**
     * Update an existing attendance record
     * 
     * @param attendance the attendance record to update
     * @return the updated attendance record
     * @throws Exception if an error occurs
     */
    @Override
    public Attendance update(Attendance attendance) throws Exception {
        String sql = "UPDATE Attendance SET student_id = ?, subject_code = ?, attendance_date = ?, " +
                   "status = ?, semester = ?, academic_year = ?, marked_by = ?, remarks = ?, " +
                   "updated_at = CURRENT_TIMESTAMP " +
                   "WHERE attendance_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, attendance.getStudentId());
            stmt.setString(2, attendance.getSubjectCode());
            stmt.setDate(3, attendance.getAttendanceDate());
            stmt.setString(4, attendance.getStatus());
            stmt.setString(5, attendance.getSemester());
            stmt.setString(6, attendance.getAcademicYear());
            stmt.setString(7, attendance.getMarkedBy());
            stmt.setString(8, attendance.getRemarks());
            stmt.setInt(9, attendance.getAttendanceId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                return attendance;
            } else {
                throw new SQLException("Failed to update attendance record, no rows affected");
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating attendance record", e);
            throw e;
        }
    }
    
    /**
     * Delete an attendance record
     * 
     * @param attendanceId the ID of the attendance record to delete
     * @return true if deletion was successful
     * @throws Exception if an error occurs
     */
    @Override
    public boolean delete(int attendanceId) throws Exception {
        String sql = "DELETE FROM Attendance WHERE attendance_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, attendanceId);
            int rowsAffected = stmt.executeUpdate();
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting attendance record", e);
            throw e;
        }
    }
    
    /**
     * Get all attendance records
     * 
     * @return list of all attendance records
     * @throws Exception if an error occurs
     */
    @Override
    public List<Attendance> getAll() throws Exception {
        String sql = "SELECT * FROM Attendance ORDER BY attendance_date DESC";
        List<Attendance> attendanceList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Attendance attendance = mapResultSetToAttendance(rs);
                attendanceList.add(attendance);
            }
            
            return attendanceList;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all attendance records", e);
            throw e;
        }
    }
    
    /**
     * Get attendance records for a student
     * 
     * @param studentId the student ID
     * @return list of attendance records for the student
     * @throws Exception if an error occurs
     */
    @Override
    public List<Attendance> getByStudent(int studentId) throws Exception {
        String sql = "SELECT a.*, u.full_name as student_name, s.subject_name " +
                   "FROM Attendance a " +
                   "JOIN Users u ON a.student_id = u.user_id " +
                   "JOIN Subjects s ON a.subject_code = s.subject_code " +
                   "WHERE a.student_id = ? " +
                   "ORDER BY a.attendance_date DESC";
        
        List<Attendance> attendanceList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Attendance attendance = mapResultSetToAttendance(rs);
                attendance.setStudentName(rs.getString("student_name"));
                attendance.setSubjectName(rs.getString("subject_name"));
                attendanceList.add(attendance);
            }
            
            return attendanceList;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance records by student", e);
            throw e;
        }
    }
    
    /**
     * Get attendance records for a subject
     * 
     * @param subjectCode the subject code
     * @return list of attendance records for the subject
     * @throws Exception if an error occurs
     */
    @Override
    public List<Attendance> getBySubject(String subjectCode) throws Exception {
        String sql = "SELECT a.*, u.full_name as student_name, s.subject_name " +
                   "FROM Attendance a " +
                   "JOIN Users u ON a.student_id = u.user_id " +
                   "JOIN Subjects s ON a.subject_code = s.subject_code " +
                   "WHERE a.subject_code = ? " +
                   "ORDER BY a.attendance_date DESC, u.full_name";
        
        List<Attendance> attendanceList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, subjectCode);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Attendance attendance = mapResultSetToAttendance(rs);
                attendance.setStudentName(rs.getString("student_name"));
                attendance.setSubjectName(rs.getString("subject_name"));
                attendanceList.add(attendance);
            }
            
            return attendanceList;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance records by subject", e);
            throw e;
        }
    }
    
    /**
     * Get attendance records for a date
     * 
     * @param date the date
     * @return list of attendance records for the date
     * @throws Exception if an error occurs
     */
    @Override
    public List<Attendance> getByDate(Date date) throws Exception {
        String sql = "SELECT a.*, u.full_name as student_name, s.subject_name " +
                   "FROM Attendance a " +
                   "JOIN Users u ON a.student_id = u.user_id " +
                   "JOIN Subjects s ON a.subject_code = s.subject_code " +
                   "WHERE a.attendance_date = ? " +
                   "ORDER BY s.subject_name, u.full_name";
        
        List<Attendance> attendanceList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, date);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Attendance attendance = mapResultSetToAttendance(rs);
                attendance.setStudentName(rs.getString("student_name"));
                attendance.setSubjectName(rs.getString("subject_name"));
                attendanceList.add(attendance);
            }
            
            return attendanceList;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance records by date", e);
            throw e;
        }
    }
    
    /**
     * Get attendance records for a student and subject
     * 
     * @param studentId the student ID
     * @param subjectCode the subject code
     * @return list of attendance records for the student and subject
     * @throws Exception if an error occurs
     */
    @Override
    public List<Attendance> getByStudentAndSubject(int studentId, String subjectCode) throws Exception {
        String sql = "SELECT a.*, u.full_name as student_name, s.subject_name " +
                   "FROM Attendance a " +
                   "JOIN Users u ON a.student_id = u.user_id " +
                   "JOIN Subjects s ON a.subject_code = s.subject_code " +
                   "WHERE a.student_id = ? AND a.subject_code = ? " +
                   "ORDER BY a.attendance_date DESC";
        
        List<Attendance> attendanceList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setString(2, subjectCode);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Attendance attendance = mapResultSetToAttendance(rs);
                attendance.setStudentName(rs.getString("student_name"));
                attendance.setSubjectName(rs.getString("subject_name"));
                attendanceList.add(attendance);
            }
            
            return attendanceList;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance records by student and subject", e);
            throw e;
        }
    }
    
    /**
     * Get attendance records for a student, subject, and semester
     * 
     * @param studentId the student ID
     * @param subjectCode the subject code
     * @param semester the semester
     * @return list of attendance records for the student, subject, and semester
     * @throws Exception if an error occurs
     */
    @Override
    public List<Attendance> getByStudentSubjectAndSemester(int studentId, String subjectCode, String semester) throws Exception {
        String sql = "SELECT a.*, u.full_name as student_name, s.subject_name " +
                   "FROM Attendance a " +
                   "JOIN Users u ON a.student_id = u.user_id " +
                   "JOIN Subjects s ON a.subject_code = s.subject_code " +
                   "WHERE a.student_id = ? AND a.subject_code = ? AND a.semester = ? " +
                   "ORDER BY a.attendance_date DESC";
        
        List<Attendance> attendanceList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setString(2, subjectCode);
            stmt.setString(3, semester);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Attendance attendance = mapResultSetToAttendance(rs);
                attendance.setStudentName(rs.getString("student_name"));
                attendance.setSubjectName(rs.getString("subject_name"));
                attendanceList.add(attendance);
            }
            
            return attendanceList;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance records by student, subject, and semester", e);
            throw e;
        }
    }
    
    /**
     * Get attendance records for a student in a semester
     * 
     * @param studentId the student ID
     * @param semester the semester
     * @param academicYear the academic year
     * @return list of attendance records for the student in the semester
     * @throws Exception if an error occurs
     */
    @Override
    public List<Attendance> getByStudentSemesterAndYear(int studentId, String semester, String academicYear) throws Exception {
        String sql = "SELECT a.*, u.full_name as student_name, s.subject_name " +
                   "FROM Attendance a " +
                   "JOIN Users u ON a.student_id = u.user_id " +
                   "JOIN Subjects s ON a.subject_code = s.subject_code " +
                   "WHERE a.student_id = ? AND a.semester = ? AND a.academic_year = ? " +
                   "ORDER BY a.attendance_date DESC, s.subject_name";
        
        List<Attendance> attendanceList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setString(2, semester);
            stmt.setString(3, academicYear);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Attendance attendance = mapResultSetToAttendance(rs);
                attendance.setStudentName(rs.getString("student_name"));
                attendance.setSubjectName(rs.getString("subject_name"));
                attendanceList.add(attendance);
            }
            
            return attendanceList;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance records by student, semester, and year", e);
            throw e;
        }
    }
    
    /**
     * Get attendance records for a specific status
     * 
     * @param status the attendance status (Present, Absent, Leave)
     * @return list of attendance records with the specified status
     * @throws Exception if an error occurs
     */
    @Override
    public List<Attendance> getByStatus(String status) throws Exception {
        String sql = "SELECT a.*, u.full_name as student_name, s.subject_name " +
                   "FROM Attendance a " +
                   "JOIN Users u ON a.student_id = u.user_id " +
                   "JOIN Subjects s ON a.subject_code = s.subject_code " +
                   "WHERE a.status = ? " +
                   "ORDER BY a.attendance_date DESC, u.full_name";
        
        List<Attendance> attendanceList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Attendance attendance = mapResultSetToAttendance(rs);
                attendance.setStudentName(rs.getString("student_name"));
                attendance.setSubjectName(rs.getString("subject_name"));
                attendanceList.add(attendance);
            }
            
            return attendanceList;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance records by status", e);
            throw e;
        }
    }
    
    /**
     * Get attendance by student, subject, and date (to check if already marked)
     * 
     * @param studentId the student ID
     * @param subjectCode the subject code
     * @param date the date
     * @return an Optional containing the attendance record if found
     * @throws Exception if an error occurs
     */
    @Override
    public Optional<Attendance> getByStudentSubjectAndDate(int studentId, String subjectCode, Date date) throws Exception {
        String sql = "SELECT a.*, u.full_name as student_name, s.subject_name " +
                   "FROM Attendance a " +
                   "JOIN Users u ON a.student_id = u.user_id " +
                   "JOIN Subjects s ON a.subject_code = s.subject_code " +
                   "WHERE a.student_id = ? AND a.subject_code = ? AND a.attendance_date = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setString(2, subjectCode);
            stmt.setDate(3, date);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Attendance attendance = mapResultSetToAttendance(rs);
                attendance.setStudentName(rs.getString("student_name"));
                attendance.setSubjectName(rs.getString("subject_name"));
                return Optional.of(attendance);
            } else {
                return Optional.empty();
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance by student, subject, and date", e);
            throw e;
        }
    }
    
    /**
     * Get attendance percentage for a student in a subject
     * 
     * @param studentId the student ID
     * @param subjectCode the subject code
     * @param semester the semester
     * @param academicYear the academic year
     * @return the attendance percentage (0-100)
     * @throws Exception if an error occurs
     */
    @Override
    public double getAttendancePercentage(int studentId, String subjectCode, String semester, String academicYear) throws Exception {
        String sql = "SELECT " +
                   "COUNT(CASE WHEN status = 'Present' THEN 1 END) as present_count, " +
                   "COUNT(*) as total_count " +
                   "FROM Attendance " +
                   "WHERE student_id = ? AND subject_code = ? AND semester = ? AND academic_year = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setString(2, subjectCode);
            stmt.setString(3, semester);
            stmt.setString(4, academicYear);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int presentCount = rs.getInt("present_count");
                int totalCount = rs.getInt("total_count");
                
                if (totalCount == 0) {
                    return 0.0; // No attendance records yet
                }
                
                return (double) presentCount / totalCount * 100.0;
            } else {
                return 0.0; // No attendance records found
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calculating attendance percentage", e);
            throw e;
        }
    }
    
    /**
     * Mark attendance for multiple students
     * 
     * @param studentIds list of student IDs
     * @param subjectCode the subject code
     * @param date the date
     * @param status the attendance status
     * @param semester the semester
     * @param academicYear the academic year
     * @param markedBy who marked the attendance
     * @return number of records created
     * @throws Exception if an error occurs
     */
    @Override
    public int markAttendanceBulk(List<Integer> studentIds, String subjectCode, Date date, 
                               String status, String semester, String academicYear, String markedBy) throws Exception {
        String sql = "INSERT INTO Attendance (student_id, subject_code, attendance_date, status, " +
                    "semester, academic_year, marked_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (student_id, subject_code, attendance_date) " +
                    "DO UPDATE SET status = EXCLUDED.status";
        
        int successCount = 0;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            
            for (Integer studentId : studentIds) {
                stmt.setInt(1, studentId);
                stmt.setString(2, subjectCode);
                stmt.setDate(3, date);
                stmt.setString(4, status);
                stmt.setString(5, semester);
                stmt.setString(6, academicYear);
                stmt.setString(7, markedBy);
                
                stmt.addBatch();
            }
            
            int[] results = stmt.executeBatch();
            
            for (int result : results) {
                if (result > 0) {
                    successCount++;
                }
            }
            
            conn.commit();
            
            return successCount;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error marking attendance in bulk", e);
            throw e;
        }
    }
    
    /**
     * Map a ResultSet to an Attendance object
     * 
     * @param rs the ResultSet
     * @return the Attendance object
     * @throws SQLException if an error occurs
     */
    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setAttendanceId(rs.getInt("attendance_id"));
        attendance.setStudentId(rs.getInt("student_id"));
        attendance.setSubjectCode(rs.getString("subject_code"));
        attendance.setAttendanceDate(rs.getDate("attendance_date"));
        attendance.setStatus(rs.getString("status"));
        attendance.setSemester(rs.getString("semester"));
        attendance.setAcademicYear(rs.getString("academic_year"));
        attendance.setMarkedBy(rs.getString("marked_by"));
        attendance.setRemarks(rs.getString("remarks"));
        attendance.setCreatedAt(rs.getTimestamp("created_at"));
        attendance.setUpdatedAt(rs.getTimestamp("updated_at"));
        
        // Set extra fields if they exist in the result set
        try {
            attendance.setStudentName(rs.getString("student_name"));
        } catch (SQLException e) {
            // Field doesn't exist in the result set, ignore
        }
        
        try {
            attendance.setSubjectName(rs.getString("subject_name"));
        } catch (SQLException e) {
            // Field doesn't exist in the result set, ignore
        }
        
        return attendance;
    }
}