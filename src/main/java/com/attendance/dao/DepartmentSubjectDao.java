package com.attendance.dao;

import com.attendance.models.DepartmentSubject;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for DepartmentSubject entities
 */
public interface DepartmentSubjectDao {
    
    /**
     * Find a department subject by department ID and subject code
     * @param departmentId The department ID
     * @param subjectCode The subject code
     * @return The department subject or null if not found
     * @throws SQLException If a database error occurs
     */
    DepartmentSubject findByDepartmentAndSubject(int departmentId, String subjectCode) throws SQLException;
    
    /**
     * Find all department subjects
     * @return List of all department subjects
     * @throws SQLException If a database error occurs
     */
    List<DepartmentSubject> findAll() throws SQLException;
    
    /**
     * Find department subjects by department
     * @param departmentId The department ID
     * @return List of department subjects for the specified department
     * @throws SQLException If a database error occurs
     */
    List<DepartmentSubject> findByDepartment(int departmentId) throws SQLException;
    
    /**
     * Find departments by subject
     * @param subjectCode The subject code
     * @return List of department subjects for the specified subject
     * @throws SQLException If a database error occurs
     */
    List<DepartmentSubject> findBySubject(String subjectCode) throws SQLException;
    
    /**
     * Find department subjects by semester
     * @param semester The semester
     * @return List of department subjects for the specified semester
     * @throws SQLException If a database error occurs
     */
    List<DepartmentSubject> findBySemester(String semester) throws SQLException;
    
    /**
     * Find department subjects by department and semester
     * @param departmentId The department ID
     * @param semester The semester
     * @return List of department subjects for the specified department and semester
     * @throws SQLException If a database error occurs
     */
    List<DepartmentSubject> findByDepartmentAndSemester(int departmentId, String semester) throws SQLException;
    
    /**
     * Save a new department subject
     * @param departmentSubject The department subject to save
     * @return The saved department subject
     * @throws SQLException If a database error occurs
     */
    DepartmentSubject save(DepartmentSubject departmentSubject) throws SQLException;
    
    /**
     * Update an existing department subject
     * @param departmentSubject The department subject to update
     * @return The updated department subject
     * @throws SQLException If a database error occurs
     */
    DepartmentSubject update(DepartmentSubject departmentSubject) throws SQLException;
    
    /**
     * Delete a department subject
     * @param departmentId The department ID
     * @param subjectCode The subject code
     * @return true if deleted, false otherwise
     * @throws SQLException If a database error occurs
     */
    boolean delete(int departmentId, String subjectCode) throws SQLException;
}