package com.attendance.controllers;

import com.attendance.dao.SubjectDao;
import com.attendance.dao.DepartmentSubjectDao;
import com.attendance.dao.TeacherAssignmentDao;
import com.attendance.dao.impl.SubjectDaoImpl;
import com.attendance.dao.impl.DepartmentSubjectDaoImpl;
import com.attendance.dao.impl.TeacherAssignmentDaoImpl;
import com.attendance.models.Subject;
import com.attendance.models.DepartmentSubject;
import com.attendance.models.TeacherAssignment;
import com.attendance.models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for subject management operations
 */
@WebServlet(name = "SubjectController", urlPatterns = {"/admin/subjects/*", "/teacher/subjects/*"})
public class SubjectController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SubjectController.class.getName());
    
    private SubjectDao subjectDao;
    private DepartmentSubjectDao departmentSubjectDao;
    private TeacherAssignmentDao teacherAssignmentDao;

    @Override
    public void init() throws ServletException {
        super.init();
        subjectDao = new SubjectDaoImpl();
        departmentSubjectDao = new DepartmentSubjectDaoImpl();
        teacherAssignmentDao = new TeacherAssignmentDaoImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        if (servletPath.equals("/admin/subjects")) {
            if ("Admin".equals(user.getRole()) || "Principal".equals(user.getRole())) {
                if (pathInfo == null || pathInfo.equals("/")) {
                    // List all subjects
                    listSubjects(request, response);
                } else if (pathInfo.equals("/create")) {
                    // Show subject creation form
                    showCreateForm(request, response);
                } else if (pathInfo.startsWith("/edit/")) {
                    // Show subject edit form
                    showEditForm(request, response, pathInfo);
                } else if (pathInfo.startsWith("/view/")) {
                    // View subject details
                    viewSubject(request, response, pathInfo);
                } else if (pathInfo.startsWith("/delete/")) {
                    // Delete subject
                    deleteSubject(request, response, pathInfo);
                } else if (pathInfo.equals("/search")) {
                    // Search subjects
                    searchSubjects(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have permission to access this page");
            }
        } else if (servletPath.equals("/teacher/subjects")) {
            if ("Teacher".equals(user.getRole())) {
                if (pathInfo == null || pathInfo.equals("/")) {
                    // List teacher's subjects
                    listTeacherSubjects(request, response, user);
                } else if (pathInfo.startsWith("/view/")) {
                    // View subject details
                    viewTeacherSubject(request, response, pathInfo, user);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only teachers can access this page");
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        if (servletPath.equals("/admin/subjects")) {
            if ("Admin".equals(user.getRole()) || "Principal".equals(user.getRole())) {
                if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/create")) {
                    // Create subject
                    createSubject(request, response);
                } else if (pathInfo.startsWith("/edit/")) {
                    // Update subject
                    updateSubject(request, response, pathInfo);
                } else if (pathInfo.equals("/search")) {
                    // Process subject search form
                    searchSubjects(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have permission to access this page");
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * List all subjects with optional filters
     */
    private void listSubjects(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Get filter parameters
            String departmentFilter = request.getParameter("department");
            String semesterFilter = request.getParameter("semester");
            
            List<Subject> subjects;
            
            // Apply filters if provided
            if (departmentFilter != null && !departmentFilter.isEmpty()) {
                try {
                    int departmentId = Integer.parseInt(departmentFilter);
                    List<DepartmentSubject> departmentSubjects = departmentSubjectDao.findByDepartment(departmentId);
                    
                    // Extract subject codes
                    subjects = new ArrayList<>();
                    for (DepartmentSubject ds : departmentSubjects) {
                        Subject subject = subjectDao.findByCode(ds.getSubjectCode());
                        if (subject != null) {
                            subjects.add(subject);
                        }
                    }
                } catch (NumberFormatException e) {
                    subjects = subjectDao.findAll();
                }
            } else if (semesterFilter != null && !semesterFilter.isEmpty()) {
                List<DepartmentSubject> semesterSubjects = departmentSubjectDao.findBySemester(semesterFilter);
                
                // Extract subject codes
                subjects = new ArrayList<>();
                for (DepartmentSubject ds : semesterSubjects) {
                    Subject subject = subjectDao.findByCode(ds.getSubjectCode());
                    if (subject != null) {
                        subjects.add(subject);
                    }
                }
            } else {
                // No filters, get all subjects
                subjects = subjectDao.findAll();
            }
            
            request.setAttribute("subjects", subjects);
            request.getRequestDispatcher("/WEB-INF/views/admin/subjects/list.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while listing subjects", e);
            request.setAttribute("error", "Failed to retrieve subjects. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/admin/subjects/list.jsp").forward(request, response);
        }
    }
    
    /**
     * Show subject creation form
     */
    private void showCreateForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/admin/subjects/create.jsp").forward(request, response);
    }
    
    /**
     * Create a new subject
     */
    private void createSubject(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String subjectName = request.getParameter("subjectName");
        String subjectCode = request.getParameter("subjectCode");
        String description = request.getParameter("description");
        
        if (subjectName == null || subjectName.trim().isEmpty() ||
            subjectCode == null || subjectCode.trim().isEmpty()) {
            
            request.setAttribute("error", "Subject name and code are required");
            request.getRequestDispatcher("/WEB-INF/views/admin/subjects/create.jsp").forward(request, response);
            return;
        }
        
        try {
            // Check if subject code already exists
            Subject existingSubject = subjectDao.findByCode(subjectCode);
            if (existingSubject != null) {
                request.setAttribute("error", "Subject code already exists");
                request.getRequestDispatcher("/WEB-INF/views/admin/subjects/create.jsp").forward(request, response);
                return;
            }
            
            // Create new subject
            Subject subject = new Subject();
            subject.setSubjectName(subjectName);
            subject.setSubjectCode(subjectCode);
            subject.setDescription(description);
            
            Subject savedSubject = subjectDao.save(subject);
            
            if (savedSubject != null) {
                response.sendRedirect(request.getContextPath() + "/admin/subjects?success=created");
            } else {
                request.setAttribute("error", "Failed to create subject. Please try again.");
                request.getRequestDispatcher("/WEB-INF/views/admin/subjects/create.jsp").forward(request, response);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while creating subject", e);
            request.setAttribute("error", "A system error occurred. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/admin/subjects/create.jsp").forward(request, response);
        }
    }
    
    /**
     * Show subject edit form
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws ServletException, IOException {
        try {
            String subjectCode = extractCodeFromPath(pathInfo, "/edit/");
            
            Subject subject = subjectDao.findByCode(subjectCode);
            if (subject == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Subject not found");
                return;
            }
            
            request.setAttribute("subject", subject);
            request.getRequestDispatcher("/WEB-INF/views/admin/subjects/edit.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while preparing subject edit form", e);
            response.sendRedirect(request.getContextPath() + "/admin/subjects?error=database");
        }
    }
    
    /**
     * Update an existing subject
     */
    private void updateSubject(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws ServletException, IOException {
        try {
            String subjectCode = extractCodeFromPath(pathInfo, "/edit/");
            
            Subject subject = subjectDao.findByCode(subjectCode);
            if (subject == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Subject not found");
                return;
            }
            
            String subjectName = request.getParameter("subjectName");
            String description = request.getParameter("description");
            
            if (subjectName == null || subjectName.trim().isEmpty()) {
                request.setAttribute("error", "Subject name is required");
                request.setAttribute("subject", subject);
                request.getRequestDispatcher("/WEB-INF/views/admin/subjects/edit.jsp").forward(request, response);
                return;
            }
            
            // Update subject fields
            subject.setSubjectName(subjectName);
            subject.setDescription(description);
            
            Subject updatedSubject = subjectDao.update(subject);
            
            if (updatedSubject != null) {
                response.sendRedirect(request.getContextPath() + "/admin/subjects?success=updated");
            } else {
                request.setAttribute("error", "Failed to update subject. Please try again.");
                request.setAttribute("subject", subject);
                request.getRequestDispatcher("/WEB-INF/views/admin/subjects/edit.jsp").forward(request, response);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while updating subject", e);
            response.sendRedirect(request.getContextPath() + "/admin/subjects?error=database");
        }
    }
    
    /**
     * View subject details
     */
    private void viewSubject(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws ServletException, IOException {
        try {
            String subjectCode = extractCodeFromPath(pathInfo, "/view/");
            
            Subject subject = subjectDao.findByCode(subjectCode);
            if (subject == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Subject not found");
                return;
            }
            
            // Get department assignments for this subject
            List<DepartmentSubject> departmentSubjects = departmentSubjectDao.findBySubject(subjectCode);
            
            // Get teacher assignments for this subject
            List<TeacherAssignment> teacherAssignments = teacherAssignmentDao.findBySubject(subjectCode);
            
            request.setAttribute("subject", subject);
            request.setAttribute("departmentSubjects", departmentSubjects);
            request.setAttribute("teacherAssignments", teacherAssignments);
            
            request.getRequestDispatcher("/WEB-INF/views/admin/subjects/view.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while viewing subject", e);
            response.sendRedirect(request.getContextPath() + "/admin/subjects?error=database");
        }
    }
    
    /**
     * Delete a subject
     */
    private void deleteSubject(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws ServletException, IOException {
        try {
            String subjectCode = extractCodeFromPath(pathInfo, "/delete/");
            
            boolean deleted = subjectDao.delete(subjectCode);
            
            if (deleted) {
                response.sendRedirect(request.getContextPath() + "/admin/subjects?success=deleted");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/subjects?error=deleteFailed");
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while deleting subject", e);
            response.sendRedirect(request.getContextPath() + "/admin/subjects?error=database");
        }
    }
    
    /**
     * Search subjects
     */
    private void searchSubjects(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = request.getParameter("query");
        
        if (query == null || query.trim().isEmpty()) {
            // No query, show all subjects
            listSubjects(request, response);
            return;
        }
        
        try {
            List<Subject> subjects = subjectDao.searchSubjects(query);
            
            request.setAttribute("subjects", subjects);
            request.setAttribute("searchQuery", query);
            request.getRequestDispatcher("/WEB-INF/views/admin/subjects/list.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while searching subjects", e);
            request.setAttribute("error", "Failed to search subjects. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/admin/subjects/list.jsp").forward(request, response);
        }
    }
    
    /**
     * List teacher's assigned subjects
     */
    private void listTeacherSubjects(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            List<TeacherAssignment> assignments = teacherAssignmentDao.findByTeacher(user.getUserId());
            
            // Get subject details
            Map<String, Subject> subjects = new HashMap<>();
            for (TeacherAssignment assignment : assignments) {
                Subject subject = subjectDao.findByCode(assignment.getSubjectCode());
                if (subject != null) {
                    subjects.put(assignment.getSubjectCode(), subject);
                }
            }
            
            request.setAttribute("assignments", assignments);
            request.setAttribute("subjects", subjects);
            
            request.getRequestDispatcher("/WEB-INF/views/teacher/subjects/list.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while listing teacher subjects", e);
            request.setAttribute("error", "Failed to retrieve subjects. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/teacher/subjects/list.jsp").forward(request, response);
        }
    }
    
    /**
     * View teacher's subject details
     */
    private void viewTeacherSubject(HttpServletRequest request, HttpServletResponse response, String pathInfo, User user) throws ServletException, IOException {
        try {
            String subjectCode = extractCodeFromPath(pathInfo, "/view/");
            String classIdStr = request.getParameter("classId");
            
            // Validate subject and class assignment for this teacher
            if (classIdStr != null && !classIdStr.trim().isEmpty()) {
                try {
                    int classId = Integer.parseInt(classIdStr);
                    
                    TeacherAssignment assignment = teacherAssignmentDao.findByTeacherSubjectAndClass(
                            user.getUserId(), subjectCode, classId);
                    
                    if (assignment == null) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not assigned to this subject and class");
                        return;
                    }
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid class ID");
                    return;
                }
            } else {
                // Check if teacher is assigned to this subject in any class
                List<TeacherAssignment> assignments = teacherAssignmentDao.findByTeacher(user.getUserId());
                boolean isAssigned = false;
                
                for (TeacherAssignment assignment : assignments) {
                    if (assignment.getSubjectCode().equals(subjectCode)) {
                        isAssigned = true;
                        break;
                    }
                }
                
                if (!isAssigned) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not assigned to this subject");
                    return;
                }
            }
            
            // Get subject details
            Subject subject = subjectDao.findByCode(subjectCode);
            if (subject == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Subject not found");
                return;
            }
            
            request.setAttribute("subject", subject);
            
            // Pass class ID if provided
            if (classIdStr != null && !classIdStr.trim().isEmpty()) {
                request.setAttribute("classId", Integer.parseInt(classIdStr));
            }
            
            request.getRequestDispatcher("/WEB-INF/views/teacher/subjects/view.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while viewing teacher subject", e);
            response.sendRedirect(request.getContextPath() + "/teacher/subjects?error=database");
        }
    }
    
    /**
     * Helper method to extract subject code from path
     */
    private String extractCodeFromPath(String pathInfo, String prefix) {
        return pathInfo.substring(prefix.length());
    }
}