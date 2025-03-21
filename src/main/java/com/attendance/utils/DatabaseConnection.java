package com.attendance.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for database connection handling
 */
public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static Connection connection = null;
    
    /**
     * Gets a connection to the database
     * @return Database connection
     * @throws SQLException If connection fails
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load the JDBC driver
                Class.forName("org.postgresql.Driver");
                
                // Get database connection parameters from environment variables
                String dbUrl = System.getenv("DATABASE_URL");
                String dbUser = System.getenv("PGUSER");
                String dbPassword = System.getenv("PGPASSWORD");
                
                // If DATABASE_URL is not available, construct it from individual parts
                if (dbUrl == null || dbUrl.trim().isEmpty()) {
                    String dbHost = System.getenv("PGHOST");
                    String dbPort = System.getenv("PGPORT");
                    String dbName = System.getenv("PGDATABASE");
                    
                    dbUrl = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
                }
                
                // Get connection
                connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                LOGGER.info("Database connection established successfully");
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "PostgreSQL JDBC driver not found", e);
                throw new SQLException("PostgreSQL JDBC driver not found", e);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to connect to database", e);
                throw e;
            }
        }
        return connection;
    }
    
    /**
     * Closes the database connection
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Database connection closed successfully");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing database connection", e);
            }
        }
    }
}