package com.attendance.models;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model class for Attendance
 */
public class Attendance {
    private int attendanceId;
    private int studentId;
    private String subjectCode;
    private Date attendanceDate;
    private String status; // Present, Absent, Leave
    private String semester;
    private String academicYear;
    private String markedBy;
    private String remarks;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Additional fields for display purposes (not stored in database)
    private String studentName;
    private String subjectName;

    /**
     * Default constructor
     */
    public Attendance() {
    }

    /**
     * Constructor with main fields
     * 
     * @param studentId the student ID
     * @param subjectCode the subject code
     * @param attendanceDate the attendance date
     * @param status the attendance status
     * @param semester the semester
     * @param academicYear the academic year
     * @param markedBy who marked the attendance
     */
    public Attendance(int studentId, String subjectCode, Date attendanceDate, String status, 
                    String semester, String academicYear, String markedBy) {
        this.studentId = studentId;
        this.subjectCode = subjectCode;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.semester = semester;
        this.academicYear = academicYear;
        this.markedBy = markedBy;
    }

    /**
     * Full constructor
     * 
     * @param attendanceId the attendance ID
     * @param studentId the student ID
     * @param subjectCode the subject code
     * @param attendanceDate the attendance date
     * @param status the attendance status
     * @param semester the semester
     * @param academicYear the academic year
     * @param markedBy who marked the attendance
     * @param remarks additional remarks
     * @param createdAt when the record was created
     * @param updatedAt when the record was last updated
     */
    public Attendance(int attendanceId, int studentId, String subjectCode, Date attendanceDate, 
                    String status, String semester, String academicYear, String markedBy, 
                    String remarks, Timestamp createdAt, Timestamp updatedAt) {
        this.attendanceId = attendanceId;
        this.studentId = studentId;
        this.subjectCode = subjectCode;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.semester = semester;
        this.academicYear = academicYear;
        this.markedBy = markedBy;
        this.remarks = remarks;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * @return the attendanceId
     */
    public int getAttendanceId() {
        return attendanceId;
    }

    /**
     * @param attendanceId the attendanceId to set
     */
    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    /**
     * @return the studentId
     */
    public int getStudentId() {
        return studentId;
    }

    /**
     * @param studentId the studentId to set
     */
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    /**
     * @return the subjectCode
     */
    public String getSubjectCode() {
        return subjectCode;
    }

    /**
     * @param subjectCode the subjectCode to set
     */
    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    /**
     * @return the attendanceDate
     */
    public Date getAttendanceDate() {
        return attendanceDate;
    }

    /**
     * @param attendanceDate the attendanceDate to set
     */
    public void setAttendanceDate(Date attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the semester
     */
    public String getSemester() {
        return semester;
    }

    /**
     * @param semester the semester to set
     */
    public void setSemester(String semester) {
        this.semester = semester;
    }

    /**
     * @return the academicYear
     */
    public String getAcademicYear() {
        return academicYear;
    }

    /**
     * @param academicYear the academicYear to set
     */
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    /**
     * @return the markedBy
     */
    public String getMarkedBy() {
        return markedBy;
    }

    /**
     * @param markedBy the markedBy to set
     */
    public void setMarkedBy(String markedBy) {
        this.markedBy = markedBy;
    }

    /**
     * @return the remarks
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * @param remarks the remarks to set
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * @return the createdAt
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt the createdAt to set
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return the updatedAt
     */
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @param updatedAt the updatedAt to set
     */
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * @return the studentName
     */
    public String getStudentName() {
        return studentName;
    }

    /**
     * @param studentName the studentName to set
     */
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    /**
     * @return the subjectName
     */
    public String getSubjectName() {
        return subjectName;
    }

    /**
     * @param subjectName the subjectName to set
     */
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
    
    /**
     * Check if the status is "Present"
     * 
     * @return true if the status is "Present"
     */
    public boolean isPresent() {
        return "Present".equalsIgnoreCase(status);
    }
    
    /**
     * Check if the status is "Absent"
     * 
     * @return true if the status is "Absent"
     */
    public boolean isAbsent() {
        return "Absent".equalsIgnoreCase(status);
    }
    
    /**
     * Check if the status is "Leave"
     * 
     * @return true if the status is "Leave"
     */
    public boolean isOnLeave() {
        return "Leave".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Attendance{" + "attendanceId=" + attendanceId + ", studentId=" + studentId + 
               ", subjectCode=" + subjectCode + ", attendanceDate=" + attendanceDate + 
               ", status=" + status + ", semester=" + semester + ", academicYear=" + academicYear + 
               ", markedBy=" + markedBy + '}';
    }
}