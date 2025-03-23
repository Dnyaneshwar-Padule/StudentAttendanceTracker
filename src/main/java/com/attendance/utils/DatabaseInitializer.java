package com.attendance.utils;

import com.attendance.dao.impl.DatabaseConnection;
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
    
    // SQL statements to create tables
    private static final String CREATE_USERS_TABLE = 
            "CREATE TABLE IF NOT EXISTS users (" +
            "id SERIAL PRIMARY KEY, " +
            "username VARCHAR(50) UNIQUE NOT NULL, " +
            "password VARCHAR(255) NOT NULL, " +
            "first_name VARCHAR(50) NOT NULL, " +
            "last_name VARCHAR(50) NOT NULL, " +
            "email VARCHAR(100) UNIQUE NOT NULL, " +
            "phone_number VARCHAR(20), " +
            "role VARCHAR(20) NOT NULL, " +
            "active BOOLEAN DEFAULT TRUE, " +
            "created_at TIMESTAMP NOT NULL, " +
            "updated_at TIMESTAMP NOT NULL, " +
            "profile_picture VARCHAR(255), " +
            "department VARCHAR(50)" +
            ")";
    
    private static final String CREATE_DEPARTMENTS_TABLE = 
            "CREATE TABLE IF NOT EXISTS departments (" +
            "id SERIAL PRIMARY KEY, " +
            "name VARCHAR(100) UNIQUE NOT NULL, " +
            "hod_id INTEGER REFERENCES users(id), " +
            "description TEXT, " +
            "created_at TIMESTAMP NOT NULL, " +
            "updated_at TIMESTAMP NOT NULL" +
            ")";
    
    private static final String CREATE_CLASSES_TABLE = 
            "CREATE TABLE IF NOT EXISTS classes (" +
            "id SERIAL PRIMARY KEY, " +
            "name VARCHAR(50) NOT NULL, " +
            "year VARCHAR(10) NOT NULL, " + // FY, SY, TY
            "semester INTEGER NOT NULL, " + // 1-6
            "department_id INTEGER REFERENCES departments(id) NOT NULL, " +
            "class_teacher_id INTEGER REFERENCES users(id), " +
            "academic_year VARCHAR(10) NOT NULL, " + // e.g., 2023-24
            "created_at TIMESTAMP NOT NULL, " +
            "updated_at TIMESTAMP NOT NULL, " +
            "UNIQUE(name, department_id, academic_year)" +
            ")";
    
    private static final String CREATE_SUBJECTS_TABLE = 
            "CREATE TABLE IF NOT EXISTS subjects (" +
            "id SERIAL PRIMARY KEY, " +
            "name VARCHAR(100) NOT NULL, " +
            "code VARCHAR(20) UNIQUE NOT NULL, " +
            "description TEXT, " +
            "credits INTEGER NOT NULL, " +
            "department_id INTEGER REFERENCES departments(id) NOT NULL, " +
            "created_at TIMESTAMP NOT NULL, " +
            "updated_at TIMESTAMP NOT NULL" +
            ")";
    
    private static final String CREATE_CLASS_SUBJECTS_TABLE = 
            "CREATE TABLE IF NOT EXISTS class_subjects (" +
            "id SERIAL PRIMARY KEY, " +
            "class_id INTEGER REFERENCES classes(id) NOT NULL, " +
            "subject_id INTEGER REFERENCES subjects(id) NOT NULL, " +
            "teacher_id INTEGER REFERENCES users(id) NOT NULL, " +
            "created_at TIMESTAMP NOT NULL, " +
            "updated_at TIMESTAMP NOT NULL, " +
            "UNIQUE(class_id, subject_id)" +
            ")";
    
    private static final String CREATE_STUDENTS_TABLE = 
            "CREATE TABLE IF NOT EXISTS students (" +
            "id SERIAL PRIMARY KEY, " +
            "user_id INTEGER REFERENCES users(id) UNIQUE NOT NULL, " +
            "roll_number VARCHAR(20) NOT NULL, " +
            "class_id INTEGER REFERENCES classes(id) NOT NULL, " +
            "admission_date DATE NOT NULL, " +
            "parent_name VARCHAR(100), " +
            "parent_phone VARCHAR(20), " +
            "parent_email VARCHAR(100), " +
            "address TEXT, " +
            "created_at TIMESTAMP NOT NULL, " +
            "updated_at TIMESTAMP NOT NULL, " +
            "UNIQUE(roll_number, class_id)" +
            ")";
    
    private static final String CREATE_ATTENDANCE_TABLE = 
            "CREATE TABLE IF NOT EXISTS attendance (" +
            "id SERIAL PRIMARY KEY, " +
            "student_id INTEGER REFERENCES students(id) NOT NULL, " +
            "class_id INTEGER REFERENCES classes(id) NOT NULL, " +
            "subject_id INTEGER REFERENCES subjects(id) NOT NULL, " +
            "date DATE NOT NULL, " +
            "status VARCHAR(10) NOT NULL, " + // Present, Absent, Late, Leave
            "remarks TEXT, " +
            "marked_by_id INTEGER REFERENCES users(id) NOT NULL, " +
            "created_at TIMESTAMP NOT NULL, " +
            "updated_at TIMESTAMP NOT NULL, " +
            "attendance_session VARCHAR(20), " + // Morning, Afternoon
            "leave_application_id INTEGER, " + // Reference to leave application if status is Leave
            "UNIQUE(student_id, subject_id, date, attendance_session)" +
            ")";
    
    private static final String CREATE_LEAVE_APPLICATIONS_TABLE = 
            "CREATE TABLE IF NOT EXISTS leave_applications (" +
            "id SERIAL PRIMARY KEY, " +
            "student_id INTEGER REFERENCES students(id) NOT NULL, " +
            "start_date DATE NOT NULL, " +
            "end_date DATE NOT NULL, " +
            "reason TEXT NOT NULL, " +
            "status VARCHAR(20) NOT NULL, " + // Pending, Approved, Rejected
            "approved_by_id INTEGER REFERENCES users(id), " +
            "approval_date TIMESTAMP, " +
            "comments TEXT, " +
            "created_at TIMESTAMP NOT NULL, " +
            "updated_at TIMESTAMP NOT NULL" +
            ")";
    
    private static final String CREATE_ENROLLMENT_REQUESTS_TABLE = 
            "CREATE TABLE IF NOT EXISTS enrollment_requests (" +
            "id SERIAL PRIMARY KEY, " +
            "student_id INTEGER REFERENCES students(id) NOT NULL, " +
            "class_id INTEGER REFERENCES classes(id) NOT NULL, " +
            "status VARCHAR(20) NOT NULL, " + // Pending, Approved, Rejected
            "request_date TIMESTAMP NOT NULL, " +
            "processed_by_id INTEGER REFERENCES users(id), " +
            "processed_date TIMESTAMP, " +
            "comments TEXT, " +
            "created_at TIMESTAMP NOT NULL, " +
            "updated_at TIMESTAMP NOT NULL, " +
            "UNIQUE(student_id, class_id)" +
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
            LOGGER.info("Creating users table");
            stmt.executeUpdate(CREATE_USERS_TABLE);
            
            LOGGER.info("Creating departments table");
            stmt.executeUpdate(CREATE_DEPARTMENTS_TABLE);
            
            LOGGER.info("Creating classes table");
            stmt.executeUpdate(CREATE_CLASSES_TABLE);
            
            LOGGER.info("Creating subjects table");
            stmt.executeUpdate(CREATE_SUBJECTS_TABLE);
            
            LOGGER.info("Creating class_subjects table");
            stmt.executeUpdate(CREATE_CLASS_SUBJECTS_TABLE);
            
            LOGGER.info("Creating students table");
            stmt.executeUpdate(CREATE_STUDENTS_TABLE);
            
            LOGGER.info("Creating attendance table");
            stmt.executeUpdate(CREATE_ATTENDANCE_TABLE);
            
            LOGGER.info("Creating leave_applications table");
            stmt.executeUpdate(CREATE_LEAVE_APPLICATIONS_TABLE);
            
            LOGGER.info("Creating enrollment_requests table");
            stmt.executeUpdate(CREATE_ENROLLMENT_REQUESTS_TABLE);
            
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
            java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE role = 'Admin'");
            if (rs.next() && rs.getInt(1) > 0) {
                LOGGER.info("Admin user already exists");
                return true;
            }
            
            // Create admin user
            String createAdminUserSql = 
                    "INSERT INTO users (username, password, first_name, last_name, email, role, created_at, updated_at) " +
                    "VALUES ('admin', 'admin123', 'System', 'Administrator', 'admin@example.com', 'Admin', NOW(), NOW())";
            
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