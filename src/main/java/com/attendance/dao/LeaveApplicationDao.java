package com.attendance.dao;

import com.attendance.models.LeaveApplication;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for LeaveApplication entities
 */
public interface LeaveApplicationDao extends BaseDao<LeaveApplication, Integer> {
    
    /**
     * Find leave applications by student
     * @param studentId The student ID
     * @return List of leave applications for the specified student
     * @throws SQLException If a database error occurs
     */
    List<LeaveApplication> findByStudent(int studentId) throws SQLException;
    
    /**
     * Find leave applications by teacher (class teacher)
     * @param teacherId The teacher ID
     * @return List of leave applications for the specified teacher to review
     * @throws SQLException If a database error occurs
     */
    List<LeaveApplication> findByTeacher(int teacherId) throws SQLException;
    
    /**
     * Find leave applications by status
     * @param status The application status (PENDING, APPROVED, REJECTED)
     * @return List of leave applications with the specified status
     * @throws SQLException If a database error occurs
     */
    List<LeaveApplication> findByStatus(String status) throws SQLException;
    
    /**
     * Find active leave applications for a specific date
     * @param date The date to check for active leave applications
     * @return List of leave applications that are active for the specified date
     * @throws SQLException If a database error occurs
     */
    List<LeaveApplication> findActiveByDate(Date date) throws SQLException;
    
    /**
     * Find active leave applications for a specific student on a specific date
     * @param studentId The student ID
     * @param date The date to check
     * @return List of leave applications that are active for the specified student and date
     * @throws SQLException If a database error occurs
     */
    List<LeaveApplication> findActiveByStudentAndDate(int studentId, Date date) throws SQLException;
    
    /**
     * Update leave application status
     * @param applicationId The application ID
     * @param status The new status (APPROVED or REJECTED)
     * @param teacherId The teacher ID
     * @param teacherComments Comments from the teacher
     * @return true if updated, false otherwise
     * @throws SQLException If a database error occurs
     */
    boolean updateStatus(int applicationId, String status, int teacherId, String teacherComments) throws SQLException;
    
    /**
     * Check if a student has an active leave for a specific date
     * @param studentId The student ID
     * @param date The date to check
     * @return true if the student has an approved leave for the date, false otherwise
     * @throws SQLException If a database error occurs
     */
    boolean hasActiveLeave(int studentId, Date date) throws SQLException;
}