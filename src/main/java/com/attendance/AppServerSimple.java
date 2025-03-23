package com.attendance;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.attendance.utils.DatabaseInitializer;

/**
 * Simplified AppServer with minimal configuration
 * Used as a fallback if the main AppServer fails with context initialization problems
 */
public class AppServerSimple {
    private static final Logger LOGGER = Logger.getLogger(AppServerSimple.class.getName());

    public static void main(String[] args) {
        try {
            LOGGER.info("Starting Simplified Student Attendance Management System");
            
            // Initialize database
            LOGGER.info("Initializing database");
            DatabaseInitializer.initialize();
            
            // Set up a basic Tomcat server
            int port = 5000;
            String webappDirLocation = "src/main/webapp/";
            
            Tomcat tomcat = new Tomcat();
            tomcat.setPort(port);
            
            // Configure connector for external access
            tomcat.getConnector().setProperty("address", "0.0.0.0");
            
            // Configure docBase and context
            File docBase = new File(webappDirLocation);
            if (!docBase.exists()) {
                LOGGER.severe("Webapp directory not found: " + docBase.getAbsolutePath());
                throw new RuntimeException("Webapp directory not found");
            }
            
            // Add webapp with minimal configuration
            Context context = tomcat.addWebapp("", docBase.getAbsolutePath());
            LOGGER.info("Configuring app with basedir: " + docBase.getAbsolutePath());
            
            // Explicitly register context listeners
            context.addApplicationListener("com.attendance.utils.DatabaseInitializer");
            
            // Add classes directory
            File classesDir = new File("target/classes");
            if (!classesDir.exists()) {
                LOGGER.warning("Classes directory not found at: " + classesDir.getAbsolutePath());
                classesDir = new File("./classes");
                if (!classesDir.exists()) {
                    LOGGER.warning("Classes directory not found at fallback location either");
                    classesDir.mkdirs(); // Create it to avoid errors
                }
            }
            
            WebResourceRoot resources = new StandardRoot(context);
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                    classesDir.getAbsolutePath(), "/"));
            context.setResources(resources);
            
            LOGGER.info("Added resources from: " + classesDir.getAbsolutePath());
            
            // Register our servlets manually (in case annotations aren't working)
            tomcat.addServlet("", "helloServlet", new com.attendance.controllers.HelloServlet());
            context.addServletMappingDecoded("/hello", "helloServlet");
            
            tomcat.addServlet("", "homeServlet", new com.attendance.controllers.HomeServlet());
            context.addServletMappingDecoded("/", "homeServlet");
            
            // Start the server
            tomcat.start();
            LOGGER.info("Server started on port: " + port);
            LOGGER.info("Application available at http://0.0.0.0:" + port);
            
            tomcat.getServer().await();
            
        } catch (LifecycleException e) {
            LOGGER.log(Level.SEVERE, "Error starting Tomcat server", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
        }
    }
}