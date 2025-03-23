package com.attendance.tests;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.impl.TestAttendanceDAOImpl;
import com.attendance.models.Attendance;
import com.attendance.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Standalone test for attendance functionality that avoids starting the web server
 */
public class StandaloneAttendanceTest {
    // Use our special test implementation
    private static final AttendanceDAO attendanceDAO = new TestAttendanceDAOImpl();
    
    public static void main(String[] args) {
        System.out.println("Starting standalone attendance tests...");
        
        try {
            // Verify database connection
            testDatabaseConnection();
            
            // Clean up existing test data
            cleanupTestData();
            
            // Test creating an attendance record
            Attendance newAttendance = testCreateAttendance();
            
            if (newAttendance != null && newAttendance.getId() > 0) {
                // Test getting attendance by ID
                testGetAttendanceById(newAttendance.getId());
                
                // Test checking if attendance exists
                testAttendanceExists(newAttendance.getStudentId(), newAttendance.getDate());
                
                // Test updating attendance
                testUpdateAttendance(newAttendance);
                
                // Test getting attendance by student ID
                testGetAttendanceByStudentId(newAttendance.getStudentId());
                
                // Clean up after tests
                testDeleteAttendance(newAttendance.getId());
            }
            
            System.out.println("All tests completed!");
            
        } catch (Exception e) {
            System.err.println("Test failed with exception:");
            e.printStackTrace();
        }
    }
    
    private static void testDatabaseConnection() {
        System.out.println("\n--- Testing database connection ---");
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Database connection successful!");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            throw new RuntimeException("Cannot continue tests without database connection", e);
        }
    }
    
    private static void cleanupTestData() {
        System.out.println("\n--- Cleaning up existing test data ---");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM Attendance WHERE student_id = ? AND subject_code = ?")) {
                
            stmt.setInt(1, 1); // Admin user
            stmt.setString(2, "CS101");
            int rowsDeleted = stmt.executeUpdate();
            
            System.out.println("Deleted " + rowsDeleted + " test attendance records");
        } catch (SQLException e) {
            System.err.println("Error cleaning up test data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static Attendance testCreateAttendance() {
        System.out.println("\n--- Testing createAttendance ---");
        try {
            // Create test attendance object
            Attendance attendance = new Attendance();
            attendance.setStudentId(1); // Admin user ID
            attendance.setSubjectCode("CS101");
            attendance.setDate(LocalDate.now());
            attendance.setStatus("Present");
            attendance.setSemester(1);
            attendance.setAcademicYear("2024-2025");
            
            // Create in database
            int id = attendanceDAO.createAttendance(attendance);
            System.out.println("Created attendance with ID: " + id);
            
            if (id > 0) {
                attendance.setId(id);
                System.out.println("✅ createAttendance test: PASSED");
                return attendance;
            } else {
                System.out.println("❌ createAttendance test: FAILED - Invalid ID");
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ createAttendance test: FAILED with exception");
            e.printStackTrace();
            return null;
        }
    }
    
    private static void testGetAttendanceById(int id) {
        System.out.println("\n--- Testing getAttendanceById ---");
        try {
            Attendance attendance = attendanceDAO.getAttendanceById(id);
            
            if (attendance != null) {
                System.out.println("Found attendance: " + attendance);
                System.out.println("✅ getAttendanceById test: PASSED");
            } else {
                System.out.println("❌ getAttendanceById test: FAILED - Not found");
            }
        } catch (Exception e) {
            System.err.println("❌ getAttendanceById test: FAILED with exception");
            e.printStackTrace();
        }
    }
    
    private static void testAttendanceExists(int studentId, LocalDate date) {
        System.out.println("\n--- Testing attendanceExists ---");
        try {
            boolean exists = attendanceDAO.attendanceExists(studentId, date);
            System.out.println("Attendance exists: " + exists);
            
            if (exists) {
                System.out.println("✅ attendanceExists test: PASSED");
            } else {
                System.out.println("❌ attendanceExists test: FAILED - Should exist");
            }
        } catch (Exception e) {
            System.err.println("❌ attendanceExists test: FAILED with exception");
            e.printStackTrace();
        }
    }
    
    private static void testUpdateAttendance(Attendance attendance) {
        System.out.println("\n--- Testing updateAttendance ---");
        try {
            // Change status
            String originalStatus = attendance.getStatus();
            String newStatus = "Absent";
            attendance.setStatus(newStatus);
            
            boolean updated = attendanceDAO.updateAttendance(attendance);
            System.out.println("Update result: " + updated);
            
            // Verify the update
            Attendance updatedAttendance = attendanceDAO.getAttendanceById(attendance.getId());
            if (updatedAttendance != null) {
                System.out.println("Updated status: " + updatedAttendance.getStatus());
                
                if (updated && newStatus.equals(updatedAttendance.getStatus())) {
                    System.out.println("✅ updateAttendance test: PASSED");
                } else {
                    System.out.println("❌ updateAttendance test: FAILED - Status not updated");
                }
            } else {
                System.out.println("❌ updateAttendance test: FAILED - Record not found");
            }
        } catch (Exception e) {
            System.err.println("❌ updateAttendance test: FAILED with exception");
            e.printStackTrace();
        }
    }
    
    private static void testGetAttendanceByStudentId(int studentId) {
        System.out.println("\n--- Testing getAttendanceByStudentId ---");
        try {
            List<Attendance> attendances = attendanceDAO.getAttendanceByStudentId(studentId);
            
            System.out.println("Found " + attendances.size() + " records for student ID " + studentId);
            if (!attendances.isEmpty()) {
                System.out.println("First record: " + attendances.get(0));
                System.out.println("✅ getAttendanceByStudentId test: PASSED");
            } else {
                System.out.println("❌ getAttendanceByStudentId test: FAILED - No records found");
            }
        } catch (Exception e) {
            System.err.println("❌ getAttendanceByStudentId test: FAILED with exception");
            e.printStackTrace();
        }
    }
    
    private static void testDeleteAttendance(int id) {
        System.out.println("\n--- Testing deleteAttendance ---");
        try {
            boolean deleted = attendanceDAO.deleteAttendance(id);
            System.out.println("Deletion result: " + deleted);
            
            // Verify deletion
            Attendance attendance = attendanceDAO.getAttendanceById(id);
            
            if (deleted && attendance == null) {
                System.out.println("✅ deleteAttendance test: PASSED");
            } else {
                System.out.println("❌ deleteAttendance test: FAILED - Record still exists");
            }
        } catch (Exception e) {
            System.err.println("❌ deleteAttendance test: FAILED with exception");
            e.printStackTrace();
        }
    }
}