package com.attendance.utils;

import com.attendance.utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to initialize the database schema
 */
public class DatabaseInitializer {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());
    
    // SQL statements to create tables - aligned with DatabaseInitializationListener
    private static final String CREATE_USERS_TABLE = 
            "CREATE TABLE IF NOT EXISTS Users (" +
            "user_id SERIAL PRIMARY KEY," +
            "name VARCHAR(255)," +
            "phone_no VARCHAR(20)," +
            "email VARCHAR(255) UNIQUE," +
            "password VARCHAR(255) NOT NULL," +
            "role VARCHAR(50)," +
            "department_id INT REFERENCES Department(department_id)" +
            ")";
    
    private static final String CREATE_DEPARTMENTS_TABLE = 
            "CREATE TABLE IF NOT EXISTS Department (" +
            "department_id SERIAL PRIMARY KEY," +
            "department_name VARCHAR(100) UNIQUE NOT NULL" +
            ")";
    
    private static final String CREATE_CLASSES_TABLE = 
            "CREATE TABLE IF NOT EXISTS Classes (" +
            "class_id SERIAL PRIMARY KEY," +
            "class_name VARCHAR(10) CHECK (class_name IN ('FY', 'SY', 'TY'))," +
            "department_id INT REFERENCES Department(department_id)" +
            ")";
    
    private static final String CREATE_SUBJECTS_TABLE = 
            "CREATE TABLE IF NOT EXISTS Subject (" +
            "subject_code VARCHAR(50) PRIMARY KEY," +
            "subject_name VARCHAR(255)" +
            ")";
    
    private static final String CREATE_DEPARTMENT_SUBJECT_TABLE = 
            "CREATE TABLE IF NOT EXISTS Department_Subject (" +
            "id SERIAL PRIMARY KEY," +
            "department_id INT REFERENCES Department(department_id)," +
            "class_id INT REFERENCES Classes(class_id)," +
            "subject_code VARCHAR(50) REFERENCES Subject(subject_code)" +
            ")";
    
    private static final String CREATE_ENROLLMENT_REQUEST_TABLE = 
            "CREATE TABLE IF NOT EXISTS EnrollmentRequest (" +
            "request_id SERIAL PRIMARY KEY," +
            "user_id INT REFERENCES Users(user_id)," +
            "requested_role VARCHAR(50)," +
            "class_id INT REFERENCES Classes(class_id)," +
            "enrollment_number CHAR(10)," +
            "submitted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "status VARCHAR(20) DEFAULT 'Pending'," +
            "verified_by INT REFERENCES Users(user_id)," +
            "verified_on TIMESTAMP" +
            ")";
    
    private static final String CREATE_STUDENT_ENROLLMENT_TABLE = 
            "CREATE TABLE IF NOT EXISTS StudentEnrollment (" +
            "enrollment_id CHAR(10) PRIMARY KEY," +
            "user_id INT REFERENCES Users(user_id)," +
            "class_id INT REFERENCES Classes(class_id)," +
            "academic_year VARCHAR(20)," +
            "enrollment_status VARCHAR(20) DEFAULT 'Active'" +
            ")";
    
    private static final String CREATE_TEACHER_ASSIGNMENT_TABLE = 
            "CREATE TABLE IF NOT EXISTS TeacherAssignment (" +
            "teacher_id INT REFERENCES Users(user_id)," +
            "subject_code VARCHAR(50) REFERENCES Subject(subject_code)," +
            "class_id INT REFERENCES Classes(class_id)," +
            "assignment_type VARCHAR(50)," +
            "PRIMARY KEY (teacher_id, subject_code, class_id)" +
            ")";
    
    private static final String CREATE_ATTENDANCE_TABLE = 
            "CREATE TABLE IF NOT EXISTS Attendance (" +
            "attendance_id SERIAL PRIMARY KEY," +
            "attendance_date DATE," +
            "subject_code VARCHAR(50) REFERENCES Subject(subject_code)," +
            "student_id INT REFERENCES Users(user_id)," +
            "semester VARCHAR(5)," +
            "academic_year VARCHAR(20)," +
            "status VARCHAR(20) DEFAULT 'Absent'" +
            ")";
    
    private static final String CREATE_LEAVE_APPLICATION_TABLE = 
            "CREATE TABLE IF NOT EXISTS LeaveApplication (" +
            "leave_id SERIAL PRIMARY KEY," +
            "student_id INT REFERENCES Users(user_id)," +
            "start_date DATE," +
            "end_date DATE," +
            "reason TEXT," +
            "status VARCHAR(20) DEFAULT 'Pending'," +
            "applied_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "processed_by INT REFERENCES Users(user_id)," +
            "processed_on TIMESTAMP" +
            ")";
    
    /**
     * Initialize the database schema
     * 
     * @return true if successful, false otherwise
     */
    public static boolean initializeDatabase() {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            LOGGER.info("Starting database initialization");
            
            conn = DatabaseConnection.getConnection();
            stmt = conn.createStatement();
            
            // Create tables in order (respecting foreign key constraints)
            LOGGER.info("Creating Department table");
            stmt.executeUpdate(CREATE_DEPARTMENTS_TABLE);
            
            LOGGER.info("Creating Users table");
            stmt.executeUpdate(CREATE_USERS_TABLE);
            
            LOGGER.info("Creating Classes table");
            stmt.executeUpdate(CREATE_CLASSES_TABLE);
            
            LOGGER.info("Creating Subject table");
            stmt.executeUpdate(CREATE_SUBJECTS_TABLE);
            
            LOGGER.info("Creating Department_Subject table");
            stmt.executeUpdate(CREATE_DEPARTMENT_SUBJECT_TABLE);
            
            LOGGER.info("Creating EnrollmentRequest table");
            stmt.executeUpdate(CREATE_ENROLLMENT_REQUEST_TABLE);
            
            LOGGER.info("Creating StudentEnrollment table");
            stmt.executeUpdate(CREATE_STUDENT_ENROLLMENT_TABLE);
            
            LOGGER.info("Creating TeacherAssignment table");
            stmt.executeUpdate(CREATE_TEACHER_ASSIGNMENT_TABLE);
            
            LOGGER.info("Creating Attendance table");
            stmt.executeUpdate(CREATE_ATTENDANCE_TABLE);
            
            LOGGER.info("Creating LeaveApplication table");
            stmt.executeUpdate(CREATE_LEAVE_APPLICATION_TABLE);
            
            LOGGER.info("Database initialization completed successfully");
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing database", e);
            return false;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing Statement", e);
                }
            }
            
            DatabaseConnection.closeConnection(conn);
        }
    }
    
    /**
     * Initialize the database schema and create admin user
     */
    public static void initialize() {
        if (initializeDatabase()) {
            createAdminUser();
        }
    }
    
    /**
     * Create an admin user in the database
     * 
     * @return true if successful, false otherwise
     */
    public static boolean createAdminUser() {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            LOGGER.info("Checking for existing admin user");
            
            conn = DatabaseConnection.getConnection();
            stmt = conn.createStatement();
            
            // Check if admin user already exists
            java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Users WHERE role = 'Admin'");
            if (rs.next() && rs.getInt(1) > 0) {
                LOGGER.info("Admin user already exists");
                return true;
            }
            
            // Create admin user
            String createAdminUserSql = 
                    "INSERT INTO Users (name, email, password, role) " +
                    "VALUES ('System Admin', 'admin@example.com', 'admin123', 'Admin')";
            
            LOGGER.info("Creating admin user");
            stmt.executeUpdate(createAdminUserSql);
            
            LOGGER.info("Admin user created successfully");
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating admin user", e);
            return false;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing Statement", e);
                }
            }
            
            DatabaseConnection.closeConnection(conn);
        }
    }
}