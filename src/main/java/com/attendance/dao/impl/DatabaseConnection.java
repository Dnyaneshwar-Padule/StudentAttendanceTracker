package com.attendance.dao.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database connection utility class
 */
public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    
    private static final String JDBC_DRIVER = "org.postgresql.Driver";
    
    // Database credentials - load from environment variables
    // Use individual parameters first, as they are in the correct format
    private static final String DB_HOST = System.getenv("PGHOST");
    private static final String DB_PORT = System.getenv("PGPORT");
    private static final String DB_NAME = System.getenv("PGDATABASE");
    private static final String DB_USER = System.getenv("PGUSER");
    private static final String DB_PASSWORD = System.getenv("PGPASSWORD");
    // DATABASE_URL is a fallback
    private static final String DATABASE_URL = System.getenv("DATABASE_URL");
    
    // Connection pool attributes
    private static final int MAX_CONNECTIONS = 10;
    private static final long CONNECTION_TIMEOUT = 30000; // 30 seconds
    
    static {
        // Print environment variables for debugging
        LOGGER.info("Environment variables for PostgreSQL:");
        LOGGER.info("PGHOST: " + (DB_HOST != null ? DB_HOST : "not set"));
        LOGGER.info("PGPORT: " + (DB_PORT != null ? DB_PORT : "not set"));
        LOGGER.info("PGDATABASE: " + (DB_NAME != null ? DB_NAME : "not set"));
        LOGGER.info("PGUSER: " + (DB_USER != null ? "is set" : "not set"));
        LOGGER.info("DATABASE_URL: " + (DATABASE_URL != null ? "is set (starts with: " + 
                 (DATABASE_URL.length() > 10 ? DATABASE_URL.substring(0, 10) + "...)" : DATABASE_URL + ")") : "not set"));
        
        // Try to load the PostgreSQL JDBC driver
        try {
            Class.forName(JDBC_DRIVER);
            LOGGER.info("PostgreSQL JDBC Driver loaded successfully");
            
            // Test the connection - but don't fail if it doesn't work yet
            Connection testConnection = null;
            try {
                testConnection = getConnection();
                LOGGER.info("Database connection test successful");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Database connection test failed: " + e.getMessage(), e);
                // Don't throw exception here, let the application start anyway
                // We'll handle connection errors on-demand in the DAO implementations
            } finally {
                if (testConnection != null) {
                    try {
                        testConnection.close();
                    } catch (SQLException e) {
                        LOGGER.log(Level.WARNING, "Error closing test connection", e);
                    }
                }
            }
            
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "PostgreSQL JDBC Driver not found", e);
            // Don't throw runtime exception to allow context to start even without DB
        }
    }
    
    /**
     * Private constructor to prevent instantiation
     */
    private DatabaseConnection() {
    }
    
    /**
     * Get a database connection
     * 
     * @return a new database connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        // First try using the individual parameters (most reliable approach)
        if (DB_HOST != null && DB_NAME != null && DB_USER != null && DB_PASSWORD != null) {
            try {
                // Ensure the port is numeric
                int port = 5432; // Default PostgreSQL port
                if (DB_PORT != null && !DB_PORT.isEmpty()) {
                    try {
                        port = Integer.parseInt(DB_PORT);
                    } catch (NumberFormatException e) {
                        LOGGER.warning("Invalid port number: " + DB_PORT + ". Using default port 5432.");
                    }
                }
                
                String jdbcUrl = "jdbc:postgresql://" + DB_HOST + ":" + port + "/" + DB_NAME;
                LOGGER.info("Attempting to connect with PGHOST/PGDATABASE env variables: " + jdbcUrl);
                
                return DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASSWORD);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to connect using individual parameters: " + e.getMessage());
                // Fall through to try DATABASE_URL if this fails
            }
        } else {
            LOGGER.info("Some individual database parameters are not set. Trying other methods.");
        }
        
        // Second, try using DATABASE_URL directly
        if (DATABASE_URL != null && !DATABASE_URL.isEmpty()) {
            try {
                // Basic URL conversion
                String jdbcUrl = DATABASE_URL;
                
                // For postgres:// or postgresql:// protocol formats
                if (jdbcUrl.startsWith("postgres://")) {
                    jdbcUrl = "jdbc:postgresql://" + jdbcUrl.substring(11);
                } else if (jdbcUrl.startsWith("postgresql://")) {
                    jdbcUrl = "jdbc:postgresql://" + jdbcUrl.substring(14);
                } else if (!jdbcUrl.startsWith("jdbc:")) {
                    jdbcUrl = "jdbc:postgresql://" + jdbcUrl;
                }
                
                // Log the sanitized URL
                LOGGER.info("Attempting to connect with DATABASE_URL: " + 
                           jdbcUrl.replaceAll(":[^:]*@", ":***@"));
                
                return DriverManager.getConnection(jdbcUrl);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to connect using DATABASE_URL: " + e.getMessage());
                // Fall through to next method if this fails
            }
        } else {
            LOGGER.info("DATABASE_URL environment variable is not set.");
        }
        
        // Last resort - try a hard-coded connection to the Replit PostgreSQL instance
        try {
            LOGGER.info("Attempting to connect with hardcoded Replit PostgreSQL connection");
            return DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/" + System.getenv("REPL_SLUG"),
                "postgres", 
                "postgres"
            );
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "All connection attempts failed", e);
            throw new SQLException("Could not establish database connection after multiple attempts", e);
        }
    }
}