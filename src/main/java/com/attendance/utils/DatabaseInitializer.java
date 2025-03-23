package com.attendance.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import com.attendance.dao.impl.DatabaseConnection;

/**
 * ServletContextListener to initialize the database schema when the application starts
 */
@WebListener
public class DatabaseInitializer implements ServletContextListener {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());
    
    /**
     * Initialize the database by creating required tables if they don't exist
     */
    public static void initialize() {
        LOGGER.info("Starting database initialization...");
        
        try {
            // Test the database connection first
            Connection testConn = null;
            try {
                testConn = DatabaseConnection.getConnection();
                LOGGER.info("Database connection test successful");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Database connection test failed: " + e.getMessage(), e);
                throw e; // Rethrow to abort initialization
            } finally {
                if (testConn != null) {
                    try {
                        testConn.close();
                    } catch (SQLException e) {
                        LOGGER.log(Level.WARNING, "Error closing test connection", e);
                    }
                }
            }
            
            // Now proceed with actual initialization
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Create tables if they don't exist
                createDepartmentTable(conn);
                createUsersTable(conn);
                createClassTable(conn);
                createSubjectTable(conn);
                createStudentClassTable(conn);
                createAttendanceTable(conn);
                createLeaveApplicationTable(conn);
                
                // Create an admin user if no users exist
                createDefaultAdminIfNeeded(conn);
                
                LOGGER.info("Database initialization completed successfully");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing database", e);
            // Don't throw exception to allow context to start
        }
    }
    
    private static void createDepartmentTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS Department (" +
                "department_id SERIAL PRIMARY KEY, " +
                "department_name VARCHAR(100) UNIQUE NOT NULL" +
                ")";
        
        LOGGER.info("Executing SQL: " + sql);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.info("Department table created or already exists");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating Department table: " + e.getMessage(), e);
            throw e; // Rethrow to allow caller to handle or abort
        }
    }
    
    private static void createUsersTable(Connection conn) throws SQLException {
        // Note: password field should store hashed passwords (not plaintext)
        // in production we would use bcrypt or similar algorithm
        String sql = "CREATE TABLE IF NOT EXISTS Users (" +
                "user_id SERIAL PRIMARY KEY, " +
                "name VARCHAR(255), " +
                "phone_no VARCHAR(20), " +
                "email VARCHAR(255) UNIQUE, " +
                "password VARCHAR(255) NOT NULL, " + // Stores hashed passwords, not plaintext
                "role VARCHAR(50), " +
                "department_id INT REFERENCES Department(department_id)" +
                ")";
        
        LOGGER.info("Executing SQL: " + sql);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.info("Users table created or already exists");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating Users table: " + e.getMessage(), e);
            throw e;
        }
    }
    
    private static void createClassTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS Classes (" +
                "class_id SERIAL PRIMARY KEY, " +
                "class_name VARCHAR(10) CHECK (class_name IN ('FY', 'SY', 'TY')), " +
                "department_id INT REFERENCES Department(department_id)" +
                ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.info("Classes table created or already exists");
        }
    }
    
    private static void createSubjectTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS Subject (" +
                "subject_code VARCHAR(50) PRIMARY KEY, " +
                "subject_name VARCHAR(255)" +
                ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.info("Subject table created or already exists");
        }
        
        // Create Department_Subject table
        String deptSubjectSql = "CREATE TABLE IF NOT EXISTS Department_Subject (" +
                "id SERIAL PRIMARY KEY, " +
                "department_id INT REFERENCES Department(department_id), " +
                "class_id INT REFERENCES Classes(class_id), " +
                "subject_code VARCHAR(50) REFERENCES Subject(subject_code)" +
                ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(deptSubjectSql);
            LOGGER.info("Department_Subject table created or already exists");
        }
        
        // Create TeacherAssignment table
        String teacherAssignmentSql = "CREATE TABLE IF NOT EXISTS TeacherAssignment (" +
                "teacher_id INT REFERENCES Users(user_id), " +
                "subject_code VARCHAR(50) REFERENCES Subject(subject_code), " +
                "class_id INT REFERENCES Classes(class_id), " +
                "assignment_type VARCHAR(50), " +
                "PRIMARY KEY (teacher_id, subject_code, class_id)" +
                ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(teacherAssignmentSql);
            LOGGER.info("TeacherAssignment table created or already exists");
        }
    }
    
    private static void createStudentClassTable(Connection conn) throws SQLException {
        // Create EnrollmentRequest table
        String enrollmentRequestSql = "CREATE TABLE IF NOT EXISTS EnrollmentRequest (" +
                "request_id SERIAL PRIMARY KEY, " +
                "user_id INT REFERENCES Users(user_id), " +
                "requested_role VARCHAR(50), " +
                "class_id INT REFERENCES Classes(class_id), " +
                "enrollment_number CHAR(10), " +
                "submitted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "status VARCHAR(20) DEFAULT 'Pending', " +
                "verified_by INT REFERENCES Users(user_id), " +
                "verified_on TIMESTAMP" +
                ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(enrollmentRequestSql);
            LOGGER.info("EnrollmentRequest table created or already exists");
        }
        
        // Create StudentEnrollment table
        String studentEnrollmentSql = "CREATE TABLE IF NOT EXISTS StudentEnrollment (" +
                "enrollment_id CHAR(10) PRIMARY KEY, " +
                "user_id INT REFERENCES Users(user_id), " +
                "class_id INT REFERENCES Classes(class_id), " +
                "academic_year VARCHAR(20), " +
                "enrollment_status VARCHAR(20) DEFAULT 'Active'" +
                ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(studentEnrollmentSql);
            LOGGER.info("StudentEnrollment table created or already exists");
        }
    }
    
    private static void createAttendanceTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS Attendance (" +
                "attendance_id SERIAL PRIMARY KEY, " +
                "attendance_date DATE, " +
                "subject_code VARCHAR(50) REFERENCES Subject(subject_code), " +
                "student_id INT REFERENCES Users(user_id), " +
                "semester VARCHAR(5), " + // values: 1-6
                "academic_year VARCHAR(20), " +
                "status VARCHAR(20) DEFAULT 'Absent'" +
                ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.info("Attendance table created or already exists");
        }
    }
    
    private static void createLeaveApplicationTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS LeaveApplications (" +
                "application_id SERIAL PRIMARY KEY, " +
                "student_id INTEGER NOT NULL, " +
                "from_date DATE NOT NULL, " +
                "to_date DATE NOT NULL, " +
                "reason TEXT NOT NULL, " +
                "status VARCHAR(20) DEFAULT 'Pending', " + // Pending, Approved, Rejected
                "approved_by INTEGER, " +
                "approval_date TIMESTAMP, " +
                "rejection_reason TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (student_id) REFERENCES Users(user_id), " +
                "FOREIGN KEY (approved_by) REFERENCES Users(user_id)" +
                ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.info("LeaveApplications table created or already exists");
        }
    }
    
    private static void createDefaultAdminIfNeeded(Connection conn) throws SQLException {
        // Check if any users exist
        String checkSql = "SELECT COUNT(*) FROM Users";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                // No users exist, create a default admin
                
                // First, create a default department
                String deptSql = "INSERT INTO Department (department_name) " +
                               "VALUES ('Administration') " +
                               "RETURNING department_id";
                
                int departmentId;
                try (PreparedStatement pstmt = conn.prepareStatement(deptSql)) {
                    try (ResultSet deptRs = pstmt.executeQuery()) {
                        if (deptRs.next()) {
                            departmentId = deptRs.getInt(1);
                        } else {
                            LOGGER.warning("Failed to create default department");
                            return;
                        }
                    }
                }
                
                // Create admin user
                String adminSql = "INSERT INTO Users (name, phone_no, email, password, role, department_id) " +
                                "VALUES (?, ?, ?, ?, ?, ?)";
                
                try (PreparedStatement pstmt = conn.prepareStatement(adminSql)) {
                    pstmt.setString(1, "System Admin");
                    pstmt.setString(2, "1234567890");
                    pstmt.setString(3, "admin@example.com");
                    // TODO: In production, use a password hashing library like BCrypt
                    // For simplicity, we're using plain text in development
                    // String hashedPassword = BCrypt.hashpw("admin123", BCrypt.gensalt());
                    pstmt.setString(4, "admin123"); // This should be hashed in production!
                    pstmt.setString(5, "Admin");
                    pstmt.setInt(6, departmentId);
                    
                    LOGGER.info("Creating default admin user with email: admin@example.com");
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        LOGGER.info("Default admin user created successfully");
                    } else {
                        LOGGER.warning("Failed to create default admin user");
                    }
                }
            }
        }
    }
    
    /**
     * Called when the context is initialized (application starts)
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("ServletContextListener initialized - initializing database");
        try {
            initialize();
            LOGGER.info("Database initialization completed in ServletContextListener");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during database initialization in ServletContextListener", e);
            // Continue context initialization even if database setup fails
        }
    }
    
    /**
     * Called when the context is destroyed (application stops)
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("ServletContextListener destroyed");
        // No cleanup needed
    }
}