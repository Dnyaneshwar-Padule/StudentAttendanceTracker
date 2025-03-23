package com.attendance.models;

import java.time.LocalDateTime;

/**
 * Represents a class in the attendance management system
 * (Named Class despite potential conflict with java.lang.Class because the DAO layer
 * and existing code references expect this naming)
 */
public class Class {
    private int id;
    private String name;
    private String year; // FY, SY, TY
    private int semester; // 1-6
    private int departmentId;
    private int classTeacherId; // References user ID
    private String academicYear; // e.g., "2023-24"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public Class() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with essential fields
     */
    public Class(String name, String year, int semester, int departmentId, int classTeacherId, String academicYear) {
        this();
        this.name = name;
        this.year = year;
        this.semester = semester;
        this.departmentId = departmentId;
        this.classTeacherId = classTeacherId;
        this.academicYear = academicYear;
    }
    
    /**
     * Full constructor
     */
    public Class(int id, String name, String year, int semester, int departmentId, 
                  int classTeacherId, String academicYear, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.semester = semester;
        this.departmentId = departmentId;
        this.classTeacherId = classTeacherId;
        this.academicYear = academicYear;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
     * @return The class ID
     */
    public int getClassId() {
        return id;
    }
    
    /**
     * Alias for setId() to maintain compatibility with existing code
     * 
     * @param classId The class ID
     */
    public void setClassId(int classId) {
        this.id = classId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Alias for getName() to maintain compatibility with existing code
     * 
     * @return The class name
     */
    public String getClassName() {
        return name;
    }
    
    /**
     * Alias for getName() to maintain compatibility with existing code
     * 
     * @return The course name (class name)
     */
    public String getCourse() {
        return name;
    }
    
    /**
     * Alias for setName() to maintain compatibility with existing code
     * 
     * @param course The course name (class name)
     */
    public void setCourse(String course) {
        this.name = course;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
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

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
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
    
    /**
     * Update the updatedAt timestamp to the current time
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Get a human-readable display name for the class
     * 
     * @return A formatted string like "FY BCA (2023-24)"
     */
    public String getDisplayName() {
        return year + " " + name + " (" + academicYear + ")";
    }
    
    @Override
    public String toString() {
        return "Class [id=" + id + ", name=" + name + ", year=" + year + ", semester=" + semester 
                + ", departmentId=" + departmentId + ", academicYear=" + academicYear + "]";
    }
}