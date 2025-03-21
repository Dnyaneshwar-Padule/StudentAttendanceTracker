package com.attendance.models;

import java.sql.Timestamp;

/**
 * Model class for Department
 */
public class Department {
    private int departmentId;
    private String departmentName;
    private String departmentCode;
    private int hodId;  // Head of Department user ID
    private String description;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Constructor with required fields
    public Department(String departmentName, String departmentCode) {
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
    }
    
    // Full constructor
    public Department(int departmentId, String departmentName, String departmentCode, 
                     int hodId, String description, Timestamp createdAt, Timestamp updatedAt) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
        this.hodId = hodId;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Default constructor
    public Department() {
    }
    
    // Getters and Setters
    public int getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public String getDepartmentCode() {
        return departmentCode;
    }
    
    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }
    
    public int getHodId() {
        return hodId;
    }
    
    public void setHodId(int hodId) {
        this.hodId = hodId;
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
        return "Department{" +
                "departmentId=" + departmentId +
                ", departmentName='" + departmentName + '\'' +
                ", departmentCode='" + departmentCode + '\'' +
                ", hodId=" + hodId +
                '}';
    }
}