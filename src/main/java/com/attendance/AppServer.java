package com.attendance;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.attendance.utils.DatabaseConnection;

/**
 * Main application server for the Student Attendance Management System.
 * This is a more robust alternative to the Main class.
 */
public class AppServer {
    private static final Logger LOGGER = Logger.getLogger(AppServer.class.getName());

    public static void main(String[] args) {
        try {
            // Log startup information
            LOGGER.info("Starting Student Attendance Management System");
            LOGGER.info("Java version: " + System.getProperty("java.version"));
            LOGGER.info("Current working directory: " + new File(".").getAbsolutePath());
            
            // Initialize the database
            initializeDatabase();
            
            // Set up and start the Tomcat server
            int port = 5000;
            String webappDirLocation = "src/main/webapp/";
            
            Tomcat tomcat = new Tomcat();
            tomcat.setPort(port);
            tomcat.getConnector().setProperty("address", "0.0.0.0");
            
            // Add the web application context
            Context context = tomcat.addWebapp("", new File(webappDirLocation).getAbsolutePath());
            LOGGER.info("Configuring app with basedir: " + new File(webappDirLocation).getAbsolutePath());
            
            // Add WEB-INF/classes directory for compiled classes
            File additionWebInfClasses = new File("target/classes");
            WebResourceRoot resources = new StandardRoot(context);
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                    additionWebInfClasses.getAbsolutePath(), "/"));
            context.setResources(resources);
            
            // Start the server
            tomcat.start();
            LOGGER.info("Server started on port: " + port);
            LOGGER.info("Application available at http://0.0.0.0:" + port);
            tomcat.getServer().await();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting the application", e);
        }
    }
    
    /**
     * Initialize the database schema and default data
     */
    private static void initializeDatabase() {
        LOGGER.info("Initializing database...");
        
        // Check database connection
        try (Connection conn = DatabaseConnection.getConnection()) {
            LOGGER.info("Database connection successful");
            
            // Create tables if they don't exist
            try (Statement stmt = conn.createStatement()) {
                // Create Department table
                stmt.execute("CREATE TABLE IF NOT EXISTS Department (" +
                        "department_id SERIAL PRIMARY KEY, " +
                        "department_name VARCHAR(100) UNIQUE NOT NULL)");
                
                // Create Classes table
                stmt.execute("CREATE TABLE IF NOT EXISTS Classes (" +
                        "class_id SERIAL PRIMARY KEY, " +
                        "class_name VARCHAR(10) CHECK (class_name IN ('FY', 'SY', 'TY')), " +
                        "department_id INT REFERENCES Department(department_id))");
                
                // Create Users table
                stmt.execute("CREATE TABLE IF NOT EXISTS Users (" +
                        "user_id SERIAL PRIMARY KEY, " +
                        "name VARCHAR(255), " +
                        "phone_no VARCHAR(20), " +
                        "email VARCHAR(255) UNIQUE, " +
                        "password VARCHAR(255) NOT NULL, " +
                        "role VARCHAR(50), " +
                        "department_id INT REFERENCES Department(department_id))");
                
                // Create EnrollmentRequest table
                stmt.execute("CREATE TABLE IF NOT EXISTS EnrollmentRequest (" +
                        "request_id SERIAL PRIMARY KEY, " +
                        "user_id INT REFERENCES Users(user_id), " +
                        "requested_role VARCHAR(50), " +
                        "class_id INT REFERENCES Classes(class_id), " +
                        "enrollment_number CHAR(10), " +
                        "submitted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "status VARCHAR(20) DEFAULT 'Pending', " +
                        "verified_by INT REFERENCES Users(user_id), " +
                        "verified_on TIMESTAMP)");
                
                // Create StudentEnrollment table
                stmt.execute("CREATE TABLE IF NOT EXISTS StudentEnrollment (" +
                        "enrollment_id CHAR(10) PRIMARY KEY, " +
                        "user_id INT REFERENCES Users(user_id), " +
                        "class_id INT REFERENCES Classes(class_id), " +
                        "academic_year VARCHAR(20), " +
                        "enrollment_status VARCHAR(20) DEFAULT 'Active')");
                
                // Create Subject table
                stmt.execute("CREATE TABLE IF NOT EXISTS Subject (" +
                        "subject_code VARCHAR(50) PRIMARY KEY, " +
                        "subject_name VARCHAR(255))");
                
                // Create Department_Subject table
                stmt.execute("CREATE TABLE IF NOT EXISTS Department_Subject (" +
                        "id SERIAL PRIMARY KEY, " +
                        "department_id INT REFERENCES Department(department_id), " +
                        "class_id INT REFERENCES Classes(class_id), " +
                        "subject_code VARCHAR(50) REFERENCES Subject(subject_code))");
                
                // Create TeacherAssignment table
                stmt.execute("CREATE TABLE IF NOT EXISTS TeacherAssignment (" +
                        "teacher_id INT REFERENCES Users(user_id), " +
                        "subject_code VARCHAR(50) REFERENCES Subject(subject_code), " +
                        "class_id INT REFERENCES Classes(class_id), " +
                        "assignment_type VARCHAR(50), " +
                        "PRIMARY KEY (teacher_id, subject_code, class_id))");
                
                // Create Attendance table
                stmt.execute("CREATE TABLE IF NOT EXISTS Attendance (" +
                        "attendance_id SERIAL PRIMARY KEY, " +
                        "attendance_date DATE, " +
                        "subject_code VARCHAR(50) REFERENCES Subject(subject_code), " +
                        "student_id INT REFERENCES Users(user_id), " +
                        "semester VARCHAR(5), " +
                        "academic_year VARCHAR(20), " +
                        "status VARCHAR(20) DEFAULT 'Absent')");
                
                LOGGER.info("Database tables created successfully");
                
                // Insert default admin user if none exists
                insertDefaultAdmin(conn);
                
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error creating database tables", e);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection failed", e);
        }
    }
    
    /**
     * Insert a default admin user if none exists
     */
    private static void insertDefaultAdmin(Connection conn) throws SQLException {
        // Check if any user exists
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Users");
            if (rs.next() && rs.getInt(1) == 0) {
                // Insert admin user
                String sql = "INSERT INTO Users (name, email, password, role) " +
                        "VALUES ('System Admin', 'admin@example.com', 'admin123', 'Admin')";
                stmt.execute(sql);
                LOGGER.info("Default admin user created successfully");
            }
        }
    }
}