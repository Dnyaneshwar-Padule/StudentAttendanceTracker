package com.attendance.dao.impl;

import com.attendance.dao.AttendanceDAO;
import com.attendance.models.Attendance;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the AttendanceDAO interface
 */
public class AttendanceDAOImpl implements AttendanceDAO {
    
    private static final Logger LOGGER = Logger.getLogger(AttendanceDAOImpl.class.getName());
    
    // SQL queries for the new schema
    private static final String SQL_CREATE_ATTENDANCE = 
            "INSERT INTO Attendance (student_id, subject_code, attendance_date, status, semester, academic_year) " +
            "VALUES (?, ?, ?, ?, ?, ?) RETURNING attendance_id";
    
    private static final String SQL_GET_ATTENDANCE_BY_ID = 
            "SELECT * FROM Attendance WHERE attendance_id = ?";
    
    private static final String SQL_GET_ATTENDANCE_BY_STUDENT_ID = 
            "SELECT * FROM Attendance WHERE student_id = ? ORDER BY attendance_date DESC";
    
    private static final String SQL_GET_ATTENDANCE_BY_STUDENT_ID_AND_DATE_RANGE = 
            "SELECT * FROM Attendance WHERE student_id = ? AND attendance_date BETWEEN ? AND ? ORDER BY attendance_date";
    
    private static final String SQL_GET_ATTENDANCE_BY_SEMESTER = 
            "SELECT * FROM Attendance WHERE semester = ? ORDER BY attendance_date DESC, student_id";
    
    private static final String SQL_GET_ATTENDANCE_BY_SEMESTER_AND_DATE = 
            "SELECT * FROM Attendance WHERE semester = ? AND attendance_date = ? ORDER BY student_id";
    
    private static final String SQL_GET_ATTENDANCE_BY_SUBJECT_CODE = 
            "SELECT * FROM Attendance WHERE subject_code = ? ORDER BY attendance_date DESC, student_id";
            
    // Keeping class_id queries for backward compatibility - we'll map semester to classId
    private static final String SQL_GET_ATTENDANCE_BY_CLASS_ID = 
            "SELECT * FROM Attendance WHERE semester = ? ORDER BY attendance_date DESC, student_id";
            
    private static final String SQL_GET_ATTENDANCE_BY_CLASS_ID_AND_DATE = 
            "SELECT * FROM Attendance WHERE semester = ? AND attendance_date = ? ORDER BY student_id";
            
    private static final String SQL_GET_ATTENDANCE_BY_SUBJECT_ID = 
            "SELECT * FROM Attendance WHERE subject_code = ? ORDER BY attendance_date DESC, student_id";
            
    private static final String SQL_GET_ATTENDANCE_BY_CLASS_ID_AND_DATE_RANGE = 
            "SELECT * FROM Attendance WHERE semester = ? AND attendance_date BETWEEN ? AND ? ORDER BY attendance_date, student_id";
            
    private static final String SQL_GET_ATTENDANCE_SUMMARY_BY_CLASS_ID = 
            "SELECT status, COUNT(*) as count FROM Attendance WHERE semester = ? GROUP BY status";
            
    private static final String SQL_GET_ATTENDANCE_SUMMARY_BY_CLASS_ID_AND_DATE_RANGE = 
            "SELECT status, COUNT(*) as count FROM Attendance WHERE semester = ? AND attendance_date BETWEEN ? AND ? GROUP BY status";
    
    private static final String SQL_GET_ATTENDANCE_BY_DATE = 
            "SELECT * FROM Attendance WHERE attendance_date = ? ORDER BY semester, student_id";
    
    private static final String SQL_UPDATE_ATTENDANCE = 
            "UPDATE Attendance SET student_id = ?, subject_code = ?, attendance_date = ?, " +
            "status = ?, semester = ?, academic_year = ? WHERE attendance_id = ?";
    
    private static final String SQL_DELETE_ATTENDANCE = 
            "DELETE FROM Attendance WHERE attendance_id = ?";
    
    private static final String SQL_GET_ATTENDANCE_BY_SEMESTER_AND_DATE_RANGE = 
            "SELECT * FROM Attendance WHERE semester = ? AND attendance_date BETWEEN ? AND ? ORDER BY attendance_date, student_id";
    
    private static final String SQL_GET_ATTENDANCE_SUMMARY_BY_STUDENT_ID = 
            "SELECT status, COUNT(*) as count FROM Attendance WHERE student_id = ? GROUP BY status";
    
    private static final String SQL_GET_ATTENDANCE_SUMMARY_BY_STUDENT_ID_AND_DATE_RANGE = 
            "SELECT status, COUNT(*) as count FROM Attendance WHERE student_id = ? AND attendance_date BETWEEN ? AND ? GROUP BY status";
    
    private static final String SQL_GET_ATTENDANCE_SUMMARY_BY_SEMESTER = 
            "SELECT status, COUNT(*) as count FROM Attendance WHERE semester = ? GROUP BY status";
    
