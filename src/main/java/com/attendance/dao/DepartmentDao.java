package com.attendance.dao;

import com.attendance.models.Department;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for Department entities
 */
public interface DepartmentDao extends BaseDao<Department, Integer> {
    
    /**
     * Find a department by name
     * @param name The department name
     * @return The department or null if not found
     * @throws SQLException If a database error occurs
     */
    Department findByName(String name) throws SQLException;
    
    /**
     * Find departments by HOD (Head of Department)
     * @param hodId The user ID of the HOD
     * @return List of departments with the specified HOD
     * @throws SQLException If a database error occurs
     */
    List<Department> findByHod(int hodId) throws SQLException;
    
    /**
     * Find department by department code
     * @param code The department code
     * @return The department or null if not found
     * @throws SQLException If a database error occurs
     */
    Department findByCode(String code) throws SQLException;
}