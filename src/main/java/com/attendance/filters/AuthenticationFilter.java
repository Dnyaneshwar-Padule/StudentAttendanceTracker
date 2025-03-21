package com.attendance.filters;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Filter for authentication and authorization
 */
@WebFilter(urlPatterns = {"/*"})
public class AuthenticationFilter implements Filter {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationFilter.class.getName());
    
    // Public URLs that don't require authentication
    private static final List<String> PUBLIC_URLS = Arrays.asList(
            "/login", "/logout", "/register", 
            "/assets", "/css", "/js", "/images", 
            "/error", "/favicon.ico"
    );
    
    // Role-specific URL prefixes
    private static final List<String> ADMIN_URLS = Arrays.asList("/admin");
    private static final List<String> PRINCIPAL_URLS = Arrays.asList("/principal");
    private static final List<String> HOD_URLS = Arrays.asList("/hod");
    private static final List<String> TEACHER_URLS = Arrays.asList("/teacher");
    private static final List<String> STUDENT_URLS = Arrays.asList("/student");
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("AuthenticationFilter initialized");
    }
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);
        
        String path = request.getServletPath();
        String contextPath = request.getContextPath();
        
        // Allow public URLs without authentication
        if (isPublicUrl(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Check if user is logged in
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(contextPath + "/login");
            return;
        }
        
        // Get user role from session
        String userRole = (String) session.getAttribute("userRole");
        
        // Check role-based access
        if (!hasAccess(path, userRole)) {
            LOGGER.warning("Access denied for user with role " + userRole + " to URL " + path);
            response.sendRedirect(contextPath + "/error?code=403");
            return;
        }
        
        // User is authenticated and authorized
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        LOGGER.info("AuthenticationFilter destroyed");
    }
    
    /**
     * Check if the URL is public (no authentication required)
     */
    private boolean isPublicUrl(String path) {
        return PUBLIC_URLS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * Check if the user has access to the requested URL based on their role
     */
    private boolean hasAccess(String path, String role) {
        if (role == null) {
            return false;
        }
        
        switch (role) {
            case "Admin":
                // Admin has access to all URLs
                return true;
                
            case "Principal":
                // Principal can access principal, HOD, teacher, and student URLs
                return PRINCIPAL_URLS.stream().anyMatch(path::startsWith) || 
                       HOD_URLS.stream().anyMatch(path::startsWith) || 
                       TEACHER_URLS.stream().anyMatch(path::startsWith) || 
                       STUDENT_URLS.stream().anyMatch(path::startsWith);
                
            case "HOD":
                // HOD can access HOD, teacher, and student URLs
                return HOD_URLS.stream().anyMatch(path::startsWith) || 
                       TEACHER_URLS.stream().anyMatch(path::startsWith) || 
                       STUDENT_URLS.stream().anyMatch(path::startsWith);
                
            case "Teacher":
                // Teacher can access teacher and student URLs
                return TEACHER_URLS.stream().anyMatch(path::startsWith) || 
                       STUDENT_URLS.stream().anyMatch(path::startsWith);
                
            case "Student":
                // Student can only access student URLs
                return STUDENT_URLS.stream().anyMatch(path::startsWith);
                
            default:
                return false;
        }
    }
}