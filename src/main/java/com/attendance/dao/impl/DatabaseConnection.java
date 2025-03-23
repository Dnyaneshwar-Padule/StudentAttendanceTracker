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
    private static final String DATABASE_URL = System.getenv("DATABASE_URL");
    private static final String DB_HOST = System.getenv("PGHOST");
    private static final String DB_PORT = System.getenv("PGPORT");
    private static final String DB_NAME = System.getenv("PGDATABASE");
    private static final String DB_USER = System.getenv("PGUSER");
    private static final String DB_PASSWORD = System.getenv("PGPASSWORD");
    
    // Connection pool attributes
    private static final int MAX_CONNECTIONS = 10;
    private static final long CONNECTION_TIMEOUT = 30000; // 30 seconds
    
    static {
        // Load the PostgreSQL JDBC driver
        try {
            Class.forName(JDBC_DRIVER);
            LOGGER.info("PostgreSQL JDBC Driver loaded successfully");
            
            // Test the connection
            Connection testConnection = null;
            try {
                testConnection = getConnection();
                LOGGER.info("Database connection test successful");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to connect to the database", e);
                throw new RuntimeException("Failed to connect to the database", e);
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
            throw new RuntimeException("PostgreSQL JDBC Driver not found", e);
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
        // Try connecting with DATABASE_URL directly
        if (DATABASE_URL != null && !DATABASE_URL.isEmpty()) {
            try {
                // If URL starts with postgres://, convert to jdbc:postgresql://
                String jdbcUrl = DATABASE_URL;
                if (jdbcUrl.startsWith("postgres://")) {
                    jdbcUrl = "jdbc:postgresql://" + jdbcUrl.substring(11);
                }
                
                LOGGER.info("Attempting to connect with URL: " + jdbcUrl.replaceAll(":[^:]*@", ":***@"));
                return DriverManager.getConnection(jdbcUrl);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to connect using DATABASE_URL: " + e.getMessage(), e);
                // Fall through to next method if this fails
            }
        } else {
            LOGGER.warning("DATABASE_URL environment variable is not set");
        }
        
        // Fallback to individual parameters
        if (DB_HOST != null && DB_PORT != null && DB_NAME != null && DB_USER != null && DB_PASSWORD != null) {
            try {
                // Ensure the port is numeric
                int port;
                try {
                    port = Integer.parseInt(DB_PORT);
                } catch (NumberFormatException e) {
                    LOGGER.warning("Invalid port number: " + DB_PORT + ". Using default port 5432.");
                    port = 5432;
                }
                
                String jdbcUrl = "jdbc:postgresql://" + DB_HOST + ":" + port + "/" + DB_NAME;
                LOGGER.info("Attempting to connect with constructed URL: " + jdbcUrl);
                
                return DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASSWORD);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to connect using individual parameters: " + e.getMessage(), e);
                throw e;
            }
        } else {
            LOGGER.warning("Individual database parameters are not properly set");
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