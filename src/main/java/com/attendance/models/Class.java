package com.attendance.models;

/**
 * Class model representing Classes table
 */
public class Class {
    private int classId;
    private String className;
    private int departmentId;
    private Department department; // For joining with Department
    
    // Constructors
    public Class() {
    }
    
    public Class(int classId, String className, int departmentId) {
        this.classId = classId;
        this.className = className;
        this.departmentId = departmentId;
    }
    
    // Getters and setters
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
    
    public Department getDepartment() {
        return department;
    }
    
    public void setDepartment(Department department) {
        this.department = department;
        if (department != null) {
            this.departmentId = department.getDepartmentId();
        }
    }
    
    @Override
    public String toString() {
        return "Class{" +
                "classId=" + classId +
                ", className='" + className + '\'' +
                ", departmentId=" + departmentId +
                '}';
    }
}