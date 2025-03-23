package com.attendance;

import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Super basic server with no webapp directory, no JSP, no context configuration
 */
public class BasicServer {
    private static final Logger LOGGER = Logger.getLogger(BasicServer.class.getName());

    // Define a simple servlet as an inner class to avoid external dependencies
    private static class SimpleServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
                throws ServletException, IOException {
            resp.setContentType("text/html;charset=UTF-8");
            resp.getWriter().println("<!DOCTYPE html>");
            resp.getWriter().println("<html>");
            resp.getWriter().println("<head>");
            resp.getWriter().println("<title>Basic Server</title>");
            resp.getWriter().println("<style>body { font-family: Arial; margin: 2em; }</style>");
            resp.getWriter().println("</head>");
            resp.getWriter().println("<body>");
            resp.getWriter().println("<h1>Basic Server is Running</h1>");
            resp.getWriter().println("<p>This is a minimal implementation with no complex configuration.</p>");
            resp.getWriter().println("</body>");
            resp.getWriter().println("</html>");
        }
    }

    public static void main(String[] args) {
        try {
            Tomcat tomcat = new Tomcat();
            tomcat.setPort(5000);

            // Connector configuration for external access
            Connector connector = tomcat.getConnector();
            connector.setProperty("address", "0.0.0.0");

            // Create a minimal context with no webapp directory
            String tempDir = new File(System.getProperty("java.io.tmpdir")).getAbsolutePath();
            LOGGER.info("Using temporary directory: " + tempDir);
            
            // Add a basic context
            Context ctx = tomcat.addContext("", tempDir);
            
            // Add a simple servlet directly
            Tomcat.addServlet(ctx, "simple", new SimpleServlet());
            ctx.addServletMappingDecoded("/*", "simple");

            // Start server
            tomcat.start();
            LOGGER.info("Basic Server started on port 5000");
            LOGGER.info("Visit http://0.0.0.0:5000/ to view");
            
            tomcat.getServer().await();
            
        } catch (LifecycleException e) {
            LOGGER.log(Level.SEVERE, "Server failed to start", e);
        }
    }
}