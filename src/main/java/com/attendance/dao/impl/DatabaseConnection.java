package com.attendance.dao.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for database connections
 */
public class DatabaseConnection {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    
    // Connection pool settings
    private static final int MAX_CONNECTIONS = 10;
    private static final int INITIAL_CONNECTIONS = 2;
    private static final long CONNECTION_TIMEOUT = 30000; // 30 seconds
    
    private static String jdbcUrl = null;
    private static String username = null;
    private static String password = null;
    
    // Initialize database connection parameters
    static {
        try {
            // Load the JDBC driver
            Class.forName("org.postgresql.Driver");
            LOGGER.info("PostgreSQL JDBC Driver loaded successfully");
            
            // Parse connection details from environment variables
            String dbUrlEnv = System.getenv("DATABASE_URL");
            String pgHost = System.getenv("PGHOST");
            String pgPort = System.getenv("PGPORT");
            String pgDatabase = System.getenv("PGDATABASE");
            String pgUser = System.getenv("PGUSER");
            String pgPassword = System.getenv("PGPASSWORD");
            
            // Use DATABASE_URL if available, otherwise use individual params
            if (dbUrlEnv != null) {
                LOGGER.info("Processing DATABASE_URL environment variable");
                
                // Convert standard postgres URL to JDBC format if needed
                if (dbUrlEnv.startsWith("postgresql://")) {
                    // Parse the URL to extract components for JDBC format
                    try {
                        // Example: postgresql://username:password@hostname:port/database?params
                        String urlWithoutProtocol = dbUrlEnv.substring("postgresql://".length());
                        
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
                        
                        // Now build the JDBC URL
                        jdbcUrl = "jdbc:postgresql://" + hostPart;
                        
                        LOGGER.info("Successfully parsed DATABASE_URL into JDBC format");
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Failed to parse postgresql:// URL format", e);
                        throw new SQLException("Failed to parse database connection parameters", e);
                    }
                } else if (dbUrlEnv.startsWith("jdbc:postgresql://")) {
                    // Already in JDBC format
                    jdbcUrl = dbUrlEnv;
                    LOGGER.info("DATABASE_URL already in JDBC format");
                } else {
                    throw new SQLException("Unrecognized DATABASE_URL format. Expected postgresql:// or jdbc:postgresql://");
                }
            } else if (pgHost != null && pgPort != null && pgDatabase != null && pgUser != null && pgPassword != null) {
                jdbcUrl = "jdbc:postgresql://" + pgHost + ":" + pgPort + "/" + pgDatabase;
                username = pgUser;
                password = pgPassword;
                LOGGER.info("Built connection URL from individual parameters");
            } else {
                throw new SQLException("Missing database connection parameters");
            }
            
            LOGGER.info("Database connection parameters initialized successfully");
            
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "PostgreSQL JDBC Driver not found", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database connection parameters", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error initializing database connection", e);
        }
    }
    
    /**
     * Get a database connection
     * 
     * @return Connection to the database
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        if (jdbcUrl == null) {
            throw new SQLException("Database connection parameters not initialized");
        }
        
        try {
            Connection conn;
            if (username != null && password != null) {
                conn = DriverManager.getConnection(jdbcUrl, username, password);
            } else {
                conn = DriverManager.getConnection(jdbcUrl);
            }
            
            if (conn != null) {
                conn.setAutoCommit(true); // Set auto-commit mode
                return conn;
            } else {
                throw new SQLException("Failed to establish database connection");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error connecting to database", e);
            throw e;
        }
    }
    
    /**
     * Close a database connection safely
     * 
     * @param connection The connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing database connection", e);
            }
        }
    }
    
    /**
     * Test the database connection
     * 
     * @return true if the connection is successful, false otherwise
     */
    public static boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed", e);
            return false;
        } finally {
            closeConnection(conn);
        }
    }
}