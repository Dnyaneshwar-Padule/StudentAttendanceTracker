package com.attendance.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * Simple test servlet to verify the Tomcat setup is working
 */
@WebServlet("/test")
public class TestServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(TestServlet.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        LOGGER.info("TestServlet: Handling GET request");
        
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Test Servlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Test Servlet</h1>");
            out.println("<p>This is a simple test servlet to verify that the Tomcat setup is working correctly.</p>");
            out.println("<p>Current time: " + new java.util.Date() + "</p>");
            out.println("<p><a href=\"/\">Back to home</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
        
        LOGGER.info("TestServlet: GET request handled successfully");
    }
}