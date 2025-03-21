package com.attendance.controllers;

import com.attendance.dao.AttendanceDao;
import com.attendance.dao.LeaveApplicationDao;
import com.attendance.dao.UserDao;
import com.attendance.dao.impl.AttendanceDaoImpl;
import com.attendance.dao.impl.LeaveApplicationDaoImpl;
import com.attendance.dao.impl.UserDaoImpl;
import com.attendance.models.User;

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
 * Servlet handling the dashboard for different user roles
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DashboardServlet.class.getName());
    private UserDao userDao;
    private AttendanceDao attendanceDao;
    private LeaveApplicationDao leaveApplicationDao;
    
    @Override
    public void init() throws ServletException {
        super.init();
        userDao = new UserDaoImpl();
        attendanceDao = new AttendanceDaoImpl();
        leaveApplicationDao = new LeaveApplicationDaoImpl();
    }
    
    /**
     * Handles GET requests to show the appropriate dashboard based on user role
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        // Check if user is logged in
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // Get user information from session
        User user = (User) session.getAttribute("user");
        String role = user.getRole();
        
        try {
            // Set common dashboard attributes
            request.setAttribute("user", user);
            
            // Different dashboard views based on user role
            String dashboardPage;
            switch (role) {
                case "Admin":
                    prepareDashboardAdmin(request);
                    dashboardPage = "/views/admin/dashboard.jsp";
                    break;
                    
                case "Principal":
                    prepareDashboardPrincipal(request);
                    dashboardPage = "/views/principal/dashboard.jsp";
                    break;
                    
                case "HOD":
                    prepareDashboardHOD(request);
                    dashboardPage = "/views/hod/dashboard.jsp";
                    break;
                    
                case "Teacher":
                    prepareDashboardTeacher(request, user.getUserId());
                    dashboardPage = "/views/teacher/dashboard.jsp";
                    break;
                    
                case "Student":
                    prepareDashboardStudent(request, user.getUserId());
                    dashboardPage = "/views/student/dashboard.jsp";
                    break;
                    
                default:
                    // Unknown role, redirect to login
                    session.invalidate();
                    response.sendRedirect(request.getContextPath() + "/login");
                    return;
            }
            
            // Forward to the appropriate dashboard page
            // For this initial version, we'll use a simple dashboard page
            request.getRequestDispatcher("/views/dashboard.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading dashboard data", e);
            request.setAttribute("errorMessage", "Failed to load dashboard data: " + e.getMessage());
            request.getRequestDispatcher("/views/error/500.jsp").forward(request, response);
        }
    }
    
    /**
     * Prepare dashboard data for Admin
     */
    private void prepareDashboardAdmin(HttpServletRequest request) throws SQLException {
        // For now, just a placeholder method
        // In a full implementation, we would add statistics and admin-specific data
        request.setAttribute("totalUsers", userDao.countUsers());
        request.setAttribute("pendingUsers", userDao.countUsersByStatus("Pending"));
    }
    
    /**
     * Prepare dashboard data for Principal
     */
    private void prepareDashboardPrincipal(HttpServletRequest request) throws SQLException {
        // For now, just a placeholder method
        // In a full implementation, we would add school-wide statistics for the principal
    }
    
    /**
     * Prepare dashboard data for HOD
     */
    private void prepareDashboardHOD(HttpServletRequest request) throws SQLException {
        // For now, just a placeholder method
        // In a full implementation, we would add department-specific data
    }
    
    /**
     * Prepare dashboard data for Teacher
     */
    private void prepareDashboardTeacher(HttpServletRequest request, int teacherId) throws SQLException {
        // For now, just a placeholder method
        // In a full implementation, we would add teacher-specific data like classes, subjects, etc.
        request.setAttribute("pendingLeaveApplications", leaveApplicationDao.findByTeacher(teacherId));
    }
    
    /**
     * Prepare dashboard data for Student
     */
    private void prepareDashboardStudent(HttpServletRequest request, int studentId) throws SQLException {
        // For now, just a placeholder method
        // In a full implementation, we would add student attendance statistics, etc.
        request.setAttribute("attendancePercentage", attendanceDao.calculateAttendancePercentage(studentId));
        request.setAttribute("leaveApplications", leaveApplicationDao.findByStudent(studentId));
    }
}