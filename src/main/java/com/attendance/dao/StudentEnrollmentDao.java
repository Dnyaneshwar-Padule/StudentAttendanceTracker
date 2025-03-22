package com.attendance.dao;

import com.attendance.models.StudentEnrollment;
import com.attendance.models.User;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for StudentEnrollment entities
 */
public interface StudentEnrollmentDao extends BaseDao<StudentEnrollment, Integer> {
    
    /**
     * Find enrollments by student
     * @param studentId The student ID
     * @return List of enrollments for the specified student
     * @throws SQLException If a database error occurs
     */
    List<StudentEnrollment> findByStudent(int studentId) throws SQLException;
    
    /**
     * Find enrollments by class
     * @param classId The class ID
     * @return List of enrollments for the specified class
     * @throws SQLException If a database error occurs
     */
    List<StudentEnrollment> findByClass(int classId) throws SQLException;
    
    /**
     * Find enrollments by academic year
     * @param academicYear The academic year
     * @return List of enrollments for the specified academic year
     * @throws SQLException If a database error occurs
     */
    List<StudentEnrollment> findByAcademicYear(String academicYear) throws SQLException;
    
    /**
     * Find enrollment by student, class, and academic year
     * @param studentId The student ID
     * @param classId The class ID
     * @param academicYear The academic year
     * @return The enrollment or null if not found
     * @throws SQLException If a database error occurs
     */
    StudentEnrollment findByStudentClassAndYear(int studentId, int classId, String academicYear) throws SQLException;
    
    /**
     * Find current enrollment for a student (latest academic year)
     * @param studentId The student ID
     * @return The current enrollment or null if not found
     * @throws SQLException If a database error occurs
     */
    StudentEnrollment findCurrentEnrollment(int studentId) throws SQLException;
    
    /**
     * Find all students enrolled in a specific class for a given academic year
     * @param classId The class ID
     * @param academicYear The academic year
     * @return List of User objects representing the students enrolled in the class
     * @throws SQLException If a database error occurs
     */
    List<User> findStudentsByClass(int classId, String academicYear) throws SQLException;
    
    /**
     * Find enrollments by student ID
     * @param studentId The student ID
     * @return List of enrollments for the specified student
     * @throws SQLException If a database error occurs
     */
    List<StudentEnrollment> findByStudentId(int studentId) throws SQLException;
}