package com.attendance;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A simplified standalone Tomcat application to verify basic functionality
 */
public class SimpleApp {
    private static final Logger LOGGER = Logger.getLogger(SimpleApp.class.getName());

    public static void main(String[] args) throws Exception {
        // Log startup information
        LOGGER.info("Starting Simple Servlet Application");
        LOGGER.info("Java version: " + System.getProperty("java.version"));
        LOGGER.info("Current working directory: " + new File(".").getAbsolutePath());
        
        // Create and configure Tomcat
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(5000);
        tomcat.getConnector().setProperty("address", "0.0.0.0");
        
        // Create a simple context
        String docBase = "src/main/webapp";
        Context ctx = tomcat.addContext("", new File(docBase).getAbsolutePath());
        
        // Add a simple servlet
        Tomcat.addServlet(ctx, "hello", new HttpServlet() {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
                    throws ServletException, IOException {
                resp.setContentType("text/html");
                PrintWriter writer = resp.getWriter();
                writer.println("<html><body>");
                writer.println("<h1>Simple Servlet Working!</h1>");
                writer.println("<p>This is a test servlet to verify Tomcat is working.</p>");
                writer.println("<p>Current time: " + new java.util.Date() + "</p>");
                writer.println("</body></html>");
            }
        });
        ctx.addServletMappingDecoded("/", "hello");
        
        // Start the server
        tomcat.start();
        LOGGER.info("Server started on port: 5000");
        LOGGER.info("Application available at http://0.0.0.0:5000");
        tomcat.getServer().await();
    }
}