    private static final String SQL_GET_ATTENDANCE_SUMMARY_BY_SEMESTER_AND_DATE_RANGE = 
            "SELECT status, COUNT(*) as count FROM Attendance WHERE semester = ? AND attendance_date BETWEEN ? AND ? GROUP BY status";
    
    private static final String SQL_CHECK_ATTENDANCE_EXISTS = 
            "SELECT COUNT(*) FROM Attendance WHERE student_id = ? AND attendance_date = ?";
    
    @Override
    public int createAttendance(Attendance attendance) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int generatedId = -1;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_CREATE_ATTENDANCE, Statement.RETURN_GENERATED_KEYS);
            
            // Map to new schema fields
            pstmt.setInt(1, attendance.getStudentId());
            pstmt.setString(2, attendance.getSubjectCode());
            pstmt.setDate(3, Date.valueOf(attendance.getDate()));
            pstmt.setString(4, attendance.getStatus());
            pstmt.setString(5, String.valueOf(attendance.getSemester()));
            pstmt.setString(6, attendance.getAcademicYear());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                    attendance.setId(generatedId); // Update the attendance object with the new ID
                }
            }
            
            LOGGER.log(Level.INFO, "Created attendance record with ID: {0}", generatedId);
            return generatedId;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating attendance record", e);
            return -1;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public Attendance getAttendanceById(int attendanceId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_ATTENDANCE_BY_ID);
            pstmt.setInt(1, attendanceId);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractAttendanceFromResultSet(rs);
            }
            
            return null;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance by ID", e);
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<Attendance> getAttendanceByStudentId(int studentId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Attendance> attendanceList = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_ATTENDANCE_BY_STUDENT_ID);
            pstmt.setInt(1, studentId);
            
            rs = pstmt.executeQuery();
            while (rs.next()) {
                attendanceList.add(extractAttendanceFromResultSet(rs));
            }
            
            return attendanceList;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance by student ID", e);
            return attendanceList;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<Attendance> getAttendanceByStudentIdAndDateRange(int studentId, LocalDate startDate, LocalDate endDate) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Attendance> attendanceList = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_ATTENDANCE_BY_STUDENT_ID_AND_DATE_RANGE);
            pstmt.setInt(1, studentId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));
            
            rs = pstmt.executeQuery();
            while (rs.next()) {
                attendanceList.add(extractAttendanceFromResultSet(rs));
            }
            
            return attendanceList;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance by student ID and date range", e);
            return attendanceList;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<Attendance> getAttendanceByClassId(int classId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Attendance> attendanceList = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_ATTENDANCE_BY_CLASS_ID);
            pstmt.setInt(1, classId);
            
            rs = pstmt.executeQuery();
            while (rs.next()) {
                attendanceList.add(extractAttendanceFromResultSet(rs));
            }
            
            return attendanceList;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance by class ID", e);
            return attendanceList;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<Attendance> getAttendanceByClassIdAndDate(int classId, LocalDate date) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Attendance> attendanceList = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_ATTENDANCE_BY_CLASS_ID_AND_DATE);
            pstmt.setInt(1, classId);
            pstmt.setDate(2, Date.valueOf(date));
            
            rs = pstmt.executeQuery();
            while (rs.next()) {
                attendanceList.add(extractAttendanceFromResultSet(rs));
            }
            
            return attendanceList;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance by class ID and date", e);
            return attendanceList;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<Attendance> getAttendanceBySubjectId(int subjectId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Attendance> attendanceList = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_ATTENDANCE_BY_SUBJECT_ID);
            pstmt.setInt(1, subjectId);
            
            rs = pstmt.executeQuery();
            while (rs.next()) {
                attendanceList.add(extractAttendanceFromResultSet(rs));
            }
            
            return attendanceList;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance by subject ID", e);
            return attendanceList;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<Attendance> getAttendanceByDate(LocalDate date) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Attendance> attendanceList = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_ATTENDANCE_BY_DATE);
            pstmt.setDate(1, Date.valueOf(date));
            
            rs = pstmt.executeQuery();
            while (rs.next()) {
                attendanceList.add(extractAttendanceFromResultSet(rs));
            }
            
            return attendanceList;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance by date", e);
            return attendanceList;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public boolean updateAttendance(Attendance attendance) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_UPDATE_ATTENDANCE);
            
            // Map to new schema fields
            pstmt.setInt(1, attendance.getStudentId());
            pstmt.setString(2, attendance.getSubjectCode());
            pstmt.setDate(3, Date.valueOf(attendance.getDate()));
            pstmt.setString(4, attendance.getStatus());
            pstmt.setString(5, String.valueOf(attendance.getSemester()));
            pstmt.setString(6, attendance.getAcademicYear());
            pstmt.setInt(7, attendance.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating attendance", e);
            return false;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    @Override
    public boolean deleteAttendance(int attendanceId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_DELETE_ATTENDANCE);
            // In the new schema, the primary key is attendance_id
            pstmt.setInt(1, attendanceId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting attendance with ID: " + attendanceId, e);
            return false;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    @Override
    public List<Attendance> getAttendanceByClassIdAndDateRange(int classId, LocalDate startDate, LocalDate endDate) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Attendance> attendanceList = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_ATTENDANCE_BY_CLASS_ID_AND_DATE_RANGE);
            pstmt.setInt(1, classId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));
            
            rs = pstmt.executeQuery();
            while (rs.next()) {
                attendanceList.add(extractAttendanceFromResultSet(rs));
            }
            
            return attendanceList;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance by class ID and date range", e);
            return attendanceList;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public Map<String, Integer> getAttendanceSummaryByStudentId(int studentId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Map<String, Integer> summary = new HashMap<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_ATTENDANCE_SUMMARY_BY_STUDENT_ID);
            pstmt.setInt(1, studentId);
            
            rs = pstmt.executeQuery();
            int total = 0;
            
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                summary.put(status, count);
                total += count;
            }
            
            summary.put("total", total);
            return summary;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance summary by student ID", e);
            return summary;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public Map<String, Integer> getAttendanceSummaryByStudentIdAndDateRange(int studentId, LocalDate startDate, LocalDate endDate) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Map<String, Integer> summary = new HashMap<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_ATTENDANCE_SUMMARY_BY_STUDENT_ID_AND_DATE_RANGE);
            pstmt.setInt(1, studentId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));
            
            rs = pstmt.executeQuery();
            int total = 0;
            
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                summary.put(status, count);
                total += count;
            }
            
            summary.put("total", total);
            return summary;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance summary by student ID and date range", e);
            return summary;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public Map<String, Integer> getAttendanceSummaryByClassId(int classId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Map<String, Integer> summary = new HashMap<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_ATTENDANCE_SUMMARY_BY_CLASS_ID);
            pstmt.setInt(1, classId);
            
            rs = pstmt.executeQuery();
            int total = 0;
            
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                summary.put(status, count);
                total += count;
            }
            
            summary.put("total", total);
            return summary;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance summary by class ID", e);
            return summary;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public Map<String, Integer> getAttendanceSummaryByClassIdAndDateRange(int classId, LocalDate startDate, LocalDate endDate) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Map<String, Integer> summary = new HashMap<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_ATTENDANCE_SUMMARY_BY_CLASS_ID_AND_DATE_RANGE);
            pstmt.setInt(1, classId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));
            
            rs = pstmt.executeQuery();
            int total = 0;
            
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                summary.put(status, count);
                total += count;
            }
            
            summary.put("total", total);
            return summary;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance summary by class ID and date range", e);
            return summary;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public boolean attendanceExists(int studentId, LocalDate date) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_CHECK_ATTENDANCE_EXISTS);
            pstmt.setInt(1, studentId);
            // In the new schema, the date column is named attendance_date
            pstmt.setDate(2, Date.valueOf(date));
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
            return false;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if attendance exists for student ID: " + studentId + " and date: " + date, e);
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Extract an Attendance object from a ResultSet
     * 
     * @param rs The ResultSet containing attendance data
     * @return The Attendance object
     * @throws SQLException if an error occurs
     */
    private Attendance extractAttendanceFromResultSet(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        
        // Map fields from the new schema
        attendance.setId(rs.getInt("attendance_id"));
        attendance.setStudentId(rs.getInt("student_id"));
        attendance.setSubjectCode(rs.getString("subject_code"));
        
        // Setting the semester
        String semesterStr = rs.getString("semester");
        try {
            int semester = Integer.parseInt(semesterStr);
            attendance.setSemester(semester);
        } catch (NumberFormatException e) {
            // Handle case where semester might be stored as non-integer value
            attendance.setSemester(1); // Default to semester 1 if parsing fails
            LOGGER.warning("Could not parse semester value: " + semesterStr);
        }
        
        // Set academic year
        attendance.setAcademicYear(rs.getString("academic_year"));
        
        // Set the attendance date
        Date date = rs.getDate("attendance_date");
        if (date != null) {
            attendance.setDate(date.toLocalDate());
        }
        
        // Set the status
        attendance.setStatus(rs.getString("status"));
        
        // Set default values for fields not in the new schema but required by the model
        attendance.setClassId(0); // Default to 0 since class_id is not in the new schema
        attendance.setSubjectId(0); // Default to 0 since subject_id is not in the new schema
        attendance.setRemarks(""); // Empty remarks since it's not in the new schema
        attendance.setMarkedById(0); // Default to 0 since marked_by_id is not in the new schema
        attendance.setCreatedAt(LocalDateTime.now()); // Set to current time
        attendance.setUpdatedAt(LocalDateTime.now()); // Set to current time
        
        return attendance;
    }
    
    /**
     * Close database resources
     * 
     * @param conn The database connection
     * @param stmt The prepared statement
     * @param rs The result set
     */
    private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing ResultSet", e);
            }
        }
        
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing Statement", e);
            }
        }
        
        DatabaseConnection.closeConnection(conn);
    }
}