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
                // Parse the DATABASE_URL to extract components
                String jdbcUrl;
                String username = null;
                String password = null;
                
                if (DB_URL.startsWith("postgres://") || DB_URL.startsWith("postgresql://")) {
                    // Format: postgresql://username:password@hostname:port/database
                    try {
                        String prefix = DB_URL.startsWith("postgres://") ? "postgres://" : "postgresql://";
                        String urlWithoutProtocol = DB_URL.substring(prefix.length());
                        
                        // Extract credentials if present
                        String hostPart;
                        if (urlWithoutProtocol.contains("@")) {
                            String[] credentialAndHost = urlWithoutProtocol.split("@", 2);
                            String credentialPart = credentialAndHost[0];
                            hostPart = credentialAndHost[1];
                            
                            // Extract username and password
                            if (credentialPart.contains(":")) {
                                String[] userAndPass = credentialPart.split(":", 2);
                                username = userAndPass[0];
                                password = userAndPass[1];
                            } else {
                                username = credentialPart;
                            }
                        } else {
                            hostPart = urlWithoutProtocol;
                        }
                        
                        // Build JDBC URL without embedding credentials
                        jdbcUrl = "jdbc:postgresql://" + hostPart;
                        LOGGER.info("Successfully parsed DATABASE_URL into JDBC format");
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error parsing DATABASE_URL, will use as-is", e);
                        // Fall back to simple replacement if parsing fails
                        String fallbackPrefix = DB_URL.startsWith("postgres://") ? "postgres://" : "postgresql://";
                        jdbcUrl = "jdbc:postgresql://" + DB_URL.substring(fallbackPrefix.length());
                    }
                } else if (DB_URL.startsWith("jdbc:postgresql://")) {
                    // Already in JDBC format
                    jdbcUrl = DB_URL;
                } else {
                    // Unknown format, attempt to prepend JDBC prefix
                    jdbcUrl = "jdbc:postgresql://" + DB_URL;
                }
                
                // Log URL (without credentials)
                String logUrl = jdbcUrl.replaceAll(":[^:@/]+@", ":****@");
                LOGGER.info("Attempting to connect using DATABASE_URL: " + logUrl);
                
                // Connect with or without credentials
                if (username != null && password != null) {
                    conn = DriverManager.getConnection(jdbcUrl, username, password);
                } else {
                    conn = DriverManager.getConnection(jdbcUrl);
                }
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