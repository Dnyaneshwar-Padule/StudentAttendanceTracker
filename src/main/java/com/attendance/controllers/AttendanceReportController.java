package com.attendance.controllers;

import com.attendance.dao.*;
import com.attendance.dao.impl.*;
import com.attendance.models.*;
import com.attendance.utils.DateUtils;
import com.attendance.utils.EmailNotificationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for generating advanced attendance reports and analytics
 */
@WebServlet(name = "AttendanceReportController", urlPatterns = {
    "/reports/attendance/*",
    "/teacher/reports/*",
    "/hod/reports/*",
    "/principal/reports/*"
})
public class AttendanceReportController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AttendanceReportController.class.getName());
    
    private AttendanceDao attendanceDao;
    private UserDao userDao;
    private ClassDao classDao;
    private SubjectDao subjectDao;
    private DepartmentDao departmentDao;
    private StudentEnrollmentDao studentEnrollmentDao;
    private TeacherAssignmentDao teacherAssignmentDao;
    private DepartmentSubjectDao departmentSubjectDao;
    private EmailNotificationService emailService;

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
        emailService = EmailNotificationService.getInstance();
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
        
        try {
            if (servletPath.equals("/reports/attendance")) {
                if (pathInfo == null || pathInfo.equals("/")) {
                    // Redirect to appropriate reports dashboard based on role
                    redirectToRoleDashboard(request, response, user);
                } else if (pathInfo.equals("/summary")) {
                    // Generate attendance summary report
                    generateSummaryReport(request, response, user);
                } else if (pathInfo.equals("/detailed")) {
                    // Generate detailed attendance report
                    generateDetailedReport(request, response, user);
                } else if (pathInfo.equals("/trend")) {
                    // Generate attendance trend report
                    generateTrendReport(request, response, user);
                } else if (pathInfo.equals("/notification")) {
                    // Send attendance report notification
                    sendReportNotification(request, response, user);
                } else if (pathInfo.equals("/download")) {
                    // Download attendance report
                    downloadReport(request, response, user);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else if (servletPath.equals("/teacher/reports")) {
                handleTeacherReports(request, response, user, pathInfo);
            } else if (servletPath.equals("/hod/reports")) {
                handleHodReports(request, response, user, pathInfo);
            } else if (servletPath.equals("/principal/reports")) {
                handlePrincipalReports(request, response, user, pathInfo);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in AttendanceReportController", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        }
    }

    private void handleTeacherReports(HttpServletRequest request, HttpServletResponse response, User user, String pathInfo) 
            throws ServletException, IOException, SQLException {
        if ("Teacher".equals(user.getRole())) {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Show reports dashboard for teachers
                showTeacherReportsDashboard(request, response, user);
            } else if (pathInfo.equals("/generate")) {
                // Show form to generate a report
                showTeacherReportForm(request, response, user);
            } else if (pathInfo.equals("/view")) {
                // View a generated report
                viewTeacherReport(request, response, user);
            } else if (pathInfo.equals("/one-click-summary")) {
                // Generate one-click attendance summary
                generateOneClickSummary(request, response, user);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only teachers can access this page");
        }
    }

    private void handleHodReports(HttpServletRequest request, HttpServletResponse response, User user, String pathInfo) 
            throws ServletException, IOException, SQLException {
        if ("HOD".equals(user.getRole())) {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Show reports dashboard for HODs
                showHodReportsDashboard(request, response, user);
            } else if (pathInfo.equals("/generate")) {
                // Show form to generate a report
                showHodReportForm(request, response, user);
            } else if (pathInfo.equals("/view")) {
                // View a generated report
                viewHodReport(request, response, user);
            } else if (pathInfo.equals("/department-analytics")) {
                // View department-wide analytics
                viewDepartmentAnalytics(request, response, user);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only HODs can access this page");
        }
    }

    private void handlePrincipalReports(HttpServletRequest request, HttpServletResponse response, User user, String pathInfo) 
            throws ServletException, IOException, SQLException {
        if ("Principal".equals(user.getRole()) || "Admin".equals(user.getRole())) {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Show reports dashboard for principal
                showPrincipalReportsDashboard(request, response, user);
            } else if (pathInfo.equals("/generate")) {
                // Show form to generate a report
                showPrincipalReportForm(request, response, user);
            } else if (pathInfo.equals("/view")) {
                // View a generated report
                viewPrincipalReport(request, response, user);
            } else if (pathInfo.equals("/institution-analytics")) {
                // View institution-wide analytics
                viewInstitutionAnalytics(request, response, user);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have permission to access this page");
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
        
        try {
            if (servletPath.equals("/reports/attendance")) {
                if (pathInfo.equals("/generate")) {
                    // Process report generation form
                    processReportGeneration(request, response, user);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else if (servletPath.equals("/teacher/reports")) {
                handleTeacherReportsPosts(request, response, user, pathInfo);
            } else if (servletPath.equals("/hod/reports")) {
                handleHodReportsPosts(request, response, user, pathInfo);
            } else if (servletPath.equals("/principal/reports")) {
                handlePrincipalReportsPosts(request, response, user, pathInfo);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in AttendanceReportController POST", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        }
    }

    private void handleTeacherReportsPosts(HttpServletRequest request, HttpServletResponse response, User user, String pathInfo) 
            throws ServletException, IOException, SQLException {
        if ("Teacher".equals(user.getRole())) {
            if (pathInfo.equals("/generate")) {
                // Process teacher report generation form
                processTeacherReportGeneration(request, response, user);
            } else if (pathInfo.equals("/notify-students")) {
                // Process notification to students
                processNotifyStudents(request, response, user);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only teachers can access this page");
        }
    }

    private void handleHodReportsPosts(HttpServletRequest request, HttpServletResponse response, User user, String pathInfo) 
            throws ServletException, IOException, SQLException {
        if ("HOD".equals(user.getRole())) {
            if (pathInfo.equals("/generate")) {
                // Process HOD report generation form
                processHodReportGeneration(request, response, user);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only HODs can access this page");
        }
    }

    private void handlePrincipalReportsPosts(HttpServletRequest request, HttpServletResponse response, User user, String pathInfo) 
            throws ServletException, IOException, SQLException {
        if ("Principal".equals(user.getRole()) || "Admin".equals(user.getRole())) {
            if (pathInfo.equals("/generate")) {
                // Process principal report generation form
                processPrincipalReportGeneration(request, response, user);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have permission to access this page");
        }
    }
    
    /**
     * Redirect to the appropriate reports dashboard based on user role
     */
    private void redirectToRoleDashboard(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        switch (user.getRole()) {
            case "Teacher":
                response.sendRedirect(request.getContextPath() + "/teacher/reports/");
                break;
            case "HOD":
                response.sendRedirect(request.getContextPath() + "/hod/reports/");
                break;
            case "Principal":
            case "Admin":
                response.sendRedirect(request.getContextPath() + "/principal/reports/");
                break;
            case "Student":
                response.sendRedirect(request.getContextPath() + "/student/attendance/summary");
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/dashboard");
                break;
        }
    }
    
    /**
     * Generate attendance summary report
     */
    private void generateSummaryReport(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Get report parameters
        String subjectCode = request.getParameter("subjectCode");
        String classIdStr = request.getParameter("classId");
        String semester = request.getParameter("semester");
        String academicYear = request.getParameter("academicYear");
        
        if (subjectCode == null || classIdStr == null || semester == null || academicYear == null) {
            response.sendRedirect(request.getContextPath() + "/reports/attendance?error=missingParams");
            return;
        }
        
        try {
            int classId = Integer.parseInt(classIdStr);
            
            // Check user permissions based on role
            boolean hasPermission = false;
            
            if ("Teacher".equals(user.getRole())) {
                // Check if teacher is assigned to this subject and class
                TeacherAssignment assignment = teacherAssignmentDao.findByTeacherSubjectAndClass(
                        user.getUserId(), subjectCode, classId);
                hasPermission = (assignment != null);
            } else if ("HOD".equals(user.getRole())) {
                // Check if class belongs to HOD's department
                List<Department> departments = departmentDao.findByHod(user.getUserId());
                if (!departments.isEmpty()) {
                    Department department = departments.get(0); // Get first department (HODs typically manage one department)
                    com.attendance.models.Class cls = classDao.findById(classId);
                    hasPermission = (cls != null && cls.getDepartmentId() == department.getDepartmentId());
                }
            } else if ("Principal".equals(user.getRole()) || "Admin".equals(user.getRole())) {
                // Principal and admin have permission to all
                hasPermission = true;
            }
            
            if (!hasPermission) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have permission to access this report");
                return;
            }
            
            // Get report data
            Map<String, Object> reportData = generateAttendanceSummaryData(subjectCode, classId, semester, academicYear);
            
            // Add report parameters to request
            request.setAttribute("subjectCode", subjectCode);
            request.setAttribute("classId", classId);
            request.setAttribute("semester", semester);
            request.setAttribute("academicYear", academicYear);
            
            // Add report data to request
            request.setAttribute("reportData", reportData);
            
            // Forward to report view
            request.getRequestDispatcher("/WEB-INF/views/reports/attendance-summary.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid class ID");
        }
    }

    // Placeholder methods to be implemented
    private void generateDetailedReport(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void generateTrendReport(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void sendReportNotification(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void downloadReport(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void showTeacherReportsDashboard(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void showTeacherReportForm(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void viewTeacherReport(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void generateOneClickSummary(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void showHodReportsDashboard(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void showHodReportForm(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void viewHodReport(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void viewDepartmentAnalytics(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void showPrincipalReportsDashboard(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void showPrincipalReportForm(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void viewPrincipalReport(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void viewInstitutionAnalytics(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void processReportGeneration(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void processTeacherReportGeneration(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void processNotifyStudents(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void processHodReportGeneration(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    private void processPrincipalReportGeneration(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException, SQLException {
        // Implementation details go here
    }
    
    /**
     * Generate attendance summary data
     */
    private Map<String, Object> generateAttendanceSummaryData(String subjectCode, int classId, String semester, String academicYear) 
            throws SQLException {
        Map<String, Object> reportData = new HashMap<>();
        
        // Get subject and class details
        Subject subject = subjectDao.findByCode(subjectCode);
        com.attendance.models.Class cls = classDao.findById(classId);
        
        reportData.put("subject", subject);
        reportData.put("class", cls);
        
        // Placeholder for implementation
        
        return reportData;
    }
}