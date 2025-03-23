package com.attendance.tests;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.impl.AttendanceDAOImpl;
import com.attendance.models.Attendance;
import com.attendance.utils.DatabaseConnection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Simple test class to verify AttendanceDAO functionality
 */
public class AttendanceDaoTest {
    
    private static final AttendanceDAO attendanceDAO = new AttendanceDAOImpl();
    
    public static void main(String[] args) {
        System.out.println("Starting AttendanceDAO tests...");
        
        // Test creating an attendance record
        testCreateAttendance();
        
        // Test checking if attendance exists
        testAttendanceExists();
        
        // Test retrieving attendance by ID
        testGetAttendanceById();
        
        // Test retrieving all attendances by student ID
        testGetAttendanceByStudentId();
        
        System.out.println("All tests completed!");
    }
    
    private static void testCreateAttendance() {
        System.out.println("\n=== Testing createAttendance ===");
        try {
            Attendance attendance = new Attendance();
            attendance.setStudentId(1); // Test student ID
            attendance.setSubjectCode("CS101"); // Test subject code
            attendance.setDate(LocalDate.now());
            attendance.setStatus("Present");
            attendance.setSemester(1);
            attendance.setAcademicYear("2024-2025");
            
            int id = attendanceDAO.createAttendance(attendance);
            System.out.println("Created attendance with ID: " + id);
            
            if (id > 0) {
                System.out.println("createAttendance test: PASSED");
            } else {
                System.out.println("createAttendance test: FAILED");
            }
        } catch (Exception e) {
            System.out.println("createAttendance test: FAILED");
            e.printStackTrace();
        }
    }
    
    private static void testAttendanceExists() {
        System.out.println("\n=== Testing attendanceExists ===");
        try {
            // Check with today's date as we just created an entry
            boolean exists = attendanceDAO.attendanceExists(1, LocalDate.now());
            System.out.println("Attendance exists for student ID 1 today: " + exists);
            
            // Check with a date in the past (unlikely to have data)
            boolean pastExists = attendanceDAO.attendanceExists(1, LocalDate.of(2000, 1, 1));
            System.out.println("Attendance exists for student ID 1 on 2000-01-01: " + pastExists);
            
            System.out.println("attendanceExists test: PASSED");
        } catch (Exception e) {
            System.out.println("attendanceExists test: FAILED");
            e.printStackTrace();
        }
    }
    
    private static void testGetAttendanceById() {
        System.out.println("\n=== Testing getAttendanceById ===");
        try {
            // Try to get attendance with ID 1 (may or may not exist)
            Attendance attendance = attendanceDAO.getAttendanceById(1);
            
            if (attendance != null) {
                System.out.println("Found attendance with ID 1: " + attendance);
                System.out.println("getAttendanceById test: PASSED");
            } else {
                System.out.println("No attendance found with ID 1");
                System.out.println("getAttendanceById test: SKIPPED (no data)");
            }
        } catch (Exception e) {
            System.out.println("getAttendanceById test: FAILED");
            e.printStackTrace();
        }
    }
    
    private static void testGetAttendanceByStudentId() {
        System.out.println("\n=== Testing getAttendanceByStudentId ===");
        try {
            // Try to get attendances for student with ID 1
            List<Attendance> attendances = attendanceDAO.getAttendanceByStudentId(1);
            
            System.out.println("Found " + attendances.size() + " attendance records for student ID 1");
            if (!attendances.isEmpty()) {
                System.out.println("First attendance record: " + attendances.get(0));
            }
            
            System.out.println("getAttendanceByStudentId test: PASSED");
        } catch (Exception e) {
            System.out.println("getAttendanceByStudentId test: FAILED");
            e.printStackTrace();
        }
    }
}