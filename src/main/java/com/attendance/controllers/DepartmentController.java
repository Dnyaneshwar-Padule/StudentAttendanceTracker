package com.attendance.controllers;

import com.attendance.dao.DepartmentDao;
import com.attendance.dao.SubjectDao;
import com.attendance.dao.DepartmentSubjectDao;
import com.attendance.dao.UserDao;
import com.attendance.dao.impl.DepartmentDaoImpl;
import com.attendance.dao.impl.SubjectDaoImpl;
import com.attendance.dao.impl.DepartmentSubjectDaoImpl;
import com.attendance.dao.impl.UserDaoImpl;
import com.attendance.models.Department;
import com.attendance.models.DepartmentSubject;
import com.attendance.models.Subject;
import com.attendance.models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for department management operations
 */
@WebServlet(name = "DepartmentController", urlPatterns = {"/admin/departments/*", "/hod/department/*"})
public class DepartmentController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DepartmentController.class.getName());
    
    private DepartmentDao departmentDao;
    private SubjectDao subjectDao;
    private DepartmentSubjectDao departmentSubjectDao;
    private UserDao userDao;

    @Override
    public void init() throws ServletException {
        super.init();
        departmentDao = new DepartmentDaoImpl();
        subjectDao = new SubjectDaoImpl();
        departmentSubjectDao = new DepartmentSubjectDaoImpl();
        userDao = new UserDaoImpl();
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
        
        if (servletPath.equals("/admin/departments")) {
            if ("Admin".equals(user.getRole()) || "Principal".equals(user.getRole())) {
                if (pathInfo == null || pathInfo.equals("/")) {
                    // List all departments
                    listDepartments(request, response);
                } else if (pathInfo.equals("/create")) {
                    // Show department creation form
                    showCreateForm(request, response);
                } else if (pathInfo.startsWith("/edit/")) {
                    // Show department edit form
                    showEditForm(request, response, pathInfo);
                } else if (pathInfo.startsWith("/view/")) {
                    // View department details
                    viewDepartment(request, response, pathInfo);
                } else if (pathInfo.startsWith("/delete/")) {
                    // Delete department
                    deleteDepartment(request, response, pathInfo);
                } else if (pathInfo.equals("/subjects")) {
                    // Manage department subjects
                    manageDepartmentSubjects(request, response);
                } else if (pathInfo.equals("/assign-hod")) {
                    // Assign HOD to department
                    showAssignHodForm(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have permission to access this page");
            }
        } else if (servletPath.equals("/hod/department")) {
            if ("HOD".equals(user.getRole())) {
                if (pathInfo == null || pathInfo.equals("/")) {
                    // View HOD's department
                    viewHodDepartment(request, response, user);
                } else if (pathInfo.equals("/subjects")) {
                    // View department subjects
                    viewDepartmentSubjects(request, response, user);
                } else if (pathInfo.equals("/teachers")) {
                    // View department teachers
                    viewDepartmentTeachers(request, response, user);
                } else if (pathInfo.equals("/students")) {
                    // View department students
                    viewDepartmentStudents(request, response, user);
                } else if (pathInfo.equals("/statistics")) {
                    // View department statistics
                    viewDepartmentStatistics(request, response, user);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only HODs can access this page");
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
        
        if (servletPath.equals("/admin/departments")) {
            if ("Admin".equals(user.getRole()) || "Principal".equals(user.getRole())) {
                if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/create")) {
                    // Create department
                    createDepartment(request, response);
                } else if (pathInfo.startsWith("/edit/")) {
                    // Update department
                    updateDepartment(request, response, pathInfo);
                } else if (pathInfo.equals("/subjects")) {
                    // Add/remove department subjects
                    processDepartmentSubjects(request, response);
                } else if (pathInfo.equals("/assign-hod")) {
                    // Process HOD assignment
                    processAssignHod(request, response);
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
     * List all departments
     */
    private void listDepartments(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<Department> departments = departmentDao.findAll();
            
            // Get HOD names for each department
            Map<Integer, String> hodNames = new HashMap<>();
            for (Department dept : departments) {
                int hodId = dept.getHodId();
                if (hodId > 0) {
                    User hod = userDao.findById(hodId);
                    if (hod != null) {
                        hodNames.put(dept.getDepartmentId(), hod.getFullName());
                    }
                }
            }
            
            request.setAttribute("departments", departments);
            request.setAttribute("hodNames", hodNames);
            request.getRequestDispatcher("/WEB-INF/views/admin/departments/list.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while listing departments", e);
            request.setAttribute("error", "Failed to retrieve departments. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/admin/departments/list.jsp").forward(request, response);
        }
    }
    
    /**
     * Show department creation form
     */
    private void showCreateForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Get all users with HOD role for dropdown
            List<User> hods = userDao.findByRole("HOD");
            request.setAttribute("hods", hods);
            
            request.getRequestDispatcher("/WEB-INF/views/admin/departments/create.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while preparing department creation form", e);
            response.sendRedirect(request.getContextPath() + "/admin/departments?error=database");
        }
    }
    
    /**
     * Create a new department
     */
    private void createDepartment(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String departmentName = request.getParameter("departmentName");
        String departmentCode = request.getParameter("departmentCode");
        String hodIdStr = request.getParameter("hodId");
        
        if (departmentName == null || departmentName.trim().isEmpty() ||
            departmentCode == null || departmentCode.trim().isEmpty()) {
            
            request.setAttribute("error", "Department name and code are required");
            showCreateForm(request, response);
            return;
        }
        
        try {
            // Check if department code already exists
            Department existingDept = departmentDao.findByCode(departmentCode);
            if (existingDept != null) {
                request.setAttribute("error", "Department code already exists");
                showCreateForm(request, response);
                return;
            }
            
            // Create new department
            Department department = new Department();
            department.setDepartmentName(departmentName);
            department.setDepartmentCode(departmentCode);
            
            // Set HOD if provided
            if (hodIdStr != null && !hodIdStr.trim().isEmpty()) {
                try {
                    int hodId = Integer.parseInt(hodIdStr);
                    User hod = userDao.findById(hodId);
                    
                    if (hod != null && "HOD".equals(hod.getRole())) {
                        department.setHodId(hodId);
                    }
                } catch (NumberFormatException e) {
                    // Invalid HOD ID, ignore
                }
            }
            
            Department savedDepartment = departmentDao.save(department);
            
            if (savedDepartment != null) {
                response.sendRedirect(request.getContextPath() + "/admin/departments?success=created");
            } else {
                request.setAttribute("error", "Failed to create department. Please try again.");
                showCreateForm(request, response);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while creating department", e);
            request.setAttribute("error", "A system error occurred. Please try again later.");
            showCreateForm(request, response);
        }
    }
    
    /**
     * Show department edit form
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws ServletException, IOException {
        try {
            int departmentId = extractIdFromPath(pathInfo, "/edit/");
            
            Department department = departmentDao.findById(departmentId);
            if (department == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Department not found");
                return;
            }
            
            // Get all users with HOD role for dropdown
            List<User> hods = userDao.findByRole("HOD");
            
            request.setAttribute("department", department);
            request.setAttribute("hods", hods);
            
            request.getRequestDispatcher("/WEB-INF/views/admin/departments/edit.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid department ID");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while preparing department edit form", e);
            response.sendRedirect(request.getContextPath() + "/admin/departments?error=database");
        }
    }
    
    /**
     * Update an existing department
     */
    private void updateDepartment(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws ServletException, IOException {
        try {
            int departmentId = extractIdFromPath(pathInfo, "/edit/");
            
            Department department = departmentDao.findById(departmentId);
            if (department == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Department not found");
                return;
            }
            
            String departmentName = request.getParameter("departmentName");
            String departmentCode = request.getParameter("departmentCode");
            String hodIdStr = request.getParameter("hodId");
            
            if (departmentName == null || departmentName.trim().isEmpty() ||
                departmentCode == null || departmentCode.trim().isEmpty()) {
                
                request.setAttribute("error", "Department name and code are required");
                request.setAttribute("department", department);
                showEditForm(request, response, pathInfo);
                return;
            }
            
            // Check if department code already exists for another department
            Department existingDept = departmentDao.findByCode(departmentCode);
            if (existingDept != null && existingDept.getDepartmentId() != departmentId) {
                request.setAttribute("error", "Department code already exists for another department");
                request.setAttribute("department", department);
                showEditForm(request, response, pathInfo);
                return;
            }
            
            // Update department fields
            department.setDepartmentName(departmentName);
            department.setDepartmentCode(departmentCode);
            
            // Update HOD if provided
            if (hodIdStr != null && !hodIdStr.trim().isEmpty()) {
                try {
                    int hodId = Integer.parseInt(hodIdStr);
                    User hod = userDao.findById(hodId);
                    
                    if (hod != null && "HOD".equals(hod.getRole())) {
                        department.setHodId(hodId);
                    }
                } catch (NumberFormatException e) {
                    // Invalid HOD ID, ignore
                }
            } else {
                // Clear HOD if none selected
                department.setHodId(0);
            }
            
            Department updatedDepartment = departmentDao.update(department);
            
            if (updatedDepartment != null) {
                response.sendRedirect(request.getContextPath() + "/admin/departments?success=updated");
            } else {
                request.setAttribute("error", "Failed to update department. Please try again.");
                request.setAttribute("department", department);
                showEditForm(request, response, pathInfo);
            }
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid department ID");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while updating department", e);
            response.sendRedirect(request.getContextPath() + "/admin/departments?error=database");
        }
    }
    
    /**
     * View department details
     */
    private void viewDepartment(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws ServletException, IOException {
        try {
            int departmentId = extractIdFromPath(pathInfo, "/view/");
            
            Department department = departmentDao.findById(departmentId);
            if (department == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Department not found");
                return;
            }
            
            // Get HOD details if available
            User hod = null;
            if (department.getHodId() > 0) {
                hod = userDao.findById(department.getHodId());
            }
            
            // Get subjects for this department
            List<DepartmentSubject> departmentSubjects = departmentSubjectDao.findByDepartment(departmentId);
            
            // Get subject details
            Map<String, Subject> subjects = new HashMap<>();
            for (DepartmentSubject ds : departmentSubjects) {
                Subject subject = subjectDao.findByCode(ds.getSubjectCode());
                if (subject != null) {
                    subjects.put(ds.getSubjectCode(), subject);
                }
            }
            
            request.setAttribute("department", department);
            request.setAttribute("hod", hod);
            request.setAttribute("departmentSubjects", departmentSubjects);
            request.setAttribute("subjects", subjects);
            
            request.getRequestDispatcher("/WEB-INF/views/admin/departments/view.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid department ID");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while viewing department", e);
            response.sendRedirect(request.getContextPath() + "/admin/departments?error=database");
        }
    }
    
    /**
     * Delete a department
     */
    private void deleteDepartment(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws ServletException, IOException {
        try {
            int departmentId = extractIdFromPath(pathInfo, "/delete/");
            
            boolean deleted = departmentDao.delete(departmentId);
            
            if (deleted) {
                response.sendRedirect(request.getContextPath() + "/admin/departments?success=deleted");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/departments?error=deleteFailed");
            }
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid department ID");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while deleting department", e);
            response.sendRedirect(request.getContextPath() + "/admin/departments?error=database");
        }
    }
    
    /**
     * Manage department subjects
     */
    private void manageDepartmentSubjects(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String departmentIdStr = request.getParameter("departmentId");
        
        if (departmentIdStr == null || departmentIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/departments?error=missingDepartment");
            return;
        }
        
        try {
            int departmentId = Integer.parseInt(departmentIdStr);
            
            Department department = departmentDao.findById(departmentId);
            if (department == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Department not found");
                return;
            }
            
            // Get all subjects
            List<Subject> allSubjects = subjectDao.findAll();
            
            // Get department subjects
            List<DepartmentSubject> departmentSubjects = departmentSubjectDao.findByDepartment(departmentId);
            
            // Create a map of subject codes to department subjects for easy lookup
            Map<String, DepartmentSubject> subjectMap = new HashMap<>();
            for (DepartmentSubject ds : departmentSubjects) {
                subjectMap.put(ds.getSubjectCode(), ds);
            }
            
            request.setAttribute("department", department);
            request.setAttribute("allSubjects", allSubjects);
            request.setAttribute("departmentSubjects", departmentSubjects);
            request.setAttribute("subjectMap", subjectMap);
            
            request.getRequestDispatcher("/WEB-INF/views/admin/departments/subjects.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid department ID");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while managing department subjects", e);
            response.sendRedirect(request.getContextPath() + "/admin/departments?error=database");
        }
    }
    
    /**
     * Process department subjects form
     */
    private void processDepartmentSubjects(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String departmentIdStr = request.getParameter("departmentId");
        String[] subjectCodes = request.getParameterValues("subjectCodes");
        
        if (departmentIdStr == null || departmentIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/departments?error=missingDepartment");
            return;
        }
        
        try {
            int departmentId = Integer.parseInt(departmentIdStr);
            
            Department department = departmentDao.findById(departmentId);
            if (department == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Department not found");
                return;
            }
            
            // Get existing department subjects
            List<DepartmentSubject> existingSubjects = departmentSubjectDao.findByDepartment(departmentId);
            
            // Create a map of existing subject codes for easy lookup
            Map<String, DepartmentSubject> existingSubjectMap = new HashMap<>();
            for (DepartmentSubject ds : existingSubjects) {
                existingSubjectMap.put(ds.getSubjectCode(), ds);
            }
            
            // Process additions and updates
            if (subjectCodes != null) {
                for (String code : subjectCodes) {
                    String semesterParam = "semester_" + code;
                    String creditsParam = "credits_" + code;
                    
                    String semester = request.getParameter(semesterParam);
                    String creditsStr = request.getParameter(creditsParam);
                    
                    if (semester != null && !semester.trim().isEmpty() && 
                        creditsStr != null && !creditsStr.trim().isEmpty()) {
                        
                        try {
                            int credits = Integer.parseInt(creditsStr);
                            
                            if (existingSubjectMap.containsKey(code)) {
                                // Update existing
                                DepartmentSubject ds = existingSubjectMap.get(code);
                                ds.setSemester(semester);
                                ds.setCredits(credits);
                                departmentSubjectDao.update(ds);
                                
                                // Remove from map to track deletions
                                existingSubjectMap.remove(code);
                            } else {
                                // Add new
                                DepartmentSubject ds = new DepartmentSubject();
                                ds.setDepartmentId(departmentId);
                                ds.setSubjectCode(code);
                                ds.setSemester(semester);
                                ds.setCredits(credits);
                                departmentSubjectDao.save(ds);
                            }
                        } catch (NumberFormatException e) {
                            // Invalid credits, skip this subject
                        }
                    }
                }
            }
            
            // Process deletions (anything left in existingSubjectMap)
            for (DepartmentSubject ds : existingSubjectMap.values()) {
                departmentSubjectDao.delete(departmentId, ds.getSubjectCode());
            }
            
            response.sendRedirect(request.getContextPath() + "/admin/departments/view/" + departmentId + "?success=subjectsUpdated");
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid department ID");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while processing department subjects", e);
            response.sendRedirect(request.getContextPath() + "/admin/departments?error=database");
        }
    }
    
    /**
     * Show assign HOD form
     */
    private void showAssignHodForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Get all departments
            List<Department> departments = departmentDao.findAll();
            
            // Get all HODs
            List<User> hods = userDao.findByRole("HOD");
            
            // Create a map of current HOD assignments
            Map<Integer, Integer> currentAssignments = new HashMap<>();
            for (Department dept : departments) {
                if (dept.getHodId() > 0) {
                    currentAssignments.put(dept.getDepartmentId(), dept.getHodId());
                }
            }
            
            request.setAttribute("departments", departments);
            request.setAttribute("hods", hods);
            request.setAttribute("currentAssignments", currentAssignments);
            
            request.getRequestDispatcher("/WEB-INF/views/admin/departments/assign-hod.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while preparing HOD assignment form", e);
            response.sendRedirect(request.getContextPath() + "/admin/departments?error=database");
        }
    }
    
    /**
     * Process HOD assignment
     */
    private void processAssignHod(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Get all departments
            List<Department> departments = departmentDao.findAll();
            
            int updatedCount = 0;
            
            // Process each department's HOD assignment
            for (Department dept : departments) {
                String hodParam = "hod_" + dept.getDepartmentId();
                String hodIdStr = request.getParameter(hodParam);
                
                if (hodIdStr != null && !hodIdStr.trim().isEmpty()) {
                    try {
                        int hodId = Integer.parseInt(hodIdStr);
                        
                        // Check if HOD exists and has HOD role
                        User hod = userDao.findById(hodId);
                        if (hod != null && "HOD".equals(hod.getRole())) {
                            // Update department's HOD if changed
                            if (dept.getHodId() != hodId) {
                                dept.setHodId(hodId);
                                departmentDao.update(dept);
                                updatedCount++;
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Invalid HOD ID, skip this department
                    }
                } else if (dept.getHodId() > 0) {
                    // Clear HOD assignment
                    dept.setHodId(0);
                    departmentDao.update(dept);
                    updatedCount++;
                }
            }
            
            response.sendRedirect(request.getContextPath() + "/admin/departments?success=hodAssigned&count=" + updatedCount);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while processing HOD assignments", e);
            response.sendRedirect(request.getContextPath() + "/admin/departments?error=database");
        }
    }
    
    /**
     * View HOD's department
     */
    private void viewHodDepartment(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Find department by HOD
            List<Department> departments = departmentDao.findByHod(user.getUserId());
            Department department = departments != null && !departments.isEmpty() ? departments.get(0) : null;
            
            if (department == null) {
                request.setAttribute("error", "You are not assigned to any department");
                request.getRequestDispatcher("/WEB-INF/views/hod/department/view.jsp").forward(request, response);
                return;
            }
            
            // Get subjects for this department
            List<DepartmentSubject> departmentSubjects = departmentSubjectDao.findByDepartment(department.getDepartmentId());
            
            // Get subject details
            Map<String, Subject> subjects = new HashMap<>();
            for (DepartmentSubject ds : departmentSubjects) {
                Subject subject = subjectDao.findByCode(ds.getSubjectCode());
                if (subject != null) {
                    subjects.put(ds.getSubjectCode(), subject);
                }
            }
            
            request.setAttribute("department", department);
            request.setAttribute("departmentSubjects", departmentSubjects);
            request.setAttribute("subjects", subjects);
            
            request.getRequestDispatcher("/WEB-INF/views/hod/department/view.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while viewing HOD department", e);
            request.setAttribute("error", "Failed to retrieve department information. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/hod/department/view.jsp").forward(request, response);
        }
    }
    
    /**
     * View department subjects for HOD
     */
    private void viewDepartmentSubjects(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Find department by HOD
            List<Department> departments = departmentDao.findByHod(user.getUserId());
            Department department = departments != null && !departments.isEmpty() ? departments.get(0) : null;
            
            if (department == null) {
                request.setAttribute("error", "You are not assigned to any department");
                request.getRequestDispatcher("/WEB-INF/views/hod/department/subjects.jsp").forward(request, response);
                return;
            }
            
            // Get subjects for this department
            List<DepartmentSubject> departmentSubjects = departmentSubjectDao.findByDepartment(department.getDepartmentId());
            
            // Get subject details
            Map<String, Subject> subjects = new HashMap<>();
            for (DepartmentSubject ds : departmentSubjects) {
                Subject subject = subjectDao.findByCode(ds.getSubjectCode());
                if (subject != null) {
                    subjects.put(ds.getSubjectCode(), subject);
                }
            }
            
            request.setAttribute("department", department);
            request.setAttribute("departmentSubjects", departmentSubjects);
            request.setAttribute("subjects", subjects);
            
            request.getRequestDispatcher("/WEB-INF/views/hod/department/subjects.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while viewing department subjects", e);
            request.setAttribute("error", "Failed to retrieve subject information. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/hod/department/subjects.jsp").forward(request, response);
        }
    }
    
    /**
     * View department teachers for HOD
     */
    private void viewDepartmentTeachers(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        // Placeholder implementation - add proper implementation based on your data model
        request.setAttribute("error", "This feature is not yet implemented");
        request.getRequestDispatcher("/WEB-INF/views/hod/department/teachers.jsp").forward(request, response);
    }
    
    /**
     * View department students for HOD
     */
    private void viewDepartmentStudents(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        // Placeholder implementation - add proper implementation based on your data model
        request.setAttribute("error", "This feature is not yet implemented");
        request.getRequestDispatcher("/WEB-INF/views/hod/department/students.jsp").forward(request, response);
    }
    
    /**
     * View department statistics for HOD
     */
    private void viewDepartmentStatistics(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        // Placeholder implementation - add proper implementation based on your data model
        request.setAttribute("error", "This feature is not yet implemented");
        request.getRequestDispatcher("/WEB-INF/views/hod/department/statistics.jsp").forward(request, response);
    }
    
    /**
     * Helper method to extract ID from path
     */
    private int extractIdFromPath(String pathInfo, String prefix) {
        String idStr = pathInfo.substring(prefix.length());
        return Integer.parseInt(idStr);
    }
}