package com.attendance.dao;

import com.attendance.models.Department;
import java.util.List;

/**
 * Data Access Object interface for Department entity
 */
public interface DepartmentDAO {
    
    /**
     * Create a new department in the database
     * 
     * @param department The department to create
     * @return The ID of the created department
     */
    int createDepartment(Department department);
    
    /**
     * Get a department by ID
     * 
     * @param departmentId The ID of the department
     * @return The department, or null if not found
     */
    Department getDepartmentById(int departmentId);
    
    /**
     * Get a department by name
     * 
     * @param name The name of the department
     * @return The department, or null if not found
     */
    Department getDepartmentByName(String name);
    
    /**
     * Update an existing department
     * 
     * @param department The department with updated information
     * @return true if successful, false otherwise
     */
    boolean updateDepartment(Department department);
    
    /**
     * Delete a department by ID
     * 
     * @param departmentId The ID of the department to delete
     * @return true if successful, false otherwise
     */
    boolean deleteDepartment(int departmentId);
    
    /**
     * Get all departments
     * 
     * @return List of all departments
     */
    List<Department> getAllDepartments();
    
    /**
     * Get departments by HOD ID
     * 
     * @param hodId The ID of the Head of Department
     * @return List of departments headed by the specified HOD
     */
    List<Department> getDepartmentsByHodId(int hodId);
}