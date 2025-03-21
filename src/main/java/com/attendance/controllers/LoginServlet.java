package com.attendance.controllers;

import com.attendance.dao.UserDao;
import com.attendance.dao.impl.UserDaoImpl;
import com.attendance.models.User;
import com.attendance.utils.PasswordUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet handling user login
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());
    private UserDao userDao;
    
    @Override
    public void init() throws ServletException {
        super.init();
        userDao = new UserDaoImpl();
    }
    
    /**
     * Handles GET requests to show the login page
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Check if user is already logged in
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            // User is already logged in, redirect to dashboard
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        
        // Forward to login page
        request.getRequestDispatcher("/views/login.jsp").forward(request, response);
    }
    
    /**
     * Handles POST requests to process login form submission
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        // Validate inputs
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("errorMessage", "Email and password are required");
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            return;
        }
        
        try {
            // Attempt to authenticate user
            User user = userDao.findByEmail(email);
            
            if (user != null && PasswordUtils.checkPassword(password, user.getPassword())) {
                // User authenticated successfully
                
                // Check if account is active
                if (!"Active".equals(user.getStatus())) {
                    request.setAttribute("errorMessage", "Your account is not active. Please contact administrator.");
                    request.getRequestDispatcher("/views/login.jsp").forward(request, response);
                    return;
                }
                
                // Create session for authenticated user
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("userRole", user.getRole());
                session.setAttribute("userName", user.getFullName());
                
                // Log successful login
                LOGGER.info("User logged in: " + user.getEmail());
                
                // Redirect to dashboard
                response.sendRedirect(request.getContextPath() + "/dashboard");
            } else {
                // Authentication failed
                request.setAttribute("errorMessage", "Invalid email or password");
                request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during login", e);
            request.setAttribute("errorMessage", "A system error occurred. Please try again later.");
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
        }
    }
}