package com.attendance;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.LifecycleException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basic server with database connectivity test
 */
public class BasicDbTestServer {
    private static final Logger LOGGER = Logger.getLogger(BasicDbTestServer.class.getName());
    
    /**
     * Test servlet with database connectivity check
     */
    public static class DbTestServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;
        private static final Logger LOGGER = Logger.getLogger(DbTestServlet.class.getName());
        
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
                throws ServletException, IOException {
            resp.setContentType("text/html;charset=UTF-8");
            
            resp.getWriter().write("<!DOCTYPE html>");
            resp.getWriter().write("<html><head><title>Database Test Server</title>");
            resp.getWriter().write("<style>");
            resp.getWriter().write("body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }");
            resp.getWriter().write("h1 { color: #2c3e50; }");
            resp.getWriter().write(".success { color: green; }");
            resp.getWriter().write(".error { color: red; }");
            resp.getWriter().write("</style>");
            resp.getWriter().write("</head><body>");
            resp.getWriter().write("<h1>Database Test Server</h1>");
            
            // Test database connection
            boolean dbConnected = false;
            String dbUrl = null;
            String dbError = null;
            
            try {
                // Get database connection parameters from environment variables
                String pgHost = System.getenv("PGHOST");
                String pgPort = System.getenv("PGPORT");
                String pgDatabase = System.getenv("PGDATABASE");
                String pgUser = System.getenv("PGUSER");
                String pgPassword = System.getenv("PGPASSWORD");
                String dbUrlEnv = System.getenv("DATABASE_URL");
                
                // Output environment variable status
                resp.getWriter().write("<h2>Database Environment Variables</h2>");
                resp.getWriter().write("<ul>");
                resp.getWriter().write("<li>PGHOST: " + (pgHost != null ? pgHost : "Not set") + "</li>");
                resp.getWriter().write("<li>PGPORT: " + (pgPort != null ? pgPort : "Not set") + "</li>");
                resp.getWriter().write("<li>PGDATABASE: " + (pgDatabase != null ? pgDatabase : "Not set") + "</li>");
                resp.getWriter().write("<li>PGUSER: " + (pgUser != null ? "Set (hidden)" : "Not set") + "</li>");
                resp.getWriter().write("<li>PGPASSWORD: " + (pgPassword != null ? "Set (hidden)" : "Not set") + "</li>");
                resp.getWriter().write("<li>DATABASE_URL: " + (dbUrlEnv != null ? "Set (hidden)" : "Not set") + "</li>");
                resp.getWriter().write("</ul>");
                
                // Load the JDBC driver first
                Class.forName("org.postgresql.Driver");
                LOGGER.info("PostgreSQL JDBC Driver loaded successfully");
                
                // Parse connection details
                String jdbcUrl = null;
                String username = null;
                String password = null;
                
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
                            // Log URL with password redacted
                            String logUrl = jdbcUrl.replaceAll(":[^:@/]+@", ":****@");
                            LOGGER.info("JDBC URL: " + logUrl);
                        } catch (Exception e) {
                            throw new SQLException("Failed to parse postgresql:// URL format", e);
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
                
                // Test connection
                String logUrl = jdbcUrl.replaceAll(":[^:@/]+@", ":****@");
                LOGGER.info("Attempting to connect to: " + logUrl);
                
                Connection connection;
                if (username != null && password != null) {
                    connection = DriverManager.getConnection(jdbcUrl, username, password);
                    LOGGER.info("Connected with username and password");
                } else {
                    connection = DriverManager.getConnection(jdbcUrl);
                    LOGGER.info("Connected with connection string only");
                }
                
                if (connection != null) {
                    dbConnected = true;
                    connection.close();
                    LOGGER.info("Database connection test successful");
                } else {
                    throw new SQLException("Failed to establish database connection");
                }
                
            } catch (ClassNotFoundException e) {
                dbError = "JDBC Driver not found: " + e.getMessage();
                LOGGER.log(Level.SEVERE, "JDBC Driver not found", e);
            } catch (SQLException e) {
                dbError = "Database connection error: " + e.getMessage();
                LOGGER.log(Level.SEVERE, "Database connection error", e);
            } catch (Exception e) {
                dbError = "Unexpected error: " + e.getMessage();
                LOGGER.log(Level.SEVERE, "Unexpected error during database test", e);
            }
            
            // Output test results
            resp.getWriter().write("<h2>Database Connection Test</h2>");
            if (dbConnected) {
                resp.getWriter().write("<p class='success'>✅ Database connection successful!</p>");
            } else {
                resp.getWriter().write("<p class='error'>❌ Database connection failed</p>");
                resp.getWriter().write("<p class='error'>Error: " + dbError + "</p>");
            }
            
            resp.getWriter().write("<hr>");
            resp.getWriter().write("<p>Next steps: This server can now be enhanced with additional functionality.</p>");
            resp.getWriter().write("</body></html>");
        }
    }

    public static void main(String[] args) {
        try {
            Tomcat tomcat = new Tomcat();
            tomcat.setPort(5000);

            // Configure connector for external access
            Connector connector = tomcat.getConnector();
            connector.setProperty("address", "0.0.0.0");

            // Create a minimal context with no webapp directory
            String tempDir = new File(System.getProperty("java.io.tmpdir")).getAbsolutePath();
            LOGGER.info("Using temp dir: " + tempDir);
            
            // Add a basic context
            Context ctx = tomcat.addContext("", tempDir);
            
            // Disable scanning to avoid classloader issues
            ctx.setJarScanner(null);
            
            // Add our db test servlet
            Tomcat.addServlet(ctx, "dbtest", new DbTestServlet());
            ctx.addServletMappingDecoded("/*", "dbtest");

            // Start server
            tomcat.start();
            LOGGER.info("Database Test Server started on port 5000");
            LOGGER.info("Visit http://0.0.0.0:5000/ to view");
            
            tomcat.getServer().await();
            
        } catch (LifecycleException e) {
            LOGGER.log(Level.SEVERE, "Server failed to start", e);
        }
    }
}