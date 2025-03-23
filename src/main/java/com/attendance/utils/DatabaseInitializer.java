package com.attendance.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * ServletContextListener to initialize the database schema when the application starts
 */
public class DatabaseInitializer implements ServletContextListener {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());
    
    /**
     * Initialize the database by creating required tables if they don't exist
     */
    public static void initialize() {
        LOGGER.info("Starting database initialization...");
        
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
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing database", e);
        }
    }
    
    private static void createDepartmentTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS Departments (" +
                "department_id SERIAL PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "description TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.info("Departments table created or already exists");
        }
    }
    
    private static void createUsersTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS Users (" +
                "user_id SERIAL PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "phone_no VARCHAR(15), " +
                "email VARCHAR(100) UNIQUE NOT NULL, " +
                "password VARCHAR(100) NOT NULL, " +
                "role VARCHAR(20) NOT NULL, " +
                "department_id INTEGER, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (department_id) REFERENCES Departments(department_id)" +
                ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.info("Users table created or already exists");
        }
    }
    
    private static void createClassTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS Classes (" +
                "class_id SERIAL PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "year VARCHAR(10) NOT NULL, " + // FY, SY, TY
                "semester INTEGER NOT NULL, " + // 1-6
                "department_id INTEGER NOT NULL, " +
                "class_teacher_id INTEGER, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (department_id) REFERENCES Departments(department_id), " +
                "FOREIGN KEY (class_teacher_id) REFERENCES Users(user_id)" +
                ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.info("Classes table created or already exists");
        }
    }
    
    private static void createSubjectTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS Subjects (" +
                "subject_id SERIAL PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "code VARCHAR(20) NOT NULL, " +
                "description TEXT, " +
                "class_id INTEGER NOT NULL, " +
                "teacher_id INTEGER, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (class_id) REFERENCES Classes(class_id), " +
                "FOREIGN KEY (teacher_id) REFERENCES Users(user_id)" +
                ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.info("Subjects table created or already exists");
        }
    }
    
    private static void createStudentClassTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS StudentClass (" +
                "id SERIAL PRIMARY KEY, " +
                "student_id INTEGER NOT NULL, " +
                "class_id INTEGER NOT NULL, " +
                "enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (student_id) REFERENCES Users(user_id), " +
                "FOREIGN KEY (class_id) REFERENCES Classes(class_id), " +
                "UNIQUE(student_id, class_id)" +
                ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.info("StudentClass table created or already exists");
        }
    }
    
    private static void createAttendanceTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS Attendance (" +
                "attendance_id SERIAL PRIMARY KEY, " +
                "student_id INTEGER NOT NULL, " +
                "subject_id INTEGER NOT NULL, " +
                "status VARCHAR(10) NOT NULL, " + // Present, Absent, Leave
                "date DATE NOT NULL, " +
                "marked_by INTEGER NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (student_id) REFERENCES Users(user_id), " +
                "FOREIGN KEY (subject_id) REFERENCES Subjects(subject_id), " +
                "FOREIGN KEY (marked_by) REFERENCES Users(user_id), " +
                "UNIQUE(student_id, subject_id, date)" +
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
                String deptSql = "INSERT INTO Departments (name, description) " +
                               "VALUES ('Administration', 'System Administration Department') " +
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
                    pstmt.setString(4, "admin123"); // Consider using password hashing in production
                    pstmt.setString(5, "Admin");
                    pstmt.setInt(6, departmentId);
                    
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
        initialize();
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