package com.attendance.controllers;

import com.attendance.dao.*;
import com.attendance.models.*;
import com.attendance.utils.SessionUtil;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for handling enrollment requests
 */
@WebServlet("/enrollment/*")
public class EnrollmentRequestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private EnrollmentRequestDAO enrollmentRequestDAO = new EnrollmentRequestDAO();
    private StudentEnrollmentDAO studentEnrollmentDAO = new StudentEnrollmentDAO();
    private UserDAO userDAO = new UserDAO();
    private ClassDAO classDAO = new ClassDAO();
    private DepartmentDAO departmentDAO = new DepartmentDAO();
    
    /**
     * Handles the HTTP GET request
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is logged in
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // Show enrollment request form
            showEnrollmentRequestForm(request, response);
        } else if (pathInfo.equals("/pending")) {
            // Show pending enrollment requests
            showPendingRequests(request, response);
        } else if (pathInfo.startsWith("/approve/")) {
            // Show approval form for a specific request
            String requestIdStr = pathInfo.substring("/approve/".length());
            try {
                int requestId = Integer.parseInt(requestIdStr);
                showApprovalForm(request, response, requestId);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request ID");
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * Handles the HTTP POST request
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is logged in
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // Submit new enrollment request
            submitEnrollmentRequest(request, response);
        } else if (pathInfo.startsWith("/approve/")) {
            // Process approval/rejection of a request
            String requestIdStr = pathInfo.substring("/approve/".length());
            try {
                int requestId = Integer.parseInt(requestIdStr);
                processRequestApproval(request, response, requestId);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request ID");
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * Show the enrollment request form
     */
    private void showEnrollmentRequestForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        User currentUser = SessionUtil.getUser(request);
        
        // Check if user already has a pending request
        // Implementation depends on your data model - this is just an example
        List<EnrollmentRequest> pendingRequests = enrollmentRequestDAO.getRequestsByStatus("Pending");
        for (EnrollmentRequest pendingRequest : pendingRequests) {
            if (pendingRequest.getUserId() == currentUser.getUserId()) {
                request.setAttribute("error", "You already have a pending enrollment request.");
                request.getRequestDispatcher("/views/enrollment/request.jsp").forward(request, response);
                return;
            }
        }
        
        // Load departments and classes for the form
        List<Department> departments = departmentDAO.getAllDepartments();
        request.setAttribute("departments", departments);
        
        // Forward to the request form
        request.getRequestDispatcher("/views/enrollment/request.jsp").forward(request, response);
    }
    
    /**
     * Submit a new enrollment request
     */
    private void submitEnrollmentRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        User currentUser = SessionUtil.getUser(request);
        
        String requestedRole = request.getParameter("requestedRole");
        String classIdStr = request.getParameter("classId");
        String enrollmentNumber = request.getParameter("enrollmentNumber");
        String departmentIdStr = request.getParameter("departmentId");
        
        // Basic validation
        if (requestedRole == null || requestedRole.trim().isEmpty()) {
            request.setAttribute("error", "Requested role is required");
            showEnrollmentRequestForm(request, response);
            return;
        }
        
        // Create enrollment request object
        EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.setUserId(currentUser.getUserId());
        enrollmentRequest.setRequestedRole(requestedRole);
        enrollmentRequest.setStatus("Pending");
        
        // Set class ID if provided (required for students)
        if (classIdStr != null && !classIdStr.isEmpty()) {
            try {
                int classId = Integer.parseInt(classIdStr);
                enrollmentRequest.setClassId(classId);
            } catch (NumberFormatException e) {
                request.setAttribute("error", "Invalid class selected");
                showEnrollmentRequestForm(request, response);
                return;
            }
        } else if ("Student".equals(requestedRole)) {
            request.setAttribute("error", "Class is required for student enrollment");
            showEnrollmentRequestForm(request, response);
            return;
        }
        
        // Set enrollment number if provided (required for students)
        if (enrollmentNumber != null && !enrollmentNumber.trim().isEmpty()) {
            enrollmentRequest.setEnrollmentNumber(enrollmentNumber);
        } else if ("Student".equals(requestedRole)) {
            request.setAttribute("error", "Enrollment number is required for student enrollment");
            showEnrollmentRequestForm(request, response);
            return;
        }
        
        // Set department ID in user record if provided and not already set
        if (departmentIdStr != null && !departmentIdStr.isEmpty() && currentUser.getDepartmentId() == 0) {
            try {
                int departmentId = Integer.parseInt(departmentIdStr);
                currentUser.setDepartmentId(departmentId);
                userDAO.updateUser(currentUser);
            } catch (NumberFormatException e) {
                request.setAttribute("error", "Invalid department selected");
                showEnrollmentRequestForm(request, response);
                return;
            }
        }
        
        // Submit the request
        int requestId = enrollmentRequestDAO.createEnrollmentRequest(enrollmentRequest);
        
        if (requestId > 0) {
            request.setAttribute("success", "Enrollment request submitted successfully. It will be reviewed by the appropriate authority.");
            request.getRequestDispatcher("/views/enrollment/request.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "Failed to submit enrollment request. Please try again.");
            showEnrollmentRequestForm(request, response);
        }
    }
    
    /**
     * Show pending enrollment requests for the current user to approve
     */
    private void showPendingRequests(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        User currentUser = SessionUtil.getUser(request);
        String userRole = currentUser.getRole();
        List<EnrollmentRequest> pendingRequests = null;
        
        // Get pending requests based on user role
        if ("Principal".equals(userRole)) {
            pendingRequests = enrollmentRequestDAO.getPendingRequestsForVerifier("Principal", null);
        } else if ("HOD".equals(userRole)) {
            pendingRequests = enrollmentRequestDAO.getPendingRequestsForVerifier("HOD", currentUser.getDepartmentId());
        } else if ("Class Teacher".equals(userRole)) {
            pendingRequests = enrollmentRequestDAO.getPendingRequestsForVerifier("Class Teacher", currentUser.getDepartmentId());
        } else {
            // User not authorized to approve requests
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not authorized to approve enrollment requests");
            return;
        }
        
        request.setAttribute("pendingRequests", pendingRequests);
        request.getRequestDispatcher("/views/enrollment/approve.jsp").forward(request, response);
    }
    
    /**
     * Show the approval form for a specific request
     */
    private void showApprovalForm(HttpServletRequest request, HttpServletResponse response, int requestId) 
            throws ServletException, IOException {
        
        User currentUser = SessionUtil.getUser(request);
        String userRole = currentUser.getRole();
        
        // Get the enrollment request
        EnrollmentRequest enrollmentRequest = enrollmentRequestDAO.getRequestById(requestId);
        
        if (enrollmentRequest == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Enrollment request not found");
            return;
        }
        
        // Check if user is authorized to approve this request
        boolean authorized = false;
        if ("Principal".equals(userRole) && "HOD".equals(enrollmentRequest.getRequestedRole())) {
            authorized = true;
        } else if ("HOD".equals(userRole) && 
                  ("Teacher".equals(enrollmentRequest.getRequestedRole()) || 
                   "Class Teacher".equals(enrollmentRequest.getRequestedRole()))) {
            // Check if HOD belongs to the same department as the requester
            User requester = userDAO.getUserById(enrollmentRequest.getUserId());
            authorized = (requester.getDepartmentId() == currentUser.getDepartmentId());
        } else if ("Class Teacher".equals(userRole) && "Student".equals(enrollmentRequest.getRequestedRole())) {
            // Check if class teacher is assigned to the requested class
            authorized = teacherAssignmentDAO.isClassTeacher(currentUser.getUserId(), enrollmentRequest.getClassId());
        }
        
        if (!authorized) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not authorized to approve this request");
            return;
        }
        
        // Load the request details and forward to the approval form
        request.setAttribute("enrollmentRequest", enrollmentRequest);
        
        // Load user details
        User requester = userDAO.getUserById(enrollmentRequest.getUserId());
        request.setAttribute("requester", requester);
        
        // If request is for a student, load class details
        if ("Student".equals(enrollmentRequest.getRequestedRole())) {
            Class classObj = classDAO.getClassById(enrollmentRequest.getClassId());
            request.setAttribute("classObj", classObj);
            
            Department department = departmentDAO.getDepartmentById(classObj.getDepartmentId());
            request.setAttribute("department", department);
        }
        
        request.getRequestDispatcher("/views/enrollment/approve.jsp").forward(request, response);
    }
    
    /**
     * Process the approval or rejection of an enrollment request
     */
    private void processRequestApproval(HttpServletRequest request, HttpServletResponse response, int requestId) 
            throws ServletException, IOException {
        
        User currentUser = SessionUtil.getUser(request);
        String decision = request.getParameter("decision");
        
        if (decision == null || !(decision.equals("Approved") || decision.equals("Rejected"))) {
            request.setAttribute("error", "Invalid decision. Please select either Approve or Reject.");
            showApprovalForm(request, response, requestId);
            return;
        }
        
        // Get the enrollment request
        EnrollmentRequest enrollmentRequest = enrollmentRequestDAO.getRequestById(requestId);
        
        if (enrollmentRequest == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Enrollment request not found");
            return;
        }
        
        // Update the request status
        boolean updated = enrollmentRequestDAO.updateRequestStatus(requestId, decision, currentUser.getUserId());
        
        if (!updated) {
            request.setAttribute("error", "Failed to update request status. Please try again.");
            showApprovalForm(request, response, requestId);
            return;
        }
        
        // If request is approved, additional steps are needed
        if (decision.equals("Approved")) {
            User requester = userDAO.getUserById(enrollmentRequest.getUserId());
            
            // Update user role
            requester.setRole(enrollmentRequest.getRequestedRole());
            userDAO.updateUser(requester);
            
            // If student, create enrollment record
            if ("Student".equals(enrollmentRequest.getRequestedRole())) {
                StudentEnrollment enrollment = new StudentEnrollment();
                enrollment.setEnrollmentId(enrollmentRequest.getEnrollmentNumber());
                enrollment.setUserId(requester.getUserId());
                enrollment.setClassId(enrollmentRequest.getClassId());
                
                // Set academic year to current year
                String academicYear = String.valueOf(java.time.Year.now().getValue());
                enrollment.setAcademicYear(academicYear);
                
                enrollment.setEnrollmentStatus("Active");
                
                studentEnrollmentDAO.createEnrollment(enrollment);
            }
        }
        
        request.setAttribute("success", "Enrollment request has been " + decision.toLowerCase() + " successfully.");
        response.sendRedirect(request.getContextPath() + "/enrollment/pending");
    }
    
    // Required by the DashboardServlet
    private TeacherAssignmentDAO teacherAssignmentDAO = new TeacherAssignmentDAO();
}
