package com.attendance.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import com.attendance.models.User;

/**
 * Utility class for session management
 */
public class SessionUtil {

    /**
     * Sets a user in the session after successful login
     * @param request The HTTP request
     * @param user The user to store in session
     */
    public static void setUser(HttpServletRequest request, User user) {
        HttpSession session = request.getSession();
        session.setAttribute("user", user);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getName());
        session.setAttribute("userRole", user.getRole());
        session.setAttribute("departmentId", user.getDepartmentId());
    }

    /**
     * Gets the currently logged in user from session
     * @param request The HTTP request
     * @return The User object or null if not logged in
     */
    public static User getUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (User) session.getAttribute("user");
        }
        return null;
    }

    /**
     * Checks if a user is logged in
     * @param request The HTTP request
     * @return true if logged in, false otherwise
     */
    public static boolean isLoggedIn(HttpServletRequest request) {
        return getUser(request) != null;
    }

    /**
     * Checks if the current user has a specific role
     * @param request The HTTP request
     * @param role The role to check for
     * @return true if user has the role, false otherwise
     */
    public static boolean hasRole(HttpServletRequest request, String role) {
        User user = getUser(request);
        if (user != null) {
            return user.getRole().equals(role);
        }
        return false;
    }

    /**
     * Checks if the current user has any of the specified roles
     * @param request The HTTP request
     * @param roles Array of roles to check
     * @return true if user has any of the roles, false otherwise
     */
    public static boolean hasAnyRole(HttpServletRequest request, String... roles) {
        User user = getUser(request);
        if (user != null) {
            for (String role : roles) {
                if (user.getRole().equals(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Redirects to the appropriate dashboard based on user role
     * @param request The HTTP request
     * @param response The HTTP response
     * @throws IOException If an input or output exception occurs
     */
    public static void redirectToDashboard(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = getUser(request);
        if (user != null) {
            switch (user.getRole()) {
                case "Student":
                    response.sendRedirect(request.getContextPath() + "/views/student/dashboard.jsp");
                    break;
                case "Teacher":
                    response.sendRedirect(request.getContextPath() + "/views/teacher/dashboard.jsp");
                    break;
                case "Class Teacher":
                    response.sendRedirect(request.getContextPath() + "/views/classteacher/dashboard.jsp");
                    break;
                case "HOD":
                    response.sendRedirect(request.getContextPath() + "/views/hod/dashboard.jsp");
                    break;
                case "Principal":
                    response.sendRedirect(request.getContextPath() + "/views/principal/dashboard.jsp");
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/login.jsp");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }

    /**
     * Invalidates the current session and redirects to login page
     * @param request The HTTP request
     * @param response The HTTP response
     * @throws IOException If an input or output exception occurs
     */
    public static void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
}
