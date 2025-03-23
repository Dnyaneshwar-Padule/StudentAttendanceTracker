package com.attendance.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents an attendance record in the attendance management system
 */
public class Attendance {
    private int id;
    private int studentId;
    private int classId;
    private int subjectId;
    private String subjectCode; // Subject code for reference
    private int semester; // Semester value
    private String academicYear; // Academic year
    private LocalDate date;
    private String status; // Present, Absent, Late, Leave
    private String remarks;
    private int markedById; // ID of the faculty who marked attendance
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String attendanceSession; // Morning, Afternoon
    private Integer leaveApplicationId; // Optional reference to a leave application if status is "Leave"
    
    /**
     * Default constructor
     */
    public Attendance() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with essential fields
     */
    public Attendance(int studentId, int classId, int subjectId, LocalDate date, String status) {
        this();
        this.studentId = studentId;
        this.classId = classId;
        this.subjectId = subjectId;
        this.date = date;
        this.status = status;
    }
    
    /**
     * Full constructor
     */
    public Attendance(int id, int studentId, int classId, int subjectId, String subjectCode,
                      int semester, String academicYear, LocalDate date, 
                      String status, String remarks, int markedById, LocalDateTime createdAt, 
                      LocalDateTime updatedAt, String attendanceSession, Integer leaveApplicationId) {
        this.id = id;
        this.studentId = studentId;
        this.classId = classId;
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.semester = semester;
        this.academicYear = academicYear;
        this.date = date;
        this.status = status;
        this.remarks = remarks;
        this.markedById = markedById;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.attendanceSession = attendanceSession;
        this.leaveApplicationId = leaveApplicationId;
    }

    // Getters and Setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Alias for getId() to maintain compatibility with existing code
     * 
     * @return The attendance ID
     */
    public int getAttendanceId() {
        return id;
    }
    
    /**
     * Alias for setId() to maintain compatibility with existing code
     * 
     * @param attendanceId The attendance ID
     */
    public void setAttendanceId(int attendanceId) {
        this.id = attendanceId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }
    
    public String getSubjectCode() {
        return subjectCode;
    }
    
    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }
    
    public int getSemester() {
        return semester;
    }
    
    public void setSemester(int semester) {
        this.semester = semester;
    }
    
    public String getAcademicYear() {
        return academicYear;
    }
    
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    /**
     * Alias for getDate() to maintain compatibility with existing code
     * Returns the attendance date
     * 
     * @return The attendance date
     */
    public LocalDate getAttendanceDate() {
        return date;
    }
    
    /**
     * Alias for setDate() to maintain compatibility with existing code
     * Sets the attendance date
     * 
     * @param attendanceDate The attendance date
     */
    public void setAttendanceDate(java.sql.Date attendanceDate) {
        if (attendanceDate != null) {
            this.date = attendanceDate.toLocalDate();
        } else {
            this.date = null;
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getMarkedById() {
        return markedById;
    }

    public void setMarkedById(int markedById) {
        this.markedById = markedById;
    }
    
    /**
     * Alias for setMarkedById() that accepts a String 
     * to maintain compatibility with existing code
     * 
     * @param markedBy The ID of the user who marked attendance, as a String
     */
    public void setMarkedBy(String markedBy) {
        if (markedBy != null && !markedBy.isEmpty()) {
            try {
                this.markedById = Integer.parseInt(markedBy);
            } catch (NumberFormatException e) {
                // Handle case where markedBy is not a valid integer
                this.markedById = 0;
            }
        }
    }
    
    /**
     * Alias for getMarkedById() to maintain compatibility with existing code
     * 
     * @return The ID of the user who marked attendance
     */
    public String getMarkedBy() {
        return String.valueOf(markedById);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAttendanceSession() {
        return attendanceSession;
    }

    public void setAttendanceSession(String attendanceSession) {
        this.attendanceSession = attendanceSession;
    }

    public Integer getLeaveApplicationId() {
        return leaveApplicationId;
    }

    public void setLeaveApplicationId(Integer leaveApplicationId) {
        this.leaveApplicationId = leaveApplicationId;
    }
    
    /**
     * Check if this attendance record indicates the student is present
     * 
     * @return true if the status is "Present", false otherwise
     */
    public boolean isPresent() {
        return "Present".equalsIgnoreCase(status);
    }
    
    /**
     * Check if this attendance record indicates the student is absent
     * 
     * @return true if the status is "Absent", false otherwise
     */
    public boolean isAbsent() {
        return "Absent".equalsIgnoreCase(status);
    }
    
    /**
     * Check if this attendance record indicates the student is late
     * 
     * @return true if the status is "Late", false otherwise
     */
    public boolean isLate() {
        return "Late".equalsIgnoreCase(status);
    }
    
    /**
     * Check if this attendance record indicates the student is on leave
     * 
     * @return true if the status is "Leave", false otherwise
     */
    public boolean isOnLeave() {
        return "Leave".equalsIgnoreCase(status);
    }
    
    /**
     * Update the updatedAt timestamp to the current time
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Attendance [id=" + id + ", studentId=" + studentId + ", classId=" + classId + ", subjectId=" + subjectId
                + ", date=" + date + ", status=" + status + ", attendanceSession=" + attendanceSession + "]";
    }
}