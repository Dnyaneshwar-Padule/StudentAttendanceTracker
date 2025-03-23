package com.attendance;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.attendance.utils.DatabaseConnection;

/**
 * Main class to launch the embedded Tomcat server
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            // Log environment startup information
            LOGGER.info("Starting Student Attendance Management System");
            LOGGER.info("Current working directory: " + new File(".").getAbsolutePath());
            LOGGER.info("Java version: " + System.getProperty("java.version"));
            
            // Test database connection before starting the server
            testDatabaseConnection();
            
            // Get the port from environment variable or use 5000 as default
            String webPort = System.getenv("PORT");
            if (webPort == null || webPort.isEmpty()) {
                webPort = "5000";
            }
            LOGGER.info("Using port: " + webPort);
    
            // Create and configure Tomcat
            Tomcat tomcat = new Tomcat();
            tomcat.setPort(Integer.parseInt(webPort));
            
            // Set the host to 0.0.0.0 to make the server accessible externally
            tomcat.getConnector().setProperty("address", "0.0.0.0");
    
            // Make sure webapp directory exists
            String webappDirLocation = "src/main/webapp/";
            File webappDir = new File(webappDirLocation);
            if (!webappDir.exists() || !webappDir.isDirectory()) {
                LOGGER.severe("Web application directory not found: " + webappDir.getAbsolutePath());
                LOGGER.info("Files in current directory: ");
                File currentDir = new File(".");
                if (currentDir.exists() && currentDir.isDirectory()) {
                    File[] files = currentDir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            LOGGER.info(" - " + file.getName() + (file.isDirectory() ? " [DIR]" : ""));
                        }
                    }
                }
                return;
            }
            
            LOGGER.info("Setting up webapp from directory: " + webappDir.getAbsolutePath());
            
            // Set the context path and web app directory
            StandardContext ctx = null;
            try {
                ctx = (StandardContext) tomcat.addWebapp("", webappDir.getAbsolutePath());
                LOGGER.info("Configured app with basedir: " + webappDir.getAbsolutePath());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to add webapp", e);
                return;
            }
    
            // Make sure classes directory exists
            File additionWebInfClasses = new File("target/classes");
            if (!additionWebInfClasses.exists() || !additionWebInfClasses.isDirectory()) {
                LOGGER.severe("Classes directory not found: " + additionWebInfClasses.getAbsolutePath());
                return;
            }
    
            // Add resources
            WebResourceRoot resources = new StandardRoot(ctx);
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                    additionWebInfClasses.getAbsolutePath(), "/"));
            ctx.setResources(resources);
    
            // Start the server
            try {
                tomcat.start();
                LOGGER.info("Server started on port: " + webPort);
                LOGGER.info("Application available at http://0.0.0.0:" + webPort);
                tomcat.getServer().await();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to start server", e);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unhandled exception in main method", e);
        }
    }
    
    /**
     * Test database connection before starting the server
     */
    private static void testDatabaseConnection() {
        LOGGER.info("Testing database connection...");
        
        // Print database environment variables for debugging (without values)
        LOGGER.info("DATABASE_URL environment variable exists: " + (System.getenv("DATABASE_URL") != null));
        LOGGER.info("PGUSER environment variable exists: " + (System.getenv("PGUSER") != null));
        LOGGER.info("PGPASSWORD environment variable exists: " + (System.getenv("PGPASSWORD") != null));
        LOGGER.info("PGHOST environment variable exists: " + (System.getenv("PGHOST") != null));
        LOGGER.info("PGPORT environment variable exists: " + (System.getenv("PGPORT") != null));
        LOGGER.info("PGDATABASE environment variable exists: " + (System.getenv("PGDATABASE") != null));
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            LOGGER.info("Database connection test successful!");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed", e);
            LOGGER.warning("Application will continue, but database functionality may not work properly.");
        }
    }
}