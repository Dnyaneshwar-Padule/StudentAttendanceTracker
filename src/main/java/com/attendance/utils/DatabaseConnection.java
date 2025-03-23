package com.attendance.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for obtaining database connections
 */
public class DatabaseConnection {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    
    // Environment variables for database connection
    private static final String DB_URL = System.getenv("DATABASE_URL");
    private static final String DB_HOST = System.getenv("PGHOST");
    private static final String DB_PORT = System.getenv("PGPORT");
    private static final String DB_NAME = System.getenv("PGDATABASE");
    private static final String DB_USER = System.getenv("PGUSER");
    private static final String DB_PASSWORD = System.getenv("PGPASSWORD");
    
    // Keep track of open connections for cleanup
    private static final List<Connection> OPEN_CONNECTIONS = new ArrayList<>();
    
    static {
        try {
            // Register JDBC driver
            Class.forName("org.postgresql.Driver");
            LOGGER.info("PostgreSQL JDBC Driver registered successfully");
            
            // Log available database environment variables for debugging
            LOGGER.info("DB_URL: " + (DB_URL != null ? "SET" : "NOT SET"));
            LOGGER.info("DB_HOST: " + (DB_HOST != null ? "SET" : "NOT SET"));
            LOGGER.info("DB_PORT: " + (DB_PORT != null ? "SET" : "NOT SET"));
            LOGGER.info("DB_NAME: " + (DB_NAME != null ? "SET" : "NOT SET"));
            LOGGER.info("DB_USER: " + (DB_USER != null ? "SET" : "NOT SET"));
            LOGGER.info("DB_PASSWORD: " + (DB_PASSWORD != null ? "SET (value hidden)" : "NOT SET"));
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
        Connection conn = null;
        
        // First, try using the full DATABASE_URL if available
        if (DB_URL != null && !DB_URL.isEmpty()) {
            try {
                // Database URL format may need fixing if it doesn't start with jdbc:
                String jdbcUrl = DB_URL;
                if (!jdbcUrl.startsWith("jdbc:")) {
                    // If URL starts with postgres://, convert it to the JDBC format
                    if (jdbcUrl.startsWith("postgres://")) {
                        jdbcUrl = jdbcUrl.replace("postgres://", "jdbc:postgresql://");
                    } else if (jdbcUrl.startsWith("postgresql://")) {
                        jdbcUrl = "jdbc:postgresql://" + jdbcUrl.substring(14);
                    } else {
                        jdbcUrl = "jdbc:postgresql://" + jdbcUrl;
                    }
                }
                LOGGER.info("Attempting to connect using DATABASE_URL: " + jdbcUrl.replaceAll("password=[^&]*", "password=****"));
                conn = DriverManager.getConnection(jdbcUrl);
                LOGGER.info("Database connection established successfully using DATABASE_URL");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to connect using DATABASE_URL, will try individual components", e);
                // Continue to the next approach
            }
        }
        
        // If DATABASE_URL approach failed or not available, try using individual components
        if (conn == null && DB_HOST != null && DB_PORT != null && DB_NAME != null) {
            String url = "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
            try {
                LOGGER.info("Attempting to connect using constructed URL: " + url);
                conn = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
                LOGGER.info("Database connection established successfully using constructed URL");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to establish database connection using constructed URL", e);
                throw e;
            }
        }
        
        // If we got here and still don't have a connection, there's no valid connection information
        if (conn == null) {
            LOGGER.severe("No valid database connection information available");
            throw new SQLException("No valid database connection information available");
        }
        
        // Track this connection for cleanup
        synchronized (OPEN_CONNECTIONS) {
            OPEN_CONNECTIONS.add(conn);
        }
        
        return conn;
    }
    
    /**
     * Close a database connection
     * @param conn The connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                // Remove from tracked connections
                synchronized (OPEN_CONNECTIONS) {
                    OPEN_CONNECTIONS.remove(conn);
                }
                LOGGER.fine("Database connection closed successfully");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing database connection", e);
            }
        }
    }
    
    /**
     * Close all open database connections
     * Used during application shutdown
     */
    public static void closeAllConnections() {
        synchronized (OPEN_CONNECTIONS) {
            LOGGER.info("Closing all open database connections: " + OPEN_CONNECTIONS.size() + " connections to close");
            
            // Create a copy to avoid concurrent modification
            List<Connection> connectionsCopy = new ArrayList<>(OPEN_CONNECTIONS);
            for (Connection conn : connectionsCopy) {
                closeConnection(conn);
            }
            
            // Clear the list even if some failed to close
            OPEN_CONNECTIONS.clear();
            LOGGER.info("All database connections have been closed");
        }
    }
}