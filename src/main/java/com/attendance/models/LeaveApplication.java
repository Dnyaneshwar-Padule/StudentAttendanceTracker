package com.attendance.models;

import java.sql.Date;

/**
 * Model class representing a student leave application
 */
public class LeaveApplication {
    private int applicationId;       // Primary key
    private int studentId;           // Student who applied for leave
    private Date fromDate;           // Leave start date
    private Date toDate;             // Leave end date
    private String reason;           // Reason for leave
    private String status;           // Status: PENDING, APPROVED, REJECTED
    private int teacherId;           // Class teacher who reviews the application
    private Date applicationDate;    // Date when the application was submitted
    private Date reviewDate;         // Date when the application was reviewed
    private String teacherComments;  // Comments from the teacher on approval/rejection
    
    /**
     * Default constructor
     */
    public LeaveApplication() {
    }
    
    /**
     * Parameterized constructor
     * @param applicationId The leave application ID
     * @param studentId The student ID
     * @param fromDate The start date of leave
     * @param toDate The end date of leave
     * @param reason The reason for leave
     * @param status The application status
     * @param teacherId The reviewer teacher ID
     * @param applicationDate The application submission date
     * @param reviewDate The application review date
     * @param teacherComments The teacher's comments
     */
    public LeaveApplication(int applicationId, int studentId, Date fromDate, Date toDate, String reason, 
                            String status, int teacherId, Date applicationDate, Date reviewDate, 
                            String teacherComments) {
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.reason = reason;
        this.status = status;
        this.teacherId = teacherId;
        this.applicationDate = applicationDate;
        this.reviewDate = reviewDate;
        this.teacherComments = teacherComments;
    }
    
    /**
     * Get the application ID
     * @return The application ID
     */
    public int getApplicationId() {
        return applicationId;
    }
    
    /**
     * Set the application ID
     * @param applicationId The application ID
     */
    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }
    
    /**
     * Get the student ID
     * @return The student ID
     */
    public int getStudentId() {
        return studentId;
    }
    
    /**
     * Set the student ID
     * @param studentId The student ID
     */
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
    
    /**
     * Get the leave start date
     * @return The leave start date
     */
    public Date getFromDate() {
        return fromDate;
    }
    
    /**
     * Set the leave start date
     * @param fromDate The leave start date
     */
    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }
    
    /**
     * Get the leave end date
     * @return The leave end date
     */
    public Date getToDate() {
        return toDate;
    }
    
    /**
     * Set the leave end date
     * @param toDate The leave end date
     */
    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }
    
    /**
     * Get the reason for leave
     * @return The reason for leave
     */
    public String getReason() {
        return reason;
    }
    
    /**
     * Set the reason for leave
     * @param reason The reason for leave
     */
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    /**
     * Get the application status
     * @return The application status
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * Set the application status
     * @param status The application status
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Get the teacher ID
     * @return The teacher ID
     */
    public int getTeacherId() {
        return teacherId;
    }
    
    /**
     * Set the teacher ID
     * @param teacherId The teacher ID
     */
    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }
    
    /**
     * Get the application submission date
     * @return The application submission date
     */
    public Date getApplicationDate() {
        return applicationDate;
    }
    
    /**
     * Set the application submission date
     * @param applicationDate The application submission date
     */
    public void setApplicationDate(Date applicationDate) {
        this.applicationDate = applicationDate;
    }
    
    /**
     * Get the application review date
     * @return The application review date
     */
    public Date getReviewDate() {
        return reviewDate;
    }
    
    /**
     * Set the application review date
     * @param reviewDate The application review date
     */
    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }
    
    /**
     * Get the teacher's comments
     * @return The teacher's comments
     */
    public String getTeacherComments() {
        return teacherComments;
    }
    
    /**
     * Set the teacher's comments
     * @param teacherComments The teacher's comments
     */
    public void setTeacherComments(String teacherComments) {
        this.teacherComments = teacherComments;
    }
    
    /**
     * Check if this leave application is active for a given date
     * @param date The date to check
     * @return true if this is an approved leave application for the date, false otherwise
     */
    public boolean isActiveForDate(Date date) {
        return status != null && 
               status.equals("APPROVED") &&
               (date.compareTo(fromDate) >= 0 && date.compareTo(toDate) <= 0);
    }
    
    @Override
    public String toString() {
        return "LeaveApplication{" +
                "applicationId=" + applicationId +
                ", studentId=" + studentId +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                ", reason='" + reason + '\'' +
                ", status='" + status + '\'' +
                ", teacherId=" + teacherId +
                ", applicationDate=" + applicationDate +
                ", reviewDate=" + reviewDate +
                ", teacherComments='" + teacherComments + '\'' +
                '}';
    }
}