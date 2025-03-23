package com.attendance;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.JarScannerCallback;

import jakarta.servlet.ServletContext;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.LifecycleException;

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
            
            // Display classpath information for debugging
            String classpath = System.getProperty("java.class.path");
            LOGGER.info("Classpath: " + classpath);
            
            // Database will be initialized by the ServletContextListener when the context is initialized
            LOGGER.info("Database initialization will be handled by DatabaseInitializationListener");
            
            // Set up and start the Tomcat server
            int port = 5000;
            String webappDirLocation = "src/main/webapp/";
            
            // Set Tomcat home directory for temporary files
            String catalinaHome = System.getProperty("java.io.tmpdir");
            System.setProperty("catalina.home", catalinaHome);
            LOGGER.info("Setting catalina.home to: " + catalinaHome);
            
            Tomcat tomcat = new Tomcat();
            tomcat.setBaseDir(catalinaHome);
            tomcat.setPort(port);
            
            // Initialize the connector explicitly (once is enough)
            tomcat.getConnector().setProperty("address", "0.0.0.0");
            
            // Add the web application context at root path
            String contextPath = "";
            File docBase = new File(webappDirLocation);
            LOGGER.info("Configuring app with basedir: " + docBase.getAbsolutePath());
            
            // Verify webapp directory exists
            if (!docBase.exists()) {
                LOGGER.severe("Webapp directory does not exist: " + docBase.getAbsolutePath());
                throw new RuntimeException("Webapp directory does not exist");
            }
            
            // Check if web.xml exists and is accessible
            File webXmlFile = new File(docBase.getAbsolutePath() + "/WEB-INF/web.xml");
            if (webXmlFile.exists()) {
                LOGGER.info("web.xml found at: " + webXmlFile.getAbsolutePath());
            } else {
                LOGGER.severe("web.xml not found at expected location: " + webXmlFile.getAbsolutePath());
                
                // List all files in the WEB-INF directory
                File webInfDir = new File(docBase.getAbsolutePath() + "/WEB-INF");
                if (webInfDir.exists() && webInfDir.isDirectory()) {
                    File[] files = webInfDir.listFiles();
                    if (files != null) {
                        LOGGER.info("Files in WEB-INF directory:");
                        for (File file : files) {
                            LOGGER.info("  - " + file.getName() + " (readable: " + file.canRead() + ")");
                        }
                    } else {
                        LOGGER.warning("Could not list files in WEB-INF directory");
                    }
                } else {
                    LOGGER.severe("WEB-INF directory does not exist at: " + webInfDir.getAbsolutePath());
                }
            }
            
            // Create context and configure
            Context context = tomcat.addWebapp(contextPath, docBase.getAbsolutePath());
            context.setCreateUploadTargets(true);
            
            // Enable detailed logging for context configuration issues
            context.setLogEffectiveWebXml(true);
            
            // Add an error reporter to capture JAR scanning issues
            context.addServletContainerInitializer((c, ctx) -> {
                LOGGER.info("Servlet container initializer called");
            }, null);
            
            // Configure the context for better servlet initialization
            // Note: setFailCtxIfServletStartFails is not available in this Tomcat version
            
            // Load our ServletContextListener manually
            context.addApplicationListener("com.attendance.listeners.DatabaseInitializationListener");
            
            // Simplify JAR scanning to avoid initialization errors
            StandardJarScanner jarScanner = new StandardJarScanner();
            jarScanner.setScanManifest(false);
            
            // Set more permissive JAR scanning to avoid TLD issues
            jarScanner.setJarScanFilter(new StandardJarScanFilter() {
                @Override
                public boolean check(JarScanType scanType, String jarName) {
                    // Limit scanning to essential JARs only
                    return false;
                }
            });
            context.setJarScanner(jarScanner);
            
            // Add class directories - with fallback for Replit
            File additionWebInfClasses = new File("target/classes");
            LOGGER.info("Adding WEB-INF/classes directory: " + additionWebInfClasses.getAbsolutePath());
            
            if (!additionWebInfClasses.exists()) {
                LOGGER.warning("Primary classes directory does not exist: " + additionWebInfClasses.getAbsolutePath());
                
                // Try fallback locations for Replit
                String[] fallbackPaths = {"./classes", "./build/classes", "./out/production/classes"};
                boolean foundClasses = false;
                
                for (String path : fallbackPaths) {
                    File fallbackDir = new File(path);
                    if (fallbackDir.exists() && fallbackDir.isDirectory()) {
                        additionWebInfClasses = fallbackDir;
                        LOGGER.info("Using fallback classes directory: " + fallbackDir.getAbsolutePath());
                        foundClasses = true;
                        break;
                    }
                }
                
                if (!foundClasses) {
                    LOGGER.warning("Could not find compiled classes in any expected location. " +
                                  "Web application may not function correctly.");
                    // Create the classes directory to avoid errors
                    additionWebInfClasses.mkdirs();
                }
            }
            
            WebResourceRoot resources = new StandardRoot(context);
            if (additionWebInfClasses.exists()) {
                resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                        additionWebInfClasses.getAbsolutePath(), "/"));
                LOGGER.info("Added resource set for: " + additionWebInfClasses.getAbsolutePath());
            }
            context.setResources(resources);
            
            // Start the server with better error reporting
            LOGGER.info("Starting Tomcat server...");
            try {
                tomcat.start();
                LOGGER.info("Server started on port: " + port);
                LOGGER.info("Application available at http://0.0.0.0:" + port);
                
                // Log any startup errors from the context
                Context ctx = (Context)tomcat.getHost().findChild("");
                if (ctx != null) {
                    if (ctx.getState().isAvailable()) {
                        LOGGER.info("Context is available and running properly");
                    } else {
                        LOGGER.severe("Context is not available. Current state: " + ctx.getStateName());
                        // Log all listeners for debugging
                        LOGGER.info("Context listeners:");
                        Object[] listeners = ctx.getApplicationLifecycleListeners();
                        if (listeners != null) {
                            for (Object listener : listeners) {
                                LOGGER.info("  - " + listener.getClass().getName());
                            }
                        } else {
                            LOGGER.info("  No listeners registered");
                        }
                    }
                } else {
                    LOGGER.severe("Could not find the ROOT context");
                }
                
                tomcat.getServer().await();
            } catch (LifecycleException e) {
                LOGGER.log(Level.SEVERE, "Error starting Tomcat server", e);
                throw e;
            }
            
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