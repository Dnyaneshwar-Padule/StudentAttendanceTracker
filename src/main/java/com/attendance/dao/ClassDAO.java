package com.attendance.dao;

import com.attendance.models.Class;
import java.util.List;

/**
 * Data Access Object interface for Class entity
 */
public interface ClassDAO {
    
    /**
     * Create a new class in the database
     * 
     * @param classObj The class to create
     * @return The ID of the created class
     */
    int createClass(Class classObj);
    
    /**
     * Get a class by ID
     * 
     * @param classId The ID of the class
     * @return The class, or null if not found
     */
    Class getClassById(int classId);
    
    /**
     * Get classes by department ID
     * 
     * @param departmentId The ID of the department
     * @return List of classes in the specified department
     */
    List<Class> getClassesByDepartmentId(int departmentId);
    
    /**
     * Alias for getClassesByDepartmentId() to maintain compatibility with existing code
     * 
     * @param departmentId The ID of the department
     * @return List of classes in the specified department
     */
    List<Class> getClassesByDepartment(int departmentId);
    
    /**
     * Get classes by class teacher ID
     * 
     * @param classTeacherId The ID of the class teacher
     * @return List of classes taught by the specified teacher
     */
    List<Class> getClassesByClassTeacherId(int classTeacherId);
    
    /**
     * Get classes by academic year
     * 
     * @param academicYear The academic year (e.g., "2023-24")
     * @return List of classes for the specified academic year
     */
    List<Class> getClassesByAcademicYear(String academicYear);
    
    /**
     * Get classes by year
     * 
     * @param year The year (e.g., "FY", "SY", "TY")
     * @return List of classes for the specified year
     */
    List<Class> getClassesByYear(String year);
    
    /**
     * Get classes by semester
     * 
     * @param semester The semester (1-6)
     * @return List of classes for the specified semester
     */
    List<Class> getClassesBySemester(int semester);
    
    /**
     * Update an existing class
     * 
     * @param classObj The class with updated information
     * @return true if successful, false otherwise
     */
    boolean updateClass(Class classObj);
    
    /**
     * Delete a class by ID
     * 
     * @param classId The ID of the class to delete
     * @return true if successful, false otherwise
     */
    boolean deleteClass(int classId);
    
    /**
     * Get all classes
     * 
     * @return List of all classes
     */
    List<Class> getAllClasses();
    
    /**
     * Get classes by department ID and academic year
     * 
     * @param departmentId The ID of the department
     * @param academicYear The academic year (e.g., "2023-24")
     * @return List of classes in the specified department for the specified academic year
     */
    List<Class> getClassesByDepartmentIdAndAcademicYear(int departmentId, String academicYear);
}