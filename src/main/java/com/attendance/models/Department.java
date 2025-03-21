package com.attendance.models;

/**
 * Department model class representing Department table
 */
public class Department {
    private int departmentId;
    private String departmentName;
    
    // Constructors
    public Department() {
    }
    
    public Department(int departmentId, String departmentName) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }
    
    // Getters and setters
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
    
    @Override
    public String toString() {
        return "Department{" +
                "departmentId=" + departmentId +
                ", departmentName='" + departmentName + '\'' +
                '}';
    }
}