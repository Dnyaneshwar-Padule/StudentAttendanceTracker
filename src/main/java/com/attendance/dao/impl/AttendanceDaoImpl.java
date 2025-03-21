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
            
            stmt.setDate(1, attendance.getAttendanceDate());
            stmt.setString(2, attendance.getSubjectCode());
            stmt.setInt(3, attendance.getStudentId());
            stmt.setString(4, attendance.getSemester());
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
            
            stmt.setDate(1, attendance.getAttendanceDate());
            stmt.setString(2, attendance.getSubjectCode());
            stmt.setInt(3, attendance.getStudentId());
            stmt.setString(4, attendance.getSemester());
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

    @Override
    public List<Attendance> findByStudentAndSubject(int studentId, String subjectCode) throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE student_id = ? AND subject_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setString(2, subjectCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attendanceList.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding attendance by student ID and subject code", e);
            throw e;
        }
        
        return attendanceList;
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
            LOGGER.log(Level.SEVERE, "Error finding attendance by student ID, subject code, and semester", e);
            throw e;
        }
        
        return attendanceList;
    }

    @Override
    public List<Attendance> findByStudentSubjectSemesterAndYear(int studentId, String subjectCode, 
                                                              String semester, String academicYear) throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE student_id = ? AND subject_code = ? AND semester = ? AND academic_year = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setString(2, subjectCode);
            stmt.setString(3, semester);
            stmt.setString(4, academicYear);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attendanceList.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding attendance by student ID, subject code, semester, and year", e);
            throw e;
        }
        
        return attendanceList;
    }

    @Override
    public List<Attendance> findBySubjectDateAndSemester(String subjectCode, Date date, String semester) throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE subject_code = ? AND attendance_date = ? AND semester = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, subjectCode);
            stmt.setDate(2, date);
            stmt.setString(3, semester);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attendanceList.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding attendance by subject code, date, and semester", e);
            throw e;
        }
        
        return attendanceList;
    }
    
    @Override
    public List<Attendance> findByClassAndSubjectAndDate(int classId, String subjectCode, Date date) throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT a.* FROM Attendance a " +
                     "JOIN StudentEnrollment se ON a.student_id = se.student_id " +
                     "WHERE se.class_id = ? AND a.subject_code = ? AND a.attendance_date = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId);
            stmt.setString(2, subjectCode);
            stmt.setDate(3, date);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attendanceList.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding attendance by class, subject code, and date", e);
            throw e;
        }
        
        return attendanceList;
    }

    @Override
    public double calculateAttendancePercentage(int studentId, String subjectCode, String semester, String academicYear) throws SQLException {
        String sql = "SELECT COUNT(*) AS total, " +
                     "SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) AS present, " +
                     "SUM(CASE WHEN status = 'On Leave' THEN 1 ELSE 0 END) AS on_leave " +
                     "FROM Attendance " +
                     "WHERE student_id = ? AND subject_code = ? AND semester = ? AND academic_year = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setString(2, subjectCode);
            stmt.setString(3, semester);
            stmt.setString(4, academicYear);
            
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
            LOGGER.log(Level.SEVERE, "Error calculating attendance percentage", e);
            throw e;
        }
        
        return 0;
    }
    
    @Override
    public double calculateSubjectAttendancePercentage(int studentId, String subjectCode, String academicYear, String semester) throws SQLException {
        // This is essentially the same as calculateAttendancePercentage but with parameters in a different order
        return calculateAttendancePercentage(studentId, subjectCode, semester, academicYear);
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
            LOGGER.log(Level.SEVERE, "Error calculating subject overall attendance percentage", e);
            throw e;
        }
        
        return 0;
    }
    
    @Override
    public double calculateClassAttendancePercentage(int classId, String academicYear, String semester, String month) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) AS total, ")
           .append("SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) AS present, ")
           .append("SUM(CASE WHEN a.status = 'On Leave' THEN 1 ELSE 0 END) AS on_leave ")
           .append("FROM Attendance a ")
           .append("JOIN StudentEnrollment se ON a.student_id = se.student_id ")
           .append("WHERE se.class_id = ? AND a.academic_year = ? AND a.semester = ?");
        
        if (month != null && !month.isEmpty()) {
            sql.append(" AND EXTRACT(MONTH FROM a.attendance_date) = ?");
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            stmt.setInt(1, classId);
            stmt.setString(2, academicYear);
            stmt.setString(3, semester);
            
            if (month != null && !month.isEmpty()) {
                stmt.setInt(4, Integer.parseInt(month));
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
            LOGGER.log(Level.SEVERE, "Error calculating class attendance percentage", e);
            throw e;
        }
        
        return 0;
    }
    
    @Override
    public double calculateSubjectClassAttendancePercentage(int classId, String subjectCode, String academicYear, String semester) throws SQLException {
        String sql = "SELECT COUNT(*) AS total, " +
                     "SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) AS present, " +
                     "SUM(CASE WHEN a.status = 'On Leave' THEN 1 ELSE 0 END) AS on_leave " +
                     "FROM Attendance a " +
                     "JOIN StudentEnrollment se ON a.student_id = se.student_id " +
                     "WHERE se.class_id = ? AND a.subject_code = ? AND a.academic_year = ? AND a.semester = ?";
        
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
            LOGGER.log(Level.SEVERE, "Error calculating subject class attendance percentage", e);
            throw e;
        }
        
        return 0;
    }
    
    @Override
    public double calculateDepartmentAttendancePercentage(int departmentId, String academicYear, String semester, String month) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) AS total, ")
           .append("SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) AS present, ")
           .append("SUM(CASE WHEN a.status = 'On Leave' THEN 1 ELSE 0 END) AS on_leave ")
           .append("FROM Attendance a ")
           .append("JOIN StudentEnrollment se ON a.student_id = se.student_id ")
           .append("JOIN Class c ON se.class_id = c.class_id ")
           .append("WHERE c.department_id = ? AND a.academic_year = ? AND a.semester = ?");
        
        if (month != null && !month.isEmpty()) {
            sql.append(" AND EXTRACT(MONTH FROM a.attendance_date) = ?");
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            stmt.setInt(1, departmentId);
            stmt.setString(2, academicYear);
            stmt.setString(3, semester);
            
            if (month != null && !month.isEmpty()) {
                stmt.setInt(4, Integer.parseInt(month));
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
            LOGGER.log(Level.SEVERE, "Error calculating department attendance percentage", e);
            throw e;
        }
        
        return 0;
    }

    @Override
    public Map<String, Double> getAttendanceSummary(int studentId, String semester, String academicYear) throws SQLException {
        Map<String, Double> summary = new HashMap<>();
        
        String sql = "SELECT subject_code, COUNT(*) AS total, " +
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
                        double percentage = (double) present / effectiveTotal * 100;
                        summary.put(subjectCode, percentage);
                    } else if (total > 0 && effectiveTotal == 0) {
                        // If all days are marked as "On Leave"
                        summary.put(subjectCode, 100.0);
                    } else {
                        summary.put(subjectCode, 0.0);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting attendance summary", e);
            throw e;
        }
        
        return summary;
    }

    @Override
    public int markAttendance(String subjectCode, Date date, String semester, String academicYear, 
                              Map<Integer, String> studentAttendance) throws SQLException {
        int count = 0;
        
        String sql = "INSERT INTO Attendance (attendance_date, subject_code, student_id, semester, academic_year, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (attendance_date, subject_code, student_id) " +
                     "DO UPDATE SET status = EXCLUDED.status";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);
            
            // Check for students on approved leave
            Map<Integer, Boolean> studentsOnLeave = new HashMap<>();
            for (int studentId : studentAttendance.keySet()) {
                boolean onLeave = leaveApplicationDao.hasActiveLeave(studentId, date);
                studentsOnLeave.put(studentId, onLeave);
            }
            
            for (Map.Entry<Integer, String> entry : studentAttendance.entrySet()) {
                int studentId = entry.getKey();
                String status = entry.getValue();
                
                // If student has an approved leave, mark as "On Leave" regardless of the provided status
                if (studentsOnLeave.getOrDefault(studentId, false)) {
                    status = "On Leave";
                }
                
                stmt.setDate(1, date);
                stmt.setString(2, subjectCode);
                stmt.setInt(3, studentId);
                stmt.setString(4, semester);
                stmt.setString(5, academicYear);
                stmt.setString(6, status);
                
                stmt.addBatch();
                count++;
            }
            
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error marking attendance", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
            throw e;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing statement", e);
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
        
        return count;
    }
    
    /**
     * Maps a database result set to an Attendance object
     * @param rs The result set positioned at the current row
     * @return A populated Attendance object
     * @throws SQLException If a database error occurs
     */
    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setAttendanceId(rs.getInt("attendance_id"));
        attendance.setAttendanceDate(rs.getDate("attendance_date"));
        attendance.setSubjectCode(rs.getString("subject_code"));
        attendance.setStudentId(rs.getInt("student_id"));
        attendance.setSemester(rs.getString("semester"));
        attendance.setAcademicYear(rs.getString("academic_year"));
        attendance.setStatus(rs.getString("status"));
        return attendance;
    }
}