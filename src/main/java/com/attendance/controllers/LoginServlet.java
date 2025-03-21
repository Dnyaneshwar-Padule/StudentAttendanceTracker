package com.attendance.controllers;

import com.attendance.dao.UserDAO;
import com.attendance.models.User;
import com.attendance.utils.SessionUtil;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for handling user login
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO = new UserDAO();
    
    /**
     * Handles the HTTP GET request - displays the login form
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is already logged in
        if (SessionUtil.isLoggedIn(request)) {
            SessionUtil.redirectToDashboard(request, response);
            return;
        }
        
        // Show login page
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
    
    /**
     * Handles the HTTP POST request - processes the login form submission
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        // Basic validation
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Email and password are required");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }
        
        // Authenticate user
        User user = userDAO.authenticateUser(email, password);
        
        if (user != null) {
            // Set user in session
            SessionUtil.setUser(request, user);
            
            // Redirect to appropriate dashboard based on role
            SessionUtil.redirectToDashboard(request, response);
        } else {
            // Login failed
            request.setAttribute("error", "Invalid email or password");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}
