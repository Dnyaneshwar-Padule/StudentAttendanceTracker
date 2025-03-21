package com.attendance.models;

/**
 * TeacherAssignment model class representing TeacherAssignment table
 */
public class TeacherAssignment {
    private int teacherId;
    private String subjectCode;
    private int classId;
    private String assignmentType;
    
    // Additional fields for joining
    private User teacher;
    private Subject subject;
    private Class classObj;
    
    // Constructors
    public TeacherAssignment() {
    }
    
    public TeacherAssignment(int teacherId, String subjectCode, int classId, String assignmentType) {
        this.teacherId = teacherId;
        this.subjectCode = subjectCode;
        this.classId = classId;
        this.assignmentType = assignmentType;
    }
    
    // Getters and setters
    public int getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }
    
    public String getSubjectCode() {
        return subjectCode;
    }
    
    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }
    
    public int getClassId() {
        return classId;
    }
    
    public void setClassId(int classId) {
        this.classId = classId;
    }
    
    public String getAssignmentType() {
        return assignmentType;
    }
    
    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }
    
    public User getTeacher() {
        return teacher;
    }
    
    public void setTeacher(User teacher) {
        this.teacher = teacher;
        if (teacher != null) {
            this.teacherId = teacher.getUserId();
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
        return "TeacherAssignment{" +
                "teacherId=" + teacherId +
                ", subjectCode='" + subjectCode + '\'' +
                ", classId=" + classId +
                ", assignmentType='" + assignmentType + '\'' +
                '}';
    }
}