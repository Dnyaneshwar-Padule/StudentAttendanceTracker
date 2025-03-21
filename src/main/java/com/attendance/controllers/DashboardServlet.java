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
 * Servlet for handling dashboard requests for different user roles
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private UserDAO userDAO = new UserDAO();
    private DepartmentDAO departmentDAO = new DepartmentDAO();
    private ClassDAO classDAO = new ClassDAO();
    private EnrollmentRequestDAO enrollmentRequestDAO = new EnrollmentRequestDAO();
    private StudentEnrollmentDAO studentEnrollmentDAO = new StudentEnrollmentDAO();
    private SubjectDAO subjectDAO = new SubjectDAO();
    private TeacherAssignmentDAO teacherAssignmentDAO = new TeacherAssignmentDAO();
    private AttendanceDAO attendanceDAO = new AttendanceDAO();
    
    /**
     * Handles the HTTP GET request - displays the appropriate dashboard based on user role
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is logged in
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // Get current user from session
        User user = SessionUtil.getUser(request);
        String role = user.getRole();
        
        // Prepare dashboard data based on user role
        switch (role) {
            case "Student":
                prepareStudentDashboard(request, user);
                request.getRequestDispatcher("/views/student/dashboard.jsp").forward(request, response);
                break;
                
            case "Teacher":
                prepareTeacherDashboard(request, user);
                request.getRequestDispatcher("/views/teacher/dashboard.jsp").forward(request, response);
                break;
                
            case "Class Teacher":
                prepareClassTeacherDashboard(request, user);
                request.getRequestDispatcher("/views/classteacher/dashboard.jsp").forward(request, response);
                break;
                
            case "HOD":
                prepareHODDashboard(request, user);
                request.getRequestDispatcher("/views/hod/dashboard.jsp").forward(request, response);
                break;
                
            case "Principal":
                preparePrincipalDashboard(request, user);
                request.getRequestDispatcher("/views/principal/dashboard.jsp").forward(request, response);
                break;
                
            default:
                response.sendRedirect(request.getContextPath() + "/login");
                break;
        }
    }
    
    /**
     * Prepares data for the student dashboard
     */
    private void prepareStudentDashboard(HttpServletRequest request, User student) {
        // Get student enrollment details
        StudentEnrollment enrollment = studentEnrollmentDAO.getEnrollmentByUserId(student.getUserId());
        request.setAttribute("enrollment", enrollment);
        
        if (enrollment != null) {
            // Get class and department details
            Class classObj = classDAO.getClassById(enrollment.getClassId());
            request.setAttribute("classObj", classObj);
            
            Department department = departmentDAO.getDepartmentById(classObj.getDepartmentId());
            request.setAttribute("department", department);
            
            // Calculate semester based on class name
            String semester = getSemesterFromClass(classObj.getClassName());
            request.setAttribute("semester", semester);
            
            // Get subjects for this class
            List<Subject> subjects = subjectDAO.getSubjectsByDepartmentAndClass(
                classObj.getDepartmentId(), classObj.getClassId());
            request.setAttribute("subjects", subjects);
            
            // Get attendance statistics
            for (Subject subject : subjects) {
                double attendancePercentage = attendanceDAO.getOverallAttendancePercentage(
                    student.getUserId(), semester, enrollment.getAcademicYear());
                request.setAttribute("attendancePercentage", attendancePercentage);
            }
        } else {
            // If student is not enrolled, show enrollment request form
            request.setAttribute("needsEnrollment", true);
            List<Department> departments = departmentDAO.getAllDepartments();
            request.setAttribute("departments", departments);
        }
    }
    
    /**
     * Prepares data for the teacher dashboard
     */
    private void prepareTeacherDashboard(HttpServletRequest request, User teacher) {
        // Get teacher's department
        Department department = departmentDAO.getDepartmentById(teacher.getDepartmentId());
        request.setAttribute("department", department);
        
        // Get teacher's class assignments
        List<TeacherAssignment> assignments = teacherAssignmentDAO.getAssignmentsByTeacher(teacher.getUserId());
        request.setAttribute("assignments", assignments);
        
        // Check if this teacher is a class teacher for any class
        boolean isClassTeacher = false;
        for (TeacherAssignment assignment : assignments) {
            if ("Class Teacher".equals(assignment.getAssignmentType())) {
                isClassTeacher = true;
                break;
            }
        }
        request.setAttribute("isClassTeacher", isClassTeacher);
    }
    
    /**
     * Prepares data for the class teacher dashboard
     */
    private void prepareClassTeacherDashboard(HttpServletRequest request, User classTeacher) {
        // Get class teacher's department
        Department department = departmentDAO.getDepartmentById(classTeacher.getDepartmentId());
        request.setAttribute("department", department);
        
        // Get classes assigned to this class teacher
        List<TeacherAssignment> assignments = teacherAssignmentDAO.getAssignmentsByTeacher(classTeacher.getUserId());
        request.setAttribute("assignments", assignments);
        
        // Filter to find classes where user is assigned as class teacher
        TeacherAssignment classAssignment = null;
        for (TeacherAssignment assignment : assignments) {
            if ("Class Teacher".equals(assignment.getAssignmentType())) {
                classAssignment = assignment;
                break;
            }
        }
        
        if (classAssignment != null) {
            // Get pending enrollment requests for this class teacher's class
            List<EnrollmentRequest> pendingRequests = enrollmentRequestDAO.getPendingRequestsForVerifier(
                "Class Teacher", classTeacher.getDepartmentId());
            request.setAttribute("pendingRequests", pendingRequests);
            
            // Get all students enrolled in this class teacher's class
            List<StudentEnrollment> enrollments = studentEnrollmentDAO.getEnrollmentsByClass(
                classAssignment.getClassId(), null);
            request.setAttribute("enrollments", enrollments);
            
            // Get current class object
            Class classObj = classDAO.getClassById(classAssignment.getClassId());
            request.setAttribute("classObj", classObj);
        }
    }
    
    /**
     * Prepares data for the HOD dashboard
     */
    private void prepareHODDashboard(HttpServletRequest request, User hod) {
        // Get HOD's department
        Department department = departmentDAO.getDepartmentById(hod.getDepartmentId());
        request.setAttribute("department", department);
        
        // Get classes in this department
        List<Class> classes = classDAO.getClassesByDepartment(department.getDepartmentId());
        request.setAttribute("classes", classes);
        
        // Get teachers in this department
        List<User> teachers = userDAO.getUsersByRoleAndDepartment("Teacher", department.getDepartmentId());
        List<User> classTeachers = userDAO.getUsersByRoleAndDepartment("Class Teacher", department.getDepartmentId());
        teachers.addAll(classTeachers);
        request.setAttribute("teachers", teachers);
        
        // Get pending enrollment requests for teachers and class teachers
        List<EnrollmentRequest> pendingRequests = enrollmentRequestDAO.getPendingRequestsForVerifier(
            "HOD", hod.getDepartmentId());
        request.setAttribute("pendingRequests", pendingRequests);
        
        // Get all subjects for this department
        List<Subject> subjects = new ArrayList<>();
        for (Class classObj : classes) {
            List<Subject> classSubjects = subjectDAO.getSubjectsByDepartmentAndClass(
                department.getDepartmentId(), classObj.getClassId());
            
            for (Subject subject : classSubjects) {
                if (!subjects.contains(subject)) {
                    subjects.add(subject);
                }
            }
        }
        request.setAttribute("subjects", subjects);
    }
    
    /**
     * Prepares data for the principal dashboard
     */
    private void preparePrincipalDashboard(HttpServletRequest request, User principal) {
        // Get all departments
        List<Department> departments = departmentDAO.getAllDepartments();
        request.setAttribute("departments", departments);
        
        // Get all HODs
        List<User> hods = userDAO.getUsersByRole("HOD");
        request.setAttribute("hods", hods);
        
        // Get pending enrollment requests for HODs
        List<EnrollmentRequest> pendingRequests = enrollmentRequestDAO.getPendingRequestsForVerifier(
            "Principal", null);
        request.setAttribute("pendingRequests", pendingRequests);
        
        // Get student statistics
        int totalStudents = 0;
        for (Department department : departments) {
            List<StudentEnrollment> enrollments = studentEnrollmentDAO.getEnrollmentsByDepartment(
                department.getDepartmentId(), null);
            totalStudents += enrollments.size();
        }
        request.setAttribute("totalStudents", totalStudents);
        
        // Get teacher statistics
        int totalTeachers = 0;
        int totalClassTeachers = 0;
        for (Department department : departments) {
            List<User> teachers = userDAO.getUsersByRoleAndDepartment("Teacher", department.getDepartmentId());
            totalTeachers += teachers.size();
            
            List<User> classTeachers = userDAO.getUsersByRoleAndDepartment("Class Teacher", department.getDepartmentId());
            totalClassTeachers += classTeachers.size();
        }
        request.setAttribute("totalTeachers", totalTeachers);
        request.setAttribute("totalClassTeachers", totalClassTeachers);
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
}
