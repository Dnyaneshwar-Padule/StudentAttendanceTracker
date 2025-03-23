package com.attendance.dao.impl;

import com.attendance.dao.AttendanceDao;
import com.attendance.dao.LeaveApplicationDao;
import com.attendance.models.Attendance;
import com.attendance.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of AttendanceDao interface for database operations
 */
public class AttendanceDaoImpl implements AttendanceDao {
    private static final Logger LOGGER = Logger.getLogger(AttendanceDaoImpl.class.getName());
    
    private LeaveApplicationDao leaveApplicationDao;
    
    /**
     * Default constructor
     */
    public AttendanceDaoImpl() {
        leaveApplicationDao = new LeaveApplicationDaoImpl();
    }
    
    /**
     * Mark attendance for multiple students in a subject
     * 
     * @param subjectCode The subject code
     * @param date The attendance date
     * @param semester The semester
     * @param academicYear The academic year
     * @param studentAttendance Map of student ID to attendance status
     * @return Number of attendance records marked
     * @throws SQLException If a database error occurs
     */
    @Override
    public int markAttendance(String subjectCode, Date date, String semester, 
                              String academicYear, Map<Integer, String> studentAttendance) throws SQLException {
        String sql = "INSERT INTO Attendance (student_id, subject_code, attendance_date, status, " +
                     "semester, academic_year, marked_by, marked_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        int markedCount = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(sql);
            
            for (Map.Entry<Integer, String> entry : studentAttendance.entrySet()) {
                int studentId = entry.getKey();
                String status = entry.getValue();
                
                pstmt.setInt(1, studentId);
                pstmt.setString(2, subjectCode);
                pstmt.setDate(3, date);
                pstmt.setString(4, status);
                pstmt.setString(5, semester);
                pstmt.setString(6, academicYear);
                pstmt.setInt(7, 1); // Assuming marked_by refers to a user ID, using 1 as default
                
                pstmt.addBatch();
                markedCount++;
            }
            
            pstmt.executeBatch();
            conn.commit();
            
            return markedCount;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
            LOGGER.log(Level.SEVERE, "Error marking attendance", e);
            throw e;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing prepared statement", e);
                }
            }
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
    
    @Override
    public double calculateInstitutionAttendancePercentage(String academicYear, String semester, String month) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT COUNT(*) AS total, " +
            "SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) AS present, " +
            "SUM(CASE WHEN status = 'On Leave' THEN 1 ELSE 0 END) AS on_leave " +
            "FROM Attendance " +
            "WHERE academic_year = ? AND semester = ?");
        
        if (month != null && !month.isEmpty()) {
            sqlBuilder.append(" AND EXTRACT(MONTH FROM attendance_date) = ?");
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            int paramIndex = 1;
            stmt.setString(paramIndex++, academicYear);
            stmt.setString(paramIndex++, semester);
            
            if (month != null && !month.isEmpty()) {
                stmt.setInt(paramIndex, Integer.parseInt(month));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int present = rs.getInt("present");
                    int onLeave = rs.getInt("on_leave");
                    
                    // Don't count "On Leave" days in the total when calculating attendance percentage
                    int effectiveTotal = total - onLeave;
                    
                    if (effectiveTotal > 0) {
                        return (double) present / effectiveTotal * 100;
                    } else if (total > 0 && effectiveTotal == 0) {
                        // If all days are marked as "On Leave"
                        return 100.0;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calculating institution attendance percentage", e);
            throw e;
        }
        
        return 0.0;
    }
    
    @Override
    public double calculateDepartmentAttendancePercentage(int departmentId, String academicYear, String semester, String month) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT COUNT(*) AS total, " +
            "SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) AS present, " +
            "SUM(CASE WHEN a.status = 'On Leave' THEN 1 ELSE 0 END) AS on_leave " +
            "FROM Attendance a " +
            "JOIN Users u ON a.student_id = u.user_id " +
            "WHERE u.department_id = ? AND a.academic_year = ? AND a.semester = ?");
        
        if (month != null && !month.isEmpty()) {
            sqlBuilder.append(" AND EXTRACT(MONTH FROM a.attendance_date) = ?");
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            int paramIndex = 1;
            stmt.setInt(paramIndex++, departmentId);
            stmt.setString(paramIndex++, academicYear);
            stmt.setString(paramIndex++, semester);
            
            if (month != null && !month.isEmpty()) {
                stmt.setInt(paramIndex, Integer.parseInt(month));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int present = rs.getInt("present");
                    int onLeave = rs.getInt("on_leave");
                    
                    // Don't count "On Leave" days in the total when calculating attendance percentage
                    int effectiveTotal = total - onLeave;
                    
                    if (effectiveTotal > 0) {
                        return (double) present / effectiveTotal * 100;
                    } else if (total > 0 && effectiveTotal == 0) {
                        // If all days are marked as "On Leave"
                        return 100.0;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calculating department attendance percentage for department ID: " + departmentId, e);
            throw e;
        }
        
        return 0.0;
    }
    
    @Override
    public double calculateSubjectClassAttendancePercentage(int classId, String subjectCode, String academicYear, String semester) throws SQLException {
        String sql = "SELECT COUNT(*) AS total, " +
                    "SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) AS present, " +
                    "SUM(CASE WHEN a.status = 'On Leave' THEN 1 ELSE 0 END) AS on_leave " +
                    "FROM Attendance a " +
                    "JOIN Users u ON a.student_id = u.user_id " +
                    "WHERE u.class_id = ? AND a.subject_code = ? " +
                    "AND a.academic_year = ? AND a.semester = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId);
            stmt.setString(2, subjectCode);
            stmt.setString(3, academicYear);
            stmt.setString(4, semester);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int present = rs.getInt("present");
                    int onLeave = rs.getInt("on_leave");
                    
                    // Don't count "On Leave" days in the total when calculating attendance percentage
                    int effectiveTotal = total - onLeave;
                    
                    if (effectiveTotal > 0) {
                        return (double) present / effectiveTotal * 100;
                    } else if (total > 0 && effectiveTotal == 0) {
                        // If all days are marked as "On Leave"
                        return 100.0;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calculating subject class attendance percentage for class ID: " + 
                       classId + ", subject: " + subjectCode, e);
            throw e;
        }
        
        return 0.0;
    }
    
    @Override
    public double calculateClassAttendancePercentage(int classId, String academicYear, String semester, String month) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT COUNT(*) AS total, " +
            "SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) AS present, " +
            "SUM(CASE WHEN a.status = 'On Leave' THEN 1 ELSE 0 END) AS on_leave " +
            "FROM Attendance a " +
            "JOIN Users u ON a.student_id = u.user_id " +
            "WHERE u.class_id = ? AND a.academic_year = ? AND a.semester = ?");
        
        if (month != null && !month.isEmpty()) {
            sqlBuilder.append(" AND EXTRACT(MONTH FROM a.attendance_date) = ?");
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            int paramIndex = 1;
            stmt.setInt(paramIndex++, classId);
            stmt.setString(paramIndex++, academicYear);
            stmt.setString(paramIndex++, semester);
            
            if (month != null && !month.isEmpty()) {
                stmt.setInt(paramIndex, Integer.parseInt(month));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int present = rs.getInt("present");
                    int onLeave = rs.getInt("on_leave");
                    
                    // Don't count "On Leave" days in the total when calculating attendance percentage
                    int effectiveTotal = total - onLeave;
                    
                    if (effectiveTotal > 0) {
                        return (double) present / effectiveTotal * 100;
                    } else if (total > 0 && effectiveTotal == 0) {
                        // If all days are marked as "On Leave"
                        return 100.0;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calculating class attendance percentage for class ID: " + 
                       classId + ", academic year: " + academicYear + ", semester: " + semester, e);
            throw e;
        }
        
        return 0.0;
    }
    
    @Override
    public double calculateSubjectOverallAttendancePercentage(String subjectCode, String academicYear, String semester) throws SQLException {
        String sql = "SELECT COUNT(*) AS total, " +
                     "SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) AS present, " +
                     "SUM(CASE WHEN status = 'On Leave' THEN 1 ELSE 0 END) AS on_leave " +
                     "FROM Attendance " +
                     "WHERE subject_code = ? AND academic_year = ? AND semester = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, subjectCode);
            stmt.setString(2, academicYear);
            stmt.setString(3, semester);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int present = rs.getInt("present");
                    int onLeave = rs.getInt("on_leave");
                    
                    // Don't count "On Leave" days in the total when calculating attendance percentage
                    int effectiveTotal = total - onLeave;
                    
                    if (effectiveTotal > 0) {
                        return (double) present / effectiveTotal * 100;
                    } else if (total > 0 && effectiveTotal == 0) {
                        // If all days are marked as "On Leave"
                        return 100.0;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calculating subject overall attendance percentage for subject: " + 
                       subjectCode + ", academic year: " + academicYear + ", semester: " + semester, e);
            throw e;
        }
        
        return 0.0;
    }
    
    @Override
    public double calculateSubjectAttendancePercentage(int studentId, String subjectCode, String academicYear, String semester) throws SQLException {
        String sql = "SELECT COUNT(*) AS total, " +
                     "SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) AS present, " +
                     "SUM(CASE WHEN status = 'On Leave' THEN 1 ELSE 0 END) AS on_leave " +
                     "FROM Attendance " +
                     "WHERE student_id = ? AND subject_code = ? AND academic_year = ? AND semester = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setString(2, subjectCode);
            stmt.setString(3, academicYear);
            stmt.setString(4, semester);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int present = rs.getInt("present");
                    int onLeave = rs.getInt("on_leave");
                    
                    // Don't count "On Leave" days in the total when calculating attendance percentage
                    int effectiveTotal = total - onLeave;
                    
                    if (effectiveTotal > 0) {
                        return (double) present / effectiveTotal * 100;
                    } else if (total > 0 && effectiveTotal == 0) {
                        // If all days are marked as "On Leave"
                        return 100.0;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calculating subject attendance percentage for student: " + 
                       studentId + ", subject: " + subjectCode + ", academic year: " + academicYear + ", semester: " + semester, e);
            throw e;
        }
        
        return 0.0;
    }
    
    /**
     * Constructor with LeaveApplicationDao dependency
     * @param leaveApplicationDao The LeaveApplicationDao implementation
     */
    public AttendanceDaoImpl(LeaveApplicationDao leaveApplicationDao) {
        this.leaveApplicationDao = leaveApplicationDao;
    }
    
    @Override
    public Map<String, Integer> getTeacherMarkedAttendanceSummary(int teacherId, String academicYear, 
                                                               String semester, String month) throws SQLException {
        Map<String, Integer> summary = new HashMap<>();
        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT COUNT(*) AS total_records, " +
            "SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) AS present_count, " +
            "SUM(CASE WHEN status = 'Absent' THEN 1 ELSE 0 END) AS absent_count, " +
            "SUM(CASE WHEN status = 'On Leave' THEN 1 ELSE 0 END) AS leave_count " +
            "FROM Attendance " +
            "WHERE marked_by = ? AND academic_year = ? AND semester = ?");
        
        if (month != null && !month.isEmpty()) {
            sqlBuilder.append(" AND EXTRACT(MONTH FROM attendance_date) = ?");
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            int paramIndex = 1;
            stmt.setInt(paramIndex++, teacherId);
            stmt.setString(paramIndex++, academicYear);
            stmt.setString(paramIndex++, semester);
            
            if (month != null && !month.isEmpty()) {
                stmt.setInt(paramIndex, Integer.parseInt(month));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    summary.put("totalRecords", rs.getInt("total_records"));
                    summary.put("presentCount", rs.getInt("present_count"));
                    summary.put("absentCount", rs.getInt("absent_count"));
                    summary.put("leaveCount", rs.getInt("leave_count"));
                } else {
                    summary.put("totalRecords", 0);
                    summary.put("presentCount", 0);
                    summary.put("absentCount", 0);
                    summary.put("leaveCount", 0);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting teacher marked attendance summary", e);
            throw e;
        }
        
        return summary;
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
            
            // Fix: Convert LocalDate to java.sql.Date
            stmt.setDate(1, java.sql.Date.valueOf(attendance.getAttendanceDate()));
            stmt.setString(2, attendance.getSubjectCode());
            stmt.setInt(3, attendance.getStudentId());
            // Fix: Convert int to String for semester
            stmt.setString(4, String.valueOf(attendance.getSemester()));
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
            
            // Fix: Convert LocalDate to java.sql.Date
            stmt.setDate(1, java.sql.Date.valueOf(attendance.getAttendanceDate()));
            stmt.setString(2, attendance.getSubjectCode());
            stmt.setInt(3, attendance.getStudentId());
            // Fix: Convert int to String for semester
            stmt.setString(4, String.valueOf(attendance.getSemester()));
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
    public List<Attendance> findByStudent(int studentId) throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE student_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attendanceList.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding attendance by student ID: " + studentId, e);
            throw e;
        }
        
        return attendanceList;
    }
    
    @Override
    public List<Attendance> findByStudent(int studentId, String academicYear, String semester, String month) throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM Attendance WHERE student_id = ?");
        
        if (academicYear != null && !academicYear.isEmpty()) {
            sqlBuilder.append(" AND academic_year = ?");
        }
        
        if (semester != null && !semester.isEmpty()) {
            sqlBuilder.append(" AND semester = ?");
        }
        
        if (month != null && !month.isEmpty()) {
            sqlBuilder.append(" AND EXTRACT(MONTH FROM attendance_date) = ?");
        }
        
        sqlBuilder.append(" ORDER BY attendance_date DESC");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            int paramIndex = 1;
            stmt.setInt(paramIndex++, studentId);
            
            if (academicYear != null && !academicYear.isEmpty()) {
                stmt.setString(paramIndex++, academicYear);
            }
            
            if (semester != null && !semester.isEmpty()) {
                stmt.setString(paramIndex++, semester);
            }
            
            if (month != null && !month.isEmpty()) {
                stmt.setInt(paramIndex++, Integer.parseInt(month));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attendanceList.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding attendance by student ID with filters", e);
            throw e;
        }
        
        return attendanceList;
    }

    @Override
    public List<Attendance> findBySubject(String subjectCode) throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, subjectCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attendanceList.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding attendance by subject code: " + subjectCode, e);
            throw e;
        }
        
        return attendanceList;
    }
    
    @Override
    public Map<String, Double> getAttendanceSummary(int studentId, String semester, String academicYear) throws SQLException {
        Map<String, Double> summary = new HashMap<>();
        String sql = "SELECT subject_code, " +
                     "COUNT(*) AS total, " +
                     "SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) AS present, " +
                     "SUM(CASE WHEN status = 'On Leave' THEN 1 ELSE 0 END) AS on_leave " +
                     "FROM Attendance " +
                     "WHERE student_id = ? AND semester = ? AND academic_year = ? " +
                     "GROUP BY subject_code";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setString(2, semester);
            stmt.setString(3, academicYear);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String subjectCode = rs.getString("subject_code");
                    int total = rs.getInt("total");
                    int present = rs.getInt("present");
                    int onLeave = rs.getInt("on_leave");
                    
                    // Don't count "On Leave" days in the total when calculating attendance percentage
                    int effectiveTotal = total - onLeave;
                    
                    if (effectiveTotal > 0) {
                        summary.put(subjectCode, (double) present / effectiveTotal * 100);
                    } else if (total > 0 && effectiveTotal == 0) {
                        // If all days are marked as "On Leave"
                        summary.put(subjectCode, 100.0);
                    } else {
                        summary.put(subjectCode, 0.0);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance summary for student: " + studentId, e);
            throw e;
        }
        
        return summary;
    }

    @Override
    public List<Attendance> findByDate(Date date) throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE attendance_date = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, date);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attendanceList.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding attendance by date: " + date, e);
            throw e;
        }
        
        return attendanceList;
    }

    // Helper method to map ResultSet to Attendance object
    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setAttendanceId(rs.getInt("attendance_id"));
        attendance.setStudentId(rs.getInt("student_id"));
        attendance.setSubjectCode(rs.getString("subject_code"));
        // Fix: Convert String to int for semester
        attendance.setSemester(Integer.parseInt(rs.getString("semester")));
        attendance.setAcademicYear(rs.getString("academic_year"));
        attendance.setAttendanceDate(rs.getDate("attendance_date"));
        attendance.setStatus(rs.getString("status"));
        return attendance;
    }
    
    @Override
    public Map<String, Double> getMonthlyDepartmentAttendanceTrend(int departmentId, String academicYear) throws SQLException {
        Map<String, Double> monthlyTrend = new HashMap<>();
        String sql = "SELECT TO_CHAR(a.attendance_date, 'Month') AS month, " +
                     "COUNT(*) AS total, " +
                     "SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) AS present, " +
                     "SUM(CASE WHEN a.status = 'On Leave' THEN 1 ELSE 0 END) AS on_leave " +
                     "FROM Attendance a " +
                     "JOIN Student s ON a.student_id = s.student_id " +
                     "JOIN Class c ON s.class_id = c.class_id " +
                     "WHERE c.department_id = ? AND a.academic_year = ? " +
                     "GROUP BY TO_CHAR(a.attendance_date, 'Month') " +
                     "ORDER BY MIN(a.attendance_date)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, departmentId);
            stmt.setString(2, academicYear);
            
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
            LOGGER.log(Level.SEVERE, "Error getting monthly department attendance trend: " + academicYear, e);
            throw e;
        }
        
        return monthlyTrend;
    }
    
    @Override
    public Map<String, Double> getSemesterDepartmentAttendanceTrend(int departmentId, String academicYear) throws SQLException {
        Map<String, Double> semesterTrend = new HashMap<>();
        String sql = "SELECT a.semester, " +
                     "COUNT(*) AS total, " +
                     "SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) AS present, " +
                     "SUM(CASE WHEN a.status = 'On Leave' THEN 1 ELSE 0 END) AS on_leave " +
                     "FROM Attendance a " +
                     "JOIN Student s ON a.student_id = s.student_id " +
                     "JOIN Class c ON s.class_id = c.class_id " +
                     "WHERE c.department_id = ? AND a.academic_year = ? " +
                     "GROUP BY a.semester " +
                     "ORDER BY a.semester";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, departmentId);
            stmt.setString(2, academicYear);
            
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
            LOGGER.log(Level.SEVERE, "Error getting semester department attendance trend: " + academicYear, e);
            throw e;
        }
        
        return semesterTrend;
    }
}