package com.attendance.models;

import java.sql.Timestamp;

/**
 * Model class for Class
 */
public class Class {
    private int classId;
    private String className;
    private int departmentId;
    private int classTeacherId;
    private int maxStudents;
    private String description;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Constructor with required fields
    public Class(String className, int departmentId) {
        this.className = className;
        this.departmentId = departmentId;
    }
    
    // Full constructor
    public Class(int classId, String className, int departmentId, int classTeacherId,
               int maxStudents, String description, Timestamp createdAt, Timestamp updatedAt) {
        this.classId = classId;
        this.className = className;
        this.departmentId = departmentId;
        this.classTeacherId = classTeacherId;
        this.maxStudents = maxStudents;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Default constructor
    public Class() {
    }
    
    // Getters and Setters
    public int getClassId() {
        return classId;
    }
    
    public void setClassId(int classId) {
        this.classId = classId;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public int getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }
    
    public int getClassTeacherId() {
        return classTeacherId;
    }
    
    public void setClassTeacherId(int classTeacherId) {
        this.classTeacherId = classTeacherId;
    }
    
    public int getMaxStudents() {
        return maxStudents;
    }
    
    public void setMaxStudents(int maxStudents) {
        this.maxStudents = maxStudents;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Class{" +
                "classId=" + classId +
                ", className='" + className + '\'' +
                ", departmentId=" + departmentId +
                ", classTeacherId=" + classTeacherId +
                '}';
    }
}