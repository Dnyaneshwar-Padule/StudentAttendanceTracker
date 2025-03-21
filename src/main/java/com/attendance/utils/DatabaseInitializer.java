package com.attendance.utils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.attendance.utils.DatabaseConnection;
import com.attendance.utils.PasswordUtils;

/**
 * Database initializer that runs when the application starts
 */
@WebListener
public class DatabaseInitializer implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Initializing database...");
        
        try {
            // Load schema SQL file
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("database/schema.sql");
            
            if (inputStream == null) {
                LOGGER.severe("Could not find database schema file!");
                return;
            }
            
            String schemaSql = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));
            
            // Execute schema SQL
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Split the SQL script by semicolons to execute each statement separately
                String[] sqlStatements = schemaSql.split(";");
                
                for (String sql : sqlStatements) {
                    if (!sql.trim().isEmpty()) {
                        stmt.execute(sql);
                    }
                }
                
                LOGGER.info("Database schema initialized successfully!");
                
                // Insert default admin user if no users exist
                insertDefaultAdmin(conn);
                
                // Initialize directory structure for biometric data
                initializeBiometricDirectories();
                
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error initializing database schema", e);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading database schema file", e);
        }
    }
    
    /**
     * Insert default admin user if no users exist
     */
    private void insertDefaultAdmin(Connection conn) {
        try {
            // Check if any users exist
            boolean usersExist = false;
            try (Statement stmt = conn.createStatement()) {
                var rs = stmt.executeQuery("SELECT COUNT(*) FROM Users");
                if (rs.next() && rs.getInt(1) > 0) {
                    usersExist = true;
                }
            }
            
            // Insert default admin if no users exist
            if (!usersExist) {
                LOGGER.info("Creating default admin user...");
                
                // Insert admin user with hashed password for "admin123"
                String hashedPassword = PasswordUtils.hashPassword("admin123");
                String sql = "INSERT INTO Users (full_name, email, password, role, status) " +
                           "VALUES ('System Admin', 'admin@example.com', '" + hashedPassword + "', 'Admin', 'Active')";
                
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                    LOGGER.info("Default admin user created successfully!");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating default admin user", e);
        }
    }
    
    /**
     * Initialize directory structure for biometric data
     */
    private void initializeBiometricDirectories() {
        try {
            // Create directories for biometric data
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("data/faces"));
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("data/models"));
            
            // Create empty placeholder file for the face cascade
            java.nio.file.Path cascadePath = java.nio.file.Paths.get("data/haarcascade_frontalface_default.xml");
            if (!java.nio.file.Files.exists(cascadePath)) {
                java.nio.file.Files.createFile(cascadePath);
                LOGGER.info("Created placeholder for face cascade file. Please replace with actual cascade file.");
            }
            
            LOGGER.info("Biometric directories initialized successfully!");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing biometric directories", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Any cleanup if needed
    }
}