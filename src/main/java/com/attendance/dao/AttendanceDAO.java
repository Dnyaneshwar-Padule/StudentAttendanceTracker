package com.attendance.dao;

import com.attendance.models.Attendance;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object Interface for Attendance
 */
public interface AttendanceDAO {
    
    /**
     * Create a new attendance record
     * 
     * @param attendance the attendance record to create
     * @return the created attendance record with generated ID
     * @throws Exception if an error occurs
     */
    Attendance create(Attendance attendance) throws Exception;
    
    /**
     * Get an attendance record by ID
     * 
     * @param attendanceId the attendance ID
     * @return an Optional containing the attendance record if found
     * @throws Exception if an error occurs
     */
    Optional<Attendance> getById(int attendanceId) throws Exception;
    
    /**
     * Update an existing attendance record
     * 
     * @param attendance the attendance record to update
     * @return the updated attendance record
     * @throws Exception if an error occurs
     */
    Attendance update(Attendance attendance) throws Exception;
    
    /**
     * Delete an attendance record
     * 
     * @param attendanceId the ID of the attendance record to delete
     * @return true if deletion was successful
     * @throws Exception if an error occurs
     */
    boolean delete(int attendanceId) throws Exception;
    
    /**
     * Get all attendance records
     * 
     * @return list of all attendance records
     * @throws Exception if an error occurs
     */
    List<Attendance> getAll() throws Exception;
    
    /**
     * Get attendance records for a student
     * 
     * @param studentId the student ID
     * @return list of attendance records for the student
     * @throws Exception if an error occurs
     */
    List<Attendance> getByStudent(int studentId) throws Exception;
    
    /**
     * Get attendance records for a subject
     * 
     * @param subjectCode the subject code
     * @return list of attendance records for the subject
     * @throws Exception if an error occurs
     */
    List<Attendance> getBySubject(String subjectCode) throws Exception;
    
    /**
     * Get attendance records for a date
     * 
     * @param date the date
     * @return list of attendance records for the date
     * @throws Exception if an error occurs
     */
    List<Attendance> getByDate(Date date) throws Exception;
    
    /**
     * Get attendance records for a student and subject
     * 
     * @param studentId the student ID
     * @param subjectCode the subject code
     * @return list of attendance records for the student and subject
     * @throws Exception if an error occurs
     */
    List<Attendance> getByStudentAndSubject(int studentId, String subjectCode) throws Exception;
    
    /**
     * Get attendance records for a student, subject, and semester
     * 
     * @param studentId the student ID
     * @param subjectCode the subject code
     * @param semester the semester
     * @return list of attendance records for the student, subject, and semester
     * @throws Exception if an error occurs
     */
    List<Attendance> getByStudentSubjectAndSemester(int studentId, String subjectCode, String semester) throws Exception;
    
    /**
     * Get attendance records for a student in a semester
     * 
     * @param studentId the student ID
     * @param semester the semester
     * @param academicYear the academic year
     * @return list of attendance records for the student in the semester
     * @throws Exception if an error occurs
     */
    List<Attendance> getByStudentSemesterAndYear(int studentId, String semester, String academicYear) throws Exception;
    
    /**
     * Get attendance records for a specific status
     * 
     * @param status the attendance status (Present, Absent, Leave)
     * @return list of attendance records with the specified status
     * @throws Exception if an error occurs
     */
    List<Attendance> getByStatus(String status) throws Exception;
    
    /**
     * Get attendance by student, subject, and date (to check if already marked)
     * 
     * @param studentId the student ID
     * @param subjectCode the subject code
     * @param date the date
     * @return an Optional containing the attendance record if found
     * @throws Exception if an error occurs
     */
    Optional<Attendance> getByStudentSubjectAndDate(int studentId, String subjectCode, Date date) throws Exception;
    
    /**
     * Get attendance percentage for a student in a subject
     * 
     * @param studentId the student ID
     * @param subjectCode the subject code
     * @param semester the semester
     * @param academicYear the academic year
     * @return the attendance percentage (0-100)
     * @throws Exception if an error occurs
     */
    double getAttendancePercentage(int studentId, String subjectCode, String semester, String academicYear) throws Exception;
    
    /**
     * Mark attendance for multiple students
     * 
     * @param studentIds list of student IDs
     * @param subjectCode the subject code
     * @param date the date
     * @param status the attendance status
     * @param semester the semester
     * @param academicYear the academic year
     * @param markedBy who marked the attendance
     * @return number of records created
     * @throws Exception if an error occurs
     */
    int markAttendanceBulk(List<Integer> studentIds, String subjectCode, Date date, 
                         String status, String semester, String academicYear, String markedBy) throws Exception;
}