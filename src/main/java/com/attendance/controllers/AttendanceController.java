package com.attendance.controllers;

import com.attendance.dao.AttendanceDao;
import com.attendance.dao.UserDao;
import com.attendance.dao.impl.AttendanceDaoImpl;
import com.attendance.dao.impl.UserDaoImpl;
import com.attendance.models.Attendance;
import com.attendance.models.User;
import com.attendance.utils.EmailNotificationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for attendance management
 */
@WebServlet(urlPatterns = {
    "/attendance/mark", 
    "/attendance/view",
    "/attendance/edit",
    "/attendance/report",
    "/attendance/dashboard"
})
public class AttendanceController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AttendanceController.class.getName());
    
    private final AttendanceDao attendanceDAO = new AttendanceDaoImpl();
    private final UserDao userDAO = new UserDaoImpl();
    private final EmailNotificationService emailService = EmailNotificationService.getInstance();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getServletPath();
        
        switch (path) {
            case "/attendance/mark":
                // Show attendance marking page
                showMarkAttendancePage(request, response);
                break;
                
            case "/attendance/view":
                // Show attendance view page
                showViewAttendancePage(request, response);
                break;
                
            case "/attendance/edit":
                // Show attendance edit page
                showEditAttendancePage(request, response);
                break;
                
            case "/attendance/report":
                // Show attendance report page
                showAttendanceReportPage(request, response);
                break;
                
            case "/attendance/dashboard":
                // Show attendance dashboard
                showAttendanceDashboard(request, response);
                break;
                
            default:
                response.sendRedirect(request.getContextPath() + "/dashboard");
                break;
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getServletPath();
        
        switch (path) {
            case "/attendance/mark":
                // Handle attendance marking
                handleMarkAttendance(request, response);
                break;
                
            case "/attendance/edit":
                // Handle attendance editing
                handleEditAttendance(request, response);
                break;
                
            default:
                response.sendRedirect(request.getContextPath() + "/dashboard");
                break;
        }
    }
    
    /**
     * Show the attendance marking page
     */
    private void showMarkAttendancePage(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        // Only teachers can mark attendance
        if (!user.isTeacher() && !user.isHOD() && !user.isPrincipal() && !user.isAdmin()) {
            request.setAttribute("errorMessage", "You don't have permission to mark attendance");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            return;
        }
        
        try {
            // Get student list for a class
            // For demonstration, we'll get all students
            List<User> students = userDAO.getByRole("Student");
            request.setAttribute("students", students);
            
            // Set default date to today
            request.setAttribute("currentDate", LocalDate.now().toString());
            
            request.getRequestDispatcher("/WEB-INF/views/attendance/mark.jsp").forward(request, response);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading mark attendance page", e);
            request.setAttribute("errorMessage", "An error occurred: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    /**
     * Handle attendance marking
     */
    private void handleMarkAttendance(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        String subjectCode = request.getParameter("subjectCode");
        String dateStr = request.getParameter("date");
        String semester = request.getParameter("semester");
        String academicYear = request.getParameter("academicYear");
        String[] selectedStudents = request.getParameterValues("selectedStudents");
        String status = request.getParameter("status");
        
        if (subjectCode == null || subjectCode.isEmpty() || 
            dateStr == null || dateStr.isEmpty() || 
            semester == null || semester.isEmpty() || 
            academicYear == null || academicYear.isEmpty() || 
            selectedStudents == null || selectedStudents.length == 0 || 
            status == null || status.isEmpty()) {
            
            request.setAttribute("errorMessage", "All fields are required");
            showMarkAttendancePage(request, response);
            return;
        }
        
        try {
            Date attendanceDate = Date.valueOf(dateStr);
            List<Integer> studentIds = new ArrayList<>();
            
            for (String studentId : selectedStudents) {
                studentIds.add(Integer.parseInt(studentId));
            }
            
            // Mark attendance for selected students
            int count = attendanceDAO.markAttendanceBulk(studentIds, subjectCode, attendanceDate, 
                                                      status, semester, academicYear, user.getFullName());
            
            // Send email notifications
            for (Integer studentId : studentIds) {
                User student = userDAO.getById(studentId);
                
                if (student != null) {
                    Attendance attendance = new Attendance();
                    attendance.setStudentId(studentId);
                    attendance.setSubjectCode(subjectCode);
                    attendance.setAttendanceDate(attendanceDate);
                    attendance.setStatus(status);
                    attendance.setSemester(semester);
                    attendance.setAcademicYear(academicYear);
                    
                    emailService.sendAttendanceNotification(student, attendance, subjectCode);
                }
            }
            
            request.setAttribute("successMessage", count + " attendance records marked successfully");
            showMarkAttendancePage(request, response);
            
            LOGGER.info("Attendance marked for " + count + " students in " + subjectCode);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error marking attendance", e);
            request.setAttribute("errorMessage", "An error occurred: " + e.getMessage());
            showMarkAttendancePage(request, response);
        }
    }
    
    /**
     * Show the attendance view page
     */
    private void showViewAttendancePage(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        String studentId = request.getParameter("studentId");
        String subjectCode = request.getParameter("subjectCode");
        String dateStr = request.getParameter("date");
        
        try {
            List<Attendance> attendanceList;
            
            if (user.isStudent()) {
                // Students can only view their own attendance
                attendanceList = attendanceDAO.getByStudent(user.getUserId());
            } else if (studentId != null && !studentId.isEmpty()) {
                // View attendance for a specific student
                attendanceList = attendanceDAO.getByStudent(Integer.parseInt(studentId));
            } else if (subjectCode != null && !subjectCode.isEmpty()) {
                // View attendance for a specific subject
                attendanceList = attendanceDAO.getBySubject(subjectCode);
            } else if (dateStr != null && !dateStr.isEmpty()) {
                // View attendance for a specific date
                Date date = Date.valueOf(dateStr);
                attendanceList = attendanceDAO.getByDate(date);
            } else {
                // Default: show recent attendance records
                attendanceList = attendanceDAO.getAll();
                
                // Limit to the most recent 100 records
                if (attendanceList.size() > 100) {
                    attendanceList = attendanceList.subList(0, 100);
                }
            }
            
            request.setAttribute("attendanceList", attendanceList);
            request.getRequestDispatcher("/WEB-INF/views/attendance/view.jsp").forward(request, response);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error viewing attendance", e);
            request.setAttribute("errorMessage", "An error occurred: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    /**
     * Show the attendance edit page
     */
    private void showEditAttendancePage(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        // Only authorized users can edit attendance
        if (user.isStudent()) {
            request.setAttribute("errorMessage", "You don't have permission to edit attendance");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            return;
        }
        
        String attendanceId = request.getParameter("id");
        
        if (attendanceId == null || attendanceId.isEmpty()) {
            request.setAttribute("errorMessage", "Attendance ID is required");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            return;
        }
        
        try {
            Attendance attendance = attendanceDAO.getById(Integer.parseInt(attendanceId));
            
            if (attendance != null) {
                request.setAttribute("attendance", attendance);
                request.getRequestDispatcher("/WEB-INF/views/attendance/edit.jsp").forward(request, response);
            } else {
                request.setAttribute("errorMessage", "Attendance record not found");
                request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading attendance edit page", e);
            request.setAttribute("errorMessage", "An error occurred: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    /**
     * Handle attendance editing
     */
    private void handleEditAttendance(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        String attendanceId = request.getParameter("attendanceId");
        String status = request.getParameter("status");
        String remarks = request.getParameter("remarks");
        
        if (attendanceId == null || attendanceId.isEmpty() || status == null || status.isEmpty()) {
            request.setAttribute("errorMessage", "Attendance ID and status are required");
            response.sendRedirect(request.getContextPath() + "/attendance/view");
            return;
        }
        
        try {
            Attendance attendance = attendanceDAO.getById(Integer.parseInt(attendanceId));
            
            if (attendance != null) {
                attendance.setStatus(status);
                attendance.setRemarks(remarks);
                attendance.setMarkedBy(user.getFullName());
                
                attendanceDAO.update(attendance);
                
                // Get the student and send notification
                User student = userDAO.getById(attendance.getStudentId());
                
                if (student != null) {
                    emailService.sendAttendanceNotification(student, attendance, attendance.getSubjectCode());
                }
                
                request.setAttribute("successMessage", "Attendance record updated successfully");
                response.sendRedirect(request.getContextPath() + "/attendance/view");
                
                LOGGER.info("Attendance record updated: " + attendanceId);
                
            } else {
                request.setAttribute("errorMessage", "Attendance record not found");
                response.sendRedirect(request.getContextPath() + "/attendance/view");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating attendance", e);
            request.setAttribute("errorMessage", "An error occurred: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/attendance/view");
        }
    }
    
    /**
     * Show the attendance report page
     */
    private void showAttendanceReportPage(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        String studentId = request.getParameter("studentId");
        String subjectCode = request.getParameter("subjectCode");
        String semester = request.getParameter("semester");
        String academicYear = request.getParameter("academicYear");
        
        try {
            Map<String, Object> reportData = new HashMap<>();
            
            if (user.isStudent()) {
                // Students can only view their own reports
                studentId = String.valueOf(user.getUserId());
            }
            
            if (studentId != null && !studentId.isEmpty() && 
                subjectCode != null && !subjectCode.isEmpty() && 
                semester != null && !semester.isEmpty() && 
                academicYear != null && !academicYear.isEmpty()) {
                
                // Generate detailed report for a student in a specific subject
                int studentIdInt = Integer.parseInt(studentId);
                List<Attendance> attendanceList = attendanceDAO.getByStudentSubjectAndSemester(
                        studentIdInt, subjectCode, semester);
                
                User student = userDAO.getById(studentIdInt);
                
                if (student != null) {
                    double percentage = attendanceDAO.getAttendancePercentage(
                            studentIdInt, subjectCode, semester, academicYear);
                    
                    reportData.put("student", student);
                    reportData.put("attendanceList", attendanceList);
                    reportData.put("subjectCode", subjectCode);
                    reportData.put("percentage", percentage);
                    reportData.put("semester", semester);
                    reportData.put("academicYear", academicYear);
                    
                    request.setAttribute("reportType", "student-subject");
                }
                
            } else if (studentId != null && !studentId.isEmpty() && 
                      semester != null && !semester.isEmpty() && 
                      academicYear != null && !academicYear.isEmpty()) {
                
                // Generate semester report for a student
                int studentIdInt = Integer.parseInt(studentId);
                List<Attendance> attendanceList = attendanceDAO.getByStudentSemesterAndYear(
                        studentIdInt, semester, academicYear);
                
                User student = userDAO.getById(studentIdInt);
                
                if (student != null) {
                    reportData.put("student", student);
                    reportData.put("attendanceList", attendanceList);
                    reportData.put("semester", semester);
                    reportData.put("academicYear", academicYear);
                    
                    request.setAttribute("reportType", "student-semester");
                }
                
            } else if (subjectCode != null && !subjectCode.isEmpty()) {
                
                // Generate report for a subject
                List<Attendance> attendanceList = attendanceDAO.getBySubject(subjectCode);
                
                reportData.put("attendanceList", attendanceList);
                reportData.put("subjectCode", subjectCode);
                
                request.setAttribute("reportType", "subject");
                
            } else {
                // Default report view
                request.setAttribute("reportType", "default");
            }
            
            request.setAttribute("reportData", reportData);
            request.getRequestDispatcher("/WEB-INF/views/attendance/report.jsp").forward(request, response);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating attendance report", e);
            request.setAttribute("errorMessage", "An error occurred: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    /**
     * Show the attendance dashboard
     */
    private void showAttendanceDashboard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        try {
            // Common statistics for all users
            Map<String, Object> dashboardData = new HashMap<>();
            
            if (user.isStudent()) {
                // Student dashboard
                int studentId = user.getUserId();
                List<Attendance> recentAttendance = attendanceDAO.getByStudent(studentId);
                
                // Limit to the most recent 10 records
                if (recentAttendance.size() > 10) {
                    recentAttendance = recentAttendance.subList(0, 10);
                }
                
                dashboardData.put("recentAttendance", recentAttendance);
                
                // Get attendance statistics by status
                long presentCount = recentAttendance.stream().filter(Attendance::isPresent).count();
                long absentCount = recentAttendance.stream().filter(Attendance::isAbsent).count();
                long leaveCount = recentAttendance.stream().filter(Attendance::isOnLeave).count();
                
                dashboardData.put("presentCount", presentCount);
                dashboardData.put("absentCount", absentCount);
                dashboardData.put("leaveCount", leaveCount);
                
                // Calculate overall percentage
                double percentage = 0;
                if (!recentAttendance.isEmpty()) {
                    percentage = (double) presentCount / recentAttendance.size() * 100;
                }
                dashboardData.put("overallPercentage", percentage);
                
                request.setAttribute("dashboardType", "student");
                
            } else {
                // Teacher/HOD/Principal/Admin dashboard
                List<Attendance> recentAttendance = attendanceDAO.getAll();
                
                // Limit to the most recent 20 records
                if (recentAttendance.size() > 20) {
                    recentAttendance = recentAttendance.subList(0, 20);
                }
                
                dashboardData.put("recentAttendance", recentAttendance);
                
                // Get counts by status
                long presentCount = recentAttendance.stream().filter(Attendance::isPresent).count();
                long absentCount = recentAttendance.stream().filter(Attendance::isAbsent).count();
                long leaveCount = recentAttendance.stream().filter(Attendance::isOnLeave).count();
                
                dashboardData.put("presentCount", presentCount);
                dashboardData.put("absentCount", absentCount);
                dashboardData.put("leaveCount", leaveCount);
                
                // Get total student count for stats
                List<User> students = userDAO.getByRole("Student");
                dashboardData.put("totalStudents", students.size());
                
                request.setAttribute("dashboardType", "staff");
            }
            
            request.setAttribute("dashboardData", dashboardData);
            request.getRequestDispatcher("/WEB-INF/views/attendance/dashboard.jsp").forward(request, response);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading attendance dashboard", e);
            request.setAttribute("errorMessage", "An error occurred: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
}