package com.attendance.controllers;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet for handling dashboard requests
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DashboardServlet.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // Check if user is logged in
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login");
            return;
        }
        
        String userRole = (String) session.getAttribute("userRole");
        
        // Redirect to appropriate dashboard based on role
        if (userRole != null) {
            switch (userRole) {
                case "Admin":
                    request.getRequestDispatcher("/views/admin/dashboard.jsp").forward(request, response);
                    break;
                case "Principal":
                    request.getRequestDispatcher("/views/principal/dashboard.jsp").forward(request, response);
                    break;
                case "HOD":
                    request.getRequestDispatcher("/views/hod/dashboard.jsp").forward(request, response);
                    break;
                case "Teacher":
                    request.getRequestDispatcher("/views/teacher/dashboard.jsp").forward(request, response);
                    break;
                case "Student":
                    request.getRequestDispatcher("/views/student/dashboard.jsp").forward(request, response);
                    break;
                default:
                    // For simplicity, let's redirect to a generic dashboard if role isn't recognized
                    request.getRequestDispatcher("/views/dashboard.jsp").forward(request, response);
                    break;
            }
        } else {
            // No role found, forward to generic dashboard
            request.getRequestDispatcher("/views/dashboard.jsp").forward(request, response);
        }
    }
}