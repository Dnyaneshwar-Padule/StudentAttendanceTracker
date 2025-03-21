package com.attendance.controllers;

import com.attendance.dao.*;
import com.attendance.models.*;
import com.attendance.utils.SessionUtil;

import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for handling attendance operations
 */
@WebServlet("/attendance/*")
public class AttendanceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private AttendanceDAO attendanceDAO = new AttendanceDAO();
    private StudentEnrollmentDAO studentEnrollmentDAO = new StudentEnrollmentDAO();
    private TeacherAssignmentDAO teacherAssignmentDAO = new TeacherAssignmentDAO();
    private UserDAO userDAO = new UserDAO();
    private ClassDAO classDAO = new ClassDAO();
    private SubjectDAO subjectDAO = new SubjectDAO();
    
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
            // Redirect to view attendance by default
            response.sendRedirect(request.getContextPath() + "/attendance/view");
        } else if (pathInfo.equals("/mark")) {
            // Show attendance marking form
            showAttendanceMarkingForm(request, response);
        } else if (pathInfo.equals("/view")) {
            // Show attendance view
            showAttendanceView(request, response);
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
        
        if (pathInfo.equals("/mark")) {
            // Process attendance marking
            markAttendance(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * Show the attendance marking form
     */
    private void showAttendanceMarkingForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        User currentUser = SessionUtil.getUser(request);
        
        // Check if user is a teacher or class teacher
        if (!("Teacher".equals(currentUser.getRole()) || "Class Teacher".equals(currentUser.getRole()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only teachers can mark attendance");
            return;
        }
        
        // Get teacher's assignments
        List<TeacherAssignment> assignments = teacherAssignmentDAO.getAssignmentsByTeacher(currentUser.getUserId());
        request.setAttribute("assignments", assignments);
        
        // Load classes and subjects for selection
        Map<Integer, Class> classes = new HashMap<>();
        Map<String, Subject> subjects = new HashMap<>();
        
        for (TeacherAssignment assignment : assignments) {
            Class classObj = classDAO.getClassById(assignment.getClassId());
            classes.put(classObj.getClassId(), classObj);
            
            Subject subject = subjectDAO.getSubjectByCode(assignment.getSubjectCode());
            subjects.put(subject.getSubjectCode(), subject);
        }
        
        request.setAttribute("classes", classes.values());
        request.setAttribute("subjects", subjects.values());
        
        // Handle form submission parameters
        String classIdStr = request.getParameter("classId");
        String subjectCode = request.getParameter("subjectCode");
        String dateStr = request.getParameter("date");
        String semesterStr = request.getParameter("semester");
        
        if (classIdStr != null && subjectCode != null && dateStr != null && semesterStr != null) {
            try {
                int classId = Integer.parseInt(classIdStr);
                Date date = Date.valueOf(dateStr);
                String semester = semesterStr;
                
                // Get current academic year
                String academicYear = String.valueOf(java.time.Year.now().getValue());
                
                // Get students in the selected class
                List<StudentEnrollment> enrollments = studentEnrollmentDAO.getEnrollmentsByClass(classId, academicYear);
                request.setAttribute("enrollments", enrollments);
                
                // Get existing attendance records
                Map<Integer, Attendance> attendanceMap = attendanceDAO.getAttendanceForBulkUpdate(
                    classId, subjectCode, date, semester, academicYear);
                request.setAttribute("attendanceMap", attendanceMap);
                
                // Set selected values
                request.setAttribute("selectedClassId", classId);
                request.setAttribute("selectedSubject", subjectCode);
                request.setAttribute("selectedDate", dateStr);
                request.setAttribute("selectedSemester", semester);
            } catch (Exception e) {
                request.setAttribute("error", "Invalid parameters provided");
            }
        }
        
        request.getRequestDispatcher("/views/attendance/mark.jsp").forward(request, response);
    }
    
    /**
     * Process the attendance marking form submission
     */
    private void markAttendance(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        User currentUser = SessionUtil.getUser(request);
        
        // Check if user is a teacher or class teacher
        if (!("Teacher".equals(currentUser.getRole()) || "Class Teacher".equals(currentUser.getRole()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only teachers can mark attendance");
            return;
        }
        
        // Get form parameters
        String classIdStr = request.getParameter("classId");
        String subjectCode = request.getParameter("subjectCode");
        String dateStr = request.getParameter("date");
        String semesterStr = request.getParameter("semester");
        
        if (classIdStr == null || subjectCode == null || dateStr == null || semesterStr == null) {
            request.setAttribute("error", "Required parameters are missing");
            showAttendanceMarkingForm(request, response);
            return;
        }
        
        try {
            int classId = Integer.parseInt(classIdStr);
            Date date = Date.valueOf(dateStr);
            String semester = semesterStr;
            
            // Verify teacher is assigned to this class and subject
            boolean isAssigned = false;
            List<TeacherAssignment> assignments = teacherAssignmentDAO.getAssignmentsByTeacher(currentUser.getUserId());
            
            for (TeacherAssignment assignment : assignments) {
                if (assignment.getClassId() == classId && assignment.getSubjectCode().equals(subjectCode)) {
                    isAssigned = true;
                    break;
                }
            }
            
            if (!isAssigned) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not assigned to this class/subject");
                return;
            }
            
            // Get current academic year
            String academicYear = String.valueOf(java.time.Year.now().getValue());
            
            // Get students in the selected class
            List<StudentEnrollment> enrollments = studentEnrollmentDAO.getEnrollmentsByClass(classId, academicYear);
            
            // Process attendance for each student
            int successCount = 0;
            
            for (StudentEnrollment enrollment : enrollments) {
                String statusParam = "status_" + enrollment.getUserId();
                String status = request.getParameter(statusParam);
                
                if (status != null && !status.isEmpty()) {
                    // Check if attendance already exists
                    boolean exists = attendanceDAO.attendanceExists(enrollment.getUserId(), subjectCode, date);
                    
                    if (exists) {
                        // Update existing attendance
                        Attendance attendance = new Attendance();
                        attendance.setStudentId(enrollment.getUserId());
                        attendance.setSubjectCode(subjectCode);
                        attendance.setAttendanceDate(date);
                        attendance.setSemester(semester);
                        attendance.setAcademicYear(academicYear);
                        attendance.setStatus(status);
                        
                        // We need the attendance ID for update
                        List<Attendance> existingAttendance = attendanceDAO.getStudentAttendance(
                            enrollment.getUserId(), subjectCode, date, date);
                        
                        if (!existingAttendance.isEmpty()) {
                            attendance.setAttendanceId(existingAttendance.get(0).getAttendanceId());
                            boolean updated = attendanceDAO.updateAttendance(attendance);
                            if (updated) {
                                successCount++;
                            }
                        }
                    } else {
                        // Create new attendance record
                        Attendance attendance = new Attendance(
                            date, subjectCode, enrollment.getUserId(), semester, academicYear, status);
                        
                        int attendanceId = attendanceDAO.recordAttendance(attendance);
                        if (attendanceId > 0) {
                            successCount++;
                        }
                    }
                }
            }
            
            if (successCount > 0) {
                request.setAttribute("success", "Attendance marked successfully for " + successCount + " students.");
            } else {
                request.setAttribute("error", "No attendance records were updated or created.");
            }
            
            // Set selected values for form redisplay
            request.setAttribute("selectedClassId", classId);
            request.setAttribute("selectedSubject", subjectCode);
            request.setAttribute("selectedDate", dateStr);
            request.setAttribute("selectedSemester", semester);
            
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", "Invalid parameters provided: " + e.getMessage());
        }
        
        showAttendanceMarkingForm(request, response);
    }
    
    /**
     * Show the attendance view page
     */
    private void showAttendanceView(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        User currentUser = SessionUtil.getUser(request);
        String userRole = currentUser.getRole();
        
        // Different views based on user role
        if ("Student".equals(userRole)) {
            showStudentAttendanceView(request, response, currentUser);
        } else if ("Teacher".equals(userRole) || "Class Teacher".equals(userRole)) {
            showTeacherAttendanceView(request, response, currentUser);
        } else if ("HOD".equals(userRole)) {
            showHODAttendanceView(request, response, currentUser);
        } else if ("Principal".equals(userRole)) {
            showPrincipalAttendanceView(request, response, currentUser);
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized role");
        }
    }
    
    /**
     * Show attendance view for students
     */
    private void showStudentAttendanceView(HttpServletRequest request, HttpServletResponse response, User student) 
            throws ServletException, IOException {
        
        // Get student's enrollment
        StudentEnrollment enrollment = studentEnrollmentDAO.getEnrollmentByUserId(student.getUserId());
        
        if (enrollment == null) {
            request.setAttribute("error", "You are not enrolled in any class");
            request.getRequestDispatcher("/views/attendance/view.jsp").forward(request, response);
            return;
        }
        
        // Get class details
        Class classObj = classDAO.getClassById(enrollment.getClassId());
        request.setAttribute("classObj", classObj);
        
        // Calculate semester based on class
        String semester = getSemesterFromClass(classObj.getClassName());
        
        // Handle optional filters
        String subjectCode = request.getParameter("subjectCode");
        String fromDateStr = request.getParameter("fromDate");
        String toDateStr = request.getParameter("toDate");
        
        // Parse dates if provided
        Date fromDate = null;
        Date toDate = null;
        
        if (fromDateStr != null && !fromDateStr.isEmpty()) {
            try {
                fromDate = Date.valueOf(fromDateStr);
            } catch (IllegalArgumentException e) {
                request.setAttribute("error", "Invalid from date format");
            }
        }
        
        if (toDateStr != null && !toDateStr.isEmpty()) {
            try {
                toDate = Date.valueOf(toDateStr);
            } catch (IllegalArgumentException e) {
                request.setAttribute("error", "Invalid to date format");
            }
        }
        
        // Get subjects for this class
        List<Subject> subjects = subjectDAO.getSubjectsByDepartmentAndClass(
            classObj.getDepartmentId(), classObj.getClassId());
        request.setAttribute("subjects", subjects);
        
        // Get attendance data
        List<Attendance> attendanceList = attendanceDAO.getStudentAttendance(
            student.getUserId(), subjectCode, fromDate, toDate);
        request.setAttribute("attendanceList", attendanceList);
        
        // Calculate attendance statistics
        if (subjectCode != null && !subjectCode.isEmpty()) {
            Map<String, Integer> summary = attendanceDAO.getAttendanceSummary(
                student.getUserId(), subjectCode, semester, enrollment.getAcademicYear());
            request.setAttribute("attendanceSummary", summary);
        } else {
            double overallPercentage = attendanceDAO.getOverallAttendancePercentage(
                student.getUserId(), semester, enrollment.getAcademicYear());
            request.setAttribute("overallPercentage", overallPercentage);
        }
        
        request.getRequestDispatcher("/views/attendance/view.jsp").forward(request, response);
    }
    
    /**
     * Show attendance view for teachers
     */
    private void showTeacherAttendanceView(HttpServletRequest request, HttpServletResponse response, User teacher) 
            throws ServletException, IOException {
        
        // Get teacher's assignments
        List<TeacherAssignment> assignments = teacherAssignmentDAO.getAssignmentsByTeacher(teacher.getUserId());
        request.setAttribute("assignments", assignments);
        
        // Load classes and subjects for selection
        Map<Integer, Class> classes = new HashMap<>();
        Map<String, Subject> subjects = new HashMap<>();
        
        for (TeacherAssignment assignment : assignments) {
            Class classObj = classDAO.getClassById(assignment.getClassId());
            classes.put(classObj.getClassId(), classObj);
            
            Subject subject = subjectDAO.getSubjectByCode(assignment.getSubjectCode());
            subjects.put(subject.getSubjectCode(), subject);
        }
        
        request.setAttribute("classes", classes.values());
        request.setAttribute("subjects", subjects.values());
        
        // Handle form filters
        String classIdStr = request.getParameter("classId");
        String subjectCode = request.getParameter("subjectCode");
        String dateStr = request.getParameter("date");
        String semesterStr = request.getParameter("semester");
        
        if (classIdStr != null && subjectCode != null && dateStr != null && semesterStr != null) {
            try {
                int classId = Integer.parseInt(classIdStr);
                Date date = Date.valueOf(dateStr);
                String semester = semesterStr;
                
                // Verify teacher is assigned to this class and subject
                boolean isAssigned = false;
                for (TeacherAssignment assignment : assignments) {
                    if (assignment.getClassId() == classId && assignment.getSubjectCode().equals(subjectCode)) {
                        isAssigned = true;
                        break;
                    }
                }
                
                if (!isAssigned) {
                    request.setAttribute("error", "You are not assigned to this class/subject");
                } else {
                    // Get attendance data for the class
                    List<Attendance> classAttendance = attendanceDAO.getClassAttendance(
                        classId, subjectCode, date, semester);
                    request.setAttribute("classAttendance", classAttendance);
                    
                    // Set selected values
                    request.setAttribute("selectedClassId", classId);
                    request.setAttribute("selectedSubject", subjectCode);
                    request.setAttribute("selectedDate", dateStr);
                    request.setAttribute("selectedSemester", semester);
                }
            } catch (IllegalArgumentException e) {
                request.setAttribute("error", "Invalid parameters provided: " + e.getMessage());
            }
        }
        
        request.getRequestDispatcher("/views/attendance/view.jsp").forward(request, response);
    }
    
    /**
     * Show attendance view for HODs
     */
    private void showHODAttendanceView(HttpServletRequest request, HttpServletResponse response, User hod) 
            throws ServletException, IOException {
        
        // Get department classes
        List<Class> classes = classDAO.getClassesByDepartment(hod.getDepartmentId());
        request.setAttribute("classes", classes);
        
        // Get department subjects
        List<Subject> allSubjects = new ArrayList<>();
        for (Class classObj : classes) {
            List<Subject> classSubjects = subjectDAO.getSubjectsByDepartmentAndClass(
                hod.getDepartmentId(), classObj.getClassId());
            
            for (Subject subject : classSubjects) {
                if (!containsSubject(allSubjects, subject)) {
                    allSubjects.add(subject);
                }
            }
        }
        request.setAttribute("subjects", allSubjects);
        
        // Handle form filters
        String classIdStr = request.getParameter("classId");
        String subjectCode = request.getParameter("subjectCode");
        String dateStr = request.getParameter("date");
        String semesterStr = request.getParameter("semester");
        
        if (classIdStr != null && subjectCode != null && dateStr != null && semesterStr != null) {
            try {
                int classId = Integer.parseInt(classIdStr);
                Date date = Date.valueOf(dateStr);
                String semester = semesterStr;
                
                // Verify class belongs to HOD's department
                boolean classInDepartment = false;
                for (Class classObj : classes) {
                    if (classObj.getClassId() == classId) {
                        classInDepartment = true;
                        break;
                    }
                }
                
                if (!classInDepartment) {
                    request.setAttribute("error", "Selected class is not in your department");
                } else {
                    // Get attendance data for the class
                    List<Attendance> classAttendance = attendanceDAO.getClassAttendance(
                        classId, subjectCode, date, semester);
                    request.setAttribute("classAttendance", classAttendance);
                    
                    // Set selected values
                    request.setAttribute("selectedClassId", classId);
                    request.setAttribute("selectedSubject", subjectCode);
                    request.setAttribute("selectedDate", dateStr);
                    request.setAttribute("selectedSemester", semester);
                }
            } catch (IllegalArgumentException e) {
                request.setAttribute("error", "Invalid parameters provided: " + e.getMessage());
            }
        }
        
        request.getRequestDispatcher("/views/attendance/view.jsp").forward(request, response);
    }
    
    /**
     * Show attendance view for Principal
     */
    private void showPrincipalAttendanceView(HttpServletRequest request, HttpServletResponse response, User principal) 
            throws ServletException, IOException {
        
        // Get all departments
        List<Department> departments = new DepartmentDAO().getAllDepartments();
        request.setAttribute("departments", departments);
        
        // Handle department selection
        String departmentIdStr = request.getParameter("departmentId");
        if (departmentIdStr != null && !departmentIdStr.isEmpty()) {
            try {
                int departmentId = Integer.parseInt(departmentIdStr);
                
                // Get classes for this department
                List<Class> classes = classDAO.getClassesByDepartment(departmentId);
                request.setAttribute("classes", classes);
                
                // Set selected department
                request.setAttribute("selectedDepartmentId", departmentId);
                
                // Handle class selection
                String classIdStr = request.getParameter("classId");
                if (classIdStr != null && !classIdStr.isEmpty()) {
                    try {
                        int classId = Integer.parseInt(classIdStr);
                        
                        // Get subjects for this class
                        List<Subject> subjects = subjectDAO.getSubjectsByDepartmentAndClass(departmentId, classId);
                        request.setAttribute("subjects", subjects);
                        
                        // Set selected class
                        request.setAttribute("selectedClassId", classId);
                        
                        // Handle subject and date selection
                        String subjectCode = request.getParameter("subjectCode");
                        String dateStr = request.getParameter("date");
                        String semesterStr = request.getParameter("semester");
                        
                        if (subjectCode != null && dateStr != null && semesterStr != null) {
                            try {
                                Date date = Date.valueOf(dateStr);
                                String semester = semesterStr;
                                
                                // Get attendance data for the class
                                List<Attendance> classAttendance = attendanceDAO.getClassAttendance(
                                    classId, subjectCode, date, semester);
                                request.setAttribute("classAttendance", classAttendance);
                                
                                // Set selected values
                                request.setAttribute("selectedSubject", subjectCode);
                                request.setAttribute("selectedDate", dateStr);
                                request.setAttribute("selectedSemester", semester);
                            } catch (IllegalArgumentException e) {
                                request.setAttribute("error", "Invalid date format");
                            }
                        }
                    } catch (NumberFormatException e) {
                        request.setAttribute("error", "Invalid class ID");
                    }
                }
            } catch (NumberFormatException e) {
                request.setAttribute("error", "Invalid department ID");
            }
        }
        
        request.getRequestDispatcher("/views/attendance/view.jsp").forward(request, response);
    }
    
    /**
     * Helper method to calculate semester from class name
     */
    private String getSemesterFromClass(String className) {
        switch (className) {
            case "FY":
                return "1"; // Default to first semester of FY
            case "SY":
                return "3"; // Default to first semester of SY
            case "TY":
                return "5"; // Default to first semester of TY
            default:
                return "1";
        }
    }
    
    /**
     * Helper method to check if a subject already exists in a list
     */
    private boolean containsSubject(List<Subject> subjects, Subject subject) {
        for (Subject s : subjects) {
            if (s.getSubjectCode().equals(subject.getSubjectCode())) {
                return true;
            }
        }
        return false;
    }
}
