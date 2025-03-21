package com.attendance.controllers;

import com.attendance.dao.*;
import com.attendance.dao.impl.*;
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
 * Servlet for handling teacher assignment operations
 */
@WebServlet("/assignments/*")
public class TeacherAssignmentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private TeacherAssignmentDao teacherAssignmentDao = new TeacherAssignmentDaoImpl();
    private UserDao userDao = new UserDaoImpl();
    private SubjectDao subjectDao = new SubjectDaoImpl();
    private ClassDao classDao = new ClassDaoImpl();
    private DepartmentDao departmentDao = new DepartmentDaoImpl();
    
    // Use a fully qualified name for model.Class to avoid conflicts with java.lang.Class
    private final java.lang.Class<?> CLASS_TYPE = com.attendance.models.Class.class;
    
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
            // List all teacher assignments
            showAllAssignments(request, response);
        } else if (pathInfo.equals("/add")) {
            // Show add assignment form
            showAddAssignmentForm(request, response);
        } else if (pathInfo.startsWith("/edit/")) {
            // Show edit assignment form
            String assignmentId = pathInfo.substring("/edit/".length());
            // Parse teacher ID, subject code, and class ID from assignment ID
            String[] parts = assignmentId.split("_");
            
            if (parts.length != 3) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid assignment ID");
                return;
            }
            
            try {
                int teacherId = Integer.parseInt(parts[0]);
                String subjectCode = parts[1];
                int classId = Integer.parseInt(parts[2]);
                
                showEditAssignmentForm(request, response, teacherId, subjectCode, classId);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid assignment ID format");
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
        
        // Check if user is HOD or Principal
        User currentUser = SessionUtil.getUser(request);
        if (!("HOD".equals(currentUser.getRole()) || "Principal".equals(currentUser.getRole()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo.equals("/add")) {
            // Process add assignment form
            addAssignment(request, response);
        } else if (pathInfo.equals("/edit")) {
            // Process edit assignment form
            editAssignment(request, response);
        } else if (pathInfo.equals("/remove")) {
            // Process remove assignment
            removeAssignment(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * Show list of all teacher assignments
     */
    private void showAllAssignments(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        User currentUser = SessionUtil.getUser(request);
        
        List<TeacherAssignment> assignments;
        List<Department> departments;
        
        if ("Principal".equals(currentUser.getRole())) {
            // Principal can see all assignments
            assignments = teacherAssignmentDao.findAll();
            departments = departmentDao.findAll();
        } else {
            // HOD can only see assignments in their department
            // We need to get all classes in this department
            List<com.attendance.models.Class> departmentClasses = classDao.findByDepartment(currentUser.getDepartmentId());
            
            // Filter assignments by department classes
            assignments = teacherAssignmentDao.findAll();
            assignments.removeIf(assignment -> !isClassInDepartment(assignment.getClassId(), departmentClasses));
            
            // Only show this department
            departments = null;
            Department department = departmentDAO.getDepartmentById(currentUser.getDepartmentId());
            request.setAttribute("department", department);
        }
        
        request.setAttribute("assignments", assignments);
        request.setAttribute("departments", departments);
        
        request.getRequestDispatcher("/views/assignments/list.jsp").forward(request, response);
    }
    
    /**
     * Show form to add a new teacher assignment
     */
    private void showAddAssignmentForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        User currentUser = SessionUtil.getUser(request);
        
        // Get teachers and departments based on user role
        List<User> teachers;
        List<Department> departments;
        
        if ("Principal".equals(currentUser.getRole())) {
            // Principal can assign any teacher
            teachers = userDAO.getUsersByRole("Teacher");
            teachers.addAll(userDAO.getUsersByRole("Class Teacher"));
            departments = departmentDAO.getAllDepartments();
        } else {
            // HOD can only assign teachers in their department
            teachers = userDAO.getUsersByRoleAndDepartment("Teacher", currentUser.getDepartmentId());
            teachers.addAll(userDAO.getUsersByRoleAndDepartment("Class Teacher", currentUser.getDepartmentId()));
            
            // Only show this department
            departments = null;
            Department department = departmentDAO.getDepartmentById(currentUser.getDepartmentId());
            request.setAttribute("department", department);
            
            // Get classes for this department
            List<com.attendance.models.Class> classes = classDAO.getClassesByDepartment(currentUser.getDepartmentId());
            request.setAttribute("classes", classes);
            
            // Get subjects for this department
            for (com.attendance.models.Class classObj : classes) {
                List<Subject> subjects = subjectDAO.getSubjectsByDepartmentAndClass(
                    currentUser.getDepartmentId(), classObj.getClassId());
                request.setAttribute("subjects", subjects);
                break; // Just get subjects from first class
            }
        }
        
        request.setAttribute("teachers", teachers);
        request.setAttribute("departments", departments);
        
        // If department is selected (for Principal), load classes and subjects
        String departmentIdStr = request.getParameter("departmentId");
        if (departmentIdStr != null && !departmentIdStr.isEmpty()) {
            try {
                int departmentId = Integer.parseInt(departmentIdStr);
                
                // Get classes for selected department
                List<com.attendance.models.Class> classes = classDAO.getClassesByDepartment(departmentId);
                request.setAttribute("classes", classes);
                
                // Get subjects for this department
                for (com.attendance.models.Class classObj : classes) {
                    List<Subject> subjects = subjectDAO.getSubjectsByDepartmentAndClass(
                        departmentId, classObj.getClassId());
                    request.setAttribute("subjects", subjects);
                    break; // Just get subjects from first class
                }
                
                request.setAttribute("selectedDepartmentId", departmentId);
            } catch (NumberFormatException e) {
                request.setAttribute("error", "Invalid department ID");
            }
        }
        
        request.getRequestDispatcher("/views/assignments/add.jsp").forward(request, response);
    }
    
    /**
     * Show form to edit an existing teacher assignment
     */
    private void showEditAssignmentForm(HttpServletRequest request, HttpServletResponse response, 
                                       int teacherId, String subjectCode, int classId) 
            throws ServletException, IOException {
        
        // Create a composite key for the assignment
        List<TeacherAssignment> assignments = teacherAssignmentDao.findAll();
        TeacherAssignment assignment = null;
        
        for (TeacherAssignment a : assignments) {
            if (a.getTeacherId() == teacherId && a.getSubjectCode().equals(subjectCode) && a.getClassId() == classId) {
                assignment = a;
                break;
            }
        }
        
        if (assignment == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Assignment not found");
            return;
        }
        
        // Get related objects
        User teacher = userDAO.getUserById(teacherId);
        Subject subject = subjectDAO.getSubjectByCode(subjectCode);
        com.attendance.models.Class classObj = classDAO.getClassById(classId);
        
        if (teacher == null || subject == null || classObj == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Referenced data not found");
            return;
        }
        
        // Check if current user is authorized to edit this assignment
        User currentUser = SessionUtil.getUser(request);
        
        if ("HOD".equals(currentUser.getRole()) && teacher.getDepartmentId() != currentUser.getDepartmentId()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You can only edit assignments in your department");
            return;
        }
        
        request.setAttribute("assignment", assignment);
        request.setAttribute("teacher", teacher);
        request.setAttribute("subject", subject);
        request.setAttribute("classObj", classObj);
        
        request.getRequestDispatcher("/views/assignments/edit.jsp").forward(request, response);
    }
    
    /**
     * Process adding a new teacher assignment
     */
    private void addAssignment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String teacherIdStr = request.getParameter("teacherId");
        String subjectCode = request.getParameter("subjectCode");
        String classIdStr = request.getParameter("classId");
        String assignmentType = request.getParameter("assignmentType");
        
        // Basic validation
        if (teacherIdStr == null || teacherIdStr.trim().isEmpty() || 
            subjectCode == null || subjectCode.trim().isEmpty() || 
            classIdStr == null || classIdStr.trim().isEmpty() || 
            assignmentType == null || assignmentType.trim().isEmpty()) {
            
            request.setAttribute("error", "All fields are required");
            showAddAssignmentForm(request, response);
            return;
        }
        
        try {
            int teacherId = Integer.parseInt(teacherIdStr);
            int classId = Integer.parseInt(classIdStr);
            
            // Verify that teacher, subject, and class exist
            User teacher = userDAO.getUserById(teacherId);
            Subject subject = subjectDAO.getSubjectByCode(subjectCode);
            com.attendance.models.Class classObj = classDAO.getClassById(classId);
            
            if (teacher == null || subject == null || classObj == null) {
                request.setAttribute("error", "Invalid teacher, subject, or class");
                showAddAssignmentForm(request, response);
                return;
            }
            
            // Verify user is authorized to assign this teacher
            User currentUser = SessionUtil.getUser(request);
            
            if ("HOD".equals(currentUser.getRole()) && teacher.getDepartmentId() != currentUser.getDepartmentId()) {
                request.setAttribute("error", "You can only assign teachers in your department");
                showAddAssignmentForm(request, response);
                return;
            }
            
            // Check if class belongs to teacher's department
            Department teacherDept = departmentDAO.getDepartmentById(teacher.getDepartmentId());
            if (classObj.getDepartmentId() != teacherDept.getDepartmentId()) {
                request.setAttribute("error", "Teacher cannot be assigned to a class outside their department");
                showAddAssignmentForm(request, response);
                return;
            }
            
            // Check if subject is assigned to this class
            List<Subject> classSubjects = subjectDAO.getSubjectsByDepartmentAndClass(
                classObj.getDepartmentId(), classObj.getClassId());
            
            boolean subjectFound = false;
            for (Subject s : classSubjects) {
                if (s.getSubjectCode().equals(subjectCode)) {
                    subjectFound = true;
                    break;
                }
            }
            
            if (!subjectFound) {
                request.setAttribute("error", "Selected subject is not assigned to this class");
                showAddAssignmentForm(request, response);
                return;
            }
            
            // If assignment type is Class Teacher, check if class already has one
            if ("Class Teacher".equals(assignmentType)) {
                User existingClassTeacher = teacherAssignmentDAO.getClassTeacher(classId);
                if (existingClassTeacher != null) {
                    request.setAttribute("error", "This class already has a Class Teacher: " + existingClassTeacher.getName());
                    showAddAssignmentForm(request, response);
                    return;
                }
            }
            
            // Create assignment
            TeacherAssignment assignment = new TeacherAssignment(teacherId, subjectCode, classId, assignmentType);
            boolean created = teacherAssignmentDAO.assignTeacher(assignment);
            
            if (created) {
                request.setAttribute("success", "Teacher assigned successfully");
                response.sendRedirect(request.getContextPath() + "/assignments/");
            } else {
                request.setAttribute("error", "Failed to assign teacher");
                showAddAssignmentForm(request, response);
            }
            
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid teacher or class ID");
            showAddAssignmentForm(request, response);
        }
    }
    
    /**
     * Process editing an existing teacher assignment
     */
    private void editAssignment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String teacherIdStr = request.getParameter("teacherId");
        String subjectCode = request.getParameter("subjectCode");
        String classIdStr = request.getParameter("classId");
        String assignmentType = request.getParameter("assignmentType");
        
        // Basic validation
        if (teacherIdStr == null || teacherIdStr.trim().isEmpty() || 
            subjectCode == null || subjectCode.trim().isEmpty() || 
            classIdStr == null || classIdStr.trim().isEmpty() || 
            assignmentType == null || assignmentType.trim().isEmpty()) {
            
            request.setAttribute("error", "All fields are required");
            response.sendRedirect(request.getContextPath() + "/assignments/");
            return;
        }
        
        try {
            int teacherId = Integer.parseInt(teacherIdStr);
            int classId = Integer.parseInt(classIdStr);
            
            // Create assignment object
            TeacherAssignment assignment = new TeacherAssignment(teacherId, subjectCode, classId, assignmentType);
            
            // If changing to Class Teacher, check if class already has one
            if ("Class Teacher".equals(assignmentType)) {
                User existingClassTeacher = teacherAssignmentDAO.getClassTeacher(classId);
                if (existingClassTeacher != null && existingClassTeacher.getUserId() != teacherId) {
                    request.setAttribute("error", "This class already has a Class Teacher: " + existingClassTeacher.getName());
                    response.sendRedirect(request.getContextPath() + "/assignments/edit/" + 
                                         teacherId + "_" + subjectCode + "_" + classId);
                    return;
                }
            }
            
            // Update assignment
            boolean updated = teacherAssignmentDAO.updateAssignment(assignment);
            
            if (updated) {
                request.setAttribute("success", "Assignment updated successfully");
                response.sendRedirect(request.getContextPath() + "/assignments/");
            } else {
                request.setAttribute("error", "Failed to update assignment");
                response.sendRedirect(request.getContextPath() + "/assignments/edit/" + 
                                     teacherId + "_" + subjectCode + "_" + classId);
            }
            
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid teacher or class ID");
            response.sendRedirect(request.getContextPath() + "/assignments/");
        }
    }
    
    /**
     * Process removing a teacher assignment
     */
    private void removeAssignment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String teacherIdStr = request.getParameter("teacherId");
        String subjectCode = request.getParameter("subjectCode");
        String classIdStr = request.getParameter("classId");
        
        // Basic validation
        if (teacherIdStr == null || teacherIdStr.trim().isEmpty() || 
            subjectCode == null || subjectCode.trim().isEmpty() || 
            classIdStr == null || classIdStr.trim().isEmpty()) {
            
            request.setAttribute("error", "Teacher, subject, and class are required");
            response.sendRedirect(request.getContextPath() + "/assignments/");
            return;
        }
        
        try {
            int teacherId = Integer.parseInt(teacherIdStr);
            int classId = Integer.parseInt(classIdStr);
            
            // Verify user is authorized to remove this assignment
            User currentUser = SessionUtil.getUser(request);
            
            if ("HOD".equals(currentUser.getRole())) {
                User teacher = userDAO.getUserById(teacherId);
                if (teacher == null || teacher.getDepartmentId() != currentUser.getDepartmentId()) {
                    request.setAttribute("error", "You can only manage assignments in your department");
                    response.sendRedirect(request.getContextPath() + "/assignments/");
                    return;
                }
            }
            
            // Remove assignment
            boolean removed = teacherAssignmentDAO.removeAssignment(teacherId, subjectCode, classId);
            
            if (removed) {
                request.setAttribute("success", "Assignment removed successfully");
            } else {
                request.setAttribute("error", "Failed to remove assignment");
            }
            
            response.sendRedirect(request.getContextPath() + "/assignments/");
            
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid teacher or class ID");
            response.sendRedirect(request.getContextPath() + "/assignments/");
        }
    }
    
    /**
     * Helper method to check if a class is in a list of classes
     */
    private boolean isClassInDepartment(int classId, List<com.attendance.models.Class> departmentClasses) {
        for (com.attendance.models.Class classObj : departmentClasses) {
            if (classObj.getClassId() == classId) {
                return true;
            }
        }
        return false;
    }
}
