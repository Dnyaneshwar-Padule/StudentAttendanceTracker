package com.attendance.dao;

import com.attendance.models.Subject;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for Subject entities
 */
public interface SubjectDao extends BaseDao<Subject, String> {
    
    /**
     * Find a subject by its code
     * @param code The subject code
     * @return The subject with the given code, or null if not found
     * @throws SQLException If a database error occurs
     */
    Subject findByCode(String code) throws SQLException;
    
    /**
     * Find subjects by department
     * @param departmentId The department ID
     * @return List of subjects in the specified department
     * @throws SQLException If a database error occurs
     */
    List<Subject> findByDepartment(int departmentId) throws SQLException;
    
    /**
     * Find subjects by semester
     * @param semester The semester number (e.g., "1", "2")
     * @return List of subjects in the specified semester
     * @throws SQLException If a database error occurs
     */
    List<Subject> findBySemester(String semester) throws SQLException;
    
    /**
     * Find subjects by department and semester
     * @param departmentId The department ID
     * @param semester The semester number
     * @return List of subjects in the specified department and semester
     * @throws SQLException If a database error occurs
     */
    List<Subject> findByDepartmentAndSemester(int departmentId, String semester) throws SQLException;
    
    /**
     * Find subjects by department and class
     * @param departmentId The department ID
     * @param classId The class ID
     * @return List of subjects in the specified department and class
     * @throws SQLException If a database error occurs
     */
    List<Subject> findByDepartmentAndClass(int departmentId, int classId) throws SQLException;
    
    /**
     * Find subjects by class
     * @param classId The class ID
     * @return List of subjects taught in the specified class
     * @throws SQLException If a database error occurs
     */
    List<Subject> findByClassId(int classId) throws SQLException;
    
    /**
     * Search subjects by name or code
     * @param query The search query
     * @return List of subjects matching the query
     * @throws SQLException If a database error occurs
     */
    List<Subject> searchSubjects(String query) throws SQLException;
}