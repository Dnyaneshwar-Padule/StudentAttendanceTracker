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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for user management operations
 */
@WebServlet(name = "UserController", urlPatterns = {"/admin/users/*", "/profile/*"})
public class UserController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());
    private UserDao userDao;

    @Override
    public void init() throws ServletException {
        super.init();
        userDao = new UserDaoImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();

        if (servletPath.equals("/admin/users")) {
            if (pathInfo == null || pathInfo.equals("/")) {
                // List all users
                listUsers(request, response);
            } else if (pathInfo.startsWith("/edit/")) {
                // Show edit form for a specific user
                displayEditForm(request, response, pathInfo);
            } else if (pathInfo.equals("/create")) {
                // Show create user form
                displayCreateForm(request, response);
            } else if (pathInfo.startsWith("/view/")) {
                // View user details
                viewUserDetails(request, response, pathInfo);
            } else if (pathInfo.startsWith("/delete/")) {
                // Delete a user
                deleteUser(request, response, pathInfo);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else if (servletPath.equals("/profile")) {
            // User profile management
            if (pathInfo == null || pathInfo.equals("/")) {
                // Display user profile
                displayUserProfile(request, response);
            } else if (pathInfo.equals("/edit")) {
                // Show edit profile form
                displayEditProfileForm(request, response);
            } else if (pathInfo.equals("/change-password")) {
                // Show change password form
                displayChangePasswordForm(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();

        if (servletPath.equals("/admin/users")) {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Create user (form submission)
                createUser(request, response);
            } else if (pathInfo.startsWith("/edit/")) {
                // Update user (form submission)
                updateUser(request, response, pathInfo);
            } else if (pathInfo.equals("/bulk-update")) {
                // Handle bulk actions
                bulkUpdateUsers(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else if (servletPath.equals("/profile")) {
            if (pathInfo.equals("/edit")) {
                // Update profile (form submission)
                updateProfile(request, response);
            } else if (pathInfo.equals("/change-password")) {
                // Change password (form submission)
                changePassword(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * List all users with optional filters
     */
    private void listUsers(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String roleFilter = request.getParameter("role");
            String statusFilter = request.getParameter("status");
            String searchQuery = request.getParameter("query");
            
            List<User> users;
            
            if (roleFilter != null && !roleFilter.isEmpty()) {
                users = userDao.findByRole(roleFilter);
            } else if (statusFilter != null && !statusFilter.isEmpty()) {
                users = userDao.findByStatus(statusFilter);
            } else if (searchQuery != null && !searchQuery.isEmpty()) {
                users = userDao.searchUsers(searchQuery);
            } else {
                users = userDao.findAll();
            }
            
            request.setAttribute("users", users);
            request.getRequestDispatcher("/WEB-INF/views/admin/users/list.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while listing users", e);
            request.setAttribute("error", "Failed to retrieve users. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/admin/users/list.jsp").forward(request, response);
        }
    }

    /**
     * Display the user creation form
     */
    private void displayCreateForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/admin/users/create.jsp").forward(request, response);
    }

    /**
     * Create a new user from form submission
     */
    private void createUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        String status = request.getParameter("status");
        
        if (fullName == null || fullName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            role == null || role.trim().isEmpty() ||
            status == null || status.trim().isEmpty()) {
            
            request.setAttribute("error", "All fields are required");
            request.getRequestDispatcher("/WEB-INF/views/admin/users/create.jsp").forward(request, response);
            return;
        }
        
        try {
            // Check if email already exists
            User existingUser = userDao.findByEmail(email);
            if (existingUser != null) {
                request.setAttribute("error", "Email already registered");
                request.getRequestDispatcher("/WEB-INF/views/admin/users/create.jsp").forward(request, response);
                return;
            }
            
            // Create new user
            User newUser = new User();
            newUser.setFullName(fullName);
            newUser.setEmail(email);
            newUser.setPassword(PasswordUtils.generateSecurePassword(password));
            newUser.setRole(role);
            newUser.setStatus(status);
            
            User savedUser = userDao.save(newUser);
            
            if (savedUser != null) {
                response.sendRedirect(request.getContextPath() + "/admin/users?success=created");
            } else {
                request.setAttribute("error", "Failed to create user. Please try again.");
                request.getRequestDispatcher("/WEB-INF/views/admin/users/create.jsp").forward(request, response);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while creating user", e);
            request.setAttribute("error", "A system error occurred. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/admin/users/create.jsp").forward(request, response);
        }
    }

    /**
     * Display the user edit form
     */
    private void displayEditForm(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws ServletException, IOException {
        try {
            int userId = extractIdFromPath(pathInfo, "/edit/");
            
            User user = userDao.findById(userId);
            if (user == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }
            
            request.setAttribute("user", user);
            request.getRequestDispatcher("/WEB-INF/views/admin/users/edit.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while fetching user for edit", e);
            response.sendRedirect(request.getContextPath() + "/admin/users?error=database");
        }
    }

    /**
     * Update a user from form submission
     */
    private void updateUser(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws ServletException, IOException {
        try {
            int userId = extractIdFromPath(pathInfo, "/edit/");
            
            User user = userDao.findById(userId);
            if (user == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }
            
            String fullName = request.getParameter("fullName");
            String email = request.getParameter("email");
            String role = request.getParameter("role");
            String status = request.getParameter("status");
            String password = request.getParameter("password");
            
            if (fullName == null || fullName.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                role == null || role.trim().isEmpty() ||
                status == null || status.trim().isEmpty()) {
                
                request.setAttribute("error", "Full name, email, role, and status are required");
                request.setAttribute("user", user);
                request.getRequestDispatcher("/WEB-INF/views/admin/users/edit.jsp").forward(request, response);
                return;
            }
            
            // Check if email already exists for another user
            User existingUser = userDao.findByEmail(email);
            if (existingUser != null && existingUser.getUserId() != userId) {
                request.setAttribute("error", "Email already registered to another user");
                request.setAttribute("user", user);
                request.getRequestDispatcher("/WEB-INF/views/admin/users/edit.jsp").forward(request, response);
                return;
            }
            
            // Update user fields
            user.setFullName(fullName);
            user.setEmail(email);
            user.setRole(role);
            user.setStatus(status);
            
            // Update password if provided
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(PasswordUtils.generateSecurePassword(password));
            }
            
            User updatedUser = userDao.update(user);
            
            if (updatedUser != null) {
                response.sendRedirect(request.getContextPath() + "/admin/users?success=updated");
            } else {
                request.setAttribute("error", "Failed to update user. Please try again.");
                request.setAttribute("user", user);
                request.getRequestDispatcher("/WEB-INF/views/admin/users/edit.jsp").forward(request, response);
            }
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while updating user", e);
            response.sendRedirect(request.getContextPath() + "/admin/users?error=database");
        }
    }

    /**
     * View user details
     */
    private void viewUserDetails(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws ServletException, IOException {
        try {
            int userId = extractIdFromPath(pathInfo, "/view/");
            
            User user = userDao.findById(userId);
            if (user == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }
            
            request.setAttribute("user", user);
            request.getRequestDispatcher("/WEB-INF/views/admin/users/view.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while fetching user details", e);
            response.sendRedirect(request.getContextPath() + "/admin/users?error=database");
        }
    }

    /**
     * Delete a user
     */
    private void deleteUser(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws ServletException, IOException {
        try {
            int userId = extractIdFromPath(pathInfo, "/delete/");
            
            // Prevent deleting the currently logged-in user
            HttpSession session = request.getSession(false);
            User currentUser = (User) session.getAttribute("user");
            
            if (currentUser != null && currentUser.getUserId() == userId) {
                response.sendRedirect(request.getContextPath() + "/admin/users?error=selfDelete");
                return;
            }
            
            boolean deleted = userDao.delete(userId);
            
            if (deleted) {
                response.sendRedirect(request.getContextPath() + "/admin/users?success=deleted");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/users?error=deleteFailed");
            }
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while deleting user", e);
            response.sendRedirect(request.getContextPath() + "/admin/users?error=database");
        }
    }

    /**
     * Bulk update users (status, role, etc.)
     */
    private void bulkUpdateUsers(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] userIds = request.getParameterValues("selectedUsers");
        String action = request.getParameter("bulkAction");
        String value = request.getParameter("bulkValue");
        
        if (userIds == null || userIds.length == 0 || action == null || action.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/users?error=invalidBulkAction");
            return;
        }
        
        try {
            int successCount = 0;
            HttpSession session = request.getSession(false);
            User currentUser = (User) session.getAttribute("user");
            
            for (String idStr : userIds) {
                try {
                    int userId = Integer.parseInt(idStr);
                    
                    // Prevent modifying the currently logged-in user
                    if (currentUser != null && currentUser.getUserId() == userId) {
                        continue;
                    }
                    
                    User user = userDao.findById(userId);
                    if (user != null) {
                        boolean updated = false;
                        
                        switch (action) {
                            case "status":
                                user.setStatus(value);
                                updated = (userDao.update(user) != null);
                                break;
                            case "role":
                                user.setRole(value);
                                updated = (userDao.update(user) != null);
                                break;
                            case "delete":
                                updated = userDao.delete(userId);
                                break;
                        }
                        
                        if (updated) {
                            successCount++;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid IDs
                    LOGGER.log(Level.WARNING, "Invalid user ID in bulk operation: " + idStr);
                }
            }
            
            if (successCount > 0) {
                response.sendRedirect(request.getContextPath() + "/admin/users?success=bulkUpdated&count=" + successCount);
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/users?error=bulkUpdateFailed");
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during bulk user update", e);
            response.sendRedirect(request.getContextPath() + "/admin/users?error=database");
        }
    }

    /**
     * Display user profile page
     */
    private void displayUserProfile(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        try {
            // Refresh user data from database
            User currentUser = userDao.findById(user.getUserId());
            if (currentUser == null) {
                // Session user no longer exists in database
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/auth/login");
                return;
            }
            
            request.setAttribute("user", currentUser);
            request.getRequestDispatcher("/WEB-INF/views/profile/view.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while fetching user profile", e);
            request.setAttribute("error", "Failed to load profile. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/profile/view.jsp").forward(request, response);
        }
    }

    /**
     * Display edit profile form
     */
    private void displayEditProfileForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        try {
            // Refresh user data from database
            User currentUser = userDao.findById(user.getUserId());
            if (currentUser == null) {
                // Session user no longer exists in database
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/auth/login");
                return;
            }
            
            request.setAttribute("user", currentUser);
            request.getRequestDispatcher("/WEB-INF/views/profile/edit.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while preparing edit profile form", e);
            response.sendRedirect(request.getContextPath() + "/profile?error=database");
        }
    }

    /**
     * Update user profile from form submission
     */
    private void updateProfile(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        try {
            // Refresh user data from database
            User currentUser = userDao.findById(user.getUserId());
            if (currentUser == null) {
                // Session user no longer exists in database
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/auth/login");
                return;
            }
            
            String fullName = request.getParameter("fullName");
            String email = request.getParameter("email");
            
            if (fullName == null || fullName.trim().isEmpty() || email == null || email.trim().isEmpty()) {
                request.setAttribute("error", "Full name and email are required");
                request.setAttribute("user", currentUser);
                request.getRequestDispatcher("/WEB-INF/views/profile/edit.jsp").forward(request, response);
                return;
            }
            
            // Check if email already exists for another user
            User existingUser = userDao.findByEmail(email);
            if (existingUser != null && existingUser.getUserId() != currentUser.getUserId()) {
                request.setAttribute("error", "Email already registered to another user");
                request.setAttribute("user", currentUser);
                request.getRequestDispatcher("/WEB-INF/views/profile/edit.jsp").forward(request, response);
                return;
            }
            
            // Update user fields
            currentUser.setFullName(fullName);
            currentUser.setEmail(email);
            
            User updatedUser = userDao.update(currentUser);
            
            if (updatedUser != null) {
                // Update session
                session.setAttribute("user", updatedUser);
                response.sendRedirect(request.getContextPath() + "/profile?success=updated");
            } else {
                request.setAttribute("error", "Failed to update profile. Please try again.");
                request.setAttribute("user", currentUser);
                request.getRequestDispatcher("/WEB-INF/views/profile/edit.jsp").forward(request, response);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while updating profile", e);
            response.sendRedirect(request.getContextPath() + "/profile?error=database");
        }
    }

    /**
     * Display change password form
     */
    private void displayChangePasswordForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/profile/change-password.jsp").forward(request, response);
    }

    /**
     * Change user password from form submission
     */
    private void changePassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        
        if (currentPassword == null || currentPassword.trim().isEmpty() ||
            newPassword == null || newPassword.trim().isEmpty() ||
            confirmPassword == null || confirmPassword.trim().isEmpty()) {
            
            request.setAttribute("error", "All fields are required");
            request.getRequestDispatcher("/WEB-INF/views/profile/change-password.jsp").forward(request, response);
            return;
        }
        
        // Validate new password
        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "New passwords do not match");
            request.getRequestDispatcher("/WEB-INF/views/profile/change-password.jsp").forward(request, response);
            return;
        }
        
        try {
            // Verify current password
            User currentUser = userDao.findById(user.getUserId());
            if (currentUser == null) {
                // Session user no longer exists in database
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/auth/login");
                return;
            }
            
            if (!PasswordUtils.verifySecurePassword(currentPassword, currentUser.getPassword())) {
                request.setAttribute("error", "Current password is incorrect");
                request.getRequestDispatcher("/WEB-INF/views/profile/change-password.jsp").forward(request, response);
                return;
            }
            
            // Update password
            currentUser.setPassword(PasswordUtils.generateSecurePassword(newPassword));
            User updatedUser = userDao.update(currentUser);
            
            if (updatedUser != null) {
                // Update session
                session.setAttribute("user", updatedUser);
                response.sendRedirect(request.getContextPath() + "/profile?success=passwordChanged");
            } else {
                request.setAttribute("error", "Failed to change password. Please try again.");
                request.getRequestDispatcher("/WEB-INF/views/profile/change-password.jsp").forward(request, response);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while changing password", e);
            request.setAttribute("error", "A system error occurred. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/profile/change-password.jsp").forward(request, response);
        }
    }

    /**
     * Helper method to extract ID from path
     */
    private int extractIdFromPath(String pathInfo, String prefix) {
        String idStr = pathInfo.substring(prefix.length());
        return Integer.parseInt(idStr);
    }
}