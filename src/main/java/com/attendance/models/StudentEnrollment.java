package com.attendance.models;

import java.sql.Date;

/**
 * StudentEnrollment model class representing StudentEnrollment table
 */
public class StudentEnrollment {
    private String enrollmentId;
    private int userId;
    private int classId;
    private String academicYear;
    private String enrollmentStatus;
    private Date enrollmentDate;
    private String semester;
    
    // Additional fields for joining
    private User user;
    private Class classObj;
    
    // Constructors
    public StudentEnrollment() {
    }
    
    public StudentEnrollment(String enrollmentId, int userId, int classId, String academicYear, String enrollmentStatus) {
        this.enrollmentId = enrollmentId;
        this.userId = userId;
        this.classId = classId;
        this.academicYear = academicYear;
        this.enrollmentStatus = enrollmentStatus;
    }
    
    public StudentEnrollment(String enrollmentId, int userId, int classId, String academicYear, String enrollmentStatus, Date enrollmentDate) {
        this.enrollmentId = enrollmentId;
        this.userId = userId;
        this.classId = classId;
        this.academicYear = academicYear;
        this.enrollmentStatus = enrollmentStatus;
        this.enrollmentDate = enrollmentDate;
    }
    
    public StudentEnrollment(String enrollmentId, int userId, int classId, String academicYear, String enrollmentStatus, Date enrollmentDate, String semester) {
        this.enrollmentId = enrollmentId;
        this.userId = userId;
        this.classId = classId;
        this.academicYear = academicYear;
        this.enrollmentStatus = enrollmentStatus;
        this.enrollmentDate = enrollmentDate;
        this.semester = semester;
    }
    
    // Getters and setters
    public String getEnrollmentId() {
        return enrollmentId;
    }
    
    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    /**
     * Get student ID (alias for getUserId for better semantic meaning)
     * @return the user ID representing the student
     */
    public int getStudentId() {
        return userId;
    }
    
    public int getClassId() {
        return classId;
    }
    
    public void setClassId(int classId) {
        this.classId = classId;
    }
    
    public String getAcademicYear() {
        return academicYear;
    }
    
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
    
    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }
    
    public void setEnrollmentStatus(String enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }
    
    /**
     * Get enrollment date
     * @return the enrollment date
     */
    public Date getEnrollmentDate() {
        return enrollmentDate;
    }
    
    /**
     * Set enrollment date
     * @param enrollmentDate the enrollment date to set
     */
    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
    
    /**
     * Get status (alias for getEnrollmentStatus)
     * @return the enrollment status
     */
    public String getStatus() {
        return enrollmentStatus;
    }
    
    /**
     * Set status (alias for setEnrollmentStatus)
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.enrollmentStatus = status;
    }
    
    /**
     * Set student ID (alias for setUserId)
     * @param studentId the student ID to set
     */
    public void setStudentId(int studentId) {
        this.userId = studentId;
    }
    
    /**
     * Get semester
     * @return the semester
     */
    public String getSemester() {
        return semester;
    }
    
    /**
     * Set semester
     * @param semester the semester to set
     */
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getUserId();
        }
    }
    
    public Class getClassObj() {
        return classObj;
    }
    
    public void setClassObj(Class classObj) {
        this.classObj = classObj;
        if (classObj != null) {
            this.classId = classObj.getClassId();
        }
    }
    
    @Override
    public String toString() {
        return "StudentEnrollment{" +
                "enrollmentId='" + enrollmentId + '\'' +
                ", userId=" + userId +
                ", classId=" + classId +
                ", academicYear='" + academicYear + '\'' +
                ", enrollmentStatus='" + enrollmentStatus + '\'' +
                ", enrollmentDate=" + enrollmentDate +
                ", semester='" + semester + '\'' +
                '}';
    }
}