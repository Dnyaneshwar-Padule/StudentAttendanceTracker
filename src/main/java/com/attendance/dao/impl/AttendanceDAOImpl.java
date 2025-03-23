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
    
    // SQL queries
    private static final String SQL_CREATE_ATTENDANCE = 
            "INSERT INTO attendance (student_id, class_id, subject_id, date, status, remarks, " +
            "marked_by_id, created_at, updated_at, attendance_session, leave_application_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
    
    private static final String SQL_GET_ATTENDANCE_BY_ID = 
            "SELECT * FROM attendance WHERE id = ?";
    
    private static final String SQL_GET_ATTENDANCE_BY_STUDENT_ID = 
            "SELECT * FROM attendance WHERE student_id = ? ORDER BY date DESC";
    
    private static final String SQL_GET_ATTENDANCE_BY_STUDENT_ID_AND_DATE_RANGE = 
            "SELECT * FROM attendance WHERE student_id = ? AND date BETWEEN ? AND ? ORDER BY date";
    
    private static final String SQL_GET_ATTENDANCE_BY_CLASS_ID = 
            "SELECT * FROM attendance WHERE class_id = ? ORDER BY date DESC, student_id";
    
    private static final String SQL_GET_ATTENDANCE_BY_CLASS_ID_AND_DATE = 
            "SELECT * FROM attendance WHERE class_id = ? AND date = ? ORDER BY student_id";
    
    private static final String SQL_GET_ATTENDANCE_BY_SUBJECT_ID = 
            "SELECT * FROM attendance WHERE subject_id = ? ORDER BY date DESC, student_id";
    
    private static final String SQL_GET_ATTENDANCE_BY_DATE = 
            "SELECT * FROM attendance WHERE date = ? ORDER BY class_id, student_id";
    
    private static final String SQL_UPDATE_ATTENDANCE = 
            "UPDATE attendance SET student_id = ?, class_id = ?, subject_id = ?, date = ?, " +
            "status = ?, remarks = ?, marked_by_id = ?, updated_at = ?, " +
            "attendance_session = ?, leave_application_id = ? WHERE id = ?";
    
    private static final String SQL_DELETE_ATTENDANCE = 
            "DELETE FROM attendance WHERE id = ?";
    
    private static final String SQL_GET_ATTENDANCE_BY_CLASS_ID_AND_DATE_RANGE = 
            "SELECT * FROM attendance WHERE class_id = ? AND date BETWEEN ? AND ? ORDER BY date, student_id";
    
    private static final String SQL_GET_ATTENDANCE_SUMMARY_BY_STUDENT_ID = 
            "SELECT status, COUNT(*) as count FROM attendance WHERE student_id = ? GROUP BY status";
    
    private static final String SQL_GET_ATTENDANCE_SUMMARY_BY_STUDENT_ID_AND_DATE_RANGE = 
            "SELECT status, COUNT(*) as count FROM attendance WHERE student_id = ? AND date BETWEEN ? AND ? GROUP BY status";
    
    private static final String SQL_GET_ATTENDANCE_SUMMARY_BY_CLASS_ID = 
            "SELECT status, COUNT(*) as count FROM attendance WHERE class_id = ? GROUP BY status";
    
    private static final String SQL_GET_ATTENDANCE_SUMMARY_BY_CLASS_ID_AND_DATE_RANGE = 
            "SELECT status, COUNT(*) as count FROM attendance WHERE class_id = ? AND date BETWEEN ? AND ? GROUP BY status";
    
    private static final String SQL_CHECK_ATTENDANCE_EXISTS = 
            "SELECT COUNT(*) FROM attendance WHERE student_id = ? AND date = ?";
    
    @Override
    public int createAttendance(Attendance attendance) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int generatedId = -1;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_CREATE_ATTENDANCE, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, attendance.getStudentId());
            pstmt.setInt(2, attendance.getClassId());
            pstmt.setInt(3, attendance.getSubjectId());
            pstmt.setDate(4, Date.valueOf(attendance.getDate()));
            pstmt.setString(5, attendance.getStatus());
            pstmt.setString(6, attendance.getRemarks());
            pstmt.setInt(7, attendance.getMarkedById());
            pstmt.setTimestamp(8, Timestamp.valueOf(attendance.getCreatedAt()));
            pstmt.setTimestamp(9, Timestamp.valueOf(attendance.getUpdatedAt()));
            pstmt.setString(10, attendance.getAttendanceSession());
            
            if (attendance.getLeaveApplicationId() != null) {
                pstmt.setInt(11, attendance.getLeaveApplicationId());
            } else {
                pstmt.setNull(11, java.sql.Types.INTEGER);
            }
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                rs = pstmt.getResultSet();
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
            
            pstmt.setInt(1, attendance.getStudentId());
            pstmt.setInt(2, attendance.getClassId());
            pstmt.setInt(3, attendance.getSubjectId());
            pstmt.setDate(4, Date.valueOf(attendance.getDate()));
            pstmt.setString(5, attendance.getStatus());
            pstmt.setString(6, attendance.getRemarks());
            pstmt.setInt(7, attendance.getMarkedById());
            pstmt.setTimestamp(8, Timestamp.valueOf(attendance.getUpdatedAt()));
            pstmt.setString(9, attendance.getAttendanceSession());
            
            if (attendance.getLeaveApplicationId() != null) {
                pstmt.setInt(10, attendance.getLeaveApplicationId());
            } else {
                pstmt.setNull(10, java.sql.Types.INTEGER);
            }
            
            pstmt.setInt(11, attendance.getId());
            
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
            pstmt.setInt(1, attendanceId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting attendance", e);
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
            pstmt.setDate(2, Date.valueOf(date));
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
            return false;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if attendance exists", e);
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
        
        attendance.setId(rs.getInt("id"));
        attendance.setStudentId(rs.getInt("student_id"));
        attendance.setClassId(rs.getInt("class_id"));
        attendance.setSubjectId(rs.getInt("subject_id"));
        
        Date date = rs.getDate("date");
        if (date != null) {
            attendance.setDate(date.toLocalDate());
        }
        
        attendance.setStatus(rs.getString("status"));
        attendance.setRemarks(rs.getString("remarks"));
        attendance.setMarkedById(rs.getInt("marked_by_id"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            attendance.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            attendance.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        attendance.setAttendanceSession(rs.getString("attendance_session"));
        
        int leaveApplicationId = rs.getInt("leave_application_id");
        if (!rs.wasNull()) {
            attendance.setLeaveApplicationId(leaveApplicationId);
        }
        
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