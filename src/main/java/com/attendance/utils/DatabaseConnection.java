package com.attendance.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for obtaining database connections
 */
public class DatabaseConnection {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    
    // Environment variables for database connection
    private static final String DB_URL = System.getenv("DATABASE_URL");
    private static final String DB_USER = System.getenv("PGUSER");
    private static final String DB_PASSWORD = System.getenv("PGPASSWORD");
    
    static {
        try {
            // Register JDBC driver
            Class.forName("org.postgresql.Driver");
            LOGGER.info("PostgreSQL JDBC Driver registered successfully");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "PostgreSQL JDBC Driver not found", e);
        }
    }
    
    /**
     * Get a database connection
     * @return A database connection
     * @throws SQLException If there's an error connecting to the database
     */
    public static Connection getConnection() throws SQLException {
        if (DB_URL == null || DB_URL.isEmpty()) {
            LOGGER.severe("Database URL is missing. Please set the DATABASE_URL environment variable.");
            throw new SQLException("Database URL is missing");
        }
        
        try {
            // Create and return a connection
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            LOGGER.fine("Database connection established successfully");
            return conn;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to establish database connection", e);
            throw e;
        }
    }
    
    /**
     * Close a database connection
     * @param conn The connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                LOGGER.fine("Database connection closed successfully");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing database connection", e);
            }
        }
    }
}