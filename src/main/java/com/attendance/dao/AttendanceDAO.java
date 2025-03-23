package com.attendance.dao;

import com.attendance.models.Attendance;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object interface for Attendance entity
 */
public interface AttendanceDAO {
    
    /**
     * Create a new attendance record
     * 
     * @param attendance The attendance record to create
     * @return The ID of the created attendance record
     */
    int createAttendance(Attendance attendance);
    
    /**
     * Get an attendance record by ID
     * 
     * @param attendanceId The ID of the attendance record
     * @return The attendance record, or null if not found
     */
    Attendance getAttendanceById(int attendanceId);
    
    /**
     * Get attendance records by student ID
     * 
     * @param studentId The ID of the student
     * @return List of attendance records for the student
     */
    List<Attendance> getAttendanceByStudentId(int studentId);
    
    /**
     * Get attendance records by student ID and date range
     * 
     * @param studentId The ID of the student
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return List of attendance records for the student within the date range
     */
    List<Attendance> getAttendanceByStudentIdAndDateRange(int studentId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get attendance records by class ID
     * 
     * @param classId The ID of the class
     * @return List of attendance records for the class
     */
    List<Attendance> getAttendanceByClassId(int classId);
    
    /**
     * Get attendance records by class ID and date
     * 
     * @param classId The ID of the class
     * @param date The date
     * @return List of attendance records for the class on the specified date
     */
    List<Attendance> getAttendanceByClassIdAndDate(int classId, LocalDate date);
    
    /**
     * Get attendance records by subject ID
     * 
     * @param subjectId The ID of the subject
     * @return List of attendance records for the subject
     */
    List<Attendance> getAttendanceBySubjectId(int subjectId);
    
    /**
     * Get attendance records by date
     * 
     * @param date The date
     * @return List of attendance records for the specified date
     */
    List<Attendance> getAttendanceByDate(LocalDate date);
    
    /**
     * Update an existing attendance record
     * 
     * @param attendance The attendance record with updated information
     * @return true if successful, false otherwise
     */
    boolean updateAttendance(Attendance attendance);
    
    /**
     * Delete an attendance record by ID
     * 
     * @param attendanceId The ID of the attendance record to delete
     * @return true if successful, false otherwise
     */
    boolean deleteAttendance(int attendanceId);
    
    /**
     * Get attendance records by class ID and date range
     * 
     * @param classId The ID of the class
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return List of attendance records for the class within the date range
     */
    List<Attendance> getAttendanceByClassIdAndDateRange(int classId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get attendance summary by student ID
     * 
     * @param studentId The ID of the student
     * @return Map containing attendance summary (present, absent, leave, total)
     */
    Map<String, Integer> getAttendanceSummaryByStudentId(int studentId);
    
    /**
     * Get attendance summary by student ID and date range
     * 
     * @param studentId The ID of the student
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return Map containing attendance summary (present, absent, leave, total)
     */
    Map<String, Integer> getAttendanceSummaryByStudentIdAndDateRange(int studentId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get attendance summary by class ID
     * 
     * @param classId The ID of the class
     * @return Map containing attendance summary (present, absent, leave, total)
     */
    Map<String, Integer> getAttendanceSummaryByClassId(int classId);
    
    /**
     * Get attendance summary by class ID and date range
     * 
     * @param classId The ID of the class
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return Map containing attendance summary (present, absent, leave, total)
     */
    Map<String, Integer> getAttendanceSummaryByClassIdAndDateRange(int classId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Check if an attendance record exists for a student on a specific date
     * 
     * @param studentId The ID of the student
     * @param date The date
     * @return true if an attendance record exists, false otherwise
     */
    boolean attendanceExists(int studentId, LocalDate date);
}