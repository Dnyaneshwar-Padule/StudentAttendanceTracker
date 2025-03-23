package com.attendance.controllers;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

/**
 * Home servlet to handle the root URL
 * Mapped in web.xml to the root URL
 */
public class HomeServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(HomeServlet.class.getName());
    private static final long serialVersionUID = 1L;
    
    /**
     * Handle GET requests
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        LOGGER.info("HomeServlet: GET request received");
        
        // First log some debug information about the environment
        LOGGER.info("Real path for / : " + request.getServletContext().getRealPath("/"));
        LOGGER.info("Real path for /index.jsp : " + request.getServletContext().getRealPath("/index.jsp"));
        LOGGER.info("Servlet context name: " + request.getServletContext().getServletContextName());
        
        // Always send direct HTML response first to verify the servlet is working correctly
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println("<!DOCTYPE html>");
        response.getWriter().println("<html>");
        response.getWriter().println("<head>");
        response.getWriter().println("    <title>Student Attendance Management System</title>");
        response.getWriter().println("    <style>");
        response.getWriter().println("        body { font-family: Arial, sans-serif; margin: 40px; }");
        response.getWriter().println("        h1 { color: #2c3e50; }");
        response.getWriter().println("        p { color: #555; }");
        response.getWriter().println("    </style>");
        response.getWriter().println("</head>");
        response.getWriter().println("<body>");
        response.getWriter().println("    <h1>Student Attendance Management System</h1>");
        response.getWriter().println("    <p>Welcome! The application is working correctly.</p>");
        response.getWriter().println("    <p>The servlet is now responding directly without JSP forwarding.</p>");
        response.getWriter().println("    <p><a href='/login'>Login</a></p>");
        response.getWriter().println("</body>");
        response.getWriter().println("</html>");
    }
}