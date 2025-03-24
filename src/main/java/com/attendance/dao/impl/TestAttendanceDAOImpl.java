package com.attendance.dao.impl;

import com.attendance.dao.AttendanceDAO;
import com.attendance.models.Attendance;
import com.attendance.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test implementation of the AttendanceDAO interface.
 * This class provides methods to interact with the Attendance table in the database
 * without starting a server.
 * 
 * This implementation follows the AttendanceDAO (uppercase) interface pattern
 * rather than the AttendanceDao (mixed case) interface. The two interfaces serve
 * different purposes in the application - AttendanceDAO focuses on core operations,
 * while AttendanceDao provides more advanced analytics capabilities.
 */
public class TestAttendanceDAOImpl implements AttendanceDAO {

    /**
     * Create a new attendance record
     *
     * @param attendance Attendance object with details
     * @return Generated attendance ID
     */
    @Override
    public int createAttendance(Attendance attendance) {
        String sql = "INSERT INTO Attendance (attendance_date, subject_code, student_id, semester, academic_year, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING attendance_id";
                    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, attendance.getDate());
            stmt.setString(2, attendance.getSubjectCode());
            stmt.setInt(3, attendance.getStudentId());
            stmt.setObject(4, attendance.getSemester());  
            stmt.setString(5, attendance.getAcademicYear());
            stmt.setString(6, attendance.getStatus());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("attendance_id");
            }
        } catch (SQLException e) {
            System.err.println("Error creating attendance record: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Update an existing attendance record
     *
     * @param attendance Attendance object with updated details
     * @return true if update successful, false otherwise
     */
    @Override
    public boolean updateAttendance(Attendance attendance) {
        String sql = "UPDATE Attendance SET attendance_date = ?, subject_code = ?, " +
                     "student_id = ?, semester = ?, academic_year = ?, status = ? " +
                     "WHERE attendance_id = ?";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, attendance.getDate());
            stmt.setString(2, attendance.getSubjectCode());
            stmt.setInt(3, attendance.getStudentId());
            stmt.setObject(4, attendance.getSemester());
            stmt.setString(5, attendance.getAcademicYear());
            stmt.setString(6, attendance.getStatus());
            stmt.setInt(7, attendance.getId());
            
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Error updating attendance record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete an attendance record by ID
     *
     * @param id Attendance ID to delete
     * @return true if deletion successful, false otherwise
     */
    @Override
    public boolean deleteAttendance(int id) {
        String sql = "DELETE FROM Attendance WHERE attendance_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting attendance record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get attendance record by ID
     *
     * @param id Attendance ID
     * @return Attendance object if found, null otherwise
     */
    @Override
    public Attendance getAttendanceById(int id) {
        String sql = "SELECT * FROM Attendance WHERE attendance_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractAttendanceFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving attendance by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all attendance records for a specific student
     *
     * @param studentId Student ID
     * @return List of Attendance objects
     */
    @Override
    public List<Attendance> getAttendanceByStudentId(int studentId) {
        String sql = "SELECT * FROM Attendance WHERE student_id = ?";
        List<Attendance> attendances = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                attendances.add(extractAttendanceFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving attendance by student ID: " + e.getMessage());
            e.printStackTrace();
        }
        return attendances;
    }

    /**
     * Check if attendance record exists for a student on a specific date
     *
     * @param studentId Student ID
     * @param date      Date to check
     * @return true if attendance record exists, false otherwise
     */
    @Override
    public boolean attendanceExists(int studentId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM Attendance WHERE student_id = ? AND attendance_date = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setObject(2, date);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if attendance exists: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    // Implementing all required methods from AttendanceDAO interface
    
    @Override
    public List<Attendance> getAttendanceByStudentIdAndDateRange(int studentId, LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendances = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE student_id = ? AND attendance_date BETWEEN ? AND ? ORDER BY attendance_date";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setObject(2, startDate);
            stmt.setObject(3, endDate);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                attendances.add(extractAttendanceFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving attendance by student ID and date range: " + e.getMessage());
            e.printStackTrace();
        }
        return attendances;
    }
    
    @Override
    public List<Attendance> getAttendanceByClassId(int classId) {
        List<Attendance> attendances = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE semester = ? ORDER BY attendance_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId); // Using class ID as semester for this implementation
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                attendances.add(extractAttendanceFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving attendance by class ID: " + e.getMessage());
            e.printStackTrace();
        }
        return attendances;
    }
    
    @Override
    public List<Attendance> getAttendanceByClassIdAndDate(int classId, LocalDate date) {
        List<Attendance> attendances = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE semester = ? AND attendance_date = ? ORDER BY student_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId); // Using class ID as semester
            stmt.setObject(2, date);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                attendances.add(extractAttendanceFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving attendance by class ID and date: " + e.getMessage());
            e.printStackTrace();
        }
        return attendances;
    }
    
    @Override
    public List<Attendance> getAttendanceBySubjectId(int subjectId) {
        List<Attendance> attendances = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE subject_code = ? ORDER BY attendance_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, subjectId); // This should be adjusted to use subject_code if needed
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                attendances.add(extractAttendanceFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving attendance by subject ID: " + e.getMessage());
            e.printStackTrace();
        }
        return attendances;
    }
    
    @Override
    public List<Attendance> getAttendanceByDate(LocalDate date) {
        List<Attendance> attendances = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE attendance_date = ? ORDER BY semester, student_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, date);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                attendances.add(extractAttendanceFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving attendance by date: " + e.getMessage());
            e.printStackTrace();
        }
        return attendances;
    }
    
    @Override
    public List<Attendance> getAttendanceByClassIdAndDateRange(int classId, LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendances = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE semester = ? AND attendance_date BETWEEN ? AND ? ORDER BY attendance_date";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId); // Using class ID as semester
            stmt.setObject(2, startDate);
            stmt.setObject(3, endDate);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                attendances.add(extractAttendanceFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving attendance by class ID and date range: " + e.getMessage());
            e.printStackTrace();
        }
        return attendances;
    }
    
    @Override
    public Map<String, Integer> getAttendanceSummaryByStudentId(int studentId) {
        Map<String, Integer> summary = new HashMap<>();
        String sql = "SELECT status, COUNT(*) as count FROM Attendance WHERE student_id = ? GROUP BY status";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                summary.put(status, count);
            }
            
            // Set default values if not present
            if (!summary.containsKey("Present")) summary.put("Present", 0);
            if (!summary.containsKey("Absent")) summary.put("Absent", 0);
            if (!summary.containsKey("Leave")) summary.put("Leave", 0);
            
            // Calculate total
            int total = summary.values().stream().mapToInt(Integer::intValue).sum();
            summary.put("Total", total);
            
        } catch (SQLException e) {
            System.err.println("Error retrieving attendance summary by student ID: " + e.getMessage());
            e.printStackTrace();
        }
        return summary;
    }
    
    @Override
    public Map<String, Integer> getAttendanceSummaryByStudentIdAndDateRange(int studentId, LocalDate startDate, LocalDate endDate) {
        Map<String, Integer> summary = new HashMap<>();
        String sql = "SELECT status, COUNT(*) as count FROM Attendance WHERE student_id = ? AND attendance_date BETWEEN ? AND ? GROUP BY status";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setObject(2, startDate);
            stmt.setObject(3, endDate);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                summary.put(status, count);
            }
            
            // Set default values if not present
            if (!summary.containsKey("Present")) summary.put("Present", 0);
            if (!summary.containsKey("Absent")) summary.put("Absent", 0);
            if (!summary.containsKey("Leave")) summary.put("Leave", 0);
            
            // Calculate total
            int total = summary.values().stream().mapToInt(Integer::intValue).sum();
            summary.put("Total", total);
            
        } catch (SQLException e) {
            System.err.println("Error retrieving attendance summary by student ID and date range: " + e.getMessage());
            e.printStackTrace();
        }
        return summary;
    }
    
    @Override
    public Map<String, Integer> getAttendanceSummaryByClassId(int classId) {
        Map<String, Integer> summary = new HashMap<>();
        String sql = "SELECT status, COUNT(*) as count FROM Attendance WHERE semester = ? GROUP BY status";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId); // Using class ID as semester
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                summary.put(status, count);
            }
            
            // Set default values if not present
            if (!summary.containsKey("Present")) summary.put("Present", 0);
            if (!summary.containsKey("Absent")) summary.put("Absent", 0);
            if (!summary.containsKey("Leave")) summary.put("Leave", 0);
            
            // Calculate total
            int total = summary.values().stream().mapToInt(Integer::intValue).sum();
            summary.put("Total", total);
            
        } catch (SQLException e) {
            System.err.println("Error retrieving attendance summary by class ID: " + e.getMessage());
            e.printStackTrace();
        }
        return summary;
    }
    
    @Override
    public Map<String, Integer> getAttendanceSummaryByClassIdAndDateRange(int classId, LocalDate startDate, LocalDate endDate) {
        Map<String, Integer> summary = new HashMap<>();
        String sql = "SELECT status, COUNT(*) as count FROM Attendance WHERE semester = ? AND attendance_date BETWEEN ? AND ? GROUP BY status";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId); // Using class ID as semester
            stmt.setObject(2, startDate);
            stmt.setObject(3, endDate);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                summary.put(status, count);
            }
            
            // Set default values if not present
            if (!summary.containsKey("Present")) summary.put("Present", 0);
            if (!summary.containsKey("Absent")) summary.put("Absent", 0);
            if (!summary.containsKey("Leave")) summary.put("Leave", 0);
            
            // Calculate total
            int total = summary.values().stream().mapToInt(Integer::intValue).sum();
            summary.put("Total", total);
            
        } catch (SQLException e) {
            System.err.println("Error retrieving attendance summary by class ID and date range: " + e.getMessage());
            e.printStackTrace();
        }
        return summary;
    }

    /**
     * Helper method to extract Attendance object from ResultSet
     *
     * @param rs ResultSet with attendance data
     * @return Attendance object
     * @throws SQLException if database error occurs
     */
    private Attendance extractAttendanceFromResultSet(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setId(rs.getInt("attendance_id"));
        attendance.setDate(rs.getObject("attendance_date", LocalDate.class));
        attendance.setSubjectCode(rs.getString("subject_code"));
        attendance.setStudentId(rs.getInt("student_id"));
        
        // Handle the semester which could be a string or an integer
        Object semObj = rs.getObject("semester");
        if (semObj instanceof String) {
            try {
                attendance.setSemester(Integer.parseInt((String) semObj));
            } catch (NumberFormatException e) {
                // If it can't be parsed as an integer, set it as 0
                attendance.setSemester(0);
            }
        } else if (semObj instanceof Integer) {
            attendance.setSemester((Integer) semObj);
        } else if (semObj != null) {
            // Try to convert to string then to integer
            try {
                attendance.setSemester(Integer.parseInt(semObj.toString()));
            } catch (NumberFormatException e) {
                attendance.setSemester(0);
            }
        }
        
        attendance.setAcademicYear(rs.getString("academic_year"));
        attendance.setStatus(rs.getString("status"));
        return attendance;
    }
}