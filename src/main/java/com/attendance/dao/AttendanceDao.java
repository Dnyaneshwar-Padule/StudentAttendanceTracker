package com.attendance.dao;

import com.attendance.models.Attendance;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * DAO interface for Attendance entities
 */
public interface AttendanceDao extends BaseDao<Attendance, Integer> {
    
    /**
     * Find attendance records by student
     * @param studentId The student ID
     * @return List of attendance records for the specified student
     * @throws SQLException If a database error occurs
     */
    List<Attendance> findByStudent(int studentId) throws SQLException;
    
    /**
     * Find attendance records by subject
     * @param subjectCode The subject code
     * @return List of attendance records for the specified subject
     * @throws SQLException If a database error occurs
     */
    List<Attendance> findBySubject(String subjectCode) throws SQLException;
    
    /**
     * Find attendance records by date
     * @param date The attendance date
     * @return List of attendance records for the specified date
     * @throws SQLException If a database error occurs
     */
    List<Attendance> findByDate(Date date) throws SQLException;
    
    /**
     * Find attendance records by student and subject
     * @param studentId The student ID
     * @param subjectCode The subject code
     * @return List of attendance records for the specified student and subject
     * @throws SQLException If a database error occurs
     */
    List<Attendance> findByStudentAndSubject(int studentId, String subjectCode) throws SQLException;
    
    /**
     * Find attendance records by student, subject, and semester
     * @param studentId The student ID
     * @param subjectCode The subject code
     * @param semester The semester
     * @return List of attendance records
     * @throws SQLException If a database error occurs
     */
    List<Attendance> findByStudentSubjectAndSemester(int studentId, String subjectCode, String semester) throws SQLException;
    
    /**
     * Find attendance records by student, subject, semester, and academic year
     * @param studentId The student ID
     * @param subjectCode The subject code
     * @param semester The semester
     * @param academicYear The academic year
     * @return List of attendance records
     * @throws SQLException If a database error occurs
     */
    List<Attendance> findByStudentSubjectSemesterAndYear(int studentId, String subjectCode, 
                                                         String semester, String academicYear) throws SQLException;
    
    /**
     * Find attendance records by subject, date, and semester
     * @param subjectCode The subject code
     * @param date The attendance date
     * @param semester The semester
     * @return List of attendance records
     * @throws SQLException If a database error occurs
     */
    List<Attendance> findBySubjectDateAndSemester(String subjectCode, Date date, String semester) throws SQLException;

    /**
     * Find attendance records by class, subject, and date
     * @param classId The class ID
     * @param subjectCode The subject code
     * @param date The attendance date
     * @return List of attendance records
     * @throws SQLException If a database error occurs
     */
    List<Attendance> findByClassAndSubjectAndDate(int classId, String subjectCode, Date date) throws SQLException;
    
    /**
     * Calculate attendance percentage for a student in a subject
     * @param studentId The student ID
     * @param subjectCode The subject code
     * @param semester The semester
     * @param academicYear The academic year
     * @return Attendance percentage (0-100)
     * @throws SQLException If a database error occurs
     */
    double calculateAttendancePercentage(int studentId, String subjectCode, String semester, String academicYear) throws SQLException;

    /**
     * Calculate attendance percentage for a specific subject for a student
     * @param studentId The student ID
     * @param subjectCode The subject code
     * @param academicYear The academic year
     * @param semester The semester
     * @return Attendance percentage (0-100)
     * @throws SQLException If a database error occurs
     */
    double calculateSubjectAttendancePercentage(int studentId, String subjectCode, String academicYear, String semester) throws SQLException;

    /**
     * Calculate overall attendance percentage for a subject across all students
     * @param subjectCode The subject code
     * @param academicYear The academic year
     * @param semester The semester
     * @return Attendance percentage (0-100)
     * @throws SQLException If a database error occurs
     */
    double calculateSubjectOverallAttendancePercentage(String subjectCode, String academicYear, String semester) throws SQLException;

    /**
     * Calculate attendance percentage for a class
     * @param classId The class ID
     * @param academicYear The academic year
     * @param semester The semester
     * @param month The month (null for all months)
     * @return Attendance percentage (0-100)
     * @throws SQLException If a database error occurs
     */
    double calculateClassAttendancePercentage(int classId, String academicYear, String semester, String month) throws SQLException;

    /**
     * Calculate subject attendance percentage for a specific class
     * @param classId The class ID
     * @param subjectCode The subject code
     * @param academicYear The academic year
     * @param semester The semester
     * @return Attendance percentage (0-100)
     * @throws SQLException If a database error occurs
     */
    double calculateSubjectClassAttendancePercentage(int classId, String subjectCode, String academicYear, String semester) throws SQLException;

    /**
     * Calculate attendance percentage for a department
     * @param departmentId The department ID
     * @param academicYear The academic year
     * @param semester The semester
     * @param month The month (null for all months)
     * @return Attendance percentage (0-100)
     * @throws SQLException If a database error occurs
     */
    double calculateDepartmentAttendancePercentage(int departmentId, String academicYear, String semester, String month) throws SQLException;

    /**
     * Get monthly attendance trend for the institution
     * @param academicYear The academic year
     * @return Map of month to attendance percentage
     * @throws SQLException If a database error occurs
     */
    Map<String, Double> getMonthlyAttendanceTrend(String academicYear) throws SQLException;

    /**
     * Get semester attendance trend for the institution
     * @param academicYear The academic year
     * @return Map of semester to attendance percentage
     * @throws SQLException If a database error occurs
     */
    Map<String, Double> getSemesterAttendanceTrend(String academicYear) throws SQLException;

    /**
     * Get monthly attendance trend for a department
     * @param departmentId The department ID
     * @param academicYear The academic year
     * @return Map of month to attendance percentage
     * @throws SQLException If a database error occurs
     */
    Map<String, Double> getMonthlyDepartmentAttendanceTrend(int departmentId, String academicYear) throws SQLException;

    /**
     * Get semester attendance trend for a department
     * @param departmentId The department ID
     * @param academicYear The academic year
     * @return Map of semester to attendance percentage
     * @throws SQLException If a database error occurs
     */
    Map<String, Double> getSemesterDepartmentAttendanceTrend(int departmentId, String academicYear) throws SQLException;
    
    /**
     * Get attendance summary for a student across all subjects
     * @param studentId The student ID
     * @param semester The semester
     * @param academicYear The academic year
     * @return Map of subject code to attendance percentage
     * @throws SQLException If a database error occurs
     */
    Map<String, Double> getAttendanceSummary(int studentId, String semester, String academicYear) throws SQLException;
    
    /**
     * Mark attendance for multiple students in a subject
     * @param subjectCode The subject code
     * @param date The attendance date
     * @param semester The semester
     * @param academicYear The academic year
     * @param studentAttendance Map of student ID to attendance status
     * @return Number of attendance records marked
     * @throws SQLException If a database error occurs
     */
    int markAttendance(String subjectCode, Date date, String semester, String academicYear, 
                       Map<Integer, String> studentAttendance) throws SQLException;
}