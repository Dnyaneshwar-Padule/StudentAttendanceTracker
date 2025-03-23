package com.attendance.models;

import java.time.LocalDateTime;

/**
 * Represents a department in the attendance management system
 */
public class Department {
    private int id;
    private String name;
    private int hodId; // Head of Department ID (references users.id)
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public Department() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with essential fields
     */
    public Department(String name, int hodId) {
        this();
        this.name = name;
        this.hodId = hodId;
    }
    
    /**
     * Full constructor
     */
    public Department(int id, String name, int hodId, String description, 
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.hodId = hodId;
        this.description = description;
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
     * @return The department ID
     */
    public int getDepartmentId() {
        return id;
    }
    
    /**
     * Alias for setId() to maintain compatibility with existing code
     * 
     * @param departmentId The department ID
     */
    public void setDepartmentId(int departmentId) {
        this.id = departmentId;
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
     * @return The department name
     */
    public String getDepartmentName() {
        return name;
    }
    
    /**
     * Alias for setName() to maintain compatibility with existing code
     * 
     * @param departmentName The department name
     */
    public void setDepartmentName(String departmentName) {
        this.name = departmentName;
    }
    
    // Department code field and accessors (needed by DepartmentController)
    private String code;
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    /**
     * Alias for getCode() to maintain compatibility with existing code
     * 
     * @return The department code
     */
    public String getDepartmentCode() {
        return code;
    }
    
    /**
     * Alias for setCode() to maintain compatibility with existing code
     * 
     * @param departmentCode The department code
     */
    public void setDepartmentCode(String departmentCode) {
        this.code = departmentCode;
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
    
    @Override
    public String toString() {
        return "Department [id=" + id + ", name=" + name + ", hodId=" + hodId + "]";
    }
}