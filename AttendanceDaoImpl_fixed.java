package com.attendance.dao.impl;

import com.attendance.dao.AttendanceDao;
import com.attendance.dao.LeaveApplicationDao;
import com.attendance.models.Attendance;
import com.attendance.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AttendanceDaoImpl implements AttendanceDao {
    private static final Logger LOGGER = Logger.getLogger(AttendanceDaoImpl.class.getName());

    private LeaveApplicationDao leaveApplicationDao;

    public AttendanceDaoImpl() {
        leaveApplicationDao = new LeaveApplicationDaoImpl();
    }

    @Override
    public Map<String, Double> getMonthlyAttendanceTrend(String academicYear) throws SQLException {
        Map<String, Double> monthlyTrend = new HashMap<>();
        String sql = "SELECT TO_CHAR(attendance_date, 'Month') AS month, " +
                     "COUNT(*) AS total, " +
                     "SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) AS present, " +
                     "SUM(CASE WHEN status = 'On Leave' THEN 1 ELSE 0 END) AS on_leave " +
                     "FROM Attendance " +
                     "WHERE academic_year = ? " +
                     "GROUP BY TO_CHAR(attendance_date, 'Month') " +
                     "ORDER BY MIN(attendance_date)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, academicYear);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String month = rs.getString("month").trim();
                    int total = rs.getInt("total");
                    int present = rs.getInt("present");
                    int onLeave = rs.getInt("on_leave");
                    
                    // Don't count "On Leave" days in the total when calculating attendance percentage
                    int effectiveTotal = total - onLeave;
                    
                    if (effectiveTotal > 0) {
                        monthlyTrend.put(month, (double) present / effectiveTotal * 100);
                    } else if (total > 0 && effectiveTotal == 0) {
                        // If all days are marked as "On Leave"
                        monthlyTrend.put(month, 100.0);
                    } else {
                        monthlyTrend.put(month, 0.0);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting monthly attendance trend for academic year: " + academicYear, e);
            throw e;
        }
        
        return monthlyTrend;
    }
    
    @Override
    public Map<String, Double> getSemesterAttendanceTrend(String academicYear) throws SQLException {
        Map<String, Double> semesterTrend = new HashMap<>();
        String sql = "SELECT semester, " +
                     "COUNT(*) AS total, " +
                     "SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) AS present, " +
                     "SUM(CASE WHEN status = 'On Leave' THEN 1 ELSE 0 END) AS on_leave " +
                     "FROM Attendance " +
                     "WHERE academic_year = ? " +
                     "GROUP BY semester";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, academicYear);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String semester = rs.getString("semester");
                    int total = rs.getInt("total");
                    int present = rs.getInt("present");
                    int onLeave = rs.getInt("on_leave");
                    
                    // Don't count "On Leave" days in the total when calculating attendance percentage
                    int effectiveTotal = total - onLeave;
                    
                    if (effectiveTotal > 0) {
                        semesterTrend.put(semester, (double) present / effectiveTotal * 100);
                    } else if (total > 0 && effectiveTotal == 0) {
                        // If all days are marked as "On Leave"
                        semesterTrend.put(semester, 100.0);
                    } else {
                        semesterTrend.put(semester, 0.0);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting semester attendance trend for academic year: " + academicYear, e);
            throw e;
        }
        
        return semesterTrend;
    }
    
    /**
     * Constructor with LeaveApplicationDao dependency
     * @param leaveApplicationDao The LeaveApplicationDao implementation
     */
    public AttendanceDaoImpl(LeaveApplicationDao leaveApplicationDao) {
        this.leaveApplicationDao = leaveApplicationDao;
    }

    @Override
    public Attendance findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM Attendance WHERE attendance_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttendance(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding attendance by ID: " + id, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public List<Attendance> findAll() throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM Attendance";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                attendanceList.add(mapResultSetToAttendance(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all attendance records", e);
            throw e;
        }
        
        return attendanceList;
    }

    @Override
    public Attendance save(Attendance attendance) throws SQLException {
        String sql = "INSERT INTO Attendance (attendance_date, subject_code, student_id, semester, academic_year, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING attendance_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(attendance.getAttendanceDate()));
            stmt.setString(2, attendance.getSubjectCode());
            stmt.setInt(3, attendance.getStudentId());
            stmt.setInt(4, attendance.getSemester());
            stmt.setString(5, attendance.getAcademicYear());
            stmt.setString(6, attendance.getStatus());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    attendance.setAttendanceId(rs.getInt(1));
                    return attendance;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving attendance: " + attendance, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public Attendance update(Attendance attendance) throws SQLException {
        String sql = "UPDATE Attendance SET attendance_date = ?, subject_code = ?, student_id = ?, " +
                     "semester = ?, academic_year = ?, status = ? WHERE attendance_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(attendance.getAttendanceDate()));
            stmt.setString(2, attendance.getSubjectCode());
            stmt.setInt(3, attendance.getStudentId());
            stmt.setInt(4, attendance.getSemester());
            stmt.setString(5, attendance.getAcademicYear());
            stmt.setString(6, attendance.getStatus());
            stmt.setInt(7, attendance.getAttendanceId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return attendance;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating attendance: " + attendance, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM Attendance WHERE attendance_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting attendance with ID: " + id, e);
            throw e;
        }
    }

    @Override
    public List<Attendance> findByStudentSubjectAndSemester(int studentId, String subjectCode, String semester) throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE student_id = ? AND subject_code = ? AND semester = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setString(2, subjectCode);
            stmt.setString(3, semester);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attendanceList.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding attendance by student, subject, and semester", e);
            throw e;
        }

        return attendanceList;
    }

    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setAttendanceId(rs.getInt("attendance_id"));
        attendance.setStudentId(rs.getInt("student_id"));
        attendance.setSubjectCode(rs.getString("subject_code"));
        attendance.setSemester(rs.getInt("semester"));
        attendance.setAcademicYear(rs.getString("academic_year"));
        attendance.setAttendanceDate(rs.getDate("attendance_date"));
        attendance.setStatus(rs.getString("status"));
        return attendance;
    }
}