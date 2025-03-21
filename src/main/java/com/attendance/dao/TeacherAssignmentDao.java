package com.attendance.dao;

import com.attendance.models.TeacherAssignment;
import com.attendance.models.User;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for TeacherAssignment entities
 */
public interface TeacherAssignmentDao {
    
    /**
     * Find a teacher assignment by teacher ID, subject code, and class ID
     * @param teacherId The teacher ID
     * @param subjectCode The subject code
     * @param classId The class ID
     * @return The teacher assignment or null if not found
     * @throws SQLException If a database error occurs
     */
    TeacherAssignment findByTeacherSubjectAndClass(int teacherId, String subjectCode, int classId) throws SQLException;
    
    /**
     * Find all teacher assignments
     * @return List of all teacher assignments
     * @throws SQLException If a database error occurs
     */
    List<TeacherAssignment> findAll() throws SQLException;
    
    /**
     * Find teacher assignments by teacher
     * @param teacherId The teacher ID
     * @return List of teacher assignments for the specified teacher
     * @throws SQLException If a database error occurs
     */
    List<TeacherAssignment> findByTeacher(int teacherId) throws SQLException;
    
    /**
     * Find teacher assignments by subject
     * @param subjectCode The subject code
     * @return List of teacher assignments for the specified subject
     * @throws SQLException If a database error occurs
     */
    List<TeacherAssignment> findBySubject(String subjectCode) throws SQLException;
    
    /**
     * Find teacher assignments by class
     * @param classId The class ID
     * @return List of teacher assignments for the specified class
     * @throws SQLException If a database error occurs
     */
    List<TeacherAssignment> findByClass(int classId) throws SQLException;
    
    /**
     * Find teacher assignments by class and subject
     * @param classId The class ID
     * @param subjectCode The subject code
     * @return List of teacher assignments for the specified class and subject
     * @throws SQLException If a database error occurs
     */
    List<TeacherAssignment> findByClassAndSubject(int classId, String subjectCode) throws SQLException;
    
    /**
     * Save a new teacher assignment
     * @param teacherAssignment The teacher assignment to save
     * @return The saved teacher assignment
     * @throws SQLException If a database error occurs
     */
    TeacherAssignment save(TeacherAssignment teacherAssignment) throws SQLException;
    
    /**
     * Update an existing teacher assignment
     * @param teacherAssignment The teacher assignment to update
     * @return The updated teacher assignment
     * @throws SQLException If a database error occurs
     */
    TeacherAssignment update(TeacherAssignment teacherAssignment) throws SQLException;
    
    /**
     * Delete a teacher assignment
     * @param teacherId The teacher ID
     * @param subjectCode The subject code
     * @param classId The class ID
     * @return true if deleted, false otherwise
     * @throws SQLException If a database error occurs
     */
    boolean delete(int teacherId, String subjectCode, int classId) throws SQLException;
    
    /**
     * Get the class teacher for a specific class
     * @param classId The class ID
     * @return The class teacher or null if not found
     * @throws SQLException If a database error occurs
     */
    User getClassTeacher(int classId) throws SQLException;
    
    /**
     * Assign a teacher to a subject and class
     * @param assignment The teacher assignment
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    boolean assignTeacher(TeacherAssignment assignment) throws SQLException;
    
    /**
     * Update an existing teacher assignment
     * @param assignment The teacher assignment to update
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    boolean updateAssignment(TeacherAssignment assignment) throws SQLException;
    
    /**
     * Remove a teacher assignment
     * @param teacherId The teacher ID
     * @param subjectCode The subject code
     * @param classId The class ID
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    boolean removeAssignment(int teacherId, String subjectCode, int classId) throws SQLException;
}