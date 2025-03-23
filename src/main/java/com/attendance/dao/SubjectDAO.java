package com.attendance.dao;

import com.attendance.models.Subject;
import java.util.List;

/**
 * Data Access Object interface for Subject entity
 */
public interface SubjectDAO {
    
    /**
     * Create a new subject in the database
     * 
     * @param subject The subject to create
     * @return The ID of the created subject
     */
    int createSubject(Subject subject);
    
    /**
     * Get a subject by ID
     * 
     * @param subjectId The ID of the subject
     * @return The subject, or null if not found
     */
    Subject getSubjectById(int subjectId);
    
    /**
     * Get subjects by department ID
     * 
     * @param departmentId The ID of the department
     * @return List of subjects in the specified department
     */
    List<Subject> getSubjectsByDepartmentId(int departmentId);
    
    /**
     * Get subjects by class ID
     * 
     * @param classId The ID of the class
     * @return List of subjects for the specified class
     */
    List<Subject> getSubjectsByClassId(int classId);
    
    /**
     * Alias for getSubjectsByClassId() to maintain compatibility with existing code
     * 
     * @param classId The ID of the class
     * @return List of subjects for the specified class
     */
    List<Subject> getSubjectsByClass(int classId);
    
    /**
     * Get subjects by teacher ID
     * 
     * @param teacherId The ID of the teacher
     * @return List of subjects taught by the specified teacher
     */
    List<Subject> getSubjectsByTeacherId(int teacherId);
    
    /**
     * Get subjects by semester
     * 
     * @param semester The semester (1-6)
     * @return List of subjects for the specified semester
     */
    List<Subject> getSubjectsBySemester(int semester);
    
    /**
     * Update an existing subject
     * 
     * @param subject The subject with updated information
     * @return true if successful, false otherwise
     */
    boolean updateSubject(Subject subject);
    
    /**
     * Delete a subject by ID
     * 
     * @param subjectId The ID of the subject to delete
     * @return true if successful, false otherwise
     */
    boolean deleteSubject(int subjectId);
    
    /**
     * Get all subjects
     * 
     * @return List of all subjects
     */
    List<Subject> getAllSubjects();
    
    /**
     * Get subjects by teacher ID and class ID
     * 
     * @param teacherId The ID of the teacher
     * @param classId The ID of the class
     * @return List of subjects taught by the specified teacher for the specified class
     */
    List<Subject> getSubjectsByTeacherIdAndClassId(int teacherId, int classId);
    
    /**
     * Assign a teacher to a subject
     * 
     * @param subjectId The ID of the subject
     * @param teacherId The ID of the teacher
     * @return true if successful, false otherwise
     */
    boolean assignTeacherToSubject(int subjectId, int teacherId);
    
    /**
     * Get a subject by code
     * 
     * @param code The code of the subject
     * @return The subject, or null if not found
     */
    Subject getSubjectByCode(String code);
    
    /**
     * Check if a subject with the given code exists
     * 
     * @param code The code to check
     * @return true if subject exists, false otherwise
     */
    boolean subjectExistsByCode(String code);
    
    /**
     * Assign a subject to a department and class
     * 
     * @param departmentId The ID of the department
     * @param classId The ID of the class
     * @param academicYear The academic year
     * @return true if successful, false otherwise
     */
    boolean assignSubjectToDepartmentClass(int departmentId, int classId, String academicYear);
    
    /**
     * Remove a subject from a department and class
     * 
     * @param departmentId The ID of the department
     * @param classId The ID of the class
     * @param academicYear The academic year
     * @return true if successful, false otherwise
     */
    boolean removeSubjectFromDepartmentClass(int departmentId, int classId, String academicYear);
}