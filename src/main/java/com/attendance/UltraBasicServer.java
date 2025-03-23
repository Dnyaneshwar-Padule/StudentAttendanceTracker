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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ultra minimal server with zero dependencies on other project components
 * Pure isolation for diagnostics
 */
public class UltraBasicServer {
    private static final Logger LOGGER = Logger.getLogger(UltraBasicServer.class.getName());
    
    /**
     * Minimal test servlet
     */
    public static class TestServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;
        
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
                throws ServletException, IOException {
            resp.setContentType("text/html;charset=UTF-8");
            resp.getWriter().write("<!DOCTYPE html>");
            resp.getWriter().write("<html><head><title>Test Server</title></head>");
            resp.getWriter().write("<body><h1>Ultra Basic Server is Working!</h1>");
            resp.getWriter().write("<p>This is a minimal test server with no dependencies.</p>");
            resp.getWriter().write("</body></html>");
        }
    }
    
    public static void main(String[] args) {
        Tomcat tomcat = new Tomcat();
        
        try {
            // Configure port
            tomcat.setPort(5000);
            
            // Configure connector for external access
            Connector connector = tomcat.getConnector();
            connector.setProperty("address", "0.0.0.0");
            
            // Use temp dir for context to avoid webapp scanning
            String tempDir = System.getProperty("java.io.tmpdir");
            LOGGER.info("Using temp dir: " + tempDir);
            
            // Create context with no scanning
            Context ctx = tomcat.addContext("", tempDir);
            
            // Disable scanning to avoid classloader issues
            ctx.setJarScanner(null);
            
            // Add our simple servlet
            Tomcat.addServlet(ctx, "test", new TestServlet());
            ctx.addServletMappingDecoded("/*", "test");
            
            // Start server
            tomcat.start();
            LOGGER.info("Ultra Basic Server started on port 5000");
            LOGGER.info("Visit http://0.0.0.0:5000/ to view");
            
            // Keep running
            tomcat.getServer().await();
            
        } catch (LifecycleException e) {
            LOGGER.log(Level.SEVERE, "Error starting server", e);
        }
    }
}