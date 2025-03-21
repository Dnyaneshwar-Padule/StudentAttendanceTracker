package com.attendance.controllers;

import com.attendance.dao.DepartmentDAO;
import com.attendance.dao.UserDAO;
import com.attendance.models.Department;
import com.attendance.models.User;

import java.io.IOException;
import java.util.List;
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
    private UserDAO userDAO = new UserDAO();
    private DepartmentDAO departmentDAO = new DepartmentDAO();
    
    /**
     * Handles the HTTP GET request - displays the registration form
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Load all departments for the registration form
        List<Department> departments = departmentDAO.getAllDepartments();
        request.setAttribute("departments", departments);
        
        request.getRequestDispatcher("/register.jsp").forward(request, response);
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
            
            // Reload departments for the form
            List<Department> departments = departmentDAO.getAllDepartments();
            request.setAttribute("departments", departments);
            
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }
        
        // Password confirmation
        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match");
            
            // Reload departments for the form
            List<Department> departments = departmentDAO.getAllDepartments();
            request.setAttribute("departments", departments);
            
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }
        
        // Email format validation (basic check)
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            request.setAttribute("error", "Invalid email format");
            
            // Reload departments for the form
            List<Department> departments = departmentDAO.getAllDepartments();
            request.setAttribute("departments", departments);
            
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }
        
        // Check if email already exists
        if (userDAO.userExistsByEmail(email)) {
            request.setAttribute("error", "Email already in use");
            
            // Reload departments for the form
            List<Department> departments = departmentDAO.getAllDepartments();
            request.setAttribute("departments", departments);
            
            request.getRequestDispatcher("/register.jsp").forward(request, response);
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
                
                // Reload departments for the form
                List<Department> departments = departmentDAO.getAllDepartments();
                request.setAttribute("departments", departments);
                
                request.getRequestDispatcher("/register.jsp").forward(request, response);
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
        
        if (departmentId > 0) {
            user.setDepartmentId(departmentId);
        }
        
        // Register the user
        int userId = userDAO.registerUser(user);
        
        if (userId > 0) {
            // Registration successful
            request.setAttribute("success", "Registration successful! You can now login.");
            response.sendRedirect(request.getContextPath() + "/login");
        } else {
            // Registration failed
            request.setAttribute("error", "Registration failed. Please try again.");
            
            // Reload departments for the form
            List<Department> departments = departmentDAO.getAllDepartments();
            request.setAttribute("departments", departments);
            
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }
}
