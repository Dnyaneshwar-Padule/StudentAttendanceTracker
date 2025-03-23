package com.attendance;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Minimal server implementation that uses no JSP/JSTL/etc.
 * Pure servlet-based to isolate the issue
 */
public class MinimalServer {
    private static final Logger LOGGER = Logger.getLogger(MinimalServer.class.getName());

    // Simple servlet inner class to avoid external dependencies
    private static class RootServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
                throws ServletException, IOException {
            resp.setContentType("text/html;charset=UTF-8");
            resp.getWriter().println("<!DOCTYPE html>");
            resp.getWriter().println("<html>");
            resp.getWriter().println("<head>");
            resp.getWriter().println("    <title>Student Attendance Management System</title>");
            resp.getWriter().println("    <style>");
            resp.getWriter().println("        body { font-family: Arial, sans-serif; margin: 40px; }");
            resp.getWriter().println("        h1 { color: #2c3e50; }");
            resp.getWriter().println("        p { color: #555; }");
            resp.getWriter().println("    </style>");
            resp.getWriter().println("</head>");
            resp.getWriter().println("<body>");
            resp.getWriter().println("    <h1>Student Attendance Management System</h1>");
            resp.getWriter().println("    <p>Minimal server is running!</p>");
            resp.getWriter().println("    <hr>");
            resp.getWriter().println("    <p>This is a simplified version without JSP processing to isolate the source of errors.</p>");
            resp.getWriter().println("</body>");
            resp.getWriter().println("</html>");
        }
    }

    public static void main(String[] args) {
        try {
            LOGGER.info("Starting Minimal Server");
            
            // Basic Tomcat setup
            Tomcat tomcat = new Tomcat();
            tomcat.setPort(5000);
            
            // Create empty context
            String docBase = new File(".").getAbsolutePath();
            Context ctx = tomcat.addContext("", docBase);
            
            // Add our servlet
            Tomcat.addServlet(ctx, "rootServlet", new RootServlet());
            ctx.addServletMappingDecoded("/*", "rootServlet");
            
            // Configure the connector explicitly for external access
            Connector connector = tomcat.getConnector();
            connector.setProperty("address", "0.0.0.0");
            
            // Start Tomcat
            tomcat.start();
            LOGGER.info("Server started on port: 5000");
            LOGGER.info("Application available at http://0.0.0.0:5000");
            
            // Keep the server running
            tomcat.getServer().await();
            
        } catch (LifecycleException e) {
            LOGGER.log(Level.SEVERE, "Error starting Tomcat server", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
        }
    }
}