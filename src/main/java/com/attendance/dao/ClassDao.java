package com.attendance.dao;

import com.attendance.models.Class;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for Class entities
 */
public interface ClassDao extends BaseDao<Class, Integer> {
    
    /**
     * Find classes by department
     * @param departmentId The department ID
     * @return List of classes in the specified department
     * @throws SQLException If a database error occurs
     */
    List<Class> findByDepartment(int departmentId) throws SQLException;
    
    /**
     * Find classes by class teacher
     * @param teacherId The user ID of the class teacher
     * @return List of classes with the specified class teacher
     * @throws SQLException If a database error occurs
     */
    List<Class> findByClassTeacher(int teacherId) throws SQLException;
    
    /**
     * Find a class by course, year, and department
     * @param course The course (e.g., "BSc", "BCA")
     * @param year The year (e.g., "FY", "SY", "TY")
     * @param departmentId The department ID
     * @return The class or null if not found
     * @throws SQLException If a database error occurs
     */
    Class findByCourseYearAndDepartment(String course, String year, int departmentId) throws SQLException;
}