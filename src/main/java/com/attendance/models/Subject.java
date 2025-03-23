package com.attendance.models;

import java.time.LocalDateTime;

/**
 * Represents a subject in the attendance management system
 */
public class Subject {
    private int id;
    private String name;
    private String code;
    private String description;
    private int departmentId;
    private int classId;
    private int teacherId; // References user ID
    private int semester; // 1-6
    private int credits;
    private String academicYear; // e.g., "2023-24"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public Subject() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Simple constructor with just name and code
     * (for compatibility with SubjectServlet)
     */
    public Subject(String name, String code) {
        this();
        this.name = name;
        this.code = code;
    }
    
    /**
     * Constructor with essential fields
     */
    public Subject(String name, String code, int departmentId, int classId, int teacherId, int semester, String academicYear) {
        this();
        this.name = name;
        this.code = code;
        this.departmentId = departmentId;
        this.classId = classId;
        this.teacherId = teacherId;
        this.semester = semester;
        this.academicYear = academicYear;
    }
    
    /**
     * Full constructor
     */
    public Subject(int id, String name, String code, String description, int departmentId, 
                   int classId, int teacherId, int semester, int credits, String academicYear,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.departmentId = departmentId;
        this.classId = classId;
        this.teacherId = teacherId;
        this.semester = semester;
        this.credits = credits;
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
     * @return The subject ID
     */
    public int getSubjectId() {
        return id;
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
     * @return The subject name
     */
    public String getSubjectName() {
        return name;
    }
    
    /**
     * Alias for setName() to maintain compatibility with existing code
     * 
     * @param subjectName The subject name
     */
    public void setSubjectName(String subjectName) {
        this.name = subjectName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    /**
     * Alias for getCode() to maintain compatibility with existing code
     * 
     * @return The subject code
     */
    public String getSubjectCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
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
     * Get a human-readable display name for the subject
     * 
     * @return A formatted string like "CS101 - Introduction to Programming"
     */
    public String getDisplayName() {
        return code + " - " + name;
    }
    
    @Override
    public String toString() {
        return "Subject [id=" + id + ", name=" + name + ", code=" + code 
                + ", departmentId=" + departmentId + ", classId=" + classId 
                + ", teacherId=" + teacherId + ", semester=" + semester + "]";
    }
}