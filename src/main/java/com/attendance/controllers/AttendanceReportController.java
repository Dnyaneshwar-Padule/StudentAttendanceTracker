package com.attendance.controllers;

import com.attendance.dao.*;
import com.attendance.dao.impl.*;
import com.attendance.models.*;
import com.attendance.utils.DateUtils;
import com.attendance.utils.EmailNotificationService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
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
        } else if (servletPath.equals("/hod/reports")) {
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
        } else if (servletPath.equals("/principal/reports")) {
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
        
        if (servletPath.equals("/reports/attendance")) {
            if (pathInfo.equals("/generate")) {
                // Process report generation form
                processReportGeneration(request, response, user);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else if (servletPath.equals("/teacher/reports")) {
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
        } else if (servletPath.equals("/hod/reports")) {
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
        } else if (servletPath.equals("/principal/reports")) {
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
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
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
    private void generateSummaryReport(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
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
                Department department = departmentDao.findByHod(user.getUserId());
                if (department != null) {
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
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while generating attendance summary report", e);
            response.sendRedirect(request.getContextPath() + "/reports/attendance?error=database");
        }
    }
    
    /**
     * Generate attendance summary data
     */
    private Map<String, Object> generateAttendanceSummaryData(String subjectCode, int classId, String semester, String academicYear) throws SQLException {
        Map<String, Object> reportData = new HashMap<>();
        
        // Get subject and class details
        Subject subject = subjectDao.findByCode(subjectCode);
        com.attendance.models.Class cls = classDao.findById(classId);
        
        reportData.put("subject", subject);
        reportData.put("class", cls);
        
        // Get students enrolled in this class
        List<StudentEnrollment> enrollments = studentEnrollmentDao.findByClass(classId);
        
        // Get student details and attendance data
        Map<Integer, User> students = new HashMap<>();
        Map<Integer, Map<String, Object>> attendanceData = new HashMap<>();
        
        int totalStudents = 0;
        int studentsBelow75 = 0;
        int studentsAbove90 = 0;
        double totalAttendancePercentage = 0;
        
        for (StudentEnrollment enrollment : enrollments) {
            int studentId = enrollment.getStudentId();
            User student = userDao.findById(studentId);
            
            if (student != null && "Student".equals(student.getRole()) && "Active".equals(student.getStatus())) {
                students.put(studentId, student);
                totalStudents++;
                
                // Calculate attendance percentage
                double percentage = attendanceDao.calculateAttendancePercentage(
                        studentId, subjectCode, semester, academicYear);
                
                // Count students in different attendance bands
                if (percentage < 75) {
                    studentsBelow75++;
                }
                if (percentage > 90) {
                    studentsAbove90++;
                }
                
                totalAttendancePercentage += percentage;
                
                // Get attendance records for detailed analysis
                List<Attendance> records = attendanceDao.findByStudentSubjectSemesterAndYear(
                        studentId, subjectCode, semester, academicYear);
                
                int totalClasses = records.size();
                int presentCount = 0;
                int absentCount = 0;
                int leaveCount = 0;
                
                for (Attendance record : records) {
                    if ("Present".equals(record.getStatus())) {
                        presentCount++;
                    } else if ("Absent".equals(record.getStatus())) {
                        absentCount++;
                    } else if ("Leave".equals(record.getStatus())) {
                        leaveCount++;
                    }
                }
                
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("percentage", percentage);
                studentData.put("totalClasses", totalClasses);
                studentData.put("presentCount", presentCount);
                studentData.put("absentCount", absentCount);
                studentData.put("leaveCount", leaveCount);
                
                attendanceData.put(studentId, studentData);
            }
        }
        
        // Calculate average attendance percentage
        double averageAttendance = totalStudents > 0 ? totalAttendancePercentage / totalStudents : 0;
        
        // Add summary statistics to report data
        reportData.put("students", students);
        reportData.put("attendanceData", attendanceData);
        reportData.put("totalStudents", totalStudents);
        reportData.put("studentsBelow75", studentsBelow75);
        reportData.put("studentsAbove90", studentsAbove90);
        reportData.put("averageAttendance", averageAttendance);
        
        return reportData;
    }
    
    /**
     * Generate detailed attendance report
     */
    private void generateDetailedReport(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        // Get report parameters
        String subjectCode = request.getParameter("subjectCode");
        String classIdStr = request.getParameter("classId");
        String semester = request.getParameter("semester");
        String academicYear = request.getParameter("academicYear");
        String fromDateStr = request.getParameter("fromDate");
        String toDateStr = request.getParameter("toDate");
        
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
                Department department = departmentDao.findByHod(user.getUserId());
                if (department != null) {
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
            
            // Parse date range if provided
            LocalDate fromDate = null;
            LocalDate toDate = null;
            
            if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
                fromDate = DateUtils.parseDate(fromDateStr);
            }
            
            if (toDateStr != null && !toDateStr.trim().isEmpty()) {
                toDate = DateUtils.parseDate(toDateStr);
            }
            
            // Get report data
            Map<String, Object> reportData = generateDetailedAttendanceData(subjectCode, classId, semester, academicYear, fromDate, toDate);
            
            // Add report parameters to request
            request.setAttribute("subjectCode", subjectCode);
            request.setAttribute("classId", classId);
            request.setAttribute("semester", semester);
            request.setAttribute("academicYear", academicYear);
            request.setAttribute("fromDate", fromDate);
            request.setAttribute("toDate", toDate);
            
            // Add report data to request
            request.setAttribute("reportData", reportData);
            
            // Forward to report view
            request.getRequestDispatcher("/WEB-INF/views/reports/attendance-detailed.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid class ID");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while generating detailed attendance report", e);
            response.sendRedirect(request.getContextPath() + "/reports/attendance?error=database");
        }
    }
    
    /**
     * Generate detailed attendance data
     */
    private Map<String, Object> generateDetailedAttendanceData(String subjectCode, int classId, String semester, 
                                                           String academicYear, LocalDate fromDate, LocalDate toDate) throws SQLException {
        Map<String, Object> reportData = new HashMap<>();
        
        // Get subject and class details
        Subject subject = subjectDao.findByCode(subjectCode);
        com.attendance.models.Class cls = classDao.findById(classId);
        
        reportData.put("subject", subject);
        reportData.put("class", cls);
        
        // Get students enrolled in this class
        List<StudentEnrollment> enrollments = studentEnrollmentDao.findByClass(classId);
        
        // Get student details
        Map<Integer, User> students = new HashMap<>();
        for (StudentEnrollment enrollment : enrollments) {
            int studentId = enrollment.getStudentId();
            User student = userDao.findById(studentId);
            
            if (student != null && "Student".equals(student.getRole()) && "Active".equals(student.getStatus())) {
                students.put(studentId, student);
            }
        }
        
        // Get all attendance dates for the subject and class
        List<Date> attendanceDates = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder("SELECT DISTINCT attendance_date FROM Attendance WHERE subject_code = ? " +
                                           "AND semester = ? AND academic_year = ? ");
        
        if (fromDate != null) {
            sql.append("AND attendance_date >= ? ");
        }
        
        if (toDate != null) {
            sql.append("AND attendance_date <= ? ");
        }
        
        sql.append("ORDER BY attendance_date");
        
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            stmt.setString(paramIndex++, subjectCode);
            stmt.setString(paramIndex++, semester);
            stmt.setString(paramIndex++, academicYear);
            
            if (fromDate != null) {
                stmt.setDate(paramIndex++, DateUtils.toSqlDate(fromDate));
            }
            
            if (toDate != null) {
                stmt.setDate(paramIndex++, DateUtils.toSqlDate(toDate));
            }
            
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attendanceDates.add(rs.getDate("attendance_date"));
                }
            }
        }
        
        // Get attendance data for all students and dates
        Map<Integer, Map<String, String>> attendanceStatus = new HashMap<>();
        
        for (int studentId : students.keySet()) {
            Map<String, String> dateStatus = new HashMap<>();
            
            // Initialize with empty status for all dates
            for (Date date : attendanceDates) {
                dateStatus.put(date.toString(), "");
            }
            
            // Get actual attendance records
            List<Attendance> records;
            
            if (fromDate != null && toDate != null) {
                // Use a more specific query for date range
                String attendanceSql = "SELECT * FROM Attendance WHERE student_id = ? AND subject_code = ? " +
                                      "AND semester = ? AND academic_year = ? " +
                                      "AND attendance_date >= ? AND attendance_date <= ?";
                
                try (java.sql.Connection conn = DatabaseConnection.getConnection();
                     java.sql.PreparedStatement stmt = conn.prepareStatement(attendanceSql)) {
                    
                    stmt.setInt(1, studentId);
                    stmt.setString(2, subjectCode);
                    stmt.setString(3, semester);
                    stmt.setString(4, academicYear);
                    stmt.setDate(5, DateUtils.toSqlDate(fromDate));
                    stmt.setDate(6, DateUtils.toSqlDate(toDate));
                    
                    records = new ArrayList<>();
                    
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            Attendance attendance = new Attendance();
                            attendance.setAttendanceId(rs.getInt("attendance_id"));
                            attendance.setAttendanceDate(rs.getDate("attendance_date"));
                            attendance.setSubjectCode(rs.getString("subject_code"));
                            attendance.setStudentId(rs.getInt("student_id"));
                            attendance.setSemester(rs.getString("semester"));
                            attendance.setAcademicYear(rs.getString("academic_year"));
                            attendance.setStatus(rs.getString("status"));
                            
                            records.add(attendance);
                        }
                    }
                }
            } else {
                // Use the standard method for all records
                records = attendanceDao.findByStudentSubjectSemesterAndYear(
                        studentId, subjectCode, semester, academicYear);
            }
            
            // Fill in actual status
            for (Attendance record : records) {
                String dateKey = record.getAttendanceDate().toString();
                if (dateStatus.containsKey(dateKey)) {
                    dateStatus.put(dateKey, record.getStatus());
                }
            }
            
            attendanceStatus.put(studentId, dateStatus);
        }
        
        // Calculate attendance percentages
        Map<Integer, Double> attendancePercentages = new HashMap<>();
        
        for (int studentId : students.keySet()) {
            double percentage = attendanceDao.calculateAttendancePercentage(
                    studentId, subjectCode, semester, academicYear);
            attendancePercentages.put(studentId, percentage);
        }
        
        // Add detailed data to report
        reportData.put("students", students);
        reportData.put("attendanceDates", attendanceDates);
        reportData.put("attendanceStatus", attendanceStatus);
        reportData.put("attendancePercentages", attendancePercentages);
        
        return reportData;
    }
    
    /**
     * Generate attendance trend report
     */
    private void generateTrendReport(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
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
                Department department = departmentDao.findByHod(user.getUserId());
                if (department != null) {
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
            Map<String, Object> reportData = generateAttendanceTrendData(subjectCode, classId, semester, academicYear);
            
            // Add report parameters to request
            request.setAttribute("subjectCode", subjectCode);
            request.setAttribute("classId", classId);
            request.setAttribute("semester", semester);
            request.setAttribute("academicYear", academicYear);
            
            // Add report data to request
            request.setAttribute("reportData", reportData);
            
            // Forward to report view
            request.getRequestDispatcher("/WEB-INF/views/reports/attendance-trend.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid class ID");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while generating attendance trend report", e);
            response.sendRedirect(request.getContextPath() + "/reports/attendance?error=database");
        }
    }
    
    /**
     * Generate attendance trend data
     */
    private Map<String, Object> generateAttendanceTrendData(String subjectCode, int classId, String semester, String academicYear) throws SQLException {
        Map<String, Object> reportData = new HashMap<>();
        
        // Get subject and class details
        Subject subject = subjectDao.findByCode(subjectCode);
        com.attendance.models.Class cls = classDao.findById(classId);
        
        reportData.put("subject", subject);
        reportData.put("class", cls);
        
        // Get all attendance dates for the subject and class
        List<Date> attendanceDates = new ArrayList<>();
        
        String sql = "SELECT DISTINCT attendance_date FROM Attendance WHERE subject_code = ? " +
                   "AND semester = ? AND academic_year = ? ORDER BY attendance_date";
        
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, subjectCode);
            stmt.setString(2, semester);
            stmt.setString(3, academicYear);
            
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attendanceDates.add(rs.getDate("attendance_date"));
                }
            }
        }
        
        // Calculate attendance percentage for each date
        Map<String, Double> dailyAttendancePercentages = new HashMap<>();
        
        for (Date date : attendanceDates) {
            // Get attendance records for this date
            String dateSql = "SELECT COUNT(*) AS total, " +
                           "SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) AS present " +
                           "FROM Attendance WHERE subject_code = ? AND attendance_date = ? " +
                           "AND semester = ? AND academic_year = ?";
            
            try (java.sql.Connection conn = DatabaseConnection.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(dateSql)) {
                
                stmt.setString(1, subjectCode);
                stmt.setDate(2, date);
                stmt.setString(3, semester);
                stmt.setString(4, academicYear);
                
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int total = rs.getInt("total");
                        int present = rs.getInt("present");
                        
                        double percentage = total > 0 ? (double) present / total * 100 : 0;
                        dailyAttendancePercentages.put(date.toString(), percentage);
                    }
                }
            }
        }
        
        // Calculate weekly attendance trend
        Map<String, Double> weeklyAttendancePercentages = new HashMap<>();
        
        // Group dates by week
        Map<String, List<Date>> weeklyDates = new HashMap<>();
        
        for (Date date : attendanceDates) {
            LocalDate localDate = date.toLocalDate();
            LocalDate weekStart = localDate.with(DayOfWeek.MONDAY);
            String weekKey = weekStart.toString();
            
            if (!weeklyDates.containsKey(weekKey)) {
                weeklyDates.put(weekKey, new ArrayList<>());
            }
            
            weeklyDates.get(weekKey).add(date);
        }
        
        // Calculate attendance percentage for each week
        for (String weekKey : weeklyDates.keySet()) {
            List<Date> dates = weeklyDates.get(weekKey);
            
            int totalRecords = 0;
            int presentRecords = 0;
            
            for (Date date : dates) {
                String dateKey = date.toString();
                double dailyPercentage = dailyAttendancePercentages.getOrDefault(dateKey, 0.0);
                
                // Get number of records for this date
                String countSql = "SELECT COUNT(*) AS count FROM Attendance WHERE subject_code = ? " +
                               "AND attendance_date = ? AND semester = ? AND academic_year = ?";
                
                try (java.sql.Connection conn = DatabaseConnection.getConnection();
                     java.sql.PreparedStatement stmt = conn.prepareStatement(countSql)) {
                    
                    stmt.setString(1, subjectCode);
                    stmt.setDate(2, date);
                    stmt.setString(3, semester);
                    stmt.setString(4, academicYear);
                    
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            int count = rs.getInt("count");
                            totalRecords += count;
                            presentRecords += Math.round(count * dailyPercentage / 100);
                        }
                    }
                }
            }
            
            double weeklyPercentage = totalRecords > 0 ? (double) presentRecords / totalRecords * 100 : 0;
            weeklyAttendancePercentages.put(weekKey, weeklyPercentage);
        }
        
        // Calculate monthly attendance trend
        Map<String, Double> monthlyAttendancePercentages = new HashMap<>();
        
        // Group dates by month
        Map<String, List<Date>> monthlyDates = new HashMap<>();
        
        for (Date date : attendanceDates) {
            LocalDate localDate = date.toLocalDate();
            String monthKey = localDate.getYear() + "-" + String.format("%02d", localDate.getMonthValue());
            
            if (!monthlyDates.containsKey(monthKey)) {
                monthlyDates.put(monthKey, new ArrayList<>());
            }
            
            monthlyDates.get(monthKey).add(date);
        }
        
        // Calculate attendance percentage for each month
        for (String monthKey : monthlyDates.keySet()) {
            List<Date> dates = monthlyDates.get(monthKey);
            
            int totalRecords = 0;
            int presentRecords = 0;
            
            for (Date date : dates) {
                String dateKey = date.toString();
                double dailyPercentage = dailyAttendancePercentages.getOrDefault(dateKey, 0.0);
                
                // Get number of records for this date
                String countSql = "SELECT COUNT(*) AS count FROM Attendance WHERE subject_code = ? " +
                               "AND attendance_date = ? AND semester = ? AND academic_year = ?";
                
                try (java.sql.Connection conn = DatabaseConnection.getConnection();
                     java.sql.PreparedStatement stmt = conn.prepareStatement(countSql)) {
                    
                    stmt.setString(1, subjectCode);
                    stmt.setDate(2, date);
                    stmt.setString(3, semester);
                    stmt.setString(4, academicYear);
                    
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            int count = rs.getInt("count");
                            totalRecords += count;
                            presentRecords += Math.round(count * dailyPercentage / 100);
                        }
                    }
                }
            }
            
            double monthlyPercentage = totalRecords > 0 ? (double) presentRecords / totalRecords * 100 : 0;
            monthlyAttendancePercentages.put(monthKey, monthlyPercentage);
        }
        
        // Add trend data to report
        reportData.put("attendanceDates", attendanceDates);
        reportData.put("dailyAttendancePercentages", dailyAttendancePercentages);
        reportData.put("weeklyAttendancePercentages", weeklyAttendancePercentages);
        reportData.put("monthlyAttendancePercentages", monthlyAttendancePercentages);
        
        return reportData;
    }
    
    /**
     * Send attendance report notification
     */
    private void sendReportNotification(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        // Get notification parameters
        String subjectCode = request.getParameter("subjectCode");
        String classIdStr = request.getParameter("classId");
        String semester = request.getParameter("semester");
        String academicYear = request.getParameter("academicYear");
        String reportType = request.getParameter("reportType");
        String reportUrl = request.getParameter("reportUrl");
        String recipientType = request.getParameter("recipientType"); // "students" or "teacher"
        
        if (subjectCode == null || classIdStr == null || semester == null || 
            academicYear == null || reportType == null || reportUrl == null || recipientType == null) {
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
                Department department = departmentDao.findByHod(user.getUserId());
                if (department != null) {
                    com.attendance.models.Class cls = classDao.findById(classId);
                    hasPermission = (cls != null && cls.getDepartmentId() == department.getDepartmentId());
                }
            } else if ("Principal".equals(user.getRole()) || "Admin".equals(user.getRole())) {
                // Principal and admin have permission to all
                hasPermission = true;
            }
            
            if (!hasPermission) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have permission to send this notification");
                return;
            }
            
            // Get subject and class details
            Subject subject = subjectDao.findByCode(subjectCode);
            com.attendance.models.Class cls = classDao.findById(classId);
            
            if (subject == null || cls == null) {
                response.sendRedirect(request.getContextPath() + "/reports/attendance?error=invalidData");
                return;
            }
            
            boolean success = false;
            int notificationCount = 0;
            
            if ("students".equals(recipientType)) {
                // Send notification to all students in the class
                List<StudentEnrollment> enrollments = studentEnrollmentDao.findByClass(classId);
                
                for (StudentEnrollment enrollment : enrollments) {
                    int studentId = enrollment.getStudentId();
                    User student = userDao.findById(studentId);
                    
                    if (student != null && "Student".equals(student.getRole()) && "Active".equals(student.getStatus())) {
                        // Calculate attendance percentage
                        double percentage = attendanceDao.calculateAttendancePercentage(
                                studentId, subjectCode, semester, academicYear);
                        
                        // Get attendance records
                        List<Attendance> records = attendanceDao.findByStudentSubjectSemesterAndYear(
                                studentId, subjectCode, semester, academicYear);
                        
                        int totalDays = records.size();
                        int presentDays = 0;
                        
                        for (Attendance record : records) {
                            if ("Present".equals(record.getStatus())) {
                                presentDays++;
                            }
                        }
                        
                        // Send attendance summary notification
                        boolean sent = EmailNotificationService.sendAttendanceSummaryNotification(
                                student, subject.getSubjectName(), semester, academicYear, presentDays, totalDays, percentage);
                        
                        if (sent) {
                            notificationCount++;
                        }
                    }
                }
                
                success = notificationCount > 0;
                
            } else if ("teacher".equals(recipientType)) {
                // Send notification to the teacher
                List<TeacherAssignment> assignments = teacherAssignmentDao.findByClassAndSubject(classId, subjectCode);
                
                for (TeacherAssignment assignment : assignments) {
                    int teacherId = assignment.getTeacherId();
                    User teacher = userDao.findById(teacherId);
                    
                    if (teacher != null && "Teacher".equals(teacher.getRole()) && "Active".equals(teacher.getStatus())) {
                        // Send attendance report notification
                        boolean sent = EmailNotificationService.sendAttendanceReportNotification(
                                teacher, subject.getSubjectName(), cls.getClassName(), semester, academicYear, reportType, reportUrl);
                        
                        if (sent) {
                            notificationCount++;
                        }
                    }
                }
                
                success = notificationCount > 0;
            }
            
            // Redirect with appropriate success/error message
            if (success) {
                response.sendRedirect(request.getContextPath() + "/reports/attendance?success=notificationSent&count=" + notificationCount);
            } else {
                response.sendRedirect(request.getContextPath() + "/reports/attendance?error=notificationFailed");
            }
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid class ID");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while sending report notification", e);
            response.sendRedirect(request.getContextPath() + "/reports/attendance?error=database");
        }
    }
    
    /**
     * Download attendance report
     */
    private void downloadReport(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        // Get download parameters
        String subjectCode = request.getParameter("subjectCode");
        String classIdStr = request.getParameter("classId");
        String semester = request.getParameter("semester");
        String academicYear = request.getParameter("academicYear");
        String format = request.getParameter("format"); // "csv" or "pdf"
        
        if (subjectCode == null || classIdStr == null || semester == null || academicYear == null || format == null) {
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
                Department department = departmentDao.findByHod(user.getUserId());
                if (department != null) {
                    com.attendance.models.Class cls = classDao.findById(classId);
                    hasPermission = (cls != null && cls.getDepartmentId() == department.getDepartmentId());
                }
            } else if ("Principal".equals(user.getRole()) || "Admin".equals(user.getRole())) {
                // Principal and admin have permission to all
                hasPermission = true;
            }
            
            if (!hasPermission) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have permission to download this report");
                return;
            }
            
            // Get subject and class details
            Subject subject = subjectDao.findByCode(subjectCode);
            com.attendance.models.Class cls = classDao.findById(classId);
            
            if (subject == null || cls == null) {
                response.sendRedirect(request.getContextPath() + "/reports/attendance?error=invalidData");
                return;
            }
            
            if ("csv".equals(format)) {
                // Generate CSV report
                generateCsvReport(response, subjectCode, classId, semester, academicYear, subject, cls);
            } else if ("pdf".equals(format)) {
                // PDF generation would typically be implemented using a library like iText or PDFBox
                // For simplicity, we'll just show an error message here
                response.sendRedirect(request.getContextPath() + "/reports/attendance?error=pdfNotImplemented");
            } else {
                response.sendRedirect(request.getContextPath() + "/reports/attendance?error=invalidFormat");
            }
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid class ID");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while downloading report", e);
            response.sendRedirect(request.getContextPath() + "/reports/attendance?error=database");
        }
    }
    
    /**
     * Generate CSV report
     */
    private void generateCsvReport(HttpServletResponse response, String subjectCode, int classId, 
                                 String semester, String academicYear, Subject subject, 
                                 com.attendance.models.Class cls) throws IOException, SQLException {
        // Set response headers for CSV download
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"attendance_report_" + 
                         subjectCode + "_" + classId + "_" + semester + ".csv\"");
        
        // Get students enrolled in this class
        List<StudentEnrollment> enrollments = studentEnrollmentDao.findByClass(classId);
        
        // Get student details
        Map<Integer, User> students = new HashMap<>();
        for (StudentEnrollment enrollment : enrollments) {
            int studentId = enrollment.getStudentId();
            User student = userDao.findById(studentId);
            
            if (student != null && "Student".equals(student.getRole()) && "Active".equals(student.getStatus())) {
                students.put(studentId, student);
            }
        }
        
        // Get all attendance dates for the subject and class
        List<Date> attendanceDates = new ArrayList<>();
        
        String sql = "SELECT DISTINCT attendance_date FROM Attendance WHERE subject_code = ? " +
                   "AND semester = ? AND academic_year = ? ORDER BY attendance_date";
        
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, subjectCode);
            stmt.setString(2, semester);
            stmt.setString(3, academicYear);
            
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attendanceDates.add(rs.getDate("attendance_date"));
                }
            }
        }
        
        // Get attendance data for all students and dates
        Map<Integer, Map<String, String>> attendanceStatus = new HashMap<>();
        
        for (int studentId : students.keySet()) {
            Map<String, String> dateStatus = new HashMap<>();
            
            // Initialize with empty status for all dates
            for (Date date : attendanceDates) {
                dateStatus.put(date.toString(), "");
            }
            
            // Get actual attendance records
            List<Attendance> records = attendanceDao.findByStudentSubjectSemesterAndYear(
                    studentId, subjectCode, semester, academicYear);
            
            // Fill in actual status
            for (Attendance record : records) {
                String dateKey = record.getAttendanceDate().toString();
                if (dateStatus.containsKey(dateKey)) {
                    dateStatus.put(dateKey, record.getStatus());
                }
            }
            
            attendanceStatus.put(studentId, dateStatus);
        }
        
        // Calculate attendance percentages
        Map<Integer, Double> attendancePercentages = new HashMap<>();
        
        for (int studentId : students.keySet()) {
            double percentage = attendanceDao.calculateAttendancePercentage(
                    studentId, subjectCode, semester, academicYear);
            attendancePercentages.put(studentId, percentage);
        }
        
        // Write CSV content
        StringBuilder csv = new StringBuilder();
        
        // Add header row with report information
        csv.append("Attendance Report\n");
        csv.append("Subject:,").append(escapeCSV(subject.getSubjectName())).append(" (").append(subjectCode).append(")\n");
        csv.append("Class:,").append(escapeCSV(cls.getClassName())).append("\n");
        csv.append("Semester:,").append(semester).append("\n");
        csv.append("Academic Year:,").append(academicYear).append("\n\n");
        
        // Add student data header row
        csv.append("Roll Number,Student Name");
        
        for (Date date : attendanceDates) {
            csv.append(",").append(date);
        }
        
        csv.append(",Present,Absent,Leave,Percentage\n");
        
        // Add student data rows
        for (Map.Entry<Integer, User> entry : students.entrySet()) {
            int studentId = entry.getKey();
            User student = entry.getValue();
            
            csv.append(studentId).append(",").append(escapeCSV(student.getFullName()));
            
            // Add attendance status for each date
            Map<String, String> dateStatus = attendanceStatus.get(studentId);
            
            int presentCount = 0;
            int absentCount = 0;
            int leaveCount = 0;
            
            for (Date date : attendanceDates) {
                String status = dateStatus.get(date.toString());
                csv.append(",").append(status);
                
                if ("Present".equals(status)) {
                    presentCount++;
                } else if ("Absent".equals(status)) {
                    absentCount++;
                } else if ("Leave".equals(status)) {
                    leaveCount++;
                }
            }
            
            // Add attendance statistics
            csv.append(",").append(presentCount);
            csv.append(",").append(absentCount);
            csv.append(",").append(leaveCount);
            csv.append(",").append(String.format("%.2f%%", attendancePercentages.getOrDefault(studentId, 0.0)));
            csv.append("\n");
        }
        
        // Write CSV content to response
        response.getWriter().write(csv.toString());
    }
    
    /**
     * Show reports dashboard for teachers
     */
    private void showTeacherReportsDashboard(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get teacher assignments
            List<TeacherAssignment> assignments = teacherAssignmentDao.findByTeacher(user.getUserId());
            
            // Collect subjects and classes
            Map<String, Subject> subjects = new HashMap<>();
            Map<Integer, com.attendance.models.Class> classes = new HashMap<>();
            
            for (TeacherAssignment assignment : assignments) {
                String subjectCode = assignment.getSubjectCode();
                int classId = assignment.getClassId();
                
                Subject subject = subjectDao.findByCode(subjectCode);
                if (subject != null) {
                    subjects.put(subjectCode, subject);
                }
                
                com.attendance.models.Class cls = classDao.findById(classId);
                if (cls != null) {
                    classes.put(classId, cls);
                }
            }
            
            // Get recent reports (placeholder implementation)
            List<Map<String, Object>> recentReports = getRecentReports(user.getUserId(), "Teacher");
            
            request.setAttribute("assignments", assignments);
            request.setAttribute("subjects", subjects);
            request.setAttribute("classes", classes);
            request.setAttribute("recentReports", recentReports);
            
            request.getRequestDispatcher("/WEB-INF/views/teacher/reports/dashboard.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while preparing teacher reports dashboard", e);
            request.setAttribute("error", "Failed to load reports dashboard. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/teacher/reports/dashboard.jsp").forward(request, response);
        }
    }
    
    /**
     * Show report generation form for teachers
     */
    private void showTeacherReportForm(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get teacher assignments
            List<TeacherAssignment> assignments = teacherAssignmentDao.findByTeacher(user.getUserId());
            
            // Collect subjects and classes
            Map<String, Subject> subjects = new HashMap<>();
            Map<Integer, com.attendance.models.Class> classes = new HashMap<>();
            
            for (TeacherAssignment assignment : assignments) {
                String subjectCode = assignment.getSubjectCode();
                int classId = assignment.getClassId();
                
                Subject subject = subjectDao.findByCode(subjectCode);
                if (subject != null) {
                    subjects.put(subjectCode, subject);
                }
                
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
            
            request.setAttribute("assignments", assignments);
            request.setAttribute("subjects", subjects);
            request.setAttribute("classes", classes);
            request.setAttribute("academicYears", academicYears);
            request.setAttribute("semesters", semesters);
            
            request.getRequestDispatcher("/WEB-INF/views/teacher/reports/generate.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while preparing teacher report form", e);
            request.setAttribute("error", "Failed to load report form. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/teacher/reports/generate.jsp").forward(request, response);
        }
    }
    
    /**
     * View a generated report for teacher
     */
    private void viewTeacherReport(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        String reportId = request.getParameter("id");
        String reportType = request.getParameter("type");
        
        if (reportId == null || reportType == null) {
            response.sendRedirect(request.getContextPath() + "/teacher/reports?error=missingParams");
            return;
        }
        
        // In a real implementation, you would retrieve the saved report from a database
        // For this example, we'll use request parameters to regenerate the report
        
        String subjectCode = request.getParameter("subjectCode");
        String classIdStr = request.getParameter("classId");
        String semester = request.getParameter("semester");
        String academicYear = request.getParameter("academicYear");
        
        if (subjectCode == null || classIdStr == null || semester == null || academicYear == null) {
            response.sendRedirect(request.getContextPath() + "/teacher/reports?error=missingReportParams");
            return;
        }
        
        // Redirect to the appropriate report type
        switch (reportType) {
            case "summary":
                response.sendRedirect(request.getContextPath() + "/reports/attendance/summary?" +
                                   "subjectCode=" + subjectCode + "&classId=" + classIdStr +
                                   "&semester=" + semester + "&academicYear=" + academicYear);
                break;
            case "detailed":
                response.sendRedirect(request.getContextPath() + "/reports/attendance/detailed?" +
                                   "subjectCode=" + subjectCode + "&classId=" + classIdStr +
                                   "&semester=" + semester + "&academicYear=" + academicYear);
                break;
            case "trend":
                response.sendRedirect(request.getContextPath() + "/reports/attendance/trend?" +
                                   "subjectCode=" + subjectCode + "&classId=" + classIdStr +
                                   "&semester=" + semester + "&academicYear=" + academicYear);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/teacher/reports?error=invalidReportType");
                break;
        }
    }
    
    /**
     * Generate one-click attendance summary
     */
    private void generateOneClickSummary(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get teacher's active assignments
            List<TeacherAssignment> assignments = teacherAssignmentDao.findByTeacher(user.getUserId());
            
            if (assignments.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/teacher/reports?error=noAssignments");
                return;
            }
            
            // Just use the first assignment for demonstration purposes
            TeacherAssignment assignment = assignments.get(0);
            String subjectCode = assignment.getSubjectCode();
            int classId = assignment.getClassId();
            
            // Get current semester and academic year
            String academicYear = DateUtils.getCurrentAcademicYear();
            
            // Get class details
            com.attendance.models.Class cls = classDao.findById(classId);
            if (cls == null) {
                response.sendRedirect(request.getContextPath() + "/teacher/reports?error=invalidClass");
                return;
            }
            
            String semester = String.valueOf(DateUtils.getCurrentSemester(cls.getClassName()));
            
            // Redirect to the summary report
            response.sendRedirect(request.getContextPath() + "/reports/attendance/summary?" +
                               "subjectCode=" + subjectCode + "&classId=" + classId +
                               "&semester=" + semester + "&academicYear=" + academicYear);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while generating one-click summary", e);
            response.sendRedirect(request.getContextPath() + "/teacher/reports?error=database");
        }
    }
    
    /**
     * Process teacher report generation form
     */
    private void processTeacherReportGeneration(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        // Get report parameters
        String subjectCode = request.getParameter("subjectCode");
        String classIdStr = request.getParameter("classId");
        String semester = request.getParameter("semester");
        String academicYear = request.getParameter("academicYear");
        String reportType = request.getParameter("reportType");
        String fromDateStr = request.getParameter("fromDate");
        String toDateStr = request.getParameter("toDate");
        
        if (subjectCode == null || classIdStr == null || semester == null || 
            academicYear == null || reportType == null) {
            response.sendRedirect(request.getContextPath() + "/teacher/reports/generate?error=missingParams");
            return;
        }
        
        try {
            int classId = Integer.parseInt(classIdStr);
            
            // Check if teacher is assigned to this subject and class
            TeacherAssignment assignment = teacherAssignmentDao.findByTeacherSubjectAndClass(
                    user.getUserId(), subjectCode, classId);
            
            if (assignment == null) {
                response.sendRedirect(request.getContextPath() + "/teacher/reports/generate?error=notAssigned");
                return;
            }
            
            // Redirect to the appropriate report type
            StringBuilder redirectUrl = new StringBuilder(request.getContextPath());
            
            switch (reportType) {
                case "summary":
                    redirectUrl.append("/reports/attendance/summary?");
                    break;
                case "detailed":
                    redirectUrl.append("/reports/attendance/detailed?");
                    break;
                case "trend":
                    redirectUrl.append("/reports/attendance/trend?");
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/teacher/reports/generate?error=invalidReportType");
                    return;
            }
            
            redirectUrl.append("subjectCode=").append(subjectCode)
                     .append("&classId=").append(classId)
                     .append("&semester=").append(semester)
                     .append("&academicYear=").append(academicYear);
            
            if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
                redirectUrl.append("&fromDate=").append(fromDateStr);
            }
            
            if (toDateStr != null && !toDateStr.trim().isEmpty()) {
                redirectUrl.append("&toDate=").append(toDateStr);
            }
            
            response.sendRedirect(redirectUrl.toString());
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid class ID");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while processing teacher report generation", e);
            response.sendRedirect(request.getContextPath() + "/teacher/reports/generate?error=database");
        }
    }
    
    /**
     * Process notification to students
     */
    private void processNotifyStudents(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        // Get notification parameters
        String subjectCode = request.getParameter("subjectCode");
        String classIdStr = request.getParameter("classId");
        String semester = request.getParameter("semester");
        String academicYear = request.getParameter("academicYear");
        String notificationType = request.getParameter("notificationType"); // "all" or "below-threshold"
        String thresholdStr = request.getParameter("threshold");
        
        if (subjectCode == null || classIdStr == null || semester == null || 
            academicYear == null || notificationType == null) {
            response.sendRedirect(request.getContextPath() + "/teacher/reports?error=missingParams");
            return;
        }
        
        try {
            int classId = Integer.parseInt(classIdStr);
            
            // Check if teacher is assigned to this subject and class
            TeacherAssignment assignment = teacherAssignmentDao.findByTeacherSubjectAndClass(
                    user.getUserId(), subjectCode, classId);
            
            if (assignment == null) {
                response.sendRedirect(request.getContextPath() + "/teacher/reports?error=notAssigned");
                return;
            }
            
            // Get subject and class details
            Subject subject = subjectDao.findByCode(subjectCode);
            com.attendance.models.Class cls = classDao.findById(classId);
            
            if (subject == null || cls == null) {
                response.sendRedirect(request.getContextPath() + "/teacher/reports?error=invalidData");
                return;
            }
            
            // Get students enrolled in this class
            List<StudentEnrollment> enrollments = studentEnrollmentDao.findByClass(classId);
            
            // For below-threshold notifications, parse the threshold
            double threshold = 75.0; // Default threshold
            if ("below-threshold".equals(notificationType) && thresholdStr != null && !thresholdStr.trim().isEmpty()) {
                try {
                    threshold = Double.parseDouble(thresholdStr);
                } catch (NumberFormatException e) {
                    // Ignore and use default threshold
                }
            }
            
            int notificationCount = 0;
            
            for (StudentEnrollment enrollment : enrollments) {
                int studentId = enrollment.getStudentId();
                User student = userDao.findById(studentId);
                
                if (student != null && "Student".equals(student.getRole()) && "Active".equals(student.getStatus())) {
                    // Calculate attendance percentage
                    double percentage = attendanceDao.calculateAttendancePercentage(
                            studentId, subjectCode, semester, academicYear);
                    
                    boolean shouldNotify = "all".equals(notificationType) || 
                                         ("below-threshold".equals(notificationType) && percentage < threshold);
                    
                    if (shouldNotify) {
                        // Get attendance records for calculating present/total days
                        List<Attendance> records = attendanceDao.findByStudentSubjectSemesterAndYear(
                                studentId, subjectCode, semester, academicYear);
                        
                        int totalDays = records.size();
                        int presentDays = 0;
                        
                        for (Attendance record : records) {
                            if ("Present".equals(record.getStatus())) {
                                presentDays++;
                            }
                        }
                        
                        boolean sent;
                        
                        if ("below-threshold".equals(notificationType) && percentage < threshold) {
                            // Send low attendance warning
                            sent = EmailNotificationService.sendLowAttendanceWarning(
                                    student, subject.getSubjectName(), semester, academicYear, percentage, threshold);
                        } else {
                            // Send regular attendance summary
                            sent = EmailNotificationService.sendAttendanceSummaryNotification(
                                    student, subject.getSubjectName(), semester, academicYear, presentDays, totalDays, percentage);
                        }
                        
                        if (sent) {
                            notificationCount++;
                        }
                    }
                }
            }
            
            // Redirect with appropriate success/error message
            if (notificationCount > 0) {
                response.sendRedirect(request.getContextPath() + "/teacher/reports?success=notificationSent&count=" + notificationCount);
            } else {
                response.sendRedirect(request.getContextPath() + "/teacher/reports?error=noNotificationsSent");
            }
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid class ID or threshold");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while sending notifications to students", e);
            response.sendRedirect(request.getContextPath() + "/teacher/reports?error=database");
        }
    }
    
    /**
     * Show reports dashboard for HODs
     */
    private void showHodReportsDashboard(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get HOD's department
            List<Department> departments = departmentDao.findByHod(user.getUserId());
            
            if (departments == null || departments.isEmpty()) {
                request.setAttribute("error", "You are not assigned to any department");
                request.getRequestDispatcher("/WEB-INF/views/hod/reports/dashboard.jsp").forward(request, response);
                return;
            }
            
            // Use the first department
            Department department = departments.get(0);
            
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
            
            // Get recent reports (placeholder implementation)
            List<Map<String, Object>> recentReports = getRecentReports(user.getUserId(), "HOD");
            
            request.setAttribute("department", department);
            request.setAttribute("classes", classes);
            request.setAttribute("subjects", subjects);
            request.setAttribute("recentReports", recentReports);
            
            request.getRequestDispatcher("/WEB-INF/views/hod/reports/dashboard.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while preparing HOD reports dashboard", e);
            request.setAttribute("error", "Failed to load reports dashboard. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/hod/reports/dashboard.jsp").forward(request, response);
        }
    }
    
    /**
     * Show report generation form for HODs
     */
    private void showHodReportForm(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get HOD's department
            Department department = departmentDao.findByHod(user.getUserId());
            
            if (department == null) {
                request.setAttribute("error", "You are not assigned to any department");
                request.getRequestDispatcher("/WEB-INF/views/hod/reports/generate.jsp").forward(request, response);
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
            
            request.setAttribute("department", department);
            request.setAttribute("classes", classes);
            request.setAttribute("subjects", subjects);
            request.setAttribute("academicYears", academicYears);
            request.setAttribute("semesters", semesters);
            
            request.getRequestDispatcher("/WEB-INF/views/hod/reports/generate.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while preparing HOD report form", e);
            request.setAttribute("error", "Failed to load report form. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/hod/reports/generate.jsp").forward(request, response);
        }
    }
    
    /**
     * View a generated report for HOD
     */
    private void viewHodReport(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        // Similar implementation to viewTeacherReport, but with HOD-specific permissions
        String reportId = request.getParameter("id");
        String reportType = request.getParameter("type");
        
        if (reportId == null || reportType == null) {
            response.sendRedirect(request.getContextPath() + "/hod/reports?error=missingParams");
            return;
        }
        
        // In a real implementation, you would retrieve the saved report from a database
        // For this example, we'll use request parameters to regenerate the report
        
        String subjectCode = request.getParameter("subjectCode");
        String classIdStr = request.getParameter("classId");
        String semester = request.getParameter("semester");
        String academicYear = request.getParameter("academicYear");
        
        if (subjectCode == null || classIdStr == null || semester == null || academicYear == null) {
            response.sendRedirect(request.getContextPath() + "/hod/reports?error=missingReportParams");
            return;
        }
        
        // Redirect to the appropriate report type
        switch (reportType) {
            case "summary":
                response.sendRedirect(request.getContextPath() + "/reports/attendance/summary?" +
                                   "subjectCode=" + subjectCode + "&classId=" + classIdStr +
                                   "&semester=" + semester + "&academicYear=" + academicYear);
                break;
            case "detailed":
                response.sendRedirect(request.getContextPath() + "/reports/attendance/detailed?" +
                                   "subjectCode=" + subjectCode + "&classId=" + classIdStr +
                                   "&semester=" + semester + "&academicYear=" + academicYear);
                break;
            case "trend":
                response.sendRedirect(request.getContextPath() + "/reports/attendance/trend?" +
                                   "subjectCode=" + subjectCode + "&classId=" + classIdStr +
                                   "&semester=" + semester + "&academicYear=" + academicYear);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/hod/reports?error=invalidReportType");
                break;
        }
    }
    
    /**
     * View department-wide analytics for HOD
     */
    private void viewDepartmentAnalytics(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get HOD's department
            Department department = departmentDao.findByHod(user.getUserId());
            
            if (department == null) {
                request.setAttribute("error", "You are not assigned to any department");
                request.getRequestDispatcher("/WEB-INF/views/hod/reports/department-analytics.jsp").forward(request, response);
                return;
            }
            
            // Get current academic year and semester
            String academicYear = DateUtils.getCurrentAcademicYear();
            
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
            
            // Calculate department-wide statistics
            Map<String, Object> departmentStats = calculateDepartmentStatistics(department, classes, departmentSubjects, academicYear);
            
            request.setAttribute("department", department);
            request.setAttribute("classes", classes);
            request.setAttribute("subjects", subjects);
            request.setAttribute("academicYear", academicYear);
            request.setAttribute("departmentStats", departmentStats);
            
            request.getRequestDispatcher("/WEB-INF/views/hod/reports/department-analytics.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while generating department analytics", e);
            request.setAttribute("error", "Failed to generate department analytics. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/hod/reports/department-analytics.jsp").forward(request, response);
        }
    }
    
    /**
     * Calculate department-wide statistics
     */
    private Map<String, Object> calculateDepartmentStatistics(Department department, List<com.attendance.models.Class> classes,
                                                           List<DepartmentSubject> departmentSubjects, String academicYear) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        int totalStudents = 0;
        int totalTeachers = 0;
        int totalSubjects = departmentSubjects.size();
        
        double overallAttendance = 0;
        int attendanceRecordCount = 0;
        
        // Calculate class-wise statistics
        Map<Integer, Map<String, Object>> classStats = new HashMap<>();
        
        for (com.attendance.models.Class cls : classes) {
            int classId = cls.getClassId();
            
            // Get students in this class
            List<StudentEnrollment> enrollments = studentEnrollmentDao.findByClass(classId);
            
            // Count active students
            int classStudents = 0;
            for (StudentEnrollment enrollment : enrollments) {
                User student = userDao.findById(enrollment.getStudentId());
                if (student != null && "Student".equals(student.getRole()) && "Active".equals(student.getStatus())) {
                    classStudents++;
                }
            }
            
            totalStudents += classStudents;
            
            // Get subjects for this class
            List<String> classSubjects = new ArrayList<>();
            for (DepartmentSubject ds : departmentSubjects) {
                // For simplicity, we're assuming all department subjects are taught in all classes
                // In a real implementation, you would filter by semester/class
                classSubjects.add(ds.getSubjectCode());
            }
            
            // Get teachers assigned to this class
            Set<Integer> classTeachers = new HashSet<>();
            for (String subjectCode : classSubjects) {
                List<TeacherAssignment> assignments = teacherAssignmentDao.findByClassAndSubject(classId, subjectCode);
                for (TeacherAssignment assignment : assignments) {
                    classTeachers.add(assignment.getTeacherId());
                }
            }
            
            totalTeachers += classTeachers.size();
            
            // Calculate average attendance for this class
            double classAttendance = 0;
            int classAttendanceCount = 0;
            
            for (String subjectCode : classSubjects) {
                for (StudentEnrollment enrollment : enrollments) {
                    int studentId = enrollment.getStudentId();
                    User student = userDao.findById(studentId);
                    
                    if (student != null && "Student".equals(student.getRole()) && "Active".equals(student.getStatus())) {
                        // Calculate attendance percentage for each subject-student pair
                        for (String semester : new String[]{"1", "2", "3", "4", "5", "6"}) {
                            double percentage = attendanceDao.calculateAttendancePercentage(
                                    studentId, subjectCode, semester, academicYear);
                            
                            if (percentage > 0) {
                                classAttendance += percentage;
                                classAttendanceCount++;
                                
                                overallAttendance += percentage;
                                attendanceRecordCount++;
                            }
                        }
                    }
                }
            }
            
            double averageAttendance = classAttendanceCount > 0 ? classAttendance / classAttendanceCount : 0;
            
            Map<String, Object> classStat = new HashMap<>();
            classStat.put("studentCount", classStudents);
            classStat.put("teacherCount", classTeachers.size());
            classStat.put("subjectCount", classSubjects.size());
            classStat.put("averageAttendance", averageAttendance);
            
            classStats.put(classId, classStat);
        }
        
        // Calculate subject-wise statistics
        Map<String, Map<String, Object>> subjectStats = new HashMap<>();
        
        for (DepartmentSubject ds : departmentSubjects) {
            String subjectCode = ds.getSubjectCode();
            
            // Calculate average attendance for this subject across all classes
            double subjectAttendance = 0;
            int subjectAttendanceCount = 0;
            
            // Count teachers assigned to this subject
            Set<Integer> subjectTeachers = new HashSet<>();
            
            for (com.attendance.models.Class cls : classes) {
                int classId = cls.getClassId();
                
                List<TeacherAssignment> assignments = teacherAssignmentDao.findByClassAndSubject(classId, subjectCode);
                for (TeacherAssignment assignment : assignments) {
                    subjectTeachers.add(assignment.getTeacherId());
                }
                
                // Get students in this class
                List<StudentEnrollment> enrollments = studentEnrollmentDao.findByClass(classId);
                
                for (StudentEnrollment enrollment : enrollments) {
                    int studentId = enrollment.getStudentId();
                    User student = userDao.findById(studentId);
                    
                    if (student != null && "Student".equals(student.getRole()) && "Active".equals(student.getStatus())) {
                        // Calculate attendance percentage for each class-student pair
                        for (String semester : new String[]{"1", "2", "3", "4", "5", "6"}) {
                            double percentage = attendanceDao.calculateAttendancePercentage(
                                    studentId, subjectCode, semester, academicYear);
                            
                            if (percentage > 0) {
                                subjectAttendance += percentage;
                                subjectAttendanceCount++;
                            }
                        }
                    }
                }
            }
            
            double averageAttendance = subjectAttendanceCount > 0 ? subjectAttendance / subjectAttendanceCount : 0;
            
            Map<String, Object> subjectStat = new HashMap<>();
            subjectStat.put("teacherCount", subjectTeachers.size());
            subjectStat.put("averageAttendance", averageAttendance);
            
            subjectStats.put(subjectCode, subjectStat);
        }
        
        // Calculate overall department statistics
        double departmentAverageAttendance = attendanceRecordCount > 0 ? overallAttendance / attendanceRecordCount : 0;
        
        stats.put("totalStudents", totalStudents);
        stats.put("totalTeachers", totalTeachers);
        stats.put("totalSubjects", totalSubjects);
        stats.put("departmentAverageAttendance", departmentAverageAttendance);
        stats.put("classStats", classStats);
        stats.put("subjectStats", subjectStats);
        
        return stats;
    }
    
    /**
     * Get recent reports (placeholder implementation)
     */
    private List<Map<String, Object>> getRecentReports(int userId, String role) {
        // In a real implementation, you would retrieve recent reports from a database
        // For this example, we'll return a static list
        
        List<Map<String, Object>> recentReports = new ArrayList<>();
        
        // Generate some placeholder data
        Map<String, Object> report1 = new HashMap<>();
        report1.put("id", "1");
        report1.put("title", "Monthly Attendance Summary");
        report1.put("type", "summary");
        report1.put("date", new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L)); // 1 week ago
        
        Map<String, Object> report2 = new HashMap<>();
        report2.put("id", "2");
        report2.put("title", "Detailed Attendance Report");
        report2.put("type", "detailed");
        report2.put("date", new Date(System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000L)); // 2 weeks ago
        
        Map<String, Object> report3 = new HashMap<>();
        report3.put("id", "3");
        report3.put("title", "Attendance Trend Analysis");
        report3.put("type", "trend");
        report3.put("date", new Date(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L)); // 1 month ago
        
        recentReports.add(report1);
        recentReports.add(report2);
        recentReports.add(report3);
        
        return recentReports;
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
    
    // Additional methods for principal reports (similar to HOD methods, but with institution-wide scope)
    
    /**
     * Show reports dashboard for principal
     */
    private void showPrincipalReportsDashboard(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get all departments
            List<Department> departments = departmentDao.findAll();
            
            // Get all classes
            List<com.attendance.models.Class> classes = classDao.findAll();
            
            // Get all subjects
            List<Subject> subjects = subjectDao.findAll();
            
            // Get recent reports (placeholder implementation)
            List<Map<String, Object>> recentReports = getRecentReports(user.getUserId(), "Principal");
            
            request.setAttribute("departments", departments);
            request.setAttribute("classes", classes);
            request.setAttribute("subjects", subjects);
            request.setAttribute("recentReports", recentReports);
            
            request.getRequestDispatcher("/WEB-INF/views/principal/reports/dashboard.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while preparing principal reports dashboard", e);
            request.setAttribute("error", "Failed to load reports dashboard. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/principal/reports/dashboard.jsp").forward(request, response);
        }
    }
    
    /**
     * Show report generation form for principal
     */
    private void showPrincipalReportForm(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get all departments
            List<Department> departments = departmentDao.findAll();
            
            // Get all classes
            List<com.attendance.models.Class> classes = classDao.findAll();
            
            // Get all subjects
            List<Subject> subjects = subjectDao.findAll();
            
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
            
            request.setAttribute("departments", departments);
            request.setAttribute("classes", classes);
            request.setAttribute("subjects", subjects);
            request.setAttribute("academicYears", academicYears);
            request.setAttribute("semesters", semesters);
            
            request.getRequestDispatcher("/WEB-INF/views/principal/reports/generate.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while preparing principal report form", e);
            request.setAttribute("error", "Failed to load report form. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/principal/reports/generate.jsp").forward(request, response);
        }
    }
    
    /**
     * View a generated report for principal
     */
    private void viewPrincipalReport(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        // Similar implementation to viewTeacherReport, but with principal-specific permissions
        String reportId = request.getParameter("id");
        String reportType = request.getParameter("type");
        
        if (reportId == null || reportType == null) {
            response.sendRedirect(request.getContextPath() + "/principal/reports?error=missingParams");
            return;
        }
        
        // In a real implementation, you would retrieve the saved report from a database
        // For this example, we'll use request parameters to regenerate the report
        
        String subjectCode = request.getParameter("subjectCode");
        String classIdStr = request.getParameter("classId");
        String semester = request.getParameter("semester");
        String academicYear = request.getParameter("academicYear");
        
        if (subjectCode == null || classIdStr == null || semester == null || academicYear == null) {
            response.sendRedirect(request.getContextPath() + "/principal/reports?error=missingReportParams");
            return;
        }
        
        // Redirect to the appropriate report type
        switch (reportType) {
            case "summary":
                response.sendRedirect(request.getContextPath() + "/reports/attendance/summary?" +
                                   "subjectCode=" + subjectCode + "&classId=" + classIdStr +
                                   "&semester=" + semester + "&academicYear=" + academicYear);
                break;
            case "detailed":
                response.sendRedirect(request.getContextPath() + "/reports/attendance/detailed?" +
                                   "subjectCode=" + subjectCode + "&classId=" + classIdStr +
                                   "&semester=" + semester + "&academicYear=" + academicYear);
                break;
            case "trend":
                response.sendRedirect(request.getContextPath() + "/reports/attendance/trend?" +
                                   "subjectCode=" + subjectCode + "&classId=" + classIdStr +
                                   "&semester=" + semester + "&academicYear=" + academicYear);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/principal/reports?error=invalidReportType");
                break;
        }
    }
    
    /**
     * View institution-wide analytics for principal
     */
    private void viewInstitutionAnalytics(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get current academic year
            String academicYear = DateUtils.getCurrentAcademicYear();
            
            // Get all departments
            List<Department> departments = departmentDao.findAll();
            
            // Get all classes
            List<com.attendance.models.Class> classes = classDao.findAll();
            
            // Get all subjects
            List<Subject> subjects = subjectDao.findAll();
            
            // Calculate institution-wide statistics
            Map<String, Object> institutionStats = calculateInstitutionStatistics(departments, classes, subjects, academicYear);
            
            request.setAttribute("departments", departments);
            request.setAttribute("classes", classes);
            request.setAttribute("subjects", subjects);
            request.setAttribute("academicYear", academicYear);
            request.setAttribute("institutionStats", institutionStats);
            
            request.getRequestDispatcher("/WEB-INF/views/principal/reports/institution-analytics.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while generating institution analytics", e);
            request.setAttribute("error", "Failed to generate institution analytics. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/principal/reports/institution-analytics.jsp").forward(request, response);
        }
    }
    
    /**
     * Calculate institution-wide statistics
     */
    private Map<String, Object> calculateInstitutionStatistics(List<Department> departments, List<com.attendance.models.Class> classes,
                                                            List<Subject> subjects, String academicYear) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        int totalStudents = 0;
        int totalTeachers = 0;
        int totalSubjects = subjects.size();
        int totalDepartments = departments.size();
        int totalClasses = classes.size();
        
        double overallAttendance = 0;
        int attendanceRecordCount = 0;
        
        // Count all active students
        String studentCountSql = "SELECT COUNT(*) AS count FROM Users WHERE role = 'Student' AND status = 'Active'";
        
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(studentCountSql)) {
            
            if (rs.next()) {
                totalStudents = rs.getInt("count");
            }
        }
        
        // Count all active teachers
        String teacherCountSql = "SELECT COUNT(*) AS count FROM Users WHERE role = 'Teacher' AND status = 'Active'";
        
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(teacherCountSql)) {
            
            if (rs.next()) {
                totalTeachers = rs.getInt("count");
            }
        }
        
        // Calculate department-wise statistics
        Map<Integer, Map<String, Object>> departmentStats = new HashMap<>();
        
        for (Department dept : departments) {
            int departmentId = dept.getDepartmentId();
            
            // Get classes in this department
            List<com.attendance.models.Class> departmentClasses = new ArrayList<>();
            for (com.attendance.models.Class cls : classes) {
                if (cls.getDepartmentId() == departmentId) {
                    departmentClasses.add(cls);
                }
            }
            
            // Get subjects for this department
            List<DepartmentSubject> departmentSubjects = departmentSubjectDao.findByDepartment(departmentId);
            
            // Calculate department statistics
            Map<String, Object> deptStats = calculateDepartmentStatistics(dept, departmentClasses, departmentSubjects, academicYear);
            
            departmentStats.put(departmentId, deptStats);
            
            // Aggregate attendance data for institution-wide average
            double deptAttendance = (double) deptStats.get("departmentAverageAttendance");
            if (deptAttendance > 0) {
                overallAttendance += deptAttendance;
                attendanceRecordCount++;
            }
        }
        
        // Calculate average attendance across all departments
        double institutionAverageAttendance = attendanceRecordCount > 0 ? overallAttendance / attendanceRecordCount : 0;
        
        // Populate the statistics map
        stats.put("totalStudents", totalStudents);
        stats.put("totalTeachers", totalTeachers);
        stats.put("totalSubjects", totalSubjects);
        stats.put("totalDepartments", totalDepartments);
        stats.put("totalClasses", totalClasses);
        stats.put("institutionAverageAttendance", institutionAverageAttendance);
        stats.put("departmentStats", departmentStats);
        
        return stats;
    }
    
    /**
     * Process principal report generation form
     */
    private void processPrincipalReportGeneration(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        // Similar implementation to processTeacherReportGeneration, but with principal-specific permissions
        // Get report parameters
        String departmentIdStr = request.getParameter("departmentId");
        String classIdStr = request.getParameter("classId");
        String subjectCode = request.getParameter("subjectCode");
        String semester = request.getParameter("semester");
        String academicYear = request.getParameter("academicYear");
        String reportType = request.getParameter("reportType");
        
        // Principal can view any report, so no need to check permissions
        
        // Redirect to the appropriate report type
        StringBuilder redirectUrl = new StringBuilder(request.getContextPath());
        
        switch (reportType) {
            case "summary":
                redirectUrl.append("/reports/attendance/summary?");
                break;
            case "detailed":
                redirectUrl.append("/reports/attendance/detailed?");
                break;
            case "trend":
                redirectUrl.append("/reports/attendance/trend?");
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/principal/reports/generate?error=invalidReportType");
                return;
        }
        
        // Add parameters to the URL
        if (subjectCode != null && !subjectCode.trim().isEmpty()) {
            redirectUrl.append("subjectCode=").append(subjectCode).append("&");
        }
        
        if (classIdStr != null && !classIdStr.trim().isEmpty()) {
            redirectUrl.append("classId=").append(classIdStr).append("&");
        }
        
        if (semester != null && !semester.trim().isEmpty()) {
            redirectUrl.append("semester=").append(semester).append("&");
        }
        
        if (academicYear != null && !academicYear.trim().isEmpty()) {
            redirectUrl.append("academicYear=").append(academicYear).append("&");
        }
        
        response.sendRedirect(redirectUrl.toString());
    }
}