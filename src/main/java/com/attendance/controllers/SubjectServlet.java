package com.attendance.controllers;

import com.attendance.dao.*;
import com.attendance.models.*;
import com.attendance.utils.SessionUtil;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for handling subject-related operations
 */
@WebServlet("/subjects/*")
public class SubjectServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private SubjectDAO subjectDAO = new SubjectDAO();
    private DepartmentDAO departmentDAO = new DepartmentDAO();
    private ClassDAO classDAO = new ClassDAO();
    
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
        
        // Check if user is HOD or Principal
        User currentUser = SessionUtil.getUser(request);
        if (!("HOD".equals(currentUser.getRole()) || "Principal".equals(currentUser.getRole()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // List all subjects
            List<Subject> subjects = subjectDAO.getAllSubjects();
            request.setAttribute("subjects", subjects);
            
            // Get departments for filtering
            List<Department> departments = departmentDAO.getAllDepartments();
            request.setAttribute("departments", departments);
            
            request.getRequestDispatcher("/views/subjects/list.jsp").forward(request, response);
        } else if (pathInfo.equals("/add")) {
            // Show add subject form
            request.getRequestDispatcher("/views/subjects/add.jsp").forward(request, response);
        } else if (pathInfo.startsWith("/edit/")) {
            // Show edit subject form
            String subjectCode = pathInfo.substring("/edit/".length());
            Subject subject = subjectDAO.getSubjectByCode(subjectCode);
            
            if (subject == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Subject not found");
                return;
            }
            
            request.setAttribute("subject", subject);
            request.getRequestDispatcher("/views/subjects/edit.jsp").forward(request, response);
        } else if (pathInfo.equals("/assign")) {
            // Show assign subject to department/class form
            List<Subject> subjects = subjectDAO.getAllSubjects();
            request.setAttribute("subjects", subjects);
            
            List<Department> departments = departmentDAO.getAllDepartments();
            request.setAttribute("departments", departments);
            
            // If department is selected, load classes
            String departmentIdStr = request.getParameter("departmentId");
            if (departmentIdStr != null && !departmentIdStr.isEmpty()) {
                try {
                    int departmentId = Integer.parseInt(departmentIdStr);
                    List<com.attendance.models.Class> classes = classDAO.getClassesByDepartment(departmentId);
                    request.setAttribute("classes", classes);
                    request.setAttribute("selectedDepartmentId", departmentId);
                } catch (NumberFormatException e) {
                    request.setAttribute("error", "Invalid department ID");
                }
            }
            
            request.getRequestDispatcher("/views/subjects/assign.jsp").forward(request, response);
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
        
        // Check if user is HOD or Principal
        User currentUser = SessionUtil.getUser(request);
        if (!("HOD".equals(currentUser.getRole()) || "Principal".equals(currentUser.getRole()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo.equals("/add")) {
            // Process add subject form
            addSubject(request, response);
        } else if (pathInfo.equals("/edit")) {
            // Process edit subject form
            editSubject(request, response);
        } else if (pathInfo.equals("/assign")) {
            // Process assign subject form
            assignSubject(request, response);
        } else if (pathInfo.equals("/remove")) {
            // Process remove subject assignment
            removeSubjectAssignment(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * Process adding a new subject
     */
    private void addSubject(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String subjectCode = request.getParameter("subjectCode");
        String subjectName = request.getParameter("subjectName");
        
        // Basic validation
        if (subjectCode == null || subjectCode.trim().isEmpty() || 
            subjectName == null || subjectName.trim().isEmpty()) {
            
            request.setAttribute("error", "Subject code and name are required");
            request.getRequestDispatcher("/views/subjects/add.jsp").forward(request, response);
            return;
        }
        
        // Check if subject already exists
        if (subjectDAO.subjectExistsByCode(subjectCode)) {
            request.setAttribute("error", "Subject with this code already exists");
            request.getRequestDispatcher("/views/subjects/add.jsp").forward(request, response);
            return;
        }
        
        // Create new subject
        Subject subject = new Subject(subjectCode, subjectName);
        boolean created = subjectDAO.createSubject(subject);
        
        if (created) {
            request.setAttribute("success", "Subject added successfully");
            response.sendRedirect(request.getContextPath() + "/subjects/");
        } else {
            request.setAttribute("error", "Failed to add subject");
            request.getRequestDispatcher("/views/subjects/add.jsp").forward(request, response);
        }
    }
    
    /**
     * Process editing an existing subject
     */
    private void editSubject(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String subjectCode = request.getParameter("subjectCode");
        String subjectName = request.getParameter("subjectName");
        
        // Basic validation
        if (subjectCode == null || subjectCode.trim().isEmpty() || 
            subjectName == null || subjectName.trim().isEmpty()) {
            
            request.setAttribute("error", "Subject code and name are required");
            response.sendRedirect(request.getContextPath() + "/subjects/edit/" + subjectCode);
            return;
        }
        
        // Check if subject exists
        Subject existingSubject = subjectDAO.getSubjectByCode(subjectCode);
        if (existingSubject == null) {
            request.setAttribute("error", "Subject not found");
            response.sendRedirect(request.getContextPath() + "/subjects/");
            return;
        }
        
        // Update subject
        existingSubject.setSubjectName(subjectName);
        boolean updated = subjectDAO.updateSubject(existingSubject);
        
        if (updated) {
            request.setAttribute("success", "Subject updated successfully");
            response.sendRedirect(request.getContextPath() + "/subjects/");
        } else {
            request.setAttribute("error", "Failed to update subject");
            response.sendRedirect(request.getContextPath() + "/subjects/edit/" + subjectCode);
        }
    }
    
    /**
     * Process assigning a subject to a department and class
     */
    private void assignSubject(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String subjectCode = request.getParameter("subjectCode");
        String departmentIdStr = request.getParameter("departmentId");
        String classIdStr = request.getParameter("classId");
        
        // Basic validation
        if (subjectCode == null || subjectCode.trim().isEmpty() || 
            departmentIdStr == null || departmentIdStr.trim().isEmpty() || 
            classIdStr == null || classIdStr.trim().isEmpty()) {
            
            request.setAttribute("error", "Subject, department, and class are required");
            response.sendRedirect(request.getContextPath() + "/subjects/assign");
            return;
        }
        
        try {
            int departmentId = Integer.parseInt(departmentIdStr);
            int classId = Integer.parseInt(classIdStr);
            
            // Verify that department and class exist
            Department department = departmentDAO.getDepartmentById(departmentId);
            com.attendance.models.Class classObj = classDAO.getClassById(classId);
            
            if (department == null || classObj == null) {
                request.setAttribute("error", "Invalid department or class");
                response.sendRedirect(request.getContextPath() + "/subjects/assign");
                return;
            }
            
            // Verify that subject exists
            Subject subject = subjectDAO.getSubjectByCode(subjectCode);
            if (subject == null) {
                request.setAttribute("error", "Invalid subject");
                response.sendRedirect(request.getContextPath() + "/subjects/assign");
                return;
            }
            
            // Check if class belongs to department
            if (classObj.getDepartmentId() != departmentId) {
                request.setAttribute("error", "Selected class does not belong to the selected department");
                response.sendRedirect(request.getContextPath() + "/subjects/assign");
                return;
            }
            
            // Assign subject to department and class
            boolean assigned = subjectDAO.assignSubjectToDepartmentClass(departmentId, classId, subjectCode);
            
            if (assigned) {
                request.setAttribute("success", "Subject assigned successfully");
                response.sendRedirect(request.getContextPath() + "/subjects/");
            } else {
                request.setAttribute("error", "Failed to assign subject");
                response.sendRedirect(request.getContextPath() + "/subjects/assign");
            }
            
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid department or class ID");
            response.sendRedirect(request.getContextPath() + "/subjects/assign");
        }
    }
    
    /**
     * Process removing a subject assignment from a department and class
     */
    private void removeSubjectAssignment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String subjectCode = request.getParameter("subjectCode");
        String departmentIdStr = request.getParameter("departmentId");
        String classIdStr = request.getParameter("classId");
        
        // Basic validation
        if (subjectCode == null || subjectCode.trim().isEmpty() || 
            departmentIdStr == null || departmentIdStr.trim().isEmpty() || 
            classIdStr == null || classIdStr.trim().isEmpty()) {
            
            request.setAttribute("error", "Subject, department, and class are required");
            response.sendRedirect(request.getContextPath() + "/subjects/");
            return;
        }
        
        try {
            int departmentId = Integer.parseInt(departmentIdStr);
            int classId = Integer.parseInt(classIdStr);
            
            // Remove subject assignment
            boolean removed = subjectDAO.removeSubjectFromDepartmentClass(departmentId, classId, subjectCode);
            
            if (removed) {
                request.setAttribute("success", "Subject assignment removed successfully");
            } else {
                request.setAttribute("error", "Failed to remove subject assignment");
            }
            
            response.sendRedirect(request.getContextPath() + "/subjects/");
            
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid department or class ID");
            response.sendRedirect(request.getContextPath() + "/subjects/");
        }
    }
}
