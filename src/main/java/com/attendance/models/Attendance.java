package com.attendance.models;

import java.sql.Date;

/**
 * Attendance model class representing Attendance table
 */
public class Attendance {
    private int attendanceId;
    private Date attendanceDate;
    private String subjectCode;
    private int studentId;
    private String semester;
    private String academicYear;
    private String status;
    
    // Additional fields for joining
    private User student;
    private Subject subject;
    
    // Constructors
    public Attendance() {
    }
    
    public Attendance(int attendanceId, Date attendanceDate, String subjectCode, int studentId, 
                      String semester, String academicYear, String status) {
        this.attendanceId = attendanceId;
        this.attendanceDate = attendanceDate;
        this.subjectCode = subjectCode;
        this.studentId = studentId;
        this.semester = semester;
        this.academicYear = academicYear;
        this.status = status;
    }
    
    // Getters and setters
    public int getAttendanceId() {
        return attendanceId;
    }
    
    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }
    
    public Date getAttendanceDate() {
        return attendanceDate;
    }
    
    public void setAttendanceDate(Date attendanceDate) {
        this.attendanceDate = attendanceDate;
    }
    
    public String getSubjectCode() {
        return subjectCode;
    }
    
    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }
    
    public int getStudentId() {
        return studentId;
    }
    
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
    
    public String getSemester() {
        return semester;
    }
    
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    public String getAcademicYear() {
        return academicYear;
    }
    
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public User getStudent() {
        return student;
    }
    
    public void setStudent(User student) {
        this.student = student;
        if (student != null) {
            this.studentId = student.getUserId();
        }
    }
    
    public Subject getSubject() {
        return subject;
    }
    
    public void setSubject(Subject subject) {
        this.subject = subject;
        if (subject != null) {
            this.subjectCode = subject.getSubjectCode();
        }
    }
    
    @Override
    public String toString() {
        return "Attendance{" +
                "attendanceId=" + attendanceId +
                ", attendanceDate=" + attendanceDate +
                ", subjectCode='" + subjectCode + '\'' +
                ", studentId=" + studentId +
                ", semester='" + semester + '\'' +
                ", academicYear='" + academicYear + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}