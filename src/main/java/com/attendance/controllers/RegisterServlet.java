package com.attendance.controllers;

import com.attendance.dao.DepartmentDao;
import com.attendance.dao.UserDao;
import com.attendance.dao.impl.DepartmentDaoImpl;
import com.attendance.dao.impl.UserDaoImpl;
import com.attendance.models.Department;
import com.attendance.models.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for handling user registration
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(RegisterServlet.class.getName());
    
    private UserDao userDao = new UserDaoImpl();
    private DepartmentDao departmentDao = new DepartmentDaoImpl();
    
    /**
     * Handles the HTTP GET request - displays the registration form
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Load all departments for the registration form
            List<Department> departments = departmentDao.findAll();
            request.setAttribute("departments", departments);
            
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading departments", e);
            request.setAttribute("error", "Error loading departments: " + e.getMessage());
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }
    
    /**
     * Handles the HTTP POST request - processes the registration form submission
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String name = request.getParameter("name");
        String phoneNo = request.getParameter("phoneNo");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String role = request.getParameter("role");
        String departmentIdStr = request.getParameter("departmentId");
        
        // Basic validation
        if (name == null || name.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            role == null || role.trim().isEmpty()) {
            
            request.setAttribute("error", "All fields marked with * are required");
            loadDepartments(request, response);
            return;
        }
        
        // Password confirmation
        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match");
            loadDepartments(request, response);
            return;
        }
        
        // Email format validation (basic check)
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            request.setAttribute("error", "Invalid email format");
            loadDepartments(request, response);
            return;
        }
        
        try {
            // Check if email already exists
            User existingUser = userDao.findByEmail(email);
            if (existingUser != null) {
                request.setAttribute("error", "Email already in use");
                loadDepartments(request, response);
                return;
            }
            
            // Parse department ID if provided
            int departmentId = 0;
            if (departmentIdStr != null && !departmentIdStr.isEmpty()) {
                try {
                    departmentId = Integer.parseInt(departmentIdStr);
                } catch (NumberFormatException e) {
                    // Invalid department ID
                    request.setAttribute("error", "Invalid department selected");
                    loadDepartments(request, response);
                    return;
                }
            }
            
            // Create user object
            User user = new User();
            user.setName(name);
            user.setPhoneNo(phoneNo);
            user.setEmail(email);
            user.setPassword(password);
            user.setRole(role);
            user.setActive(true); // Set as active by default
            
            if (departmentId > 0) {
                user.setDepartmentId(departmentId);
            }
            
            // Register the user
            User registeredUser = userDao.registerUser(user);
            
            if (registeredUser != null && registeredUser.getUserId() > 0) {
                // Registration successful
                request.setAttribute("success", "Registration successful! You can now login.");
                response.sendRedirect(request.getContextPath() + "/login");
            } else {
                // Registration failed
                request.setAttribute("error", "Registration failed. Please try again.");
                loadDepartments(request, response);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error during registration process", e);
            request.setAttribute("error", "Registration error: " + e.getMessage());
            loadDepartments(request, response);
        }
    }
    
    /**
     * Helper method to load departments and forward to the registration page
     */
    private void loadDepartments(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            List<Department> departments = departmentDao.findAll();
            request.setAttribute("departments", departments);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading departments", e);
            request.setAttribute("departments", new ArrayList<>());
        }
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }
}
