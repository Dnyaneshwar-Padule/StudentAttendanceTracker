package com.attendance.controllers;

import com.attendance.dao.*;
import com.attendance.dao.impl.*;
import com.attendance.models.*;
import com.attendance.utils.DatabaseConnection;
import com.attendance.utils.DateUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for advanced attendance filtering and searching functionality
 */
@WebServlet(name = "AttendanceFilterController", urlPatterns = {
    "/teacher/attendance/filter/*", 
    "/student/attendance/filter/*",
    "/hod/attendance/filter/*"
})
public class AttendanceFilterController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AttendanceFilterController.class.getName());
    
    private AttendanceDao attendanceDao;
    private UserDao userDao;
    private ClassDao classDao;
    private SubjectDao subjectDao;
    private DepartmentDao departmentDao;
    private StudentEnrollmentDao studentEnrollmentDao;
    private TeacherAssignmentDao teacherAssignmentDao;
    private DepartmentSubjectDao departmentSubjectDao;

    @Override
    public void init() throws ServletException {
        super.init();
        attendanceDao = new AttendanceDaoImpl();
        userDao = new UserDaoImpl();
        classDao = new ClassDaoImpl();
        subjectDao = new SubjectDaoImpl();
        departmentDao = new DepartmentDaoImpl();
        studentEnrollmentDao = new StudentEnrollmentDaoImpl();
        teacherAssignmentDao = new TeacherAssignmentDaoImpl();
        departmentSubjectDao = new DepartmentSubjectDaoImpl();
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
        
        if (servletPath.equals("/teacher/attendance/filter")) {
            if ("Teacher".equals(user.getRole())) {
                if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/form")) {
                    // Show filter form for teachers
                    showTeacherFilterForm(request, response, user);
                } else if (pathInfo.equals("/results")) {
                    // Show filter results for teachers
                    showTeacherFilterResults(request, response, user);
                } else if (pathInfo.equals("/export")) {
                    // Export filtered results to CSV
                    exportFilteredResults(request, response, user);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only teachers can access this page");
            }
        } else if (servletPath.equals("/student/attendance/filter")) {
            if ("Student".equals(user.getRole())) {
                if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/form")) {
                    // Show filter form for students
                    showStudentFilterForm(request, response, user);
                } else if (pathInfo.equals("/results")) {
                    // Show filter results for students
                    showStudentFilterResults(request, response, user);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only students can access this page");
            }
        } else if (servletPath.equals("/hod/attendance/filter")) {
            if ("HOD".equals(user.getRole())) {
                if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/form")) {
                    // Show filter form for HODs
                    showHodFilterForm(request, response, user);
                } else if (pathInfo.equals("/results")) {
                    // Show filter results for HODs
                    showHodFilterResults(request, response, user);
                } else if (pathInfo.equals("/export")) {
                    // Export filtered results to CSV
                    exportFilteredResults(request, response, user);
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
        
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        if (servletPath.equals("/teacher/attendance/filter")) {
            if ("Teacher".equals(user.getRole())) {
                // Process teacher filter form
                processTeacherFilter(request, response, user);
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only teachers can access this page");
            }
        } else if (servletPath.equals("/student/attendance/filter")) {
            if ("Student".equals(user.getRole())) {
                // Process student filter form
                processStudentFilter(request, response, user);
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only students can access this page");
            }
        } else if (servletPath.equals("/hod/attendance/filter")) {
            if ("HOD".equals(user.getRole())) {
                // Process HOD filter form
                processHodFilter(request, response, user);
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only HODs can access this page");
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * Show filter form for teachers
     */
    private void showTeacherFilterForm(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get teacher assignments
            List<TeacherAssignment> assignments = teacherAssignmentDao.findByTeacher(user.getUserId());
            
            // Collect subjects and classes
            Set<String> subjectCodes = new HashSet<>();
            Set<Integer> classIds = new HashSet<>();
            
            for (TeacherAssignment assignment : assignments) {
                subjectCodes.add(assignment.getSubjectCode());
                classIds.add(assignment.getClassId());
            }
            
            // Get subject details
            Map<String, Subject> subjects = new HashMap<>();
            for (String code : subjectCodes) {
                Subject subject = subjectDao.findByCode(code);
                if (subject != null) {
                    subjects.put(code, subject);
                }
            }
            
            // Get class details
            Map<Integer, com.attendance.models.Class> classes = new HashMap<>();
            for (int classId : classIds) {
                com.attendance.models.Class cls = classDao.findById(classId);
                if (cls != null) {
                    classes.put(classId, cls);
                }
            }
            
            // Get list of academic years and semesters for dropdowns
            List<String> academicYears = new ArrayList<>();
            List<String> semesters = new ArrayList<>();
            
            // Add current and past academic years (e.g., 2024-2025, 2023-2024)
            int currentYear = LocalDate.now().getYear();
            for (int i = 0; i < 5; i++) {
                int startYear = currentYear - i;
                academicYears.add(startYear + "-" + (startYear + 1));
            }
            
            // Add semesters (1-6 for three-year program)
            for (int i = 1; i <= 6; i++) {
                semesters.add(String.valueOf(i));
            }
            
            request.setAttribute("subjects", subjects);
            request.setAttribute("classes", classes);
            request.setAttribute("academicYears", academicYears);
            request.setAttribute("semesters", semesters);
            
            request.getRequestDispatcher("/WEB-INF/views/teacher/attendance/filter-form.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while preparing teacher filter form", e);
            request.setAttribute("error", "Failed to prepare filter form. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/teacher/attendance/filter-form.jsp").forward(request, response);
        }
    }
    
    /**
     * Process teacher filter form submission
     */
    private void processTeacherFilter(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        // Get filter parameters
        String subjectCode = request.getParameter("subjectCode");
        String classIdStr = request.getParameter("classId");
        String semester = request.getParameter("semester");
        String academicYear = request.getParameter("academicYear");
        String fromDateStr = request.getParameter("fromDate");
        String toDateStr = request.getParameter("toDate");
        String statusFilter = request.getParameter("status");
        String thresholdStr = request.getParameter("threshold");
        String comparisonType = request.getParameter("comparisonType");
        
        // Store filter parameters in session for results page
        HttpSession session = request.getSession();
        Map<String, Object> filterParams = new HashMap<>();
        
        if (subjectCode != null && !subjectCode.isEmpty()) {
            filterParams.put("subjectCode", subjectCode);
        }
        
        if (classIdStr != null && !classIdStr.isEmpty()) {
            try {
                int classId = Integer.parseInt(classIdStr);
                filterParams.put("classId", classId);
            } catch (NumberFormatException e) {
                // Invalid class ID, ignore
            }
        }
        
        if (semester != null && !semester.isEmpty()) {
            filterParams.put("semester", semester);
        }
        
        if (academicYear != null && !academicYear.isEmpty()) {
            filterParams.put("academicYear", academicYear);
        }
        
        if (fromDateStr != null && !fromDateStr.isEmpty()) {
            filterParams.put("fromDate", fromDateStr);
        }
        
        if (toDateStr != null && !toDateStr.isEmpty()) {
            filterParams.put("toDate", toDateStr);
        }
        
        if (statusFilter != null && !statusFilter.isEmpty()) {
            filterParams.put("status", statusFilter);
        }
        
        if (thresholdStr != null && !thresholdStr.isEmpty()) {
            try {
                double threshold = Double.parseDouble(thresholdStr);
                filterParams.put("threshold", threshold);
            } catch (NumberFormatException e) {
                // Invalid threshold, ignore
            }
        }
        
        if (comparisonType != null && !comparisonType.isEmpty()) {
            filterParams.put("comparisonType", comparisonType);
        }
        
        // Store teacher ID to restrict to their assigned classes/subjects
        filterParams.put("teacherId", user.getUserId());
        
        session.setAttribute("attendanceFilterParams", filterParams);
        
        // Redirect to results page
        response.sendRedirect(request.getContextPath() + "/teacher/attendance/filter/results");
    }
    
    /**
     * Show filter results for teachers
     */
    private void showTeacherFilterResults(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Map<String, Object> filterParams = (Map<String, Object>) session.getAttribute("attendanceFilterParams");
        
        if (filterParams == null || !filterParams.containsKey("teacherId") || 
            (int)filterParams.get("teacherId") != user.getUserId()) {
            // Invalid or missing filter parameters, return to filter form
            response.sendRedirect(request.getContextPath() + "/teacher/attendance/filter/form");
            return;
        }
        
        try {
            // Process filter parameters to fetch attendance data
            List<Map<String, Object>> filteredResults = getFilteredAttendanceResults(filterParams);
            
            // Get additional data for display
            Map<Integer, User> students = new HashMap<>();
            Map<String, Subject> subjects = new HashMap<>();
            Map<Integer, com.attendance.models.Class> classes = new HashMap<>();
            
            // Collect unique IDs
            Set<Integer> studentIds = new HashSet<>();
            Set<String> subjectCodes = new HashSet<>();
            Set<Integer> classIds = new HashSet<>();
            
            for (Map<String, Object> result : filteredResults) {
                if (result.containsKey("studentId")) {
                    studentIds.add((Integer) result.get("studentId"));
                }
                if (result.containsKey("subjectCode")) {
                    subjectCodes.add((String) result.get("subjectCode"));
                }
                if (result.containsKey("classId")) {
                    classIds.add((Integer) result.get("classId"));
                }
            }
            
            // Fetch details for display
            for (int studentId : studentIds) {
                User student = userDao.findById(studentId);
                if (student != null) {
                    students.put(studentId, student);
                }
            }
            
            for (String code : subjectCodes) {
                Subject subject = subjectDao.findByCode(code);
                if (subject != null) {
                    subjects.put(code, subject);
                }
            }
            
            for (int classId : classIds) {
                com.attendance.models.Class cls = classDao.findById(classId);
                if (cls != null) {
                    classes.put(classId, cls);
                }
            }
            
            request.setAttribute("filterParams", filterParams);
            request.setAttribute("filteredResults", filteredResults);
            request.setAttribute("students", students);
            request.setAttribute("subjects", subjects);
            request.setAttribute("classes", classes);
            
            request.getRequestDispatcher("/WEB-INF/views/teacher/attendance/filter-results.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while processing attendance filter", e);
            request.setAttribute("error", "Failed to filter attendance data. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/teacher/attendance/filter-results.jsp").forward(request, response);
        }
    }
    
    /**
     * Show filter form for students
     */
    private void showStudentFilterForm(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get student's current enrollment
            StudentEnrollment enrollment = studentEnrollmentDao.findCurrentEnrollment(user.getUserId());
            
            if (enrollment == null) {
                request.setAttribute("error", "You are not currently enrolled in any class");
                request.getRequestDispatcher("/WEB-INF/views/student/attendance/filter-form.jsp").forward(request, response);
                return;
            }
            
            int classId = enrollment.getClassId();
            String academicYear = enrollment.getAcademicYear();
            
            // Get class details
            com.attendance.models.Class cls = classDao.findById(classId);
            
            if (cls == null) {
                request.setAttribute("error", "Your enrolled class information could not be found");
                request.getRequestDispatcher("/WEB-INF/views/student/attendance/filter-form.jsp").forward(request, response);
                return;
            }
            
            // Get subjects for this class
            int departmentId = cls.getDepartmentId();
            
            // Get all semesters for this student's course
            List<String> semesters = new ArrayList<>();
            for (int i = 1; i <= 6; i++) {
                semesters.add(String.valueOf(i));
            }
            
            // Get subject details
            List<DepartmentSubject> departmentSubjects = departmentSubjectDao.findByDepartment(departmentId);
            Map<String, Subject> subjects = new HashMap<>();
            
            for (DepartmentSubject ds : departmentSubjects) {
                Subject subject = subjectDao.findByCode(ds.getSubjectCode());
                if (subject != null) {
                    subjects.put(ds.getSubjectCode(), subject);
                }
            }
            
            request.setAttribute("enrollment", enrollment);
            request.setAttribute("class", cls);
            request.setAttribute("subjects", subjects);
            request.setAttribute("semesters", semesters);
            request.setAttribute("academicYear", academicYear);
            
            request.getRequestDispatcher("/WEB-INF/views/student/attendance/filter-form.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while preparing student filter form", e);
            request.setAttribute("error", "Failed to prepare filter form. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/student/attendance/filter-form.jsp").forward(request, response);
        }
    }
    
    /**
     * Process student filter form submission
     */
    private void processStudentFilter(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        // Get filter parameters
        String subjectCode = request.getParameter("subjectCode");
        String semester = request.getParameter("semester");
        String academicYear = request.getParameter("academicYear");
        String fromDateStr = request.getParameter("fromDate");
        String toDateStr = request.getParameter("toDate");
        String statusFilter = request.getParameter("status");
        
        // Store filter parameters in session for results page
        HttpSession session = request.getSession();
        Map<String, Object> filterParams = new HashMap<>();
        
        if (subjectCode != null && !subjectCode.isEmpty()) {
            filterParams.put("subjectCode", subjectCode);
        }
        
        if (semester != null && !semester.isEmpty()) {
            filterParams.put("semester", semester);
        }
        
        if (academicYear != null && !academicYear.isEmpty()) {
            filterParams.put("academicYear", academicYear);
        }
        
        if (fromDateStr != null && !fromDateStr.isEmpty()) {
            filterParams.put("fromDate", fromDateStr);
        }
        
        if (toDateStr != null && !toDateStr.isEmpty()) {
            filterParams.put("toDate", toDateStr);
        }
        
        if (statusFilter != null && !statusFilter.isEmpty()) {
            filterParams.put("status", statusFilter);
        }
        
        // Restrict to the student's own records
        filterParams.put("studentId", user.getUserId());
        
        session.setAttribute("attendanceFilterParams", filterParams);
        
        // Redirect to results page
        response.sendRedirect(request.getContextPath() + "/student/attendance/filter/results");
    }
    
    /**
     * Show filter results for students
     */
    private void showStudentFilterResults(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Map<String, Object> filterParams = (Map<String, Object>) session.getAttribute("attendanceFilterParams");
        
        if (filterParams == null || !filterParams.containsKey("studentId") || 
            (int)filterParams.get("studentId") != user.getUserId()) {
            // Invalid or missing filter parameters, return to filter form
            response.sendRedirect(request.getContextPath() + "/student/attendance/filter/form");
            return;
        }
        
        try {
            // Process filter parameters to fetch attendance data
            List<Map<String, Object>> filteredResults = getFilteredAttendanceResults(filterParams);
            
            // Get additional data for display
            Map<String, Subject> subjects = new HashMap<>();
            
            // Collect unique subject codes
            Set<String> subjectCodes = new HashSet<>();
            
            for (Map<String, Object> result : filteredResults) {
                if (result.containsKey("subjectCode")) {
                    subjectCodes.add((String) result.get("subjectCode"));
                }
            }
            
            // Fetch subject details for display
            for (String code : subjectCodes) {
                Subject subject = subjectDao.findByCode(code);
                if (subject != null) {
                    subjects.put(code, subject);
                }
            }
            
            // Calculate attendance summary
            Map<String, Double> attendancePercentages = new HashMap<>();
            Map<String, Integer> presentCounts = new HashMap<>();
            Map<String, Integer> totalCounts = new HashMap<>();
            
            for (String code : subjectCodes) {
                int presentCount = 0;
                int totalCount = 0;
                
                for (Map<String, Object> result : filteredResults) {
                    if (code.equals(result.get("subjectCode"))) {
                        totalCount++;
                        if ("Present".equals(result.get("status"))) {
                            presentCount++;
                        }
                    }
                }
                
                presentCounts.put(code, presentCount);
                totalCounts.put(code, totalCount);
                
                if (totalCount > 0) {
                    double percentage = (double) presentCount / totalCount * 100;
                    attendancePercentages.put(code, percentage);
                } else {
                    attendancePercentages.put(code, 0.0);
                }
            }
            
            request.setAttribute("filterParams", filterParams);
            request.setAttribute("filteredResults", filteredResults);
            request.setAttribute("subjects", subjects);
            request.setAttribute("attendancePercentages", attendancePercentages);
            request.setAttribute("presentCounts", presentCounts);
            request.setAttribute("totalCounts", totalCounts);
            
            request.getRequestDispatcher("/WEB-INF/views/student/attendance/filter-results.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while processing student attendance filter", e);
            request.setAttribute("error", "Failed to filter attendance data. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/student/attendance/filter-results.jsp").forward(request, response);
        }
    }
    
    /**
     * Show filter form for HODs
     */
    private void showHodFilterForm(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get HOD's department
            List<Department> departments = departmentDao.findByHod(user.getUserId());
            Department department = departments != null && !departments.isEmpty() ? departments.get(0) : null;
            
            if (department == null) {
                request.setAttribute("error", "You are not assigned to any department");
                request.getRequestDispatcher("/WEB-INF/views/hod/attendance/filter-form.jsp").forward(request, response);
                return;
            }
            
            // Get classes in the department
            List<com.attendance.models.Class> classes = classDao.findByDepartment(department.getDepartmentId());
            
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
            
            // Get list of semesters
            List<String> semesters = new ArrayList<>();
            for (int i = 1; i <= 6; i++) {
                semesters.add(String.valueOf(i));
            }
            
            // Get list of academic years
            List<String> academicYears = new ArrayList<>();
            int currentYear = LocalDate.now().getYear();
            for (int i = 0; i < 5; i++) {
                int startYear = currentYear - i;
                academicYears.add(startYear + "-" + (startYear + 1));
            }
            
            request.setAttribute("department", department);
            request.setAttribute("classes", classes);
            request.setAttribute("subjects", subjects);
            request.setAttribute("semesters", semesters);
            request.setAttribute("academicYears", academicYears);
            
            request.getRequestDispatcher("/WEB-INF/views/hod/attendance/filter-form.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while preparing HOD filter form", e);
            request.setAttribute("error", "Failed to prepare filter form. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/hod/attendance/filter-form.jsp").forward(request, response);
        }
    }
    
    /**
     * Process HOD filter form submission
     */
    private void processHodFilter(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get HOD's department
            List<Department> departments = departmentDao.findByHod(user.getUserId());
            Department department = departments != null && !departments.isEmpty() ? departments.get(0) : null;
            
            if (department == null) {
                request.setAttribute("error", "You are not assigned to any department");
                request.getRequestDispatcher("/WEB-INF/views/hod/attendance/filter-form.jsp").forward(request, response);
                return;
            }
            
            // Get filter parameters
            String subjectCode = request.getParameter("subjectCode");
            String classIdStr = request.getParameter("classId");
            String semester = request.getParameter("semester");
            String academicYear = request.getParameter("academicYear");
            String fromDateStr = request.getParameter("fromDate");
            String toDateStr = request.getParameter("toDate");
            String statusFilter = request.getParameter("status");
            String thresholdStr = request.getParameter("threshold");
            String comparisonType = request.getParameter("comparisonType");
            String reportType = request.getParameter("reportType");
            
            // Store filter parameters in session for results page
            HttpSession session = request.getSession();
            Map<String, Object> filterParams = new HashMap<>();
            
            filterParams.put("departmentId", department.getDepartmentId());
            
            if (subjectCode != null && !subjectCode.isEmpty()) {
                filterParams.put("subjectCode", subjectCode);
            }
            
            if (classIdStr != null && !classIdStr.isEmpty()) {
                try {
                    int classId = Integer.parseInt(classIdStr);
                    
                    // Verify class belongs to HOD's department
                    com.attendance.models.Class cls = classDao.findById(classId);
                    if (cls != null && cls.getDepartmentId() == department.getDepartmentId()) {
                        filterParams.put("classId", classId);
                    }
                } catch (NumberFormatException e) {
                    // Invalid class ID, ignore
                }
            }
            
            if (semester != null && !semester.isEmpty()) {
                filterParams.put("semester", semester);
            }
            
            if (academicYear != null && !academicYear.isEmpty()) {
                filterParams.put("academicYear", academicYear);
            }
            
            if (fromDateStr != null && !fromDateStr.isEmpty()) {
                filterParams.put("fromDate", fromDateStr);
            }
            
            if (toDateStr != null && !toDateStr.isEmpty()) {
                filterParams.put("toDate", toDateStr);
            }
            
            if (statusFilter != null && !statusFilter.isEmpty()) {
                filterParams.put("status", statusFilter);
            }
            
            if (thresholdStr != null && !thresholdStr.isEmpty()) {
                try {
                    double threshold = Double.parseDouble(thresholdStr);
                    filterParams.put("threshold", threshold);
                } catch (NumberFormatException e) {
                    // Invalid threshold, ignore
                }
            }
            
            if (comparisonType != null && !comparisonType.isEmpty()) {
                filterParams.put("comparisonType", comparisonType);
            }
            
            if (reportType != null && !reportType.isEmpty()) {
                filterParams.put("reportType", reportType);
            }
            
            session.setAttribute("attendanceFilterParams", filterParams);
            
            // Redirect to results page
            response.sendRedirect(request.getContextPath() + "/hod/attendance/filter/results");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while processing HOD filter", e);
            request.setAttribute("error", "Failed to process filter. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/hod/attendance/filter-form.jsp").forward(request, response);
        }
    }
    
    /**
     * Show filter results for HODs
     */
    private void showHodFilterResults(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Map<String, Object> filterParams = (Map<String, Object>) session.getAttribute("attendanceFilterParams");
        
        if (filterParams == null || !filterParams.containsKey("departmentId")) {
            // Invalid or missing filter parameters, return to filter form
            response.sendRedirect(request.getContextPath() + "/hod/attendance/filter/form");
            return;
        }
        
        try {
            // Verify user is HOD of the department in filter params
            List<Department> departments = departmentDao.findByHod(user.getUserId());
            Department department = departments != null && !departments.isEmpty() ? departments.get(0) : null;
            
            if (department == null || department.getDepartmentId() != (int)filterParams.get("departmentId")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not authorized to view this department's data");
                return;
            }
            
            // Process filter parameters to fetch attendance data
            List<Map<String, Object>> filteredResults = getFilteredAttendanceResults(filterParams);
            
            // Get additional data for display
            Map<Integer, User> students = new HashMap<>();
            Map<String, Subject> subjects = new HashMap<>();
            Map<Integer, com.attendance.models.Class> classes = new HashMap<>();
            
            // Collect unique IDs
            Set<Integer> studentIds = new HashSet<>();
            Set<String> subjectCodes = new HashSet<>();
            Set<Integer> classIds = new HashSet<>();
            
            for (Map<String, Object> result : filteredResults) {
                if (result.containsKey("studentId")) {
                    studentIds.add((Integer) result.get("studentId"));
                }
                if (result.containsKey("subjectCode")) {
                    subjectCodes.add((String) result.get("subjectCode"));
                }
                if (result.containsKey("classId")) {
                    classIds.add((Integer) result.get("classId"));
                }
            }
            
            // Fetch details for display
            for (int studentId : studentIds) {
                User student = userDao.findById(studentId);
                if (student != null) {
                    students.put(studentId, student);
                }
            }
            
            for (String code : subjectCodes) {
                Subject subject = subjectDao.findByCode(code);
                if (subject != null) {
                    subjects.put(code, subject);
                }
            }
            
            for (int classId : classIds) {
                com.attendance.models.Class cls = classDao.findById(classId);
                if (cls != null) {
                    classes.put(classId, cls);
                }
            }
            
            request.setAttribute("department", department);
            request.setAttribute("filterParams", filterParams);
            request.setAttribute("filteredResults", filteredResults);
            request.setAttribute("students", students);
            request.setAttribute("subjects", subjects);
            request.setAttribute("classes", classes);
            
            // Generate appropriate report view based on report type
            String reportType = (String) filterParams.getOrDefault("reportType", "detailed");
            String reportView;
            
            switch (reportType) {
                case "summary":
                    reportView = "/WEB-INF/views/hod/attendance/summary-report.jsp";
                    break;
                case "below-threshold":
                    reportView = "/WEB-INF/views/hod/attendance/threshold-report.jsp";
                    break;
                case "class-comparison":
                    reportView = "/WEB-INF/views/hod/attendance/class-comparison-report.jsp";
                    break;
                case "subject-comparison":
                    reportView = "/WEB-INF/views/hod/attendance/subject-comparison-report.jsp";
                    break;
                case "detailed":
                default:
                    reportView = "/WEB-INF/views/hod/attendance/detailed-report.jsp";
                    break;
            }
            
            request.getRequestDispatcher(reportView).forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while processing HOD attendance filter", e);
            request.setAttribute("error", "Failed to filter attendance data. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/hod/attendance/filter-results.jsp").forward(request, response);
        }
    }
    
    /**
     * Export filtered results to CSV
     */
    private void exportFilteredResults(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Map<String, Object> filterParams = (Map<String, Object>) session.getAttribute("attendanceFilterParams");
        
        if (filterParams == null) {
            // Invalid or missing filter parameters, return to appropriate filter form
            if ("Teacher".equals(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/teacher/attendance/filter/form");
            } else if ("HOD".equals(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/hod/attendance/filter/form");
            } else {
                response.sendRedirect(request.getContextPath() + "/dashboard");
            }
            return;
        }
        
        try {
            // Process filter parameters to fetch attendance data
            List<Map<String, Object>> filteredResults = getFilteredAttendanceResults(filterParams);
            
            // Get additional data for CSV
            Map<Integer, User> students = new HashMap<>();
            Map<String, Subject> subjects = new HashMap<>();
            Map<Integer, com.attendance.models.Class> classes = new HashMap<>();
            
            // Collect unique IDs
            Set<Integer> studentIds = new HashSet<>();
            Set<String> subjectCodes = new HashSet<>();
            Set<Integer> classIds = new HashSet<>();
            
            for (Map<String, Object> result : filteredResults) {
                if (result.containsKey("studentId")) {
                    studentIds.add((Integer) result.get("studentId"));
                }
                if (result.containsKey("subjectCode")) {
                    subjectCodes.add((String) result.get("subjectCode"));
                }
                if (result.containsKey("classId")) {
                    classIds.add((Integer) result.get("classId"));
                }
            }
            
            // Fetch details for CSV
            for (int studentId : studentIds) {
                User student = userDao.findById(studentId);
                if (student != null) {
                    students.put(studentId, student);
                }
            }
            
            for (String code : subjectCodes) {
                Subject subject = subjectDao.findByCode(code);
                if (subject != null) {
                    subjects.put(code, subject);
                }
            }
            
            for (int classId : classIds) {
                com.attendance.models.Class cls = classDao.findById(classId);
                if (cls != null) {
                    classes.put(classId, cls);
                }
            }
            
            // Generate CSV content
            StringBuilder csv = new StringBuilder();
            
            // Add header row
            csv.append("Date,Student ID,Student Name,Subject Code,Subject Name,Class,Semester,Status\n");
            
            // Add data rows
            for (Map<String, Object> result : filteredResults) {
                Date date = (Date) result.get("attendanceDate");
                int studentId = (int) result.get("studentId");
                String subjectCode = (String) result.get("subjectCode");
                String semester = (String) result.get("semester");
                String status = (String) result.get("status");
                int classId = (int) result.getOrDefault("classId", 0);
                
                String studentName = "";
                if (students.containsKey(studentId)) {
                    studentName = students.get(studentId).getFullName();
                }
                
                String subjectName = "";
                if (subjects.containsKey(subjectCode)) {
                    subjectName = subjects.get(subjectCode).getSubjectName();
                }
                
                String className = "";
                if (classes.containsKey(classId)) {
                    className = classes.get(classId).getClassName();
                }
                
                csv.append(date).append(",");
                csv.append(studentId).append(",");
                csv.append(escapeCSV(studentName)).append(",");
                csv.append(escapeCSV(subjectCode)).append(",");
                csv.append(escapeCSV(subjectName)).append(",");
                csv.append(escapeCSV(className)).append(",");
                csv.append(escapeCSV(semester)).append(",");
                csv.append(escapeCSV(status)).append("\n");
            }
            
            // Set response headers for CSV download
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=\"attendance_report.csv\"");
            
            // Write CSV content to response
            response.getWriter().write(csv.toString());
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while exporting attendance data", e);
            if ("Teacher".equals(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/teacher/attendance/filter/results?error=exportFailed");
            } else if ("HOD".equals(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/hod/attendance/filter/results?error=exportFailed");
            } else {
                response.sendRedirect(request.getContextPath() + "/dashboard?error=exportFailed");
            }
        }
    }
    
    /**
     * Get filtered attendance results based on filter parameters
     */
    private List<Map<String, Object>> getFilteredAttendanceResults(Map<String, Object> filterParams) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        
        // Base SQL for attendance query
        StringBuilder sql = new StringBuilder("SELECT a.attendance_id, a.attendance_date, a.student_id, a.subject_code, " +
                                            "a.semester, a.academic_year, a.status, se.class_id " +
                                            "FROM Attendance a " +
                                            "JOIN StudentEnrollments se ON a.student_id = se.student_id " +
                                            "WHERE 1=1 ");
        
        // List of parameters for prepared statement
        List<Object> params = new ArrayList<>();
        
        // Add where clauses based on filter parameters
        if (filterParams.containsKey("studentId")) {
            sql.append("AND a.student_id = ? ");
            params.add(filterParams.get("studentId"));
        }
        
        if (filterParams.containsKey("subjectCode")) {
            sql.append("AND a.subject_code = ? ");
            params.add(filterParams.get("subjectCode"));
        }
        
        if (filterParams.containsKey("classId")) {
            sql.append("AND se.class_id = ? ");
            params.add(filterParams.get("classId"));
        }
        
        if (filterParams.containsKey("semester")) {
            sql.append("AND a.semester = ? ");
            params.add(filterParams.get("semester"));
        }
        
        if (filterParams.containsKey("academicYear")) {
            sql.append("AND a.academic_year = ? ");
            params.add(filterParams.get("academicYear"));
        }
        
        if (filterParams.containsKey("fromDate")) {
            try {
                LocalDate fromDate = LocalDate.parse((String) filterParams.get("fromDate"));
                sql.append("AND a.attendance_date >= ? ");
                params.add(Date.valueOf(fromDate));
            } catch (Exception e) {
                // Invalid date format, ignore
            }
        }
        
        if (filterParams.containsKey("toDate")) {
            try {
                LocalDate toDate = LocalDate.parse((String) filterParams.get("toDate"));
                sql.append("AND a.attendance_date <= ? ");
                params.add(Date.valueOf(toDate));
            } catch (Exception e) {
                // Invalid date format, ignore
            }
        }
        
        if (filterParams.containsKey("status")) {
            sql.append("AND a.status = ? ");
            params.add(filterParams.get("status"));
        }
        
        if (filterParams.containsKey("departmentId")) {
            sql.append("AND EXISTS (SELECT 1 FROM Classes c WHERE c.class_id = se.class_id AND c.department_id = ?) ");
            params.add(filterParams.get("departmentId"));
        }
        
        if (filterParams.containsKey("teacherId")) {
            sql.append("AND EXISTS (SELECT 1 FROM TeacherAssignments ta WHERE ta.subject_code = a.subject_code " +
                     "AND ta.class_id = se.class_id AND ta.teacher_id = ?) ");
            params.add(filterParams.get("teacherId"));
        }
        
        // Add order clause
        sql.append("ORDER BY a.attendance_date DESC, a.subject_code, se.class_id, a.student_id");
        
        // Execute query
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                setParameter(stmt, i + 1, params.get(i));
            }
            
            // Execute and process results
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("attendanceId", rs.getInt("attendance_id"));
                    result.put("attendanceDate", rs.getDate("attendance_date"));
                    result.put("studentId", rs.getInt("student_id"));
                    result.put("subjectCode", rs.getString("subject_code"));
                    result.put("semester", rs.getString("semester"));
                    result.put("academicYear", rs.getString("academic_year"));
                    result.put("status", rs.getString("status"));
                    result.put("classId", rs.getInt("class_id"));
                    
                    results.add(result);
                }
            }
        }
        
        // Apply additional filtering that's complex to do in SQL
        
        // Filter by attendance threshold if specified
        if (filterParams.containsKey("threshold") && filterParams.containsKey("comparisonType")) {
            double threshold = (double) filterParams.get("threshold");
            String comparisonType = (String) filterParams.get("comparisonType");
            
            results = filterByAttendanceThreshold(results, threshold, comparisonType);
        }
        
        return results;
    }
    
    /**
     * Filter results by attendance threshold
     */
    private List<Map<String, Object>> filterByAttendanceThreshold(List<Map<String, Object>> results, 
                                                               double threshold, String comparisonType) {
        // Group attendance by student and subject
        Map<String, List<Map<String, Object>>> grouped = new HashMap<>();
        
        for (Map<String, Object> result : results) {
            int studentId = (int) result.get("studentId");
            String subjectCode = (String) result.get("subjectCode");
            String key = studentId + "_" + subjectCode;
            
            if (!grouped.containsKey(key)) {
                grouped.put(key, new ArrayList<>());
            }
            
            grouped.get(key).add(result);
        }
        
        // Calculate attendance percentages and filter
        List<Map<String, Object>> filteredResults = new ArrayList<>();
        
        for (String key : grouped.keySet()) {
            List<Map<String, Object>> studentSubjectAttendance = grouped.get(key);
            
            int totalCount = studentSubjectAttendance.size();
            int presentCount = 0;
            
            for (Map<String, Object> attendance : studentSubjectAttendance) {
                if ("Present".equals(attendance.get("status"))) {
                    presentCount++;
                }
            }
            
            double percentage = totalCount > 0 ? (double) presentCount / totalCount * 100 : 0;
            
            boolean includeRecords = false;
            
            switch (comparisonType) {
                case "below":
                    includeRecords = percentage < threshold;
                    break;
                case "above":
                    includeRecords = percentage > threshold;
                    break;
                case "equal":
                    includeRecords = Math.abs(percentage - threshold) < 0.01; // Allow small margin due to floating point
                    break;
                default:
                    includeRecords = true;
            }
            
            if (includeRecords) {
                filteredResults.addAll(studentSubjectAttendance);
            }
        }
        
        return filteredResults;
    }
    
    /**
     * Set parameter of appropriate type in prepared statement
     */
    private void setParameter(PreparedStatement stmt, int index, Object param) throws SQLException {
        if (param instanceof String) {
            stmt.setString(index, (String) param);
        } else if (param instanceof Integer) {
            stmt.setInt(index, (Integer) param);
        } else if (param instanceof Date) {
            stmt.setDate(index, (Date) param);
        } else if (param instanceof Double) {
            stmt.setDouble(index, (Double) param);
        } else if (param instanceof Boolean) {
            stmt.setBoolean(index, (Boolean) param);
        } else if (param == null) {
            stmt.setNull(index, java.sql.Types.VARCHAR);
        }
    }
    
    /**
     * Escape a string for CSV format
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        
        boolean needsQuoting = value.contains(",") || value.contains("\"") || value.contains("\n");
        
        if (needsQuoting) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        } else {
            return value;
        }
    }
}