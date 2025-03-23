package com.attendance.tests;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.impl.AttendanceDAOImpl;
import com.attendance.models.Attendance;
import com.attendance.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * A simple standalone test for AttendanceDAO implementation
 * This test doesn't start a server and only focuses on database operations
 */
public class SimpleAttendanceTest {
    
    private static final AttendanceDAO attendanceDAO = new AttendanceDAOImpl();
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Starting Simple Attendance DAO Test");
        System.out.println("========================================");
        
        try {
            // Clean up any test data from previous runs
            cleanUpTestData();
            
            // Test attendance creation
            testCreateAttendance();
            
            // Test attendance exists check
            testAttendanceExists();
            
            // Test get by student ID
            testGetByStudentId();
            
            // Test updating attendance
            testUpdateAttendance();
            
            // Clean up after tests
            cleanUpTestData();
            
            System.out.println("========================================");
            System.out.println("All tests completed!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("Test failed with exception:");
            e.printStackTrace();
        }
    }
    
    private static void cleanUpTestData() {
        System.out.println("\n--- Cleaning up test data ---");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM Attendance WHERE student_id = ? AND subject_code = ?")) {
                
            stmt.setInt(1, 1); // Admin user ID
            stmt.setString(2, "CS101"); // Test subject
            int rowsDeleted = stmt.executeUpdate();
            
            System.out.println("Deleted " + rowsDeleted + " test attendance records");
        } catch (SQLException e) {
            System.err.println("Error cleaning up test data:");
            e.printStackTrace();
        }
    }
    
    private static void testCreateAttendance() {
        System.out.println("\n--- Testing createAttendance ---");
        try {
            // Create a test attendance record
            Attendance attendance = new Attendance();
            attendance.setStudentId(1); // Admin user
            attendance.setSubjectCode("CS101");
            attendance.setDate(LocalDate.now());
            attendance.setStatus("Present");
            attendance.setSemester(1);
            attendance.setAcademicYear("2024-2025");
            
            int id = attendanceDAO.createAttendance(attendance);
            System.out.println("Created attendance record with ID: " + id);
            
            if (id > 0) {
                System.out.println("✅ createAttendance: PASSED");
            } else {
                System.out.println("❌ createAttendance: FAILED - Invalid ID returned");
            }
        } catch (Exception e) {
            System.err.println("❌ createAttendance: FAILED with exception:");
            e.printStackTrace();
        }
    }
    
    private static void testAttendanceExists() {
        System.out.println("\n--- Testing attendanceExists ---");
        try {
            // Should find the record we just created
            boolean exists = attendanceDAO.attendanceExists(1, LocalDate.now());
            System.out.println("Attendance exists for today: " + exists);
            
            // Shouldn't find a record for a date in the distant past
            boolean pastExists = attendanceDAO.attendanceExists(1, LocalDate.of(2000, 1, 1));
            System.out.println("Attendance exists for 2000-01-01: " + pastExists);
            
            if (exists && !pastExists) {
                System.out.println("✅ attendanceExists: PASSED");
            } else {
                System.out.println("❌ attendanceExists: FAILED - Expected true for today, false for past date");
            }
        } catch (Exception e) {
            System.err.println("❌ attendanceExists: FAILED with exception:");
            e.printStackTrace();
        }
    }
    
    private static void testGetByStudentId() {
        System.out.println("\n--- Testing getAttendanceByStudentId ---");
        try {
            // Get attendance for student ID 1
            List<Attendance> attendances = attendanceDAO.getAttendanceByStudentId(1);
            
            System.out.println("Found " + attendances.size() + " attendance records for student ID 1");
            if (!attendances.isEmpty()) {
                System.out.println("First record: " + attendances.get(0));
                System.out.println("✅ getAttendanceByStudentId: PASSED");
            } else {
                System.out.println("❌ getAttendanceByStudentId: FAILED - No records found");
            }
        } catch (Exception e) {
            System.err.println("❌ getAttendanceByStudentId: FAILED with exception:");
            e.printStackTrace();
        }
    }
    
    private static void testUpdateAttendance() {
        System.out.println("\n--- Testing updateAttendance ---");
        try {
            // Get the attendance record we created
            List<Attendance> attendances = attendanceDAO.getAttendanceByStudentId(1);
            if (attendances.isEmpty()) {
                System.out.println("❌ updateAttendance: FAILED - No records found to update");
                return;
            }
            
            Attendance attendance = attendances.get(0);
            String originalStatus = attendance.getStatus();
            System.out.println("Original status: " + originalStatus);
            
            // Update to a different status
            String newStatus = "Absent";
            attendance.setStatus(newStatus);
            
            boolean updated = attendanceDAO.updateAttendance(attendance);
            System.out.println("Update result: " + updated);
            
            // Verify the update
            Attendance updatedAttendance = attendanceDAO.getAttendanceById(attendance.getId());
            if (updatedAttendance != null) {
                System.out.println("New status: " + updatedAttendance.getStatus());
                
                if (updated && newStatus.equals(updatedAttendance.getStatus())) {
                    System.out.println("✅ updateAttendance: PASSED");
                } else {
                    System.out.println("❌ updateAttendance: FAILED - Status not updated correctly");
                }
            } else {
                System.out.println("❌ updateAttendance: FAILED - Could not retrieve updated record");
            }
            
        } catch (Exception e) {
            System.err.println("❌ updateAttendance: FAILED with exception:");
            e.printStackTrace();
        }
    }
}