package com.attendance.controllers;

import com.attendance.dao.UserDao;
import com.attendance.dao.impl.UserDaoImpl;
import com.attendance.models.User;
import com.attendance.utils.PasswordUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for authentication
 */
@WebServlet(urlPatterns = {"/login", "/logout", "/register"})
public class AuthController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());
    private final UserDao userDAO = new UserDaoImpl();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getServletPath();
        
        switch (path) {
            case "/login":
                // Show login page
                request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
                break;
                
            case "/logout":
                // Handle logout
                handleLogout(request, response);
                break;
                
            case "/register":
                // Show registration page
                request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
                break;
                
            default:
                response.sendRedirect(request.getContextPath() + "/login");
                break;
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getServletPath();
        
        switch (path) {
            case "/login":
                // Handle login
                handleLogin(request, response);
                break;
                
            case "/register":
                // Handle registration
                handleRegistration(request, response);
                break;
                
            default:
                response.sendRedirect(request.getContextPath() + "/login");
                break;
        }
    }
    
    /**
     * Handle user login
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("errorMessage", "Email and password are required");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }
        
        try {
            Optional<User> userOpt = userDAO.authenticateOptional(email, password);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                if (!"Active".equals(user.getStatus())) {
                    request.setAttribute("errorMessage", "Your account is not active");
                    request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
                    return;
                }
                
                // Create session for the user
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("userRole", user.getRole());
                session.setAttribute("userName", user.getFullName());
                
                LOGGER.info("User logged in: " + user.getEmail());
                
                // Redirect based on role
                redirectByRole(user, request, response);
                
            } else {
                request.setAttribute("errorMessage", "Invalid email or password");
                request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during login", e);
            request.setAttribute("errorMessage", "An error occurred during login");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
        }
    }
    
    /**
     * Handle user registration
     */
    private void handleRegistration(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String role = "Student";  // Default role for registration is Student
        
        // Validate input
        if (fullName == null || fullName.isEmpty() || 
            email == null || email.isEmpty() || 
            password == null || password.isEmpty() ||
            confirmPassword == null || confirmPassword.isEmpty()) {
            
            request.setAttribute("errorMessage", "All required fields must be filled");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Passwords do not match");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
            return;
        }
        
        try {
            // Check if email already exists
            User existingUser = userDAO.getByEmail(email);
            if (existingUser != null) {
                request.setAttribute("errorMessage", "Email already registered");
                request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
                return;
            }
            
            // Create a new user
            User user = new User(fullName, email, password, role);
            user.setPhone(phone);
            user.setAddress(address);
            
            // Save the user
            User createdUser = userDAO.create(user);
            
            LOGGER.info("User registered: " + createdUser.getEmail());
            
            // Set success message and redirect to login
            request.setAttribute("successMessage", "Registration successful! Please login to continue.");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during registration", e);
            request.setAttribute("errorMessage", "An error occurred during registration");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
        }
    }
    
    /**
     * Handle user logout
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            session.invalidate();
        }
        
        response.sendRedirect(request.getContextPath() + "/login");
    }
    
    /**
     * Redirect user based on role
     */
    private void redirectByRole(User user, HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String contextPath = request.getContextPath();
        
        switch (user.getRole()) {
            case "Admin":
                response.sendRedirect(contextPath + "/admin/dashboard");
                break;
                
            case "Principal":
                response.sendRedirect(contextPath + "/principal/dashboard");
                break;
                
            case "HOD":
                response.sendRedirect(contextPath + "/hod/dashboard");
                break;
                
            case "Teacher":
                response.sendRedirect(contextPath + "/teacher/dashboard");
                break;
                
            case "Student":
                response.sendRedirect(contextPath + "/student/dashboard");
                break;
                
            default:
                response.sendRedirect(contextPath + "/dashboard");
                break;
        }
    }
}