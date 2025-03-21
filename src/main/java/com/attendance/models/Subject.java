package com.attendance.models;

import java.sql.Timestamp;

/**
 * Model class for Subject
 */
public class Subject {
    private String subjectCode;
    private String subjectName;
    private String description;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Constructor with required fields
    public Subject(String subjectCode, String subjectName) {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
    }
    
    // Full constructor
    public Subject(String subjectCode, String subjectName, String description, 
                 Timestamp createdAt, Timestamp updatedAt) {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Default constructor
    public Subject() {
    }
    
    // Getters and Setters
    public String getSubjectCode() {
        return subjectCode;
    }
    
    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }
    
    public String getSubjectName() {
        return subjectName;
    }
    
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
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
        return "Subject{" +
                "subjectCode='" + subjectCode + '\'' +
                ", subjectName='" + subjectName + '\'' +
                '}';
    }
}