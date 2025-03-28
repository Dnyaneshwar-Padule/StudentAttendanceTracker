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
@WebServlet(name = "FixedAttendanceReportController", urlPatterns = {
    "/reports/attendance/*",
    "/teacher/reports/*",
    "/hod/reports/*",
    "/principal/reports/*"
})
public class FixedAttendanceReportController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(FixedAttendanceReportController.class.getName());
    
    private AttendanceDao attendanceDao;
    private UserDao userDao;
    private ClassDao classDao;
    private SubjectDao subjectDao;
    private DepartmentDao departmentDao;
    private TeacherAssignmentDao teacherAssignmentDao;
    private StudentEnrollmentDao studentEnrollmentDao;
    
    @Override
    public void init() throws ServletException {
        attendanceDao = new AttendanceDaoImpl();
        userDao = new UserDaoImpl();
        classDao = new ClassDaoImpl();
        subjectDao = new SubjectDaoImpl();
        departmentDao = new DepartmentDaoImpl();
        teacherAssignmentDao = new TeacherAssignmentDaoImpl();
        studentEnrollmentDao = new StudentEnrollmentDaoImpl();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Extract path for routing
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();
        
        // Handle different report types based on URL pattern
        if (servletPath.contains("/teacher/reports")) {
            handleTeacherReports(request, response, pathInfo);
        } else if (servletPath.contains("/hod/reports")) {
            handleHodReports(request, response, pathInfo);
        } else if (servletPath.contains("/principal/reports")) {
            handlePrincipalReports(request, response, pathInfo);
        } else {
            // Default reports handling
            if (pathInfo == null || pathInfo.equals("/")) {
                showReportsDashboard(request, response);
            } else if (pathInfo.equals("/student")) {
                showStudentReport(request, response);
            } else if (pathInfo.equals("/class")) {
                showClassReport(request, response);
            } else if (pathInfo.equals("/subject")) {
                showSubjectReport(request, response);
            } else if (pathInfo.equals("/department")) {
                showDepartmentReport(request, response);
            } else if (pathInfo.equals("/institution")) {
                showInstitutionReport(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Extract path for routing
        String pathInfo = request.getPathInfo();
        
        // Handle different report generation requests
        if (pathInfo == null || pathInfo.equals("/")) {
            generateReport(request, response);
        } else if (pathInfo.equals("/export")) {
            exportReport(request, response);
        } else if (pathInfo.equals("/email")) {
            emailReport(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * Show the main reports dashboard
     */
    private void showReportsDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Get the current user from session
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        
        // Load user-specific report options based on role
        if (currentUser != null) {
            String role = currentUser.getRole();
            
            try {
                // Prepare common data for dashboard
                request.setAttribute("userCount", userDao.findAll().size());
                request.setAttribute("activeUserCount", userDao.findByStatus("Active").size());
                
                // Role-specific data
                if ("Student".equals(role)) {
                    prepareStudentDashboard(request, currentUser);
                } else if ("Teacher".equals(role) || "Class Teacher".equals(role)) {
                    prepareTeacherDashboard(request, currentUser);
                } else if ("HOD".equals(role)) {
                    prepareHodDashboard(request, currentUser);
                } else if ("Principal".equals(role) || "Admin".equals(role)) {
                    preparePrincipalDashboard(request, currentUser);
                }
                
                request.getRequestDispatcher("/WEB-INF/views/reports/dashboard.jsp")
                       .forward(request, response);
                
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error loading report dashboard", e);
                request.setAttribute("error", "Failed to load report dashboard: " + e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            }
        } else {
            // Redirect to login if not authenticated
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
    
    /**
     * Prepare dashboard data for student users
     */
    private void prepareStudentDashboard(HttpServletRequest request, User student) 
            throws SQLException {
        
        // Find the student's enrollment
        List<StudentEnrollment> enrollments = 
                studentEnrollmentDao.findByStudentId(student.getUserId());
        
        if (!enrollments.isEmpty()) {
            StudentEnrollment enrollment = enrollments.get(0); // Get the most recent
            
            // Calculate attendance statistics
            double overallAttendance = 
                    attendanceDao.calculateAttendancePercentage(
                            student.getUserId(), 
                            enrollment.getAcademicYear(),
                            null, // semester
                            null  // month
                    );
            
            // Current semester attendance
            String currentSemester = getCurrentSemester();
            double semesterAttendance = 
                    attendanceDao.calculateAttendancePercentage(
                            student.getUserId(), 
                            enrollment.getAcademicYear(),
                            currentSemester,
                            null  // month
                    );
            
            // Current month attendance
            String currentMonth = getCurrentMonth();
            double monthlyAttendance = 
                    attendanceDao.calculateAttendancePercentage(
                            student.getUserId(), 
                            enrollment.getAcademicYear(),
                            currentSemester,
                            currentMonth
                    );
            
            // Subject-wise attendance
            Map<String, Double> subjectAttendance = new HashMap<>();
            List<Subject> subjects = subjectDao.findByClassId(enrollment.getClassId());
            
            for (Subject subject : subjects) {
                double percentage = 
                        attendanceDao.calculateSubjectAttendancePercentage(
                                student.getUserId(), 
                                subject.getSubjectCode(),
                                enrollment.getAcademicYear(),
                                currentSemester
                        );
                subjectAttendance.put(subject.getSubjectName(), percentage);
            }
            
            // Set the data as request attributes
            request.setAttribute("enrollment", enrollment);
            request.setAttribute("overallAttendance", overallAttendance);
            request.setAttribute("semesterAttendance", semesterAttendance);
            request.setAttribute("monthlyAttendance", monthlyAttendance);
            request.setAttribute("subjectAttendance", subjectAttendance);
            request.setAttribute("subjects", subjects);
            
            // Get class information
            com.attendance.models.Class studentClass = classDao.findById(enrollment.getClassId());
            request.setAttribute("studentClass", studentClass);
            
            // Get recent attendance records
            List<Attendance> recentAttendance = 
                    attendanceDao.findRecentByStudent(student.getUserId(), 10);
            request.setAttribute("recentAttendance", recentAttendance);
        }
    }
    
    /**
     * Prepare dashboard data for teacher users
     */
    private void prepareTeacherDashboard(HttpServletRequest request, User teacher) 
            throws SQLException {
        
        // Find the teacher's assignments
        List<TeacherAssignment> assignments = 
                teacherAssignmentDao.findByTeacherId(teacher.getUserId());
        
        Map<Integer, Double> classAttendance = new HashMap<>();
        Map<String, Double> subjectAttendance = new HashMap<>();
        
        // Class attendance for class teacher
        if ("Class Teacher".equals(teacher.getRole())) {
            for (TeacherAssignment assignment : assignments) {
                if ("Class Teacher".equals(assignment.getAssignmentType())) {
                    int classId = assignment.getClassId();
                    double attendancePercentage = 
                            attendanceDao.calculateClassAttendancePercentage(
                                    classId,
                                    getCurrentAcademicYear(),
                                    getCurrentSemester(),
                                    null  // all months
                            );
                    classAttendance.put(classId, attendancePercentage);
                }
            }
        }
        
        // Subject attendance for all teachers
        for (TeacherAssignment assignment : assignments) {
            String subjectCode = assignment.getSubjectCode();
            double attendancePercentage = 
                    attendanceDao.calculateSubjectOverallAttendancePercentage(
                            subjectCode,
                            getCurrentAcademicYear(),
                            getCurrentSemester()
                    );
            subjectAttendance.put(subjectCode, attendancePercentage);
        }
        
        // Get class and subject information
        List<com.attendance.models.Class> classes = new ArrayList<>();
        for (Integer classId : classAttendance.keySet()) {
            classes.add(classDao.findById(classId));
        }
        
        List<Subject> subjects = new ArrayList<>();
        for (String subjectCode : subjectAttendance.keySet()) {
            subjects.add(subjectDao.findByCode(subjectCode));
        }
        
        // Set data as request attributes
        request.setAttribute("assignments", assignments);
        request.setAttribute("classAttendance", classAttendance);
        request.setAttribute("subjectAttendance", subjectAttendance);
        request.setAttribute("classes", classes);
        request.setAttribute("subjects", subjects);
        
        // Get attendance statistics for the last seven days
        Map<String, Object> weeklySummary = 
                attendanceDao.getWeeklyAttendanceSummary(teacher.getUserId());
        request.setAttribute("weeklySummary", weeklySummary);
    }
    
    /**
     * Prepare dashboard data for HOD users
     */
    private void prepareHodDashboard(HttpServletRequest request, User hod) 
            throws SQLException {
        
        int departmentId = hod.getDepartmentId();
        Department department = departmentDao.findById(departmentId);
        
        // Department statistics
        int totalStudents = userDao.findByRoleAndDepartment("Student", departmentId).size();
        int totalTeachers = userDao.findByRoleAndDepartment("Teacher", departmentId).size();
        int totalClasses = classDao.findByDepartment(departmentId).size();
        
        // Department attendance overview
        double departmentAttendance = 
                attendanceDao.calculateDepartmentAttendancePercentage(
                        departmentId,
                        getCurrentAcademicYear(),
                        getCurrentSemester(),
                        null  // all months
                );
        
        // Class-wise attendance in the department
        Map<Integer, Double> classAttendance = new HashMap<>();
        List<com.attendance.models.Class> departmentClasses = classDao.findByDepartment(departmentId);
        
        for (com.attendance.models.Class cls : departmentClasses) {
            double attendancePercentage = 
                    attendanceDao.calculateClassAttendancePercentage(
                            cls.getClassId(),
                            getCurrentAcademicYear(),
                            getCurrentSemester(),
                            null  // all months
                    );
            classAttendance.put(cls.getClassId(), attendancePercentage);
        }
        
        // Subject-wise attendance in the department
        Map<String, Double> subjectAttendance = new HashMap<>();
        List<Subject> departmentSubjects = subjectDao.findByDepartment(departmentId);
        
        for (Subject subject : departmentSubjects) {
            double attendancePercentage = 
                    attendanceDao.calculateSubjectOverallAttendancePercentage(
                            subject.getSubjectCode(),
                            getCurrentAcademicYear(),
                            getCurrentSemester()
                    );
            subjectAttendance.put(subject.getSubjectCode(), attendancePercentage);
        }
        
        // Set data as request attributes
        request.setAttribute("department", department);
        request.setAttribute("totalStudents", totalStudents);
        request.setAttribute("totalTeachers", totalTeachers);
        request.setAttribute("totalClasses", totalClasses);
        request.setAttribute("departmentAttendance", departmentAttendance);
        request.setAttribute("classAttendance", classAttendance);
        request.setAttribute("subjectAttendance", subjectAttendance);
        request.setAttribute("classes", departmentClasses);
        request.setAttribute("subjects", departmentSubjects);
    }
    
    /**
     * Prepare dashboard data for Principal or Admin users
     */
    private void preparePrincipalDashboard(HttpServletRequest request, User principal) 
            throws SQLException {
        
        // Institution-wide statistics
        int totalStudents = userDao.findByRole("Student").size();
        int totalTeachers = userDao.findByRole("Teacher").size();
        int totalHods = userDao.findByRole("HOD").size();
        int totalDepartments = departmentDao.findAll().size();
        
        // Overall attendance statistics
        double institutionAttendance = 
                attendanceDao.calculateInstitutionAttendancePercentage(
                        getCurrentAcademicYear(),
                        getCurrentSemester(),
                        null  // all months
                );
        
        // Department-wise attendance
        Map<Integer, Double> departmentAttendance = new HashMap<>();
        List<Department> allDepartments = departmentDao.findAll();
        
        for (Department dept : allDepartments) {
            double attendancePercentage = 
                    attendanceDao.calculateDepartmentAttendancePercentage(
                            dept.getDepartmentId(),
                            getCurrentAcademicYear(),
                            getCurrentSemester(),
                            null  // all months
                    );
            departmentAttendance.put(dept.getDepartmentId(), attendancePercentage);
        }
        
        // Monthly trend for current academic year
        Map<String, Double> monthlyTrend = 
                attendanceDao.getMonthlyAttendanceTrend(getCurrentAcademicYear());
        
        // Set data as request attributes
        request.setAttribute("totalStudents", totalStudents);
        request.setAttribute("totalTeachers", totalTeachers);
        request.setAttribute("totalHods", totalHods);
        request.setAttribute("totalDepartments", totalDepartments);
        request.setAttribute("institutionAttendance", institutionAttendance);
        request.setAttribute("departmentAttendance", departmentAttendance);
        request.setAttribute("departments", allDepartments);
        request.setAttribute("monthlyTrend", monthlyTrend);
    }
    
    /**
     * Show detailed student attendance report
     */
    private void showStudentReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        
        // Get student ID from request parameter, or use current user ID for student role
        int studentId = 0;
        String studentIdParam = request.getParameter("studentId");
        
        if (studentIdParam != null && !studentIdParam.isEmpty()) {
            try {
                studentId = Integer.parseInt(studentIdParam);
            } catch (NumberFormatException e) {
                // Invalid student ID
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student ID");
                return;
            }
        } else if (currentUser != null && "Student".equals(currentUser.getRole())) {
            studentId = currentUser.getUserId();
        } else {
            // No student ID provided and not a student user
            request.setAttribute("error", "Student ID is required");
            request.getRequestDispatcher("/WEB-INF/views/reports/student_form.jsp").forward(request, response);
            return;
        }
        
        try {
            // Get student details
            User student = userDao.findById(studentId);
            if (student == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Student not found");
                return;
            }
            
            // Check authorization (only the student, their teachers, HOD, or principal can view)
            if (!isAuthorizedToViewStudentReport(currentUser, student)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                        "You are not authorized to view this student's report");
                return;
            }
            
            // Get student enrollment
            List<StudentEnrollment> enrollments = 
                    studentEnrollmentDao.findByStudentId(studentId);
            
            if (enrollments.isEmpty()) {
                request.setAttribute("error", "Student is not enrolled in any class");
                request.getRequestDispatcher("/WEB-INF/views/reports/student_report.jsp").forward(request, response);
                return;
            }
            
            StudentEnrollment enrollment = enrollments.get(0); // Get the most recent
            
            // Get report parameters
            String academicYear = request.getParameter("academicYear");
            String semester = request.getParameter("semester");
            String month = request.getParameter("month");
            String subjectCode = request.getParameter("subjectCode");
            
            // Use default values if not provided
            if (academicYear == null || academicYear.isEmpty()) {
                academicYear = enrollment.getAcademicYear();
            }
            if (semester == null || semester.isEmpty()) {
                semester = getCurrentSemester();
            }
            
            // Get class details
            com.attendance.models.Class studentClass = classDao.findById(enrollment.getClassId());
            
            // Get attendance statistics
            double overallAttendance = 
                    attendanceDao.calculateAttendancePercentage(
                            studentId, 
                            academicYear,
                            semester,
                            month
                    );
            
            // Get subject-wise attendance if no specific subject is selected
            Map<String, Double> subjectAttendance = new HashMap<>();
            List<Subject> subjects = subjectDao.findByClassId(enrollment.getClassId());
            
            for (Subject subject : subjects) {
                double percentage = 
                        attendanceDao.calculateSubjectAttendancePercentage(
                                studentId, 
                                subject.getSubjectCode(),
                                academicYear,
                                semester
                        );
                subjectAttendance.put(subject.getSubjectCode(), percentage);
            }
            
            // Get detailed attendance records
            List<Attendance> attendanceRecords;
            if (subjectCode != null && !subjectCode.isEmpty()) {
                // Filtered by subject
                attendanceRecords = attendanceDao.findByStudentAndSubject(
                        studentId, subjectCode, academicYear, semester, month);
            } else {
                // All subjects
                attendanceRecords = attendanceDao.findByStudent(
                        studentId, academicYear, semester, month);
            }
            
            // Set data as request attributes
            request.setAttribute("student", student);
            request.setAttribute("enrollment", enrollment);
            request.setAttribute("studentClass", studentClass);
            request.setAttribute("academicYear", academicYear);
            request.setAttribute("semester", semester);
            request.setAttribute("month", month);
            request.setAttribute("subjectCode", subjectCode);
            request.setAttribute("overallAttendance", overallAttendance);
            request.setAttribute("subjectAttendance", subjectAttendance);
            request.setAttribute("attendanceRecords", attendanceRecords);
            request.setAttribute("subjects", subjects);
            
            // Forward to the view
            request.getRequestDispatcher("/WEB-INF/views/reports/student_report.jsp")
                   .forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating student report", e);
            request.setAttribute("error", "Failed to generate student report: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    /**
     * Check if the current user is authorized to view a student's report
     */
    private boolean isAuthorizedToViewStudentReport(User currentUser, User student) 
            throws SQLException {
        
        if (currentUser == null || student == null) {
            return false;
        }
        
        // The student can view their own report
        if (currentUser.getUserId() == student.getUserId()) {
            return true;
        }
        
        // Principal and Admin can view any student's report
        String role = currentUser.getRole();
        if ("Principal".equals(role) || "Admin".equals(role)) {
            return true;
        }
        
        // HOD can view reports of students in their department
        if ("HOD".equals(role) && currentUser.getDepartmentId() == student.getDepartmentId()) {
            return true;
        }
        
        // Teachers can view reports of students they teach
        if ("Teacher".equals(role) || "Class Teacher".equals(role)) {
            List<StudentEnrollment> enrollments = 
                    studentEnrollmentDao.findByStudentId(student.getUserId());
            
            if (!enrollments.isEmpty()) {
                StudentEnrollment enrollment = enrollments.get(0);
                
                // Class teacher can view reports of students in their class
                if ("Class Teacher".equals(role)) {
                    List<TeacherAssignment> classAssignments = 
                            teacherAssignmentDao.findByTeacherIdAndType(
                                    currentUser.getUserId(), "Class Teacher");
                    
                    for (TeacherAssignment assignment : classAssignments) {
                        if (assignment.getClassId() == enrollment.getClassId()) {
                            return true;
                        }
                    }
                }
                
                // Subject teacher can view reports of students they teach
                List<TeacherAssignment> subjectAssignments = 
                        teacherAssignmentDao.findByTeacherId(currentUser.getUserId());
                
                for (TeacherAssignment assignment : subjectAssignments) {
                    if (assignment.getClassId() == enrollment.getClassId()) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Show detailed class attendance report
     */
    private void showClassReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        
        // Get class ID from request parameter
        String classIdParam = request.getParameter("classId");
        if (classIdParam == null || classIdParam.isEmpty()) {
            // No class ID provided, show class selection form
            try {
                List<com.attendance.models.Class> classes;
                
                // Filter classes based on user role
                if ("Principal".equals(currentUser.getRole()) || "Admin".equals(currentUser.getRole())) {
                    classes = classDao.findAll();
                } else if ("HOD".equals(currentUser.getRole())) {
                    classes = classDao.findByDepartment(currentUser.getDepartmentId());
                } else if ("Teacher".equals(currentUser.getRole()) || "Class Teacher".equals(currentUser.getRole())) {
                    // Get classes the teacher is assigned to
                    List<TeacherAssignment> assignments = 
                            teacherAssignmentDao.findByTeacherId(currentUser.getUserId());
                    
                    Set<Integer> classIds = new HashSet<>();
                    for (TeacherAssignment assignment : assignments) {
                        classIds.add(assignment.getClassId());
                    }
                    
                    classes = new ArrayList<>();
                    for (Integer classId : classIds) {
                        classes.add(classDao.findById(classId));
                    }
                } else {
                    // Students can't access class reports
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                            "You are not authorized to view class reports");
                    return;
                }
                
                request.setAttribute("classes", classes);
                request.getRequestDispatcher("/WEB-INF/views/reports/class_form.jsp").forward(request, response);
                return;
                
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error loading class selection form", e);
                request.setAttribute("error", "Failed to load class selection form: " + e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
                return;
            }
        }
        
        // Process the class report
        try {
            int classId = Integer.parseInt(classIdParam);
            
            // Get class details
            com.attendance.models.Class classObj = classDao.findById(classId);
            if (classObj == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Class not found");
                return;
            }
            
            // Check authorization
            if (!isAuthorizedToViewClassReport(currentUser, classObj)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                        "You are not authorized to view this class report");
                return;
            }
            
            // Get report parameters
            String academicYear = request.getParameter("academicYear");
            String semester = request.getParameter("semester");
            String month = request.getParameter("month");
            String subjectCode = request.getParameter("subjectCode");
            String date = request.getParameter("date");
            
            // Use default values if not provided
            if (academicYear == null || academicYear.isEmpty()) {
                academicYear = getCurrentAcademicYear();
            }
            if (semester == null || semester.isEmpty()) {
                semester = getCurrentSemester();
            }
            
            // Get class attendance statistics
            double overallAttendance = 
                    attendanceDao.calculateClassAttendancePercentage(
                            classId, 
                            academicYear,
                            semester,
                            month
                    );
            
            // Get students in the class
            List<User> students = studentEnrollmentDao.findStudentsByClass(
                    classId, academicYear);
            
            // Get subjects taught in the class
            List<Subject> subjects = subjectDao.findByClassId(classId);
            
            // Get subject-wise attendance if no specific subject is selected
            Map<String, Double> subjectAttendance = new HashMap<>();
            for (Subject subject : subjects) {
                double percentage = 
                        attendanceDao.calculateSubjectClassAttendancePercentage(
                                classId,
                                subject.getSubjectCode(),
                                academicYear,
                                semester
                        );
                subjectAttendance.put(subject.getSubjectCode(), percentage);
            }
            
            // Get student-wise attendance statistics
            Map<Integer, Double> studentAttendance = new HashMap<>();
            for (User student : students) {
                double percentage;
                if (subjectCode != null && !subjectCode.isEmpty()) {
                    // Subject-specific attendance
                    percentage = attendanceDao.calculateSubjectAttendancePercentage(
                            student.getUserId(),
                            subjectCode,
                            academicYear,
                            semester
                    );
                } else {
                    // Overall attendance
                    percentage = attendanceDao.calculateAttendancePercentage(
                            student.getUserId(),
                            academicYear,
                            semester,
                            month
                    );
                }
                studentAttendance.put(student.getUserId(), percentage);
            }
            
            // Get daily attendance records for a specific date if provided
            Map<Integer, String> dailyAttendance = new HashMap<>();
            if (date != null && !date.isEmpty()) {
                Date sqlDate = Date.valueOf(date);
                List<Attendance> records;
                
                if (subjectCode != null && !subjectCode.isEmpty()) {
                    // Subject-specific daily attendance
                    records = attendanceDao.findByClassAndSubjectAndDate(
                            classId, subjectCode, sqlDate);
                } else {
                    // All subjects for the date
                    records = attendanceDao.findByClassAndDate(classId, sqlDate);
                }
                
                // Group by student ID
                Map<Integer, List<Attendance>> studentRecords = new HashMap<>();
                for (Attendance record : records) {
                    int studentId = record.getStudentId();
                    if (!studentRecords.containsKey(studentId)) {
                        studentRecords.put(studentId, new ArrayList<>());
                    }
                    studentRecords.get(studentId).add(record);
                }
                
                // Determine overall status for each student
                for (User student : students) {
                    int studentId = student.getUserId();
                    List<Attendance> studentDailyRecords = studentRecords.get(studentId);
                    
                    if (studentDailyRecords == null || studentDailyRecords.isEmpty()) {
                        dailyAttendance.put(studentId, "Not Marked");
                    } else {
                        // Check if all records are present
                        boolean allPresent = true;
                        for (Attendance record : studentDailyRecords) {
                            if (!"Present".equals(record.getStatus())) {
                                allPresent = false;
                                break;
                            }
                        }
                        dailyAttendance.put(studentId, allPresent ? "Present" : "Absent");
                    }
                }
            }
            
            // Get class teacher
            User classTeacher = null;
            List<TeacherAssignment> assignments = 
                    teacherAssignmentDao.findByClassIdAndType(classId, "Class Teacher");
            if (!assignments.isEmpty()) {
                int teacherId = assignments.get(0).getTeacherId();
                classTeacher = userDao.findById(teacherId);
            }
            
            // Set data as request attributes
            request.setAttribute("classObj", classObj);
            request.setAttribute("academicYear", academicYear);
            request.setAttribute("semester", semester);
            request.setAttribute("month", month);
            request.setAttribute("date", date);
            request.setAttribute("subjectCode", subjectCode);
            request.setAttribute("overallAttendance", overallAttendance);
            request.setAttribute("students", students);
            request.setAttribute("subjects", subjects);
            request.setAttribute("subjectAttendance", subjectAttendance);
            request.setAttribute("studentAttendance", studentAttendance);
            request.setAttribute("dailyAttendance", dailyAttendance);
            request.setAttribute("classTeacher", classTeacher);
            
            // Forward to the view
            request.getRequestDispatcher("/WEB-INF/views/reports/class_report.jsp")
                   .forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid class ID");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating class report", e);
            request.setAttribute("error", "Failed to generate class report: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    /**
     * Check if the current user is authorized to view a class report
     */
    private boolean isAuthorizedToViewClassReport(User currentUser, com.attendance.models.Class classObj) 
            throws SQLException {
        
        if (currentUser == null || classObj == null) {
            return false;
        }
        
        String role = currentUser.getRole();
        
        // Principal and Admin can view any class report
        if ("Principal".equals(role) || "Admin".equals(role)) {
            return true;
        }
        
        // HOD can view reports of classes in their department
        if ("HOD".equals(role) && currentUser.getDepartmentId() == classObj.getDepartmentId()) {
            return true;
        }
        
        // Teachers can view reports of classes they teach
        if ("Teacher".equals(role) || "Class Teacher".equals(role)) {
            List<TeacherAssignment> assignments = 
                    teacherAssignmentDao.findByTeacherId(currentUser.getUserId());
            
            for (TeacherAssignment assignment : assignments) {
                if (assignment.getClassId() == classObj.getClassId()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Show detailed subject attendance report
     */
    private void showSubjectReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        
        // Get subject code from request parameter
        String subjectCode = request.getParameter("subjectCode");
        if (subjectCode == null || subjectCode.isEmpty()) {
            // No subject code provided, show subject selection form
            try {
                List<Subject> subjects;
                
                // Filter subjects based on user role
                if ("Principal".equals(currentUser.getRole()) || "Admin".equals(currentUser.getRole())) {
                    subjects = subjectDao.findAll();
                } else if ("HOD".equals(currentUser.getRole())) {
                    subjects = subjectDao.findByDepartment(currentUser.getDepartmentId());
                } else if ("Teacher".equals(currentUser.getRole()) || "Class Teacher".equals(currentUser.getRole())) {
                    // Get subjects the teacher is assigned to
                    List<TeacherAssignment> assignments = 
                            teacherAssignmentDao.findByTeacherId(currentUser.getUserId());
                    
                    Set<String> subjectCodes = new HashSet<>();
                    for (TeacherAssignment assignment : assignments) {
                        subjectCodes.add(assignment.getSubjectCode());
                    }
                    
                    subjects = new ArrayList<>();
                    for (String code : subjectCodes) {
                        subjects.add(subjectDao.findByCode(code));
                    }
                } else {
                    // Students can't access subject reports directly
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                            "You are not authorized to view subject reports");
                    return;
                }
                
                request.setAttribute("subjects", subjects);
                request.getRequestDispatcher("/WEB-INF/views/reports/subject_form.jsp").forward(request, response);
                return;
                
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error loading subject selection form", e);
                request.setAttribute("error", "Failed to load subject selection form: " + e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
                return;
            }
        }
        
        // Process the subject report
        try {
            // Get subject details
            Subject subject = subjectDao.findByCode(subjectCode);
            if (subject == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Subject not found");
                return;
            }
            
            // Check authorization
            if (!isAuthorizedToViewSubjectReport(currentUser, subject)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                        "You are not authorized to view this subject report");
                return;
            }
            
            // Get report parameters
            String academicYear = request.getParameter("academicYear");
            String semester = request.getParameter("semester");
            String month = request.getParameter("month");
            String classIdParam = request.getParameter("classId");
            String date = request.getParameter("date");
            
            // Use default values if not provided
            if (academicYear == null || academicYear.isEmpty()) {
                academicYear = getCurrentAcademicYear();
            }
            if (semester == null || semester.isEmpty()) {
                semester = getCurrentSemester();
            }
            
            Integer classId = null;
            if (classIdParam != null && !classIdParam.isEmpty()) {
                classId = Integer.parseInt(classIdParam);
            }
            
            // Get subject teacher(s)
            List<User> subjectTeachers = new ArrayList<>();
            List<TeacherAssignment> assignments = 
                    teacherAssignmentDao.findBySubjectCode(subjectCode);
            
            for (TeacherAssignment assignment : assignments) {
                User teacher = userDao.findById(assignment.getTeacherId());
                if (teacher != null && !subjectTeachers.contains(teacher)) {
                    subjectTeachers.add(teacher);
                }
            }
            
            // Get classes where this subject is taught
            List<com.attendance.models.Class> subjectClasses = new ArrayList<>();
            Map<Integer, List<User>> classStudents = new HashMap<>();
            
            for (TeacherAssignment assignment : assignments) {
                com.attendance.models.Class classObj = classDao.findById(assignment.getClassId());
                if (classObj != null && !subjectClasses.contains(classObj)) {
                    subjectClasses.add(classObj);
                    
                    // Get students for this class
                    List<User> students = studentEnrollmentDao.findStudentsByClass(
                            classObj.getClassId(), academicYear);
                    
                    classStudents.put(classObj.getClassId(), students);
                }
            }
            
            // Calculate subject attendance percentages
            double overallAttendance = 
                    attendanceDao.calculateSubjectOverallAttendancePercentage(
                            subjectCode,
                            academicYear,
                            semester
                    );
            
            // Class-wise subject attendance
            Map<Integer, Double> classAttendance = new HashMap<>();
            for (com.attendance.models.Class classObj : subjectClasses) {
                double percentage = 
                        attendanceDao.calculateSubjectClassAttendancePercentage(
                                classObj.getClassId(),
                                subjectCode,
                                academicYear,
                                semester
                        );
                classAttendance.put(classObj.getClassId(), percentage);
            }
            
            // Get student-wise attendance for a specific class if selected
            Map<Integer, Double> studentAttendance = new HashMap<>();
            List<User> students = new ArrayList<>();
            
            if (classId != null) {
                students = classStudents.get(classId);
                if (students != null) {
                    for (User student : students) {
                        double percentage = attendanceDao.calculateSubjectAttendancePercentage(
                                student.getUserId(),
                                subjectCode,
                                academicYear,
                                semester
                        );
                        studentAttendance.put(student.getUserId(), percentage);
                    }
                }
            }
            
            // Get daily attendance records for a specific date and class if provided
            Map<Integer, String> dailyAttendance = new HashMap<>();
            if (date != null && !date.isEmpty() && classId != null) {
                Date sqlDate = Date.valueOf(date);
                List<Attendance> records = attendanceDao.findByClassAndSubjectAndDate(
                        classId, subjectCode, sqlDate);
                
                // Determine status for each student
                for (User student : students) {
                    int studentId = student.getUserId();
                    boolean found = false;
                    
                    for (Attendance record : records) {
                        if (record.getStudentId() == studentId) {
                            dailyAttendance.put(studentId, record.getStatus());
                            found = true;
                            break;
                        }
                    }
                    
                    if (!found) {
                        dailyAttendance.put(studentId, "Not Marked");
                    }
                }
            }
            
            // Set data as request attributes
            request.setAttribute("subject", subject);
            request.setAttribute("academicYear", academicYear);
            request.setAttribute("semester", semester);
            request.setAttribute("month", month);
            request.setAttribute("classId", classId);
            request.setAttribute("date", date);
            request.setAttribute("overallAttendance", overallAttendance);
            request.setAttribute("subjectTeachers", subjectTeachers);
            request.setAttribute("subjectClasses", subjectClasses);
            request.setAttribute("classAttendance", classAttendance);
            request.setAttribute("students", students);
            request.setAttribute("studentAttendance", studentAttendance);
            request.setAttribute("dailyAttendance", dailyAttendance);
            
            // Forward to the view
            request.getRequestDispatcher("/WEB-INF/views/reports/subject_report.jsp")
                   .forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid class ID");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating subject report", e);
            request.setAttribute("error", "Failed to generate subject report: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    /**
     * Check if the current user is authorized to view a subject report
     */
    private boolean isAuthorizedToViewSubjectReport(User currentUser, Subject subject) 
            throws SQLException {
        
        if (currentUser == null || subject == null) {
            return false;
        }
        
        String role = currentUser.getRole();
        
        // Principal and Admin can view any subject report
        if ("Principal".equals(role) || "Admin".equals(role)) {
            return true;
        }
        
        // HOD can view reports of subjects in their department
        if ("HOD".equals(role) && currentUser.getDepartmentId() == subject.getDepartmentId()) {
            return true;
        }
        
        // Teachers can view reports of subjects they teach
        if ("Teacher".equals(role) || "Class Teacher".equals(role)) {
            List<TeacherAssignment> assignments = 
                    teacherAssignmentDao.findByTeacherId(currentUser.getUserId());
            
            for (TeacherAssignment assignment : assignments) {
                if (assignment.getSubjectCode().equals(subject.getSubjectCode())) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Show detailed department attendance report
     */
    private void showDepartmentReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        
        // Check if user is authorized to view department reports
        String role = currentUser.getRole();
        if (!("Principal".equals(role) || "Admin".equals(role) || "HOD".equals(role))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                    "You are not authorized to view department reports");
            return;
        }
        
        // Get department ID from request parameter
        String departmentIdParam = request.getParameter("departmentId");
        if (departmentIdParam == null || departmentIdParam.isEmpty()) {
            // No department ID provided, show department selection form
            try {
                List<Department> departments;
                
                // Filter departments based on user role
                if ("Principal".equals(role) || "Admin".equals(role)) {
                    departments = departmentDao.findAll();
                } else if ("HOD".equals(role)) {
                    // HOD can only view their own department
                    departments = new ArrayList<>();
                    departments.add(departmentDao.findById(currentUser.getDepartmentId()));
                } else {
                    departments = new ArrayList<>();
                }
                
                request.setAttribute("departments", departments);
                request.getRequestDispatcher("/WEB-INF/views/reports/department_form.jsp").forward(request, response);
                return;
                
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error loading department selection form", e);
                request.setAttribute("error", "Failed to load department selection form: " + e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
                return;
            }
        }
        
        // Process the department report
        try {
            int departmentId = Integer.parseInt(departmentIdParam);
            
            // Get department details
            Department department = departmentDao.findById(departmentId);
            if (department == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Department not found");
                return;
            }
            
            // Check authorization for HOD
            if ("HOD".equals(role) && currentUser.getDepartmentId() != departmentId) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                        "You are not authorized to view this department report");
                return;
            }
            
            // Get report parameters
            String academicYear = request.getParameter("academicYear");
            String semester = request.getParameter("semester");
            String month = request.getParameter("month");
            
            // Use default values if not provided
            if (academicYear == null || academicYear.isEmpty()) {
                academicYear = getCurrentAcademicYear();
            }
            if (semester == null || semester.isEmpty()) {
                semester = getCurrentSemester();
            }
            
            // Get department attendance statistics
            double overallAttendance = 
                    attendanceDao.calculateDepartmentAttendancePercentage(
                            departmentId, 
                            academicYear,
                            semester,
                            month
                    );
            
            // Get HOD of the department
            User hod = null;
            List<User> hodList = userDao.findByRoleAndDepartment("HOD", departmentId);
            if (!hodList.isEmpty()) {
                hod = hodList.get(0);
            }
            
            // Get classes in the department
            List<com.attendance.models.Class> classes = classDao.findByDepartment(departmentId);
            
            // Class-wise attendance
            Map<Integer, Double> classAttendance = new HashMap<>();
            for (com.attendance.models.Class classObj : classes) {
                double percentage = 
                        attendanceDao.calculateClassAttendancePercentage(
                                classObj.getClassId(),
                                academicYear,
                                semester,
                                month
                        );
                classAttendance.put(classObj.getClassId(), percentage);
            }
            
            // Get subjects in the department
            List<Subject> subjects = subjectDao.findByDepartment(departmentId);
            
            // Subject-wise attendance
            Map<String, Double> subjectAttendance = new HashMap<>();
            for (Subject subject : subjects) {
                double percentage = 
                        attendanceDao.calculateSubjectOverallAttendancePercentage(
                                subject.getSubjectCode(),
                                academicYear,
                                semester
                        );
                subjectAttendance.put(subject.getSubjectCode(), percentage);
            }
            
            // Get teachers in the department
            List<User> teachers = userDao.findByRoleAndDepartment("Teacher", departmentId);
            teachers.addAll(userDao.findByRoleAndDepartment("Class Teacher", departmentId));
            
            // Get students in the department
            List<User> students = userDao.findByRoleAndDepartment("Student", departmentId);
            
            // Student count by class
            Map<Integer, Integer> classStudentCount = new HashMap<>();
            for (com.attendance.models.Class classObj : classes) {
                List<User> classStudents = studentEnrollmentDao.findStudentsByClass(
                        classObj.getClassId(), academicYear);
                classStudentCount.put(classObj.getClassId(), classStudents.size());
            }
            
            // Monthly trend for the department
            Map<String, Double> monthlyTrend = 
                    attendanceDao.getMonthlyDepartmentAttendanceTrend(
                            departmentId, academicYear);
            
            // Set data as request attributes
            request.setAttribute("department", department);
            request.setAttribute("hod", hod);
            request.setAttribute("academicYear", academicYear);
            request.setAttribute("semester", semester);
            request.setAttribute("month", month);
            request.setAttribute("overallAttendance", overallAttendance);
            request.setAttribute("classes", classes);
            request.setAttribute("classAttendance", classAttendance);
            request.setAttribute("subjects", subjects);
            request.setAttribute("subjectAttendance", subjectAttendance);
            request.setAttribute("teachers", teachers);
            request.setAttribute("students", students);
            request.setAttribute("classStudentCount", classStudentCount);
            request.setAttribute("monthlyTrend", monthlyTrend);
            
            // Forward to the view
            request.getRequestDispatcher("/WEB-INF/views/reports/department_report.jsp")
                   .forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid department ID");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating department report", e);
            request.setAttribute("error", "Failed to generate department report: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    /**
     * Show institution-wide attendance report
     */
    private void showInstitutionReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        
        // Check if user is authorized to view institution reports
        String role = currentUser.getRole();
        if (!("Principal".equals(role) || "Admin".equals(role))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                    "You are not authorized to view institution-wide reports");
            return;
        }
        
        try {
            // Get report parameters
            String academicYear = request.getParameter("academicYear");
            String semester = request.getParameter("semester");
            String month = request.getParameter("month");
            
            // Use default values if not provided
            if (academicYear == null || academicYear.isEmpty()) {
                academicYear = getCurrentAcademicYear();
            }
            if (semester == null || semester.isEmpty()) {
                semester = getCurrentSemester();
            }
            
            // Get institution-wide attendance statistics
            double overallAttendance = 
                    attendanceDao.calculateInstitutionAttendancePercentage(
                            academicYear,
                            semester,
                            month
                    );
            
            // Get departments
            List<Department> departments = departmentDao.findAll();
            
            // Department-wise attendance
            Map<Integer, Double> departmentAttendance = new HashMap<>();
            for (Department department : departments) {
                double percentage = 
                        attendanceDao.calculateDepartmentAttendancePercentage(
                                department.getDepartmentId(),
                                academicYear,
                                semester,
                                month
                        );
                departmentAttendance.put(department.getDepartmentId(), percentage);
            }
            
            // Get all classes
            List<com.attendance.models.Class> classes = classDao.findAll();
            
            // Class-wise attendance (top 10 highest and lowest)
            Map<Integer, Double> classAttendance = new HashMap<>();
            for (com.attendance.models.Class classObj : classes) {
                double percentage = 
                        attendanceDao.calculateClassAttendancePercentage(
                                classObj.getClassId(),
                                academicYear,
                                semester,
                                month
                        );
                classAttendance.put(classObj.getClassId(), percentage);
            }
            
            // Monthly trend across all departments
            Map<String, Double> monthlyTrend = 
                    attendanceDao.getMonthlyAttendanceTrend(academicYear);
            
            // Student count by department
            Map<Integer, Integer> departmentStudentCount = new HashMap<>();
            for (Department department : departments) {
                List<User> deptStudents = userDao.findByRoleAndDepartment(
                        "Student", department.getDepartmentId());
                departmentStudentCount.put(department.getDepartmentId(), deptStudents.size());
            }
            
            // Overall statistics
            int totalStudents = userDao.findByRole("Student").size();
            int totalTeachers = userDao.findByRole("Teacher").size() + 
                              userDao.findByRole("Class Teacher").size();
            int totalHods = userDao.findByRole("HOD").size();
            
            // Set data as request attributes
            request.setAttribute("academicYear", academicYear);
            request.setAttribute("semester", semester);
            request.setAttribute("month", month);
            request.setAttribute("overallAttendance", overallAttendance);
            request.setAttribute("departments", departments);
            request.setAttribute("departmentAttendance", departmentAttendance);
            request.setAttribute("classes", classes);
            request.setAttribute("classAttendance", classAttendance);
            request.setAttribute("monthlyTrend", monthlyTrend);
            request.setAttribute("departmentStudentCount", departmentStudentCount);
            request.setAttribute("totalStudents", totalStudents);
            request.setAttribute("totalTeachers", totalTeachers);
            request.setAttribute("totalHods", totalHods);
            request.setAttribute("totalDepartments", departments.size());
            
            // Forward to the view
            request.getRequestDispatcher("/WEB-INF/views/reports/institution_report.jsp")
                   .forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating institution report", e);
            request.setAttribute("error", "Failed to generate institution report: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    /**
     * Handle teacher-specific reports
     */
    private void handleTeacherReports(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {
        
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        
        // Check if user is a teacher
        String role = currentUser.getRole();
        if (!("Teacher".equals(role) || "Class Teacher".equals(role))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                    "You are not authorized to view teacher reports");
            return;
        }
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Show teacher dashboard
                prepareTeacherDashboard(request, currentUser);
                request.getRequestDispatcher("/WEB-INF/views/reports/teacher_dashboard.jsp")
                       .forward(request, response);
            } else if (pathInfo.equals("/classes")) {
                // Show teacher's classes
                List<TeacherAssignment> assignments = 
                        teacherAssignmentDao.findByTeacherId(currentUser.getUserId());
                
                Set<Integer> classIds = new HashSet<>();
                for (TeacherAssignment assignment : assignments) {
                    classIds.add(assignment.getClassId());
                }
                
                List<com.attendance.models.Class> classes = new ArrayList<>();
                for (Integer classId : classIds) {
                    classes.add(classDao.findById(classId));
                }
                
                request.setAttribute("classes", classes);
                request.getRequestDispatcher("/WEB-INF/views/reports/teacher_classes.jsp")
                       .forward(request, response);
            } else if (pathInfo.equals("/subjects")) {
                // Show teacher's subjects
                List<TeacherAssignment> assignments = 
                        teacherAssignmentDao.findByTeacherId(currentUser.getUserId());
                
                Set<String> subjectCodes = new HashSet<>();
                for (TeacherAssignment assignment : assignments) {
                    subjectCodes.add(assignment.getSubjectCode());
                }
                
                List<Subject> subjects = new ArrayList<>();
                for (String code : subjectCodes) {
                    subjects.add(subjectDao.findByCode(code));
                }
                
                request.setAttribute("subjects", subjects);
                request.getRequestDispatcher("/WEB-INF/views/reports/teacher_subjects.jsp")
                       .forward(request, response);
            } else if (pathInfo.equals("/attendance-summary")) {
                // Show teacher's attendance marking summary
                String academicYear = request.getParameter("academicYear");
                String semester = request.getParameter("semester");
                String month = request.getParameter("month");
                
                // Use default values if not provided
                if (academicYear == null || academicYear.isEmpty()) {
                    academicYear = getCurrentAcademicYear();
                }
                if (semester == null || semester.isEmpty()) {
                    semester = getCurrentSemester();
                }
                
                // Get teacher's assignments
                List<TeacherAssignment> assignments = 
                        teacherAssignmentDao.findByTeacherId(currentUser.getUserId());
                
                // Get attendance records marked by this teacher
                Map<String, Integer> markedAttendance = 
                        attendanceDao.getTeacherMarkedAttendanceSummary(
                                currentUser.getUserId(),
                                academicYear,
                                semester,
                                month
                        );
                
                request.setAttribute("academicYear", academicYear);
                request.setAttribute("semester", semester);
                request.setAttribute("month", month);
                request.setAttribute("assignments", assignments);
                request.setAttribute("markedAttendance", markedAttendance);
                request.getRequestDispatcher("/WEB-INF/views/reports/teacher_attendance_summary.jsp")
                       .forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error processing teacher report", e);
            request.setAttribute("error", "Failed to process teacher report: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    /**
     * Handle HOD-specific reports
     */
    private void handleHodReports(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {
        
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        
        // Check if user is an HOD
        if (!"HOD".equals(currentUser.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                    "You are not authorized to view HOD reports");
            return;
        }
        
        try {
            int departmentId = currentUser.getDepartmentId();
            Department department = departmentDao.findById(departmentId);
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // Show HOD dashboard
                prepareHodDashboard(request, currentUser);
                request.getRequestDispatcher("/WEB-INF/views/reports/hod_dashboard.jsp")
                       .forward(request, response);
            } else if (pathInfo.equals("/teachers")) {
                // Show department teachers
                List<User> teachers = userDao.findByRoleAndDepartment("Teacher", departmentId);
                teachers.addAll(userDao.findByRoleAndDepartment("Class Teacher", departmentId));
                
                // Get teacher assignments
                Map<Integer, List<TeacherAssignment>> teacherAssignments = new HashMap<>();
                for (User teacher : teachers) {
                    List<TeacherAssignment> assignments = 
                            teacherAssignmentDao.findByTeacherId(teacher.getUserId());
                    teacherAssignments.put(teacher.getUserId(), assignments);
                }
                
                request.setAttribute("department", department);
                request.setAttribute("teachers", teachers);
                request.setAttribute("teacherAssignments", teacherAssignments);
                request.getRequestDispatcher("/WEB-INF/views/reports/hod_teachers.jsp")
                       .forward(request, response);
            } else if (pathInfo.equals("/classes")) {
                // Show department classes
                List<com.attendance.models.Class> classes = classDao.findByDepartment(departmentId);
                
                // Get class teachers
                Map<Integer, User> classTeachers = new HashMap<>();
                for (com.attendance.models.Class classObj : classes) {
                    List<TeacherAssignment> assignments = 
                            teacherAssignmentDao.findByClassIdAndType(
                                    classObj.getClassId(), "Class Teacher");
                    if (!assignments.isEmpty()) {
                        int teacherId = assignments.get(0).getTeacherId();
                        classTeachers.put(classObj.getClassId(), userDao.findById(teacherId));
                    }
                }
                
                // Get student counts
                Map<Integer, Integer> classStudentCount = new HashMap<>();
                for (com.attendance.models.Class classObj : classes) {
                    List<User> classStudents = studentEnrollmentDao.findStudentsByClass(
                            classObj.getClassId(), getCurrentAcademicYear());
                    classStudentCount.put(classObj.getClassId(), classStudents.size());
                }
                
                // Get attendance percentages
                Map<Integer, Double> classAttendance = new HashMap<>();
                for (com.attendance.models.Class classObj : classes) {
                    double percentage = 
                            attendanceDao.calculateClassAttendancePercentage(
                                    classObj.getClassId(),
                                    getCurrentAcademicYear(),
                                    getCurrentSemester(),
                                    null  // all months
                            );
                    classAttendance.put(classObj.getClassId(), percentage);
                }
                
                request.setAttribute("department", department);
                request.setAttribute("classes", classes);
                request.setAttribute("classTeachers", classTeachers);
                request.setAttribute("classStudentCount", classStudentCount);
                request.setAttribute("classAttendance", classAttendance);
                request.getRequestDispatcher("/WEB-INF/views/reports/hod_classes.jsp")
                       .forward(request, response);
            } else if (pathInfo.equals("/subjects")) {
                // Show department subjects
                List<Subject> subjects = subjectDao.findByDepartment(departmentId);
                
                // Get subject teachers
                Map<String, List<User>> subjectTeachers = new HashMap<>();
                for (Subject subject : subjects) {
                    List<TeacherAssignment> assignments = 
                            teacherAssignmentDao.findBySubjectCode(subject.getSubjectCode());
                    
                    List<User> teachers = new ArrayList<>();
                    for (TeacherAssignment assignment : assignments) {
                        User teacher = userDao.findById(assignment.getTeacherId());
                        if (teacher != null && !teachers.contains(teacher)) {
                            teachers.add(teacher);
                        }
                    }
                    
                    subjectTeachers.put(subject.getSubjectCode(), teachers);
                }
                
                // Get attendance percentages
                Map<String, Double> subjectAttendance = new HashMap<>();
                for (Subject subject : subjects) {
                    double percentage = 
                            attendanceDao.calculateSubjectOverallAttendancePercentage(
                                    subject.getSubjectCode(),
                                    getCurrentAcademicYear(),
                                    getCurrentSemester()
                            );
                    subjectAttendance.put(subject.getSubjectCode(), percentage);
                }
                
                request.setAttribute("department", department);
                request.setAttribute("subjects", subjects);
                request.setAttribute("subjectTeachers", subjectTeachers);
                request.setAttribute("subjectAttendance", subjectAttendance);
                request.getRequestDispatcher("/WEB-INF/views/reports/hod_subjects.jsp")
                       .forward(request, response);
            } else if (pathInfo.equals("/students")) {
                // Show department students
                List<User> students = userDao.findByRoleAndDepartment("Student", departmentId);
                
                // Get class-wise students
                List<com.attendance.models.Class> classes = classDao.findByDepartment(departmentId);
                Map<Integer, List<User>> classStudents = new HashMap<>();
                
                for (com.attendance.models.Class classObj : classes) {
                    List<User> classStudentList = studentEnrollmentDao.findStudentsByClass(
                            classObj.getClassId(), getCurrentAcademicYear());
                    classStudents.put(classObj.getClassId(), classStudentList);
                }
                
                request.setAttribute("department", department);
                request.setAttribute("students", students);
                request.setAttribute("classes", classes);
                request.setAttribute("classStudents", classStudents);
                request.getRequestDispatcher("/WEB-INF/views/reports/hod_students.jsp")
                       .forward(request, response);
            } else if (pathInfo.equals("/attendance-trend")) {
                // Show department attendance trend
                String academicYear = request.getParameter("academicYear");
                
                // Use default value if not provided
                if (academicYear == null || academicYear.isEmpty()) {
                    academicYear = getCurrentAcademicYear();
                }
                
                // Get monthly trend
                Map<String, Double> monthlyTrend = 
                        attendanceDao.getMonthlyDepartmentAttendanceTrend(
                                departmentId, academicYear);
                
                // Get semester-wise trend
                Map<String, Double> semesterTrend = 
                        attendanceDao.getSemesterDepartmentAttendanceTrend(
                                departmentId, academicYear);
                
                request.setAttribute("department", department);
                request.setAttribute("academicYear", academicYear);
                request.setAttribute("monthlyTrend", monthlyTrend);
                request.setAttribute("semesterTrend", semesterTrend);
                request.getRequestDispatcher("/WEB-INF/views/reports/hod_attendance_trend.jsp")
                       .forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error processing HOD report", e);
            request.setAttribute("error", "Failed to process HOD report: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    /**
     * Handle Principal-specific reports
     */
    private void handlePrincipalReports(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {
        
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        
        // Check if user is a Principal or Admin
        String role = currentUser.getRole();
        if (!("Principal".equals(role) || "Admin".equals(role))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                    "You are not authorized to view principal reports");
            return;
        }
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Show principal dashboard
                preparePrincipalDashboard(request, currentUser);
                request.getRequestDispatcher("/WEB-INF/views/reports/principal_dashboard.jsp")
                       .forward(request, response);
            } else if (pathInfo.equals("/departments")) {
                // Show all departments
                List<Department> departments = departmentDao.findAll();
                
                // Get HODs
                Map<Integer, User> departmentHods = new HashMap<>();
                for (Department dept : departments) {
                    List<User> hods = userDao.findByRoleAndDepartment("HOD", dept.getDepartmentId());
                    if (!hods.isEmpty()) {
                        departmentHods.put(dept.getDepartmentId(), hods.get(0));
                    }
                }
                
                // Get student and teacher counts
                Map<Integer, Integer> departmentStudentCount = new HashMap<>();
                Map<Integer, Integer> departmentTeacherCount = new HashMap<>();
                
                for (Department dept : departments) {
                    List<User> students = userDao.findByRoleAndDepartment("Student", dept.getDepartmentId());
                    departmentStudentCount.put(dept.getDepartmentId(), students.size());
                    
                    List<User> teachers = userDao.findByRoleAndDepartment("Teacher", dept.getDepartmentId());
                    teachers.addAll(userDao.findByRoleAndDepartment("Class Teacher", dept.getDepartmentId()));
                    departmentTeacherCount.put(dept.getDepartmentId(), teachers.size());
                }
                
                // Get attendance percentages
                Map<Integer, Double> departmentAttendance = new HashMap<>();
                for (Department dept : departments) {
                    double percentage = 
                            attendanceDao.calculateDepartmentAttendancePercentage(
                                    dept.getDepartmentId(),
                                    getCurrentAcademicYear(),
                                    getCurrentSemester(),
                                    null  // all months
                            );
                    departmentAttendance.put(dept.getDepartmentId(), percentage);
                }
                
                request.setAttribute("departments", departments);
                request.setAttribute("departmentHods", departmentHods);
                request.setAttribute("departmentStudentCount", departmentStudentCount);
                request.setAttribute("departmentTeacherCount", departmentTeacherCount);
                request.setAttribute("departmentAttendance", departmentAttendance);
                request.getRequestDispatcher("/WEB-INF/views/reports/principal_departments.jsp")
                       .forward(request, response);
            } else if (pathInfo.equals("/institution-trend")) {
                // Show institution-wide attendance trend
                String academicYear = request.getParameter("academicYear");
                
                // Use default value if not provided
                if (academicYear == null || academicYear.isEmpty()) {
                    academicYear = getCurrentAcademicYear();
                }
                
                // Get monthly trend
                Map<String, Double> monthlyTrend = 
                        attendanceDao.getMonthlyAttendanceTrend(academicYear);
                
                // Get semester-wise trend
                Map<String, Double> semesterTrend = 
                        attendanceDao.getSemesterAttendanceTrend(academicYear);
                
                // Get department-wise trend
                List<Department> departments = departmentDao.findAll();
                Map<Integer, Map<String, Double>> departmentMonthlyTrend = new HashMap<>();
                
                for (Department dept : departments) {
                    Map<String, Double> deptTrend = 
                            attendanceDao.getMonthlyDepartmentAttendanceTrend(
                                    dept.getDepartmentId(), academicYear);
                    departmentMonthlyTrend.put(dept.getDepartmentId(), deptTrend);
                }
                
                request.setAttribute("academicYear", academicYear);
                request.setAttribute("monthlyTrend", monthlyTrend);
                request.setAttribute("semesterTrend", semesterTrend);
                request.setAttribute("departments", departments);
                request.setAttribute("departmentMonthlyTrend", departmentMonthlyTrend);
                request.getRequestDispatcher("/WEB-INF/views/reports/principal_institution_trend.jsp")
                       .forward(request, response);
            } else if (pathInfo.equals("/top-performers")) {
                // Show top performing classes, subjects, and departments
                String academicYear = request.getParameter("academicYear");
                String semester = request.getParameter("semester");
                
                // Use default values if not provided
                if (academicYear == null || academicYear.isEmpty()) {
                    academicYear = getCurrentAcademicYear();
                }
                if (semester == null || semester.isEmpty()) {
                    semester = getCurrentSemester();
                }
                
                // Get top 5 classes by attendance
                List<com.attendance.models.Class> allClasses = classDao.findAll();
                Map<Integer, Double> classAttendance = new HashMap<>();
                
                for (com.attendance.models.Class classObj : allClasses) {
                    double percentage = 
                            attendanceDao.calculateClassAttendancePercentage(
                                    classObj.getClassId(),
                                    academicYear,
                                    semester,
                                    null  // all months
                            );
                    classAttendance.put(classObj.getClassId(), percentage);
                }
                
                // Sort classes by attendance percentage (descending)
                List<Map.Entry<Integer, Double>> sortedClasses = new ArrayList<>(classAttendance.entrySet());
                sortedClasses.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
                
                List<com.attendance.models.Class> topClasses = new ArrayList<>();
                List<Double> topClassPercentages = new ArrayList<>();
                
                int count = 0;
                for (Map.Entry<Integer, Double> entry : sortedClasses) {
                    if (count < 5) {  // Top 5
                        topClasses.add(classDao.findById(entry.getKey()));
                        topClassPercentages.add(entry.getValue());
                        count++;
                    } else {
                        break;
                    }
                }
                
                // Get top 5 subjects by attendance
                List<Subject> allSubjects = subjectDao.findAll();
                Map<String, Double> subjectAttendance = new HashMap<>();
                
                for (Subject subject : allSubjects) {
                    double percentage = 
                            attendanceDao.calculateSubjectOverallAttendancePercentage(
                                    subject.getSubjectCode(),
                                    academicYear,
                                    semester
                            );
                    subjectAttendance.put(subject.getSubjectCode(), percentage);
                }
                
                // Sort subjects by attendance percentage (descending)
                List<Map.Entry<String, Double>> sortedSubjects = new ArrayList<>(subjectAttendance.entrySet());
                sortedSubjects.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
                
                List<Subject> topSubjects = new ArrayList<>();
                List<Double> topSubjectPercentages = new ArrayList<>();
                
                count = 0;
                for (Map.Entry<String, Double> entry : sortedSubjects) {
                    if (count < 5) {  // Top 5
                        topSubjects.add(subjectDao.findByCode(entry.getKey()));
                        topSubjectPercentages.add(entry.getValue());
                        count++;
                    } else {
                        break;
                    }
                }
                
                // Get top 3 departments by attendance
                List<Department> allDepartments = departmentDao.findAll();
                Map<Integer, Double> departmentAttendance = new HashMap<>();
                
                for (Department dept : allDepartments) {
                    double percentage = 
                            attendanceDao.calculateDepartmentAttendancePercentage(
                                    dept.getDepartmentId(),
                                    academicYear,
                                    semester,
                                    null  // all months
                            );
                    departmentAttendance.put(dept.getDepartmentId(), percentage);
                }
                
                // Sort departments by attendance percentage (descending)
                List<Map.Entry<Integer, Double>> sortedDepartments = new ArrayList<>(departmentAttendance.entrySet());
                sortedDepartments.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
                
                List<Department> topDepartments = new ArrayList<>();
                List<Double> topDepartmentPercentages = new ArrayList<>();
                
                count = 0;
                for (Map.Entry<Integer, Double> entry : sortedDepartments) {
                    if (count < 3) {  // Top 3
                        topDepartments.add(departmentDao.findById(entry.getKey()));
                        topDepartmentPercentages.add(entry.getValue());
                        count++;
                    } else {
                        break;
                    }
                }
                
                request.setAttribute("academicYear", academicYear);
                request.setAttribute("semester", semester);
                request.setAttribute("topClasses", topClasses);
                request.setAttribute("topClassPercentages", topClassPercentages);
                request.setAttribute("topSubjects", topSubjects);
                request.setAttribute("topSubjectPercentages", topSubjectPercentages);
                request.setAttribute("topDepartments", topDepartments);
                request.setAttribute("topDepartmentPercentages", topDepartmentPercentages);
                request.getRequestDispatcher("/WEB-INF/views/reports/principal_top_performers.jsp")
                       .forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error processing principal report", e);
            request.setAttribute("error", "Failed to process principal report: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    /**
     * Generate a report based on form input
     */
    private void generateReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String reportType = request.getParameter("reportType");
        
        if ("student".equals(reportType)) {
            // Redirect to student report
            String studentId = request.getParameter("studentId");
            String academicYear = request.getParameter("academicYear");
            String semester = request.getParameter("semester");
            String month = request.getParameter("month");
            String subjectCode = request.getParameter("subjectCode");
            
            StringBuilder url = new StringBuilder(request.getContextPath());
            url.append("/reports/attendance/student?studentId=").append(studentId);
            
            if (academicYear != null && !academicYear.isEmpty()) {
                url.append("&academicYear=").append(academicYear);
            }
            if (semester != null && !semester.isEmpty()) {
                url.append("&semester=").append(semester);
            }
            if (month != null && !month.isEmpty()) {
                url.append("&month=").append(month);
            }
            if (subjectCode != null && !subjectCode.isEmpty()) {
                url.append("&subjectCode=").append(subjectCode);
            }
            
            response.sendRedirect(url.toString());
        } else if ("class".equals(reportType)) {
            // Redirect to class report
            String classId = request.getParameter("classId");
            String academicYear = request.getParameter("academicYear");
            String semester = request.getParameter("semester");
            String month = request.getParameter("month");
            String date = request.getParameter("date");
            String subjectCode = request.getParameter("subjectCode");
            
            StringBuilder url = new StringBuilder(request.getContextPath());
            url.append("/reports/attendance/class?classId=").append(classId);
            
            if (academicYear != null && !academicYear.isEmpty()) {
                url.append("&academicYear=").append(academicYear);
            }
            if (semester != null && !semester.isEmpty()) {
                url.append("&semester=").append(semester);
            }
            if (month != null && !month.isEmpty()) {
                url.append("&month=").append(month);
            }
            if (date != null && !date.isEmpty()) {
                url.append("&date=").append(date);
            }
            if (subjectCode != null && !subjectCode.isEmpty()) {
                url.append("&subjectCode=").append(subjectCode);
            }
            
            response.sendRedirect(url.toString());
        } else if ("subject".equals(reportType)) {
            // Redirect to subject report
            String subjectCode = request.getParameter("subjectCode");
            String academicYear = request.getParameter("academicYear");
            String semester = request.getParameter("semester");
            String month = request.getParameter("month");
            String classId = request.getParameter("classId");
            String date = request.getParameter("date");
            
            StringBuilder url = new StringBuilder(request.getContextPath());
            url.append("/reports/attendance/subject?subjectCode=").append(subjectCode);
            
            if (academicYear != null && !academicYear.isEmpty()) {
                url.append("&academicYear=").append(academicYear);
            }
            if (semester != null && !semester.isEmpty()) {
                url.append("&semester=").append(semester);
            }
            if (month != null && !month.isEmpty()) {
                url.append("&month=").append(month);
            }
            if (classId != null && !classId.isEmpty()) {
                url.append("&classId=").append(classId);
            }
            if (date != null && !date.isEmpty()) {
                url.append("&date=").append(date);
            }
            
            response.sendRedirect(url.toString());
        } else if ("department".equals(reportType)) {
            // Redirect to department report
            String departmentId = request.getParameter("departmentId");
            String academicYear = request.getParameter("academicYear");
            String semester = request.getParameter("semester");
            String month = request.getParameter("month");
            
            StringBuilder url = new StringBuilder(request.getContextPath());
            url.append("/reports/attendance/department?departmentId=").append(departmentId);
            
            if (academicYear != null && !academicYear.isEmpty()) {
                url.append("&academicYear=").append(academicYear);
            }
            if (semester != null && !semester.isEmpty()) {
                url.append("&semester=").append(semester);
            }
            if (month != null && !month.isEmpty()) {
                url.append("&month=").append(month);
            }
            
            response.sendRedirect(url.toString());
        } else if ("institution".equals(reportType)) {
            // Redirect to institution report
            String academicYear = request.getParameter("academicYear");
            String semester = request.getParameter("semester");
            String month = request.getParameter("month");
            
            StringBuilder url = new StringBuilder(request.getContextPath());
            url.append("/reports/attendance/institution");
            
            boolean hasParam = false;
            if (academicYear != null && !academicYear.isEmpty()) {
                url.append("?academicYear=").append(academicYear);
                hasParam = true;
            }
            if (semester != null && !semester.isEmpty()) {
                url.append(hasParam ? "&" : "?").append("semester=").append(semester);
                hasParam = true;
            }
            if (month != null && !month.isEmpty()) {
                url.append(hasParam ? "&" : "?").append("month=").append(month);
            }
            
            response.sendRedirect(url.toString());
        } else {
            // Invalid report type
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid report type");
        }
    }
    
    /**
     * Export a report as CSV
     */
    private void exportReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Implementation for exporting reports as CSV
        // This would involve generating CSV data based on the report type and parameters
        // and setting appropriate response headers for file download
        
        String reportType = request.getParameter("reportType");
        String format = request.getParameter("format"); // CSV, PDF, etc.
        
        // Set response headers for file download
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", 
                "attachment; filename=\"" + reportType + "_report.csv\"");
        
        // Generate and write CSV data
        // This is just a placeholder implementation
        response.getWriter().println("Report Type: " + reportType);
        response.getWriter().println("This feature is not yet implemented");
    }
    
    /**
     * Email a report to the specified recipients
     */
    private void emailReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Implementation for emailing reports
        // This would involve generating the report data and using EmailNotificationService
        // to send it to the specified recipients
        
        String reportType = request.getParameter("reportType");
        String recipients = request.getParameter("recipients");
        String subject = request.getParameter("subject");
        String message = request.getParameter("message");
        
        if (recipients == null || recipients.isEmpty()) {
            request.setAttribute("error", "Recipients are required");
            request.getRequestDispatcher("/WEB-INF/views/reports/email_form.jsp").forward(request, response);
            return;
        }
        
        // Generate report data
        // This is just a placeholder implementation
        String reportData = "Report Type: " + reportType + "\n\n";
        reportData += "This feature is not yet implemented";
        
        // Send email
        EmailNotificationService emailService = EmailNotificationService.getInstance();
        boolean sent = emailService.sendEmail(recipients, subject, message + "\n\n" + reportData);
        
        if (sent) {
            request.setAttribute("success", "Report has been emailed successfully");
        } else {
            request.setAttribute("error", "Failed to email report. Please try again later.");
        }
        
        request.getRequestDispatcher("/WEB-INF/views/reports/email_result.jsp").forward(request, response);
    }
    
    /* Helper methods */
    
    /**
     * Get the current academic year
     */
    private String getCurrentAcademicYear() {
        return String.valueOf(java.time.Year.now().getValue());
    }
    
    /**
     * Get the current semester based on the current month
     */
    private String getCurrentSemester() {
        int month = java.time.LocalDate.now().getMonthValue();
        
        // This is a simplified logic; adapt to your institution's semester schedule
        if (month >= 7 && month <= 12) {
            return "Odd"; // First semester (July-December)
        } else {
            return "Even"; // Second semester (January-June)
        }
    }
    
    /**
     * Get the current month name
     */
    private String getCurrentMonth() {
        return java.time.LocalDate.now().getMonth().toString();
    }
}