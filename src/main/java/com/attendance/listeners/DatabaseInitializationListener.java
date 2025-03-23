package com.attendance.listeners;

import com.attendance.dao.impl.*;
import com.attendance.utils.DatabaseConnection;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ServletContextListener implementation to handle database initialization
 * when the application starts up.
 */
@WebListener
public class DatabaseInitializationListener implements ServletContextListener {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializationListener.class.getName());
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Initializing database on application startup");
        
        try {
            // Initialize database schema
            initializeDatabase();
            
            // Initialize default data if needed
            // initializeDefaultData();
            
            LOGGER.info("Database initialization completed successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database", e);
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Application shutting down - cleaning up database connections");
        // Clean up any remaining connections
        DatabaseConnection.closeAllConnections();
    }
    
    /**
     * Initialize the database schema if it doesn't exist
     */
    private void initializeDatabase() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.createStatement();
            
            // Create Department table
            String createDepartmentTable = 
                "CREATE TABLE IF NOT EXISTS Department (" +
                "    department_id SERIAL PRIMARY KEY," +
                "    department_name VARCHAR(100) UNIQUE NOT NULL" +
                ")";
            LOGGER.info("Executing SQL: " + createDepartmentTable);
            stmt.execute(createDepartmentTable);
            
            // Create Classes table
            String createClassesTable = 
                "CREATE TABLE IF NOT EXISTS Classes (" +
                "    class_id SERIAL PRIMARY KEY," +
                "    class_name VARCHAR(10) CHECK (class_name IN ('FY', 'SY', 'TY'))," +
                "    department_id INT REFERENCES Department(department_id)" +
                ")";
            LOGGER.info("Executing SQL: " + createClassesTable);
            stmt.execute(createClassesTable);
            
            // Create Users table
            String createUsersTable = 
                "CREATE TABLE IF NOT EXISTS Users (" +
                "    user_id SERIAL PRIMARY KEY," +
                "    name VARCHAR(255)," +
                "    phone_no VARCHAR(20)," +
                "    email VARCHAR(255) UNIQUE," +
                "    password VARCHAR(255) NOT NULL," +
                "    role VARCHAR(50)," +
                "    department_id INT REFERENCES Department(department_id)" +
                ")";
            LOGGER.info("Executing SQL: " + createUsersTable);
            stmt.execute(createUsersTable);
            
            // Create EnrollmentRequest table
            String createEnrollmentRequestTable = 
                "CREATE TABLE IF NOT EXISTS EnrollmentRequest (" +
                "    request_id SERIAL PRIMARY KEY," +
                "    user_id INT REFERENCES Users(user_id)," +
                "    requested_role VARCHAR(50)," +
                "    class_id INT REFERENCES Classes(class_id)," +
                "    enrollment_number CHAR(10)," +
                "    submitted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    status VARCHAR(20) DEFAULT 'Pending'," +
                "    verified_by INT REFERENCES Users(user_id)," +
                "    verified_on TIMESTAMP" +
                ")";
            LOGGER.info("Executing SQL: " + createEnrollmentRequestTable);
            stmt.execute(createEnrollmentRequestTable);
            
            // Create StudentEnrollment table
            String createStudentEnrollmentTable = 
                "CREATE TABLE IF NOT EXISTS StudentEnrollment (" +
                "    enrollment_id CHAR(10) PRIMARY KEY," +
                "    user_id INT REFERENCES Users(user_id)," +
                "    class_id INT REFERENCES Classes(class_id)," +
                "    academic_year VARCHAR(20)," +
                "    enrollment_status VARCHAR(20) DEFAULT 'Active'" +
                ")";
            LOGGER.info("Executing SQL: " + createStudentEnrollmentTable);
            stmt.execute(createStudentEnrollmentTable);
            
            // Create Subject table
            String createSubjectTable = 
                "CREATE TABLE IF NOT EXISTS Subject (" +
                "    subject_code VARCHAR(50) PRIMARY KEY," +
                "    subject_name VARCHAR(255)" +
                ")";
            LOGGER.info("Executing SQL: " + createSubjectTable);
            stmt.execute(createSubjectTable);
            
            // Create Department_Subject table
            String createDepartmentSubjectTable = 
                "CREATE TABLE IF NOT EXISTS Department_Subject (" +
                "    id SERIAL PRIMARY KEY," +
                "    department_id INT REFERENCES Department(department_id)," +
                "    class_id INT REFERENCES Classes(class_id)," +
                "    subject_code VARCHAR(50) REFERENCES Subject(subject_code)" +
                ")";
            LOGGER.info("Executing SQL: " + createDepartmentSubjectTable);
            stmt.execute(createDepartmentSubjectTable);
            
            // Create TeacherAssignment table
            String createTeacherAssignmentTable = 
                "CREATE TABLE IF NOT EXISTS TeacherAssignment (" +
                "    teacher_id INT REFERENCES Users(user_id)," +
                "    subject_code VARCHAR(50) REFERENCES Subject(subject_code)," +
                "    class_id INT REFERENCES Classes(class_id)," +
                "    assignment_type VARCHAR(50)," +
                "    PRIMARY KEY (teacher_id, subject_code, class_id)" +
                ")";
            LOGGER.info("Executing SQL: " + createTeacherAssignmentTable);
            stmt.execute(createTeacherAssignmentTable);
            
            // Create Attendance table
            String createAttendanceTable = 
                "CREATE TABLE IF NOT EXISTS Attendance (" +
                "    attendance_id SERIAL PRIMARY KEY," +
                "    attendance_date DATE," +
                "    subject_code VARCHAR(50) REFERENCES Subject(subject_code)," +
                "    student_id INT REFERENCES Users(user_id)," +
                "    semester VARCHAR(5)," +
                "    academic_year VARCHAR(20)," +
                "    status VARCHAR(20) DEFAULT 'Absent'" +
                ")";
            LOGGER.info("Executing SQL: " + createAttendanceTable);
            stmt.execute(createAttendanceTable);
            
            // Create LeaveApplication table
            String createLeaveApplicationTable = 
                "CREATE TABLE IF NOT EXISTS LeaveApplication (" +
                "    leave_id SERIAL PRIMARY KEY," +
                "    student_id INT REFERENCES Users(user_id)," +
                "    start_date DATE," +
                "    end_date DATE," +
                "    reason TEXT," +
                "    status VARCHAR(20) DEFAULT 'Pending'," +
                "    applied_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    processed_by INT REFERENCES Users(user_id)," +
                "    processed_on TIMESTAMP" +
                ")";
            LOGGER.info("Executing SQL: " + createLeaveApplicationTable);
            stmt.execute(createLeaveApplicationTable);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating database tables", e);
            throw e;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing statement", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection", e);
                }
            }
        }
    }
    
    /**
     * Initialize default data if needed (admin user, default departments, etc.)
     */
    private void initializeDefaultData() throws SQLException {
        // Add code here to populate initial data if needed
    }
}