package com.attendance.controllers;

import com.attendance.dao.*;
import com.attendance.dao.impl.*;
import com.attendance.models.*;
import com.attendance.utils.DateUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for student-related operations
 */
@WebServlet(name = "StudentController", urlPatterns = {
    "/student/*",
    "/admin/students/*",
    "/teacher/students/*",
    "/hod/students/*"
})
public class StudentController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(StudentController.class.getName());
    
    private UserDao userDao;
    private StudentEnrollmentDao studentEnrollmentDao;
    private ClassDao classDao;
    private DepartmentDao departmentDao;
    private DepartmentSubjectDao departmentSubjectDao;
    private SubjectDao subjectDao;
    private AttendanceDao attendanceDao;
    
    @Override
    public void init() throws ServletException {
        super.init();
        userDao = new UserDaoImpl();
        studentEnrollmentDao = new StudentEnrollmentDaoImpl();
        classDao = new ClassDaoImpl();
        departmentDao = new DepartmentDaoImpl();
        departmentSubjectDao = new DepartmentSubjectDaoImpl();
        subjectDao = new SubjectDaoImpl();
        attendanceDao = new AttendanceDaoImpl();
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
        
        if (servletPath.equals("/student")) {
            if ("Student".equals(user.getRole())) {
                if (pathInfo == null || pathInfo.equals("/")) {
                    // Student dashboard (already handled by DashboardController)
                    response.sendRedirect(request.getContextPath() + "/dashboard");
                } else if (pathInfo.equals("/profile")) {
                    // Student profile view
                    showStudentProfile(request, response, user);
                } else if (pathInfo.equals("/enrollment")) {
                    // Student enrollment view
                    showStudentEnrollment(request, response, user);
                } else if (pathInfo.equals("/attendance")) {
                    // Student attendance view
                    showStudentAttendance(request, response, user);
                } else if (pathInfo.equals("/subjects")) {
                    // Student subjects view
                    showStudentSubjects(request, response, user);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only students can access this page");
            }
        } else if (servletPath.equals("/admin/students")) {
            if ("Admin".equals(user.getRole()) || "Principal".equals(user.getRole())) {
                if (pathInfo == null || pathInfo.equals("/")) {
                    // List all students
                    listStudents(request, response);
                } else if (pathInfo.equals("/create")) {
                    // Show student creation form
                    showStudentCreateForm(request, response);
                } else if (pathInfo.startsWith("/edit/")) {
                    // Show student edit form
                    showStudentEditForm(request, response, pathInfo);
                } else if (pathInfo.startsWith("/view/")) {
                    // View student details
                    viewStudentDetails(request, response, pathInfo);
                } else if (pathInfo.equals("/enroll")) {
                    // Show student enrollment form
                    showStudentEnrollmentForm(request, response);
                } else if (pathInfo.startsWith("/attendance/")) {
                    // View student attendance records
                    viewStudentAttendanceRecords(request, response, pathInfo);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have permission to access this page");
            }
        } else if (servletPath.equals("/teacher/students")) {
            if ("Teacher".equals(user.getRole())) {
                if (pathInfo == null || pathInfo.equals("/")) {
                    // List students in teacher's assigned classes
                    listTeacherStudents(request, response, user);
                } else if (pathInfo.startsWith("/view/")) {
                    // View student details
                    viewTeacherStudentDetails(request, response, user, pathInfo);
                } else if (pathInfo.startsWith("/attendance/")) {
                    // View student attendance for teacher's subjects
                    viewTeacherStudentAttendance(request, response, user, pathInfo);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only teachers can access this page");
            }
        } else if (servletPath.equals("/hod/students")) {
            if ("HOD".equals(user.getRole())) {
                if (pathInfo == null || pathInfo.equals("/")) {
                    // List students in HOD's department
                    listHodStudents(request, response, user);
                } else if (pathInfo.startsWith("/view/")) {
                    // View student details
                    viewHodStudentDetails(request, response, user, pathInfo);
                } else if (pathInfo.startsWith("/attendance/")) {
                    // View student attendance records
                    viewHodStudentAttendance(request, response, user, pathInfo);
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
        
        if (servletPath.equals("/student")) {
            if ("Student".equals(user.getRole())) {
                if (pathInfo.equals("/profile/update")) {
                    // Update student profile
                    updateStudentProfile(request, response, user);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only students can access this page");
            }
        } else if (servletPath.equals("/admin/students")) {
            if ("Admin".equals(user.getRole()) || "Principal".equals(user.getRole())) {
                if (pathInfo.equals("/create")) {
                    // Create new student
                    createStudent(request, response);
                } else if (pathInfo.startsWith("/edit/")) {
                    // Update student details
                    updateStudent(request, response, pathInfo);
                } else if (pathInfo.equals("/enroll")) {
                    // Enroll student in a class
                    enrollStudent(request, response);
                } else if (pathInfo.equals("/search")) {
                    // Search students
                    searchStudents(request, response);
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
     * Show student profile view
     */
    private void showStudentProfile(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get current enrollment details
            StudentEnrollment enrollment = studentEnrollmentDao.findCurrentEnrollment(user.getUserId());
            
            if (enrollment != null) {
                // Get class details
                com.attendance.models.Class cls = classDao.findById(enrollment.getClassId());
                
                if (cls != null) {
                    // Get department details
                    Department department = departmentDao.findById(cls.getDepartmentId());
                    request.setAttribute("department", department);
                }
                
                request.setAttribute("class", cls);
                request.setAttribute("enrollment", enrollment);
            }
            
            request.setAttribute("student", user);
            request.getRequestDispatcher("/WEB-INF/views/student/profile.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while showing student profile", e);
            request.setAttribute("error", "Failed to load profile. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/student/profile.jsp").forward(request, response);
        }
    }
    
    /**
     * Show student enrollment view
     */
    private void showStudentEnrollment(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get all enrollments for this student
            List<StudentEnrollment> enrollments = studentEnrollmentDao.findByStudent(user.getUserId());
            
            // Get class details for each enrollment
            Map<Integer, com.attendance.models.Class> classes = new HashMap<>();
            Map<Integer, Department> departments = new HashMap<>();
            
            for (StudentEnrollment enrollment : enrollments) {
                int classId = enrollment.getClassId();
                com.attendance.models.Class cls = classDao.findById(classId);
                
                if (cls != null) {
                    classes.put(classId, cls);
                    
                    // Get department details
                    Department department = departmentDao.findById(cls.getDepartmentId());
                    if (department != null) {
                        departments.put(cls.getDepartmentId(), department);
                    }
                }
            }
            
            request.setAttribute("student", user);
            request.setAttribute("enrollments", enrollments);
            request.setAttribute("classes", classes);
            request.setAttribute("departments", departments);
            
            request.getRequestDispatcher("/WEB-INF/views/student/enrollment.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while showing student enrollment", e);
            request.setAttribute("error", "Failed to load enrollment data. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/student/enrollment.jsp").forward(request, response);
        }
    }
    
    /**
     * Show student attendance view
     */
    private void showStudentAttendance(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get current enrollment
            StudentEnrollment enrollment = studentEnrollmentDao.findCurrentEnrollment(user.getUserId());
            
            if (enrollment == null) {
                request.setAttribute("error", "You are not currently enrolled in any class.");
                request.getRequestDispatcher("/WEB-INF/views/student/attendance.jsp").forward(request, response);
                return;
            }
            
            int classId = enrollment.getClassId();
            String academicYear = enrollment.getAcademicYear();
            String semester = enrollment.getSemester();
            
            // Get class details
            com.attendance.models.Class cls = classDao.findById(classId);
            
            if (cls == null) {
                request.setAttribute("error", "Invalid class enrollment.");
                request.getRequestDispatcher("/WEB-INF/views/student/attendance.jsp").forward(request, response);
                return;
            }
            
            // Get department subjects for this semester
            List<DepartmentSubject> departmentSubjects = departmentSubjectDao.findByDepartmentAndSemester(
                    cls.getDepartmentId(), semester);
            
            // Get attendance records for each subject
            Map<String, List<Attendance>> subjectAttendance = new HashMap<>();
            Map<String, Double> attendancePercentages = new HashMap<>();
            Map<String, Subject> subjects = new HashMap<>();
            
            for (DepartmentSubject deptSubject : departmentSubjects) {
                String subjectCode = deptSubject.getSubjectCode();
                
                // Get subject details
                Subject subject = subjectDao.findByCode(subjectCode);
                if (subject != null) {
                    subjects.put(subjectCode, subject);
                    
                    // Get attendance records
                    List<Attendance> records = attendanceDao.findByStudentSubjectSemesterAndYear(
                            user.getUserId(), subjectCode, semester, academicYear);
                    
                    subjectAttendance.put(subjectCode, records);
                    
                    // Calculate attendance percentage
                    double percentage = attendanceDao.calculateAttendancePercentage(
                            user.getUserId(), subjectCode, semester, academicYear);
                    
                    attendancePercentages.put(subjectCode, percentage);
                }
            }
            
            request.setAttribute("student", user);
            request.setAttribute("class", cls);
            request.setAttribute("enrollment", enrollment);
            request.setAttribute("subjects", subjects);
            request.setAttribute("subjectAttendance", subjectAttendance);
            request.setAttribute("attendancePercentages", attendancePercentages);
            
            request.getRequestDispatcher("/WEB-INF/views/student/attendance.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while showing student attendance", e);
            request.setAttribute("error", "Failed to load attendance data. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/student/attendance.jsp").forward(request, response);
        }
    }
    
    /**
     * Show student subjects view
     */
    private void showStudentSubjects(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get current enrollment
            StudentEnrollment enrollment = studentEnrollmentDao.findCurrentEnrollment(user.getUserId());
            
            if (enrollment == null) {
                request.setAttribute("error", "You are not currently enrolled in any class.");
                request.getRequestDispatcher("/WEB-INF/views/student/subjects.jsp").forward(request, response);
                return;
            }
            
            int classId = enrollment.getClassId();
            String semester = enrollment.getSemester();
            
            // Get class details
            com.attendance.models.Class cls = classDao.findById(classId);
            
            if (cls == null) {
                request.setAttribute("error", "Invalid class enrollment.");
                request.getRequestDispatcher("/WEB-INF/views/student/subjects.jsp").forward(request, response);
                return;
            }
            
            // Get department subjects for this semester
            List<DepartmentSubject> departmentSubjects = departmentSubjectDao.findByDepartmentAndSemester(
                    cls.getDepartmentId(), semester);
            
            // Get subject details
            List<Subject> subjects = new ArrayList<>();
            for (DepartmentSubject deptSubject : departmentSubjects) {
                Subject subject = subjectDao.findByCode(deptSubject.getSubjectCode());
                if (subject != null) {
                    subjects.add(subject);
                }
            }
            
            request.setAttribute("student", user);
            request.setAttribute("class", cls);
            request.setAttribute("enrollment", enrollment);
            request.setAttribute("subjects", subjects);
            
            request.getRequestDispatcher("/WEB-INF/views/student/subjects.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while showing student subjects", e);
            request.setAttribute("error", "Failed to load subject data. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/student/subjects.jsp").forward(request, response);
        }
    }
    
    /**
     * Update student profile
     */
    private void updateStudentProfile(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        
        if (fullName == null || fullName.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            request.setAttribute("error", "Name and email are required fields.");
            showStudentProfile(request, response, user);
            return;
        }
        
        try {
            // Update user details
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhone(phone);
            user.setAddress(address);
            
            User updatedUser = userDao.update(user);
            
            if (updatedUser != null) {
                // Update session attribute
                HttpSession session = request.getSession();
                session.setAttribute("user", updatedUser);
                
                request.setAttribute("success", "Profile updated successfully.");
            } else {
                request.setAttribute("error", "Failed to update profile.");
            }
            
            showStudentProfile(request, response, updatedUser != null ? updatedUser : user);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while updating student profile", e);
            request.setAttribute("error", "A system error occurred. Please try again later.");
            showStudentProfile(request, response, user);
        }
    }
    
    /**
     * List all students (admin view)
     */
    private void listStudents(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Check for filter parameters
            String departmentId = request.getParameter("departmentId");
            String classId = request.getParameter("classId");
            String academicYear = request.getParameter("academicYear");
            String status = request.getParameter("status");
            
            List<User> students;
            
            if (departmentId != null && !departmentId.isEmpty()) {
                // Filter by department
                students = userDao.findStudentsByDepartment(Integer.parseInt(departmentId));
            } else if (classId != null && !classId.isEmpty()) {
                // Filter by class
                students = userDao.findStudentsByClass(Integer.parseInt(classId));
            } else if (academicYear != null && !academicYear.isEmpty()) {
                // Filter by academic year
                students = userDao.findStudentsByAcademicYear(academicYear);
            } else if (status != null && !status.isEmpty()) {
                // Filter by status
                students = userDao.findUsersByRoleAndStatus("Student", status);
            } else {
                // No filters, get all students
                students = userDao.findUsersByRole("Student");
            }
            
            // Get current enrollments for each student
            Map<Integer, StudentEnrollment> enrollments = new HashMap<>();
            Map<Integer, com.attendance.models.Class> classes = new HashMap<>();
            Map<Integer, Department> departments = new HashMap<>();
            
            for (User student : students) {
                StudentEnrollment enrollment = studentEnrollmentDao.findCurrentEnrollment(student.getUserId());
                
                if (enrollment != null) {
                    enrollments.put(student.getUserId(), enrollment);
                    
                    // Get class details
                    com.attendance.models.Class cls = classDao.findById(enrollment.getClassId());
                    if (cls != null) {
                        classes.put(enrollment.getClassId(), cls);
                        
                        // Get department details
                        Department department = departmentDao.findById(cls.getDepartmentId());
                        if (department != null) {
                            departments.put(cls.getDepartmentId(), department);
                        }
                    }
                }
            }
            
            // Get all departments and classes for filter dropdowns
            List<Department> allDepartments = departmentDao.findAll();
            List<com.attendance.models.Class> allClasses = classDao.findAll();
            
            // Get list of academic years for filter dropdown
            List<String> academicYears = new ArrayList<>();
            int currentYear = LocalDate.now().getYear();
            for (int i = 0; i < 5; i++) {
                int startYear = currentYear - i;
                academicYears.add(startYear + "-" + (startYear + 1));
            }
            
            request.setAttribute("students", students);
            request.setAttribute("enrollments", enrollments);
            request.setAttribute("classes", classes);
            request.setAttribute("departments", departments);
            request.setAttribute("allDepartments", allDepartments);
            request.setAttribute("allClasses", allClasses);
            request.setAttribute("academicYears", academicYears);
            
            // Set filter parameters for maintaining state
            request.setAttribute("departmentFilter", departmentId);
            request.setAttribute("classFilter", classId);
            request.setAttribute("academicYearFilter", academicYear);
            request.setAttribute("statusFilter", status);
            
            request.getRequestDispatcher("/WEB-INF/views/admin/students/list.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while listing students", e);
            request.setAttribute("error", "Failed to retrieve students. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/admin/students/list.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid filter parameters.");
            request.getRequestDispatcher("/WEB-INF/views/admin/students/list.jsp").forward(request, response);
        }
    }
    
    /**
     * Show student creation form
     */
    private void showStudentCreateForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Get all departments for dropdown
            List<Department> departments = departmentDao.findAll();
            
            // Get all classes for dropdown
            List<com.attendance.models.Class> classes = classDao.findAll();
            
            // Group classes by department
            Map<Integer, List<com.attendance.models.Class>> classesByDepartment = new HashMap<>();
            
            for (com.attendance.models.Class cls : classes) {
                int departmentId = cls.getDepartmentId();
                
                if (!classesByDepartment.containsKey(departmentId)) {
                    classesByDepartment.put(departmentId, new ArrayList<>());
                }
                
                classesByDepartment.get(departmentId).add(cls);
            }
            
            // Get current academic year
            String currentAcademicYear = DateUtils.getCurrentAcademicYear();
            
            request.setAttribute("departments", departments);
            request.setAttribute("classesByDepartment", classesByDepartment);
            request.setAttribute("currentAcademicYear", currentAcademicYear);
            
            request.getRequestDispatcher("/WEB-INF/views/admin/students/create.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while preparing student create form", e);
            request.setAttribute("error", "Failed to load form. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/admin/students/create.jsp").forward(request, response);
        }
    }
    
    /**
     * Create a new student
     */
    private void createStudent(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String classIdStr = request.getParameter("classId");
        String semester = request.getParameter("semester");
        String academicYear = request.getParameter("academicYear");
        
        // Validate required fields
        if (fullName == null || fullName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            classIdStr == null || classIdStr.trim().isEmpty() ||
            semester == null || semester.trim().isEmpty() ||
            academicYear == null || academicYear.trim().isEmpty()) {
            
            request.setAttribute("error", "All required fields must be filled.");
            showStudentCreateForm(request, response);
            return;
        }
        
        try {
            int classId = Integer.parseInt(classIdStr);
            
            // Check if email already exists
            User existingUser = userDao.findByEmail(email);
            if (existingUser != null) {
                request.setAttribute("error", "Email address is already in use.");
                showStudentCreateForm(request, response);
                return;
            }
            
            // Create new user
            User newStudent = new User();
            newStudent.setFullName(fullName);
            newStudent.setEmail(email);
            newStudent.setPassword(password); // Password will be hashed in the DAO implementation
            newStudent.setPhone(phone);
            newStudent.setAddress(address);
            newStudent.setRole("Student");
            newStudent.setStatus("Active");
            
            User savedStudent = userDao.save(newStudent);
            
            if (savedStudent != null) {
                // Create enrollment record
                StudentEnrollment enrollment = new StudentEnrollment();
                enrollment.setStudentId(savedStudent.getUserId());
                enrollment.setClassId(classId);
                enrollment.setSemester(semester);
                enrollment.setAcademicYear(academicYear);
                enrollment.setEnrollmentDate(new java.sql.Date(System.currentTimeMillis()));
                enrollment.setStatus("Active");
                
                StudentEnrollment savedEnrollment = studentEnrollmentDao.save(enrollment);
                
                if (savedEnrollment != null) {
                    response.sendRedirect(request.getContextPath() + "/admin/students?success=created");
                } else {
                    // Enrollment failed, but user was created
                    response.sendRedirect(request.getContextPath() + "/admin/students?success=partiallyCreated&error=enrollmentFailed");
                }
            } else {
                request.setAttribute("error", "Failed to create student. Please try again.");
                showStudentCreateForm(request, response);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while creating student", e);
            request.setAttribute("error", "A system error occurred. Please try again later.");
            showStudentCreateForm(request, response);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid class ID.");
            showStudentCreateForm(request, response);
        }
    }
    
    /**
     * Show student edit form
     */
    private void showStudentEditForm(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws ServletException, IOException {
        try {
            // Extract user ID from path
            int userId = extractIdFromPath(pathInfo, "/edit/");
            
            // Get student details
            User student = userDao.findById(userId);
            
            if (student == null || !"Student".equals(student.getRole())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Student not found");
                return;
            }
            
            // Get current enrollment
            StudentEnrollment enrollment = studentEnrollmentDao.findCurrentEnrollment(userId);
            
            // Get all departments for dropdown
            List<Department> departments = departmentDao.findAll();
            
            // Get all classes for dropdown
            List<com.attendance.models.Class> classes = classDao.findAll();
            
            // Group classes by department
            Map<Integer, List<com.attendance.models.Class>> classesByDepartment = new HashMap<>();
            
            for (com.attendance.models.Class cls : classes) {
                int departmentId = cls.getDepartmentId();
                
                if (!classesByDepartment.containsKey(departmentId)) {
                    classesByDepartment.put(departmentId, new ArrayList<>());
                }
                
                classesByDepartment.get(departmentId).add(cls);
            }
            
            // Get class and department details for current enrollment
            com.attendance.models.Class currentClass = null;
            Department currentDepartment = null;
            
            if (enrollment != null) {
                currentClass = classDao.findById(enrollment.getClassId());
                
                if (currentClass != null) {
                    currentDepartment = departmentDao.findById(currentClass.getDepartmentId());
                }
            }
            
            request.setAttribute("student", student);
            request.setAttribute("enrollment", enrollment);
            request.setAttribute("currentClass", currentClass);
            request.setAttribute("currentDepartment", currentDepartment);
            request.setAttribute("departments", departments);
            request.setAttribute("classesByDepartment", classesByDepartment);
            
            request.getRequestDispatcher("/WEB-INF/views/admin/students/edit.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while preparing student edit form", e);
            response.sendRedirect(request.getContextPath() + "/admin/students?error=database");
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student ID");
        }
    }
    
    /**
     * Update student details
     */
    private void updateStudent(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws ServletException, IOException {
        try {
            // Extract user ID from path
            int userId = extractIdFromPath(pathInfo, "/edit/");
            
            // Get student details
            User student = userDao.findById(userId);
            
            if (student == null || !"Student".equals(student.getRole())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Student not found");
                return;
            }
            
            // Get form data
            String fullName = request.getParameter("fullName");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            String address = request.getParameter("address");
            String status = request.getParameter("status");
            String newPassword = request.getParameter("newPassword");
            
            // Check if email already exists for another user
            if (!student.getEmail().equals(email)) {
                User existingUser = userDao.findByEmail(email);
                if (existingUser != null && existingUser.getUserId() != userId) {
                    request.setAttribute("error", "Email address is already in use.");
                    showStudentEditForm(request, response, pathInfo);
                    return;
                }
            }
            
            // Update student details
            student.setFullName(fullName);
            student.setEmail(email);
            student.setPhone(phone);
            student.setAddress(address);
            student.setStatus(status);
            
            // Update password if provided
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                student.setPassword(newPassword); // Will be hashed in the DAO implementation
            }
            
            User updatedStudent = userDao.update(student);
            
            if (updatedStudent != null) {
                // Check if enrollment needs to be updated
                String classIdStr = request.getParameter("classId");
                String semester = request.getParameter("semester");
                String academicYear = request.getParameter("academicYear");
                String enrollmentStatus = request.getParameter("enrollmentStatus");
                
                if (classIdStr != null && !classIdStr.trim().isEmpty() &&
                    semester != null && !semester.trim().isEmpty() &&
                    academicYear != null && !academicYear.trim().isEmpty() &&
                    enrollmentStatus != null && !enrollmentStatus.trim().isEmpty()) {
                    
                    int classId = Integer.parseInt(classIdStr);
                    
                    // Get current enrollment
                    StudentEnrollment enrollment = studentEnrollmentDao.findCurrentEnrollment(userId);
                    
                    if (enrollment != null) {
                        // Update existing enrollment
                        enrollment.setClassId(classId);
                        enrollment.setSemester(semester);
                        enrollment.setAcademicYear(academicYear);
                        enrollment.setStatus(enrollmentStatus);
                        
                        StudentEnrollment updatedEnrollment = studentEnrollmentDao.update(enrollment);
                        
                        if (updatedEnrollment == null) {
                            response.sendRedirect(request.getContextPath() + "/admin/students?success=partiallyUpdated&error=enrollmentUpdateFailed");
                            return;
                        }
                    } else {
                        // Create new enrollment
                        StudentEnrollment newEnrollment = new StudentEnrollment();
                        newEnrollment.setStudentId(userId);
                        newEnrollment.setClassId(classId);
                        newEnrollment.setSemester(semester);
                        newEnrollment.setAcademicYear(academicYear);
                        newEnrollment.setEnrollmentDate(new java.sql.Date(System.currentTimeMillis()));
                        newEnrollment.setStatus(enrollmentStatus);
                        
                        StudentEnrollment savedEnrollment = studentEnrollmentDao.save(newEnrollment);
                        
                        if (savedEnrollment == null) {
                            response.sendRedirect(request.getContextPath() + "/admin/students?success=partiallyUpdated&error=enrollmentCreateFailed");
                            return;
                        }
                    }
                }
                
                response.sendRedirect(request.getContextPath() + "/admin/students?success=updated");
                
            } else {
                request.setAttribute("error", "Failed to update student. Please try again.");
                showStudentEditForm(request, response, pathInfo);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while updating student", e);
            response.sendRedirect(request.getContextPath() + "/admin/students?error=database");
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student ID or class ID");
        }
    }
    
    /**
     * View student details
     */
    private void viewStudentDetails(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws ServletException, IOException {
        try {
            // Extract user ID from path
            int userId = extractIdFromPath(pathInfo, "/view/");
            
            // Get student details
            User student = userDao.findById(userId);
            
            if (student == null || !"Student".equals(student.getRole())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Student not found");
                return;
            }
            
            // Get all enrollments for this student
            List<StudentEnrollment> enrollments = studentEnrollmentDao.findByStudent(userId);
            
            // Get class details for each enrollment
            Map<Integer, com.attendance.models.Class> classes = new HashMap<>();
            Map<Integer, Department> departments = new HashMap<>();
            
            for (StudentEnrollment enrollment : enrollments) {
                int classId = enrollment.getClassId();
                com.attendance.models.Class cls = classDao.findById(classId);
                
                if (cls != null) {
                    classes.put(classId, cls);
                    
                    // Get department details
                    Department department = departmentDao.findById(cls.getDepartmentId());
                    if (department != null) {
                        departments.put(cls.getDepartmentId(), department);
                    }
                }
            }
            
            // Get current enrollment
            StudentEnrollment currentEnrollment = studentEnrollmentDao.findCurrentEnrollment(userId);
            
            // Get attendance summary if currently enrolled
            Map<String, Map<String, Object>> attendanceSummary = new HashMap<>();
            
            if (currentEnrollment != null) {
                int classId = currentEnrollment.getClassId();
                String academicYear = currentEnrollment.getAcademicYear();
                String semester = currentEnrollment.getSemester();
                
                // Get class details
                com.attendance.models.Class cls = classDao.findById(classId);
                
                if (cls != null) {
                    // Get department subjects for this semester
                    List<DepartmentSubject> departmentSubjects = departmentSubjectDao.findByDepartmentAndSemester(
                            cls.getDepartmentId(), semester);
                    
                    for (DepartmentSubject deptSubject : departmentSubjects) {
                        String subjectCode = deptSubject.getSubjectCode();
                        
                        // Get subject details
                        Subject subject = subjectDao.findByCode(subjectCode);
                        
                        if (subject != null) {
                            // Get attendance records
                            List<Attendance> records = attendanceDao.findByStudentSubjectSemesterAndYear(
                                    userId, subjectCode, semester, academicYear);
                            
                            int totalClasses = records.size();
                            int presentCount = 0;
                            int absentCount = 0;
                            int leaveCount = 0;
                            
                            for (Attendance record : records) {
                                switch (record.getStatus()) {
                                    case "Present":
                                        presentCount++;
                                        break;
                                    case "Absent":
                                        absentCount++;
                                        break;
                                    case "Leave":
                                        leaveCount++;
                                        break;
                                }
                            }
                            
                            // Calculate attendance percentage
                            double percentage = totalClasses > 0 ? (double) presentCount / totalClasses * 100 : 0;
                            
                            Map<String, Object> subjectSummary = new HashMap<>();
                            subjectSummary.put("subject", subject);
                            subjectSummary.put("totalClasses", totalClasses);
                            subjectSummary.put("presentCount", presentCount);
                            subjectSummary.put("absentCount", absentCount);
                            subjectSummary.put("leaveCount", leaveCount);
                            subjectSummary.put("percentage", percentage);
                            
                            attendanceSummary.put(subjectCode, subjectSummary);
                        }
                    }
                }
            }
            
            request.setAttribute("student", student);
            request.setAttribute("enrollments", enrollments);
            request.setAttribute("classes", classes);
            request.setAttribute("departments", departments);
            request.setAttribute("currentEnrollment", currentEnrollment);
            request.setAttribute("attendanceSummary", attendanceSummary);
            
            request.getRequestDispatcher("/WEB-INF/views/admin/students/view.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while viewing student details", e);
            response.sendRedirect(request.getContextPath() + "/admin/students?error=database");
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student ID");
        }
    }
    
    /**
     * Show student enrollment form
     */
    private void showStudentEnrollmentForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Get all students for dropdown
            List<User> students = userDao.findUsersByRole("Student");
            
            // Get all departments for dropdown
            List<Department> departments = departmentDao.findAll();
            
            // Get all classes for dropdown
            List<com.attendance.models.Class> classes = classDao.findAll();
            
            // Group classes by department
            Map<Integer, List<com.attendance.models.Class>> classesByDepartment = new HashMap<>();
            
            for (com.attendance.models.Class cls : classes) {
                int departmentId = cls.getDepartmentId();
                
                if (!classesByDepartment.containsKey(departmentId)) {
                    classesByDepartment.put(departmentId, new ArrayList<>());
                }
                
                classesByDepartment.get(departmentId).add(cls);
            }
            
            // Get current academic year
            String currentAcademicYear = DateUtils.getCurrentAcademicYear();
            
            request.setAttribute("students", students);
            request.setAttribute("departments", departments);
            request.setAttribute("classesByDepartment", classesByDepartment);
            request.setAttribute("currentAcademicYear", currentAcademicYear);
            
            request.getRequestDispatcher("/WEB-INF/views/admin/students/enroll.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while preparing student enrollment form", e);
            request.setAttribute("error", "Failed to load form. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/admin/students/enroll.jsp").forward(request, response);
        }
    }
    
    /**
     * Enroll student in a class
     */
    private void enrollStudent(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String studentIdStr = request.getParameter("studentId");
        String classIdStr = request.getParameter("classId");
        String semester = request.getParameter("semester");
        String academicYear = request.getParameter("academicYear");
        
        if (studentIdStr == null || studentIdStr.trim().isEmpty() ||
            classIdStr == null || classIdStr.trim().isEmpty() ||
            semester == null || semester.trim().isEmpty() ||
            academicYear == null || academicYear.trim().isEmpty()) {
            
            request.setAttribute("error", "All fields are required.");
            showStudentEnrollmentForm(request, response);
            return;
        }
        
        try {
            int studentId = Integer.parseInt(studentIdStr);
            int classId = Integer.parseInt(classIdStr);
            
            // Check if student exists
            User student = userDao.findById(studentId);
            if (student == null || !"Student".equals(student.getRole())) {
                request.setAttribute("error", "Invalid student selected.");
                showStudentEnrollmentForm(request, response);
                return;
            }
            
            // Check if class exists
            com.attendance.models.Class cls = classDao.findById(classId);
            if (cls == null) {
                request.setAttribute("error", "Invalid class selected.");
                showStudentEnrollmentForm(request, response);
                return;
            }
            
            // Check if student already has an active enrollment
            StudentEnrollment existingEnrollment = studentEnrollmentDao.findCurrentEnrollment(studentId);
            
            if (existingEnrollment != null) {
                // Update existing enrollment
                existingEnrollment.setClassId(classId);
                existingEnrollment.setSemester(semester);
                existingEnrollment.setAcademicYear(academicYear);
                existingEnrollment.setStatus("Active");
                
                StudentEnrollment updatedEnrollment = studentEnrollmentDao.update(existingEnrollment);
                
                if (updatedEnrollment != null) {
                    response.sendRedirect(request.getContextPath() + "/admin/students?success=enrolled");
                } else {
                    request.setAttribute("error", "Failed to update enrollment. Please try again.");
                    showStudentEnrollmentForm(request, response);
                }
            } else {
                // Create new enrollment
                StudentEnrollment newEnrollment = new StudentEnrollment();
                newEnrollment.setStudentId(studentId);
                newEnrollment.setClassId(classId);
                newEnrollment.setSemester(semester);
                newEnrollment.setAcademicYear(academicYear);
                newEnrollment.setEnrollmentDate(new java.sql.Date(System.currentTimeMillis()));
                newEnrollment.setStatus("Active");
                
                StudentEnrollment savedEnrollment = studentEnrollmentDao.save(newEnrollment);
                
                if (savedEnrollment != null) {
                    response.sendRedirect(request.getContextPath() + "/admin/students?success=enrolled");
                } else {
                    request.setAttribute("error", "Failed to create enrollment. Please try again.");
                    showStudentEnrollmentForm(request, response);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while enrolling student", e);
            request.setAttribute("error", "A system error occurred. Please try again later.");
            showStudentEnrollmentForm(request, response);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid student ID or class ID.");
            showStudentEnrollmentForm(request, response);
        }
    }
    
    /**
     * View student attendance records
     */
    private void viewStudentAttendanceRecords(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws ServletException, IOException {
        try {
            // Extract user ID from path
            int userId = extractIdFromPath(pathInfo, "/attendance/");
            
            // Get student details
            User student = userDao.findById(userId);
            
            if (student == null || !"Student".equals(student.getRole())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Student not found");
                return;
            }
            
            // Get filter parameters
            String subjectCode = request.getParameter("subjectCode");
            String semester = request.getParameter("semester");
            String academicYear = request.getParameter("academicYear");
            String fromDateStr = request.getParameter("fromDate");
            String toDateStr = request.getParameter("toDate");
            
            // Get current enrollment
            StudentEnrollment currentEnrollment = studentEnrollmentDao.findCurrentEnrollment(userId);
            
            // Get all enrollments for this student
            List<StudentEnrollment> allEnrollments = studentEnrollmentDao.findByStudent(userId);
            
            // Get class details for each enrollment
            Map<Integer, com.attendance.models.Class> classes = new HashMap<>();
            Map<Integer, Department> departments = new HashMap<>();
            
            for (StudentEnrollment enrollment : allEnrollments) {
                int classId = enrollment.getClassId();
                com.attendance.models.Class cls = classDao.findById(classId);
                
                if (cls != null) {
                    classes.put(classId, cls);
                    
                    // Get department details
                    Department department = departmentDao.findById(cls.getDepartmentId());
                    if (department != null) {
                        departments.put(cls.getDepartmentId(), department);
                    }
                }
            }
            
            // Determine which enrollment to use for attendance records
            StudentEnrollment enrollmentToUse = currentEnrollment;
            
            if (academicYear != null && !academicYear.trim().isEmpty() && semester != null && !semester.trim().isEmpty()) {
                // Find specific enrollment based on academic year and semester
                for (StudentEnrollment enrollment : allEnrollments) {
                    if (academicYear.equals(enrollment.getAcademicYear()) && semester.equals(enrollment.getSemester())) {
                        enrollmentToUse = enrollment;
                        break;
                    }
                }
            }
            
            // If no specific enrollment found, use current enrollment
            if (enrollmentToUse == null) {
                request.setAttribute("error", "No enrollment found for the selected criteria.");
                request.setAttribute("student", student);
                request.setAttribute("enrollments", allEnrollments);
                request.setAttribute("classes", classes);
                request.setAttribute("departments", departments);
                request.getRequestDispatcher("/WEB-INF/views/admin/students/attendance.jsp").forward(request, response);
                return;
            }
            
            // Get class details for the enrollment
            com.attendance.models.Class cls = classes.get(enrollmentToUse.getClassId());
            
            if (cls == null) {
                request.setAttribute("error", "Invalid class enrollment.");
                request.setAttribute("student", student);
                request.setAttribute("enrollments", allEnrollments);
                request.setAttribute("classes", classes);
                request.setAttribute("departments", departments);
                request.getRequestDispatcher("/WEB-INF/views/admin/students/attendance.jsp").forward(request, response);
                return;
            }
            
            // Get department details
            Department department = departments.get(cls.getDepartmentId());
            
            // Get available subjects
            List<DepartmentSubject> departmentSubjects = departmentSubjectDao.findByDepartmentAndSemester(
                    cls.getDepartmentId(), enrollmentToUse.getSemester());
            
            Map<String, Subject> subjects = new HashMap<>();
            for (DepartmentSubject deptSubject : departmentSubjects) {
                Subject subject = subjectDao.findByCode(deptSubject.getSubjectCode());
                if (subject != null) {
                    subjects.put(deptSubject.getSubjectCode(), subject);
                }
            }
            
            // Get attendance records based on filters
            List<Attendance> attendanceRecords;
            
            if (subjectCode != null && !subjectCode.trim().isEmpty()) {
                // Filter by subject
                attendanceRecords = attendanceDao.findByStudentSubjectSemesterAndYear(
                        userId, subjectCode, enrollmentToUse.getSemester(), enrollmentToUse.getAcademicYear());
            } else {
                // Get all attendance records for this semester/year
                attendanceRecords = new ArrayList<>();
                for (String code : subjects.keySet()) {
                    List<Attendance> records = attendanceDao.findByStudentSubjectSemesterAndYear(
                            userId, code, enrollmentToUse.getSemester(), enrollmentToUse.getAcademicYear());
                    attendanceRecords.addAll(records);
                }
            }
            
            // Apply date filters if provided
            if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
                LocalDate fromDate = DateUtils.parseDate(fromDateStr);
                if (fromDate != null) {
                    attendanceRecords.removeIf(record -> 
                        record.getAttendanceDate().toLocalDate().isBefore(fromDate));
                }
            }
            
            if (toDateStr != null && !toDateStr.trim().isEmpty()) {
                LocalDate toDate = DateUtils.parseDate(toDateStr);
                if (toDate != null) {
                    attendanceRecords.removeIf(record -> 
                        record.getAttendanceDate().toLocalDate().isAfter(toDate));
                }
            }
            
            // Calculate attendance summary
            Map<String, Map<String, Object>> attendanceSummary = new HashMap<>();
            
            for (String code : subjects.keySet()) {
                List<Attendance> records = attendanceDao.findByStudentSubjectSemesterAndYear(
                        userId, code, enrollmentToUse.getSemester(), enrollmentToUse.getAcademicYear());
                
                int totalClasses = records.size();
                int presentCount = 0;
                int absentCount = 0;
                int leaveCount = 0;
                
                for (Attendance record : records) {
                    switch (record.getStatus()) {
                        case "Present":
                            presentCount++;
                            break;
                        case "Absent":
                            absentCount++;
                            break;
                        case "Leave":
                            leaveCount++;
                            break;
                    }
                }
                
                // Calculate attendance percentage
                double percentage = totalClasses > 0 ? (double) presentCount / totalClasses * 100 : 0;
                
                Map<String, Object> subjectSummary = new HashMap<>();
                subjectSummary.put("totalClasses", totalClasses);
                subjectSummary.put("presentCount", presentCount);
                subjectSummary.put("absentCount", absentCount);
                subjectSummary.put("leaveCount", leaveCount);
                subjectSummary.put("percentage", percentage);
                
                attendanceSummary.put(code, subjectSummary);
            }
            
            request.setAttribute("student", student);
            request.setAttribute("enrollments", allEnrollments);
            request.setAttribute("classes", classes);
            request.setAttribute("departments", departments);
            request.setAttribute("currentEnrollment", enrollmentToUse);
            request.setAttribute("department", department);
            request.setAttribute("class", cls);
            request.setAttribute("subjects", subjects);
            request.setAttribute("attendanceRecords", attendanceRecords);
            request.setAttribute("attendanceSummary", attendanceSummary);
            
            // Set filter parameters for maintaining state
            request.setAttribute("subjectFilter", subjectCode);
            request.setAttribute("semesterFilter", enrollmentToUse.getSemester());
            request.setAttribute("academicYearFilter", enrollmentToUse.getAcademicYear());
            request.setAttribute("fromDateFilter", fromDateStr);
            request.setAttribute("toDateFilter", toDateStr);
            
            request.getRequestDispatcher("/WEB-INF/views/admin/students/attendance.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while viewing student attendance", e);
            response.sendRedirect(request.getContextPath() + "/admin/students?error=database");
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student ID");
        }
    }
    
    /**
     * Search students
     */
    private void searchStudents(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = request.getParameter("query");
        
        if (query == null || query.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/students");
            return;
        }
        
        try {
            // Search for students matching the query
            List<User> students = userDao.searchStudents(query);
            
            // Get current enrollments for each student
            Map<Integer, StudentEnrollment> enrollments = new HashMap<>();
            Map<Integer, com.attendance.models.Class> classes = new HashMap<>();
            Map<Integer, Department> departments = new HashMap<>();
            
            for (User student : students) {
                StudentEnrollment enrollment = studentEnrollmentDao.findCurrentEnrollment(student.getUserId());
                
                if (enrollment != null) {
                    enrollments.put(student.getUserId(), enrollment);
                    
                    // Get class details
                    com.attendance.models.Class cls = classDao.findById(enrollment.getClassId());
                    if (cls != null) {
                        classes.put(enrollment.getClassId(), cls);
                        
                        // Get department details
                        Department department = departmentDao.findById(cls.getDepartmentId());
                        if (department != null) {
                            departments.put(cls.getDepartmentId(), department);
                        }
                    }
                }
            }
            
            // Get all departments and classes for filter dropdowns
            List<Department> allDepartments = departmentDao.findAll();
            List<com.attendance.models.Class> allClasses = classDao.findAll();
            
            // Get list of academic years for filter dropdown
            List<String> academicYears = new ArrayList<>();
            int currentYear = LocalDate.now().getYear();
            for (int i = 0; i < 5; i++) {
                int startYear = currentYear - i;
                academicYears.add(startYear + "-" + (startYear + 1));
            }
            
            request.setAttribute("students", students);
            request.setAttribute("enrollments", enrollments);
            request.setAttribute("classes", classes);
            request.setAttribute("departments", departments);
            request.setAttribute("allDepartments", allDepartments);
            request.setAttribute("allClasses", allClasses);
            request.setAttribute("academicYears", academicYears);
            request.setAttribute("searchQuery", query);
            
            request.getRequestDispatcher("/WEB-INF/views/admin/students/list.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while searching students", e);
            response.sendRedirect(request.getContextPath() + "/admin/students?error=database");
        }
    }
    
    /**
     * List students in teacher's assigned classes
     */
    private void listTeacherStudents(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get classes where this teacher is assigned
            List<TeacherAssignment> assignments = new ArrayList<>();
            TeacherAssignmentDao teacherAssignmentDao = new TeacherAssignmentDaoImpl();
            assignments = teacherAssignmentDao.findByTeacher(user.getUserId());
            
            if (assignments.isEmpty()) {
                request.setAttribute("error", "You are not assigned to any classes.");
                request.getRequestDispatcher("/WEB-INF/views/teacher/students/list.jsp").forward(request, response);
                return;
            }
            
            // Collect unique class IDs
            Set<Integer> classIds = new HashSet<>();
            Set<String> subjectCodes = new HashSet<>();
            
            for (TeacherAssignment assignment : assignments) {
                classIds.add(assignment.getClassId());
                subjectCodes.add(assignment.getSubjectCode());
            }
            
            // Get filter parameters
            String classIdStr = request.getParameter("classId");
            String subjectCode = request.getParameter("subjectCode");
            
            List<User> students = new ArrayList<>();
            
            if (classIdStr != null && !classIdStr.trim().isEmpty()) {
                // Filter by specific class
                int classId = Integer.parseInt(classIdStr);
                
                // Check if teacher is assigned to this class
                if (classIds.contains(classId)) {
                    students = userDao.findStudentsByClass(classId);
                } else {
                    request.setAttribute("error", "You are not authorized to view students in this class.");
                    request.getRequestDispatcher("/WEB-INF/views/teacher/students/list.jsp").forward(request, response);
                    return;
                }
            } else {
                // Get students from all assigned classes
                for (int classId : classIds) {
                    List<User> classStudents = userDao.findStudentsByClass(classId);
                    students.addAll(classStudents);
                }
                
                // Remove duplicates
                students = students.stream()
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Get current enrollments for each student
            Map<Integer, StudentEnrollment> enrollments = new HashMap<>();
            Map<Integer, com.attendance.models.Class> classes = new HashMap<>();
            Map<Integer, Department> departments = new HashMap<>();
            
            for (User student : students) {
                StudentEnrollment enrollment = studentEnrollmentDao.findCurrentEnrollment(student.getUserId());
                
                if (enrollment != null) {
                    enrollments.put(student.getUserId(), enrollment);
                    
                    // Get class details
                    com.attendance.models.Class cls = classDao.findById(enrollment.getClassId());
                    if (cls != null) {
                        classes.put(enrollment.getClassId(), cls);
                        
                        // Get department details
                        Department department = departmentDao.findById(cls.getDepartmentId());
                        if (department != null) {
                            departments.put(cls.getDepartmentId(), department);
                        }
                    }
                }
            }
            
            // Get subject details
            Map<String, Subject> subjects = new HashMap<>();
            for (String code : subjectCodes) {
                Subject subject = subjectDao.findByCode(code);
                if (subject != null) {
                    subjects.put(code, subject);
                }
            }
            
            // Get all assigned classes for filter dropdown
            Map<Integer, com.attendance.models.Class> assignedClasses = new HashMap<>();
            for (int classId : classIds) {
                com.attendance.models.Class cls = classDao.findById(classId);
                if (cls != null) {
                    assignedClasses.put(classId, cls);
                }
            }
            
            request.setAttribute("students", students);
            request.setAttribute("enrollments", enrollments);
            request.setAttribute("classes", classes);
            request.setAttribute("departments", departments);
            request.setAttribute("subjects", subjects);
            request.setAttribute("assignedClasses", assignedClasses);
            request.setAttribute("assignments", assignments);
            
            // Set filter parameters for maintaining state
            request.setAttribute("classFilter", classIdStr);
            request.setAttribute("subjectFilter", subjectCode);
            
            request.getRequestDispatcher("/WEB-INF/views/teacher/students/list.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while listing teacher's students", e);
            request.setAttribute("error", "Failed to retrieve students. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/teacher/students/list.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid class ID.");
            request.getRequestDispatcher("/WEB-INF/views/teacher/students/list.jsp").forward(request, response);
        }
    }
    
    /**
     * View student details (teacher view)
     */
    private void viewTeacherStudentDetails(HttpServletRequest request, HttpServletResponse response, User user, String pathInfo) throws ServletException, IOException {
        try {
            // Extract user ID from path
            int studentId = extractIdFromPath(pathInfo, "/view/");
            
            // Get student details
            User student = userDao.findById(studentId);
            
            if (student == null || !"Student".equals(student.getRole())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Student not found");
                return;
            }
            
            // Get current enrollment
            StudentEnrollment enrollment = studentEnrollmentDao.findCurrentEnrollment(studentId);
            
            if (enrollment == null) {
                request.setAttribute("error", "Student is not currently enrolled in any class.");
                request.setAttribute("student", student);
                request.getRequestDispatcher("/WEB-INF/views/teacher/students/view.jsp").forward(request, response);
                return;
            }
            
            // Get class details
            com.attendance.models.Class cls = classDao.findById(enrollment.getClassId());
            
            if (cls == null) {
                request.setAttribute("error", "Invalid class enrollment.");
                request.setAttribute("student", student);
                request.getRequestDispatcher("/WEB-INF/views/teacher/students/view.jsp").forward(request, response);
                return;
            }
            
            // Check if teacher is assigned to this class
            TeacherAssignmentDao teacherAssignmentDao = new TeacherAssignmentDaoImpl();
            List<TeacherAssignment> assignments = teacherAssignmentDao.findByTeacher(user.getUserId());
            
            boolean isAssigned = false;
            Set<String> teacherSubjects = new HashSet<>();
            
            for (TeacherAssignment assignment : assignments) {
                if (assignment.getClassId() == enrollment.getClassId()) {
                    isAssigned = true;
                    teacherSubjects.add(assignment.getSubjectCode());
                }
            }
            
            if (!isAssigned) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not authorized to view details of this student");
                return;
            }
            
            // Get department details
            Department department = departmentDao.findById(cls.getDepartmentId());
            
            // Get subject details
            Map<String, Subject> subjects = new HashMap<>();
            for (String code : teacherSubjects) {
                Subject subject = subjectDao.findByCode(code);
                if (subject != null) {
                    subjects.put(code, subject);
                }
            }
            
            // Get attendance summary for teacher's subjects
            Map<String, Map<String, Object>> attendanceSummary = new HashMap<>();
            
            for (String subjectCode : teacherSubjects) {
                // Get attendance records
                List<Attendance> records = attendanceDao.findByStudentSubjectSemesterAndYear(
                        studentId, subjectCode, enrollment.getSemester(), enrollment.getAcademicYear());
                
                int totalClasses = records.size();
                int presentCount = 0;
                int absentCount = 0;
                int leaveCount = 0;
                
                for (Attendance record : records) {
                    switch (record.getStatus()) {
                        case "Present":
                            presentCount++;
                            break;
                        case "Absent":
                            absentCount++;
                            break;
                        case "Leave":
                            leaveCount++;
                            break;
                    }
                }
                
                // Calculate attendance percentage
                double percentage = totalClasses > 0 ? (double) presentCount / totalClasses * 100 : 0;
                
                Map<String, Object> subjectSummary = new HashMap<>();
                subjectSummary.put("totalClasses", totalClasses);
                subjectSummary.put("presentCount", presentCount);
                subjectSummary.put("absentCount", absentCount);
                subjectSummary.put("leaveCount", leaveCount);
                subjectSummary.put("percentage", percentage);
                
                attendanceSummary.put(subjectCode, subjectSummary);
            }
            
            request.setAttribute("student", student);
            request.setAttribute("enrollment", enrollment);
            request.setAttribute("class", cls);
            request.setAttribute("department", department);
            request.setAttribute("subjects", subjects);
            request.setAttribute("attendanceSummary", attendanceSummary);
            
            request.getRequestDispatcher("/WEB-INF/views/teacher/students/view.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while viewing student details (teacher view)", e);
            response.sendRedirect(request.getContextPath() + "/teacher/students?error=database");
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student ID");
        }
    }
    
    /**
     * View student attendance for teacher's subjects
     */
    private void viewTeacherStudentAttendance(HttpServletRequest request, HttpServletResponse response, User user, String pathInfo) throws ServletException, IOException {
        try {
            // Extract user ID from path
            int studentId = extractIdFromPath(pathInfo, "/attendance/");
            
            // Get student details
            User student = userDao.findById(studentId);
            
            if (student == null || !"Student".equals(student.getRole())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Student not found");
                return;
            }
            
            // Get filter parameters
            String subjectCode = request.getParameter("subjectCode");
            String fromDateStr = request.getParameter("fromDate");
            String toDateStr = request.getParameter("toDate");
            
            // Get current enrollment
            StudentEnrollment enrollment = studentEnrollmentDao.findCurrentEnrollment(studentId);
            
            if (enrollment == null) {
                request.setAttribute("error", "Student is not currently enrolled in any class.");
                request.setAttribute("student", student);
                request.getRequestDispatcher("/WEB-INF/views/teacher/students/attendance.jsp").forward(request, response);
                return;
            }
            
            // Get class details
            com.attendance.models.Class cls = classDao.findById(enrollment.getClassId());
            
            if (cls == null) {
                request.setAttribute("error", "Invalid class enrollment.");
                request.setAttribute("student", student);
                request.getRequestDispatcher("/WEB-INF/views/teacher/students/attendance.jsp").forward(request, response);
                return;
            }
            
            // Check if teacher is assigned to this class
            TeacherAssignmentDao teacherAssignmentDao = new TeacherAssignmentDaoImpl();
            List<TeacherAssignment> assignments = teacherAssignmentDao.findByTeacher(user.getUserId());
            
            boolean isAssigned = false;
            Set<String> teacherSubjects = new HashSet<>();
            
            for (TeacherAssignment assignment : assignments) {
                if (assignment.getClassId() == enrollment.getClassId()) {
                    isAssigned = true;
                    teacherSubjects.add(assignment.getSubjectCode());
                }
            }
            
            if (!isAssigned) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not authorized to view attendance of this student");
                return;
            }
            
            // If subject code is provided, check if teacher teaches that subject
            if (subjectCode != null && !subjectCode.trim().isEmpty() && !teacherSubjects.contains(subjectCode)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not authorized to view attendance for this subject");
                return;
            }
            
            // Get department details
            Department department = departmentDao.findById(cls.getDepartmentId());
            
            // Get subject details
            Map<String, Subject> subjects = new HashMap<>();
            for (String code : teacherSubjects) {
                Subject subject = subjectDao.findByCode(code);
                if (subject != null) {
                    subjects.put(code, subject);
                }
            }
            
            // Get attendance records based on filters
            List<Attendance> attendanceRecords;
            
            if (subjectCode != null && !subjectCode.trim().isEmpty() && teacherSubjects.contains(subjectCode)) {
                // Filter by subject
                attendanceRecords = attendanceDao.findByStudentSubjectSemesterAndYear(
                        studentId, subjectCode, enrollment.getSemester(), enrollment.getAcademicYear());
            } else {
                // Get attendance records for all subjects taught by this teacher
                attendanceRecords = new ArrayList<>();
                for (String code : teacherSubjects) {
                    List<Attendance> records = attendanceDao.findByStudentSubjectSemesterAndYear(
                            studentId, code, enrollment.getSemester(), enrollment.getAcademicYear());
                    attendanceRecords.addAll(records);
                }
            }
            
            // Apply date filters if provided
            if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
                LocalDate fromDate = DateUtils.parseDate(fromDateStr);
                if (fromDate != null) {
                    attendanceRecords.removeIf(record -> 
                        record.getAttendanceDate().toLocalDate().isBefore(fromDate));
                }
            }
            
            if (toDateStr != null && !toDateStr.trim().isEmpty()) {
                LocalDate toDate = DateUtils.parseDate(toDateStr);
                if (toDate != null) {
                    attendanceRecords.removeIf(record -> 
                        record.getAttendanceDate().toLocalDate().isAfter(toDate));
                }
            }
            
            // Calculate attendance summary
            Map<String, Map<String, Object>> attendanceSummary = new HashMap<>();
            
            for (String code : teacherSubjects) {
                // Get attendance records
                List<Attendance> records = attendanceDao.findByStudentSubjectSemesterAndYear(
                        studentId, code, enrollment.getSemester(), enrollment.getAcademicYear());
                
                int totalClasses = records.size();
                int presentCount = 0;
                int absentCount = 0;
                int leaveCount = 0;
                
                for (Attendance record : records) {
                    switch (record.getStatus()) {
                        case "Present":
                            presentCount++;
                            break;
                        case "Absent":
                            absentCount++;
                            break;
                        case "Leave":
                            leaveCount++;
                            break;
                    }
                }
                
                // Calculate attendance percentage
                double percentage = totalClasses > 0 ? (double) presentCount / totalClasses * 100 : 0;
                
                Map<String, Object> subjectSummary = new HashMap<>();
                subjectSummary.put("totalClasses", totalClasses);
                subjectSummary.put("presentCount", presentCount);
                subjectSummary.put("absentCount", absentCount);
                subjectSummary.put("leaveCount", leaveCount);
                subjectSummary.put("percentage", percentage);
                
                attendanceSummary.put(code, subjectSummary);
            }
            
            request.setAttribute("student", student);
            request.setAttribute("enrollment", enrollment);
            request.setAttribute("class", cls);
            request.setAttribute("department", department);
            request.setAttribute("subjects", subjects);
            request.setAttribute("attendanceRecords", attendanceRecords);
            request.setAttribute("attendanceSummary", attendanceSummary);
            
            // Set filter parameters for maintaining state
            request.setAttribute("subjectFilter", subjectCode);
            request.setAttribute("fromDateFilter", fromDateStr);
            request.setAttribute("toDateFilter", toDateStr);
            
            request.getRequestDispatcher("/WEB-INF/views/teacher/students/attendance.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while viewing student attendance (teacher view)", e);
            response.sendRedirect(request.getContextPath() + "/teacher/students?error=database");
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student ID");
        }
    }
    
    /**
     * List students in HOD's department
     */
    private void listHodStudents(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        try {
            // Get HOD's department
            List<Department> departmentList = departmentDao.findByHod(user.getUserId());
            Department department = departmentList.isEmpty() ? null : departmentList.get(0);
            
            if (department == null) {
                request.setAttribute("error", "You are not assigned as HOD to any department.");
                request.getRequestDispatcher("/WEB-INF/views/hod/students/list.jsp").forward(request, response);
                return;
            }
            
            // Get filter parameters
            String classIdStr = request.getParameter("classId");
            String academicYear = request.getParameter("academicYear");
            String status = request.getParameter("status");
            
            List<User> students;
            
            if (classIdStr != null && !classIdStr.trim().isEmpty()) {
                // Filter by class
                int classId = Integer.parseInt(classIdStr);
                
                // Check if class belongs to HOD's department
                com.attendance.models.Class cls = classDao.findById(classId);
                if (cls == null || cls.getDepartmentId() != department.getDepartmentId()) {
                    request.setAttribute("error", "You are not authorized to view students in this class.");
                    request.getRequestDispatcher("/WEB-INF/views/hod/students/list.jsp").forward(request, response);
                    return;
                }
                
                students = userDao.findStudentsByClass(classId);
                
            } else {
                // Get all students in department
                students = userDao.findStudentsByDepartment(department.getDepartmentId());
                
                // Apply additional filters
                if (academicYear != null && !academicYear.trim().isEmpty()) {
                    students = students.stream()
                        .filter(s -> {
                            try {
                                StudentEnrollment enrollment = studentEnrollmentDao.findCurrentEnrollment(s.getUserId());
                                return enrollment != null && academicYear.equals(enrollment.getAcademicYear());
                            } catch (SQLException e) {
                                return false;
                            }
                        })
                        .collect(java.util.stream.Collectors.toList());
                }
                
                if (status != null && !status.trim().isEmpty()) {
                    students = students.stream()
                        .filter(s -> status.equals(s.getStatus()))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            // Get current enrollments for each student
            Map<Integer, StudentEnrollment> enrollments = new HashMap<>();
            Map<Integer, com.attendance.models.Class> classes = new HashMap<>();
            
            for (User student : students) {
                StudentEnrollment enrollment = studentEnrollmentDao.findCurrentEnrollment(student.getUserId());
                
                if (enrollment != null) {
                    enrollments.put(student.getUserId(), enrollment);
                    
                    // Get class details
                    com.attendance.models.Class cls = classDao.findById(enrollment.getClassId());
                    if (cls != null) {
                        classes.put(enrollment.getClassId(), cls);
                    }
                }
            }
            
            // Get classes in this department for filter dropdown
            List<com.attendance.models.Class> departmentClasses = classDao.findByDepartment(department.getDepartmentId());
            
            // Get list of academic years for filter dropdown
            List<String> academicYears = new ArrayList<>();
            int currentYear = LocalDate.now().getYear();
            for (int i = 0; i < 5; i++) {
                int startYear = currentYear - i;
                academicYears.add(startYear + "-" + (startYear + 1));
            }
            
            request.setAttribute("students", students);
            request.setAttribute("enrollments", enrollments);
            request.setAttribute("classes", classes);
            request.setAttribute("department", department);
            request.setAttribute("departmentClasses", departmentClasses);
            request.setAttribute("academicYears", academicYears);
            
            // Set filter parameters for maintaining state
            request.setAttribute("classFilter", classIdStr);
            request.setAttribute("academicYearFilter", academicYear);
            request.setAttribute("statusFilter", status);
            
            request.getRequestDispatcher("/WEB-INF/views/hod/students/list.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while listing HOD's students", e);
            request.setAttribute("error", "Failed to retrieve students. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/hod/students/list.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid class ID.");
            request.getRequestDispatcher("/WEB-INF/views/hod/students/list.jsp").forward(request, response);
        }
    }
    
    /**
     * View student details (HOD view)
     */
    private void viewHodStudentDetails(HttpServletRequest request, HttpServletResponse response, User user, String pathInfo) throws ServletException, IOException {
        try {
            // Get HOD's department
            List<Department> departmentList = departmentDao.findByHod(user.getUserId());
            Department department = departmentList.isEmpty() ? null : departmentList.get(0);
            
            if (department == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not assigned as HOD to any department");
                return;
            }
            
            // Extract user ID from path
            int studentId = extractIdFromPath(pathInfo, "/view/");
            
            // Get student details
            User student = userDao.findById(studentId);
            
            if (student == null || !"Student".equals(student.getRole())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Student not found");
                return;
            }
            
            // Get current enrollment
            StudentEnrollment enrollment = studentEnrollmentDao.findCurrentEnrollment(studentId);
            
            if (enrollment == null) {
                request.setAttribute("error", "Student is not currently enrolled in any class.");
                request.setAttribute("student", student);
                request.setAttribute("department", department);
                request.getRequestDispatcher("/WEB-INF/views/hod/students/view.jsp").forward(request, response);
                return;
            }
            
            // Get class details
            com.attendance.models.Class cls = classDao.findById(enrollment.getClassId());
            
            if (cls == null) {
                request.setAttribute("error", "Invalid class enrollment.");
                request.setAttribute("student", student);
                request.setAttribute("department", department);
                request.getRequestDispatcher("/WEB-INF/views/hod/students/view.jsp").forward(request, response);
                return;
            }
            
            // Check if class belongs to HOD's department
            if (cls.getDepartmentId() != department.getDepartmentId()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not authorized to view details of this student");
                return;
            }
            
            // Get all enrollments for this student
            List<StudentEnrollment> allEnrollments = studentEnrollmentDao.findByStudent(studentId);
            
            // Get class details for each enrollment
            Map<Integer, com.attendance.models.Class> classes = new HashMap<>();
            
            for (StudentEnrollment enroll : allEnrollments) {
                int classId = enroll.getClassId();
                com.attendance.models.Class classObj = classDao.findById(classId);
                
                if (classObj != null && classObj.getDepartmentId() == department.getDepartmentId()) {
                    classes.put(classId, classObj);
                }
            }
            
            // Get department subjects for current semester
            List<DepartmentSubject> departmentSubjects = departmentSubjectDao.findByDepartmentAndSemester(
                    department.getDepartmentId(), enrollment.getSemester());
            
            // Get subject details
            Map<String, Subject> subjects = new HashMap<>();
            for (DepartmentSubject deptSubject : departmentSubjects) {
                Subject subject = subjectDao.findByCode(deptSubject.getSubjectCode());
                if (subject != null) {
                    subjects.put(deptSubject.getSubjectCode(), subject);
                }
            }
            
            // Get attendance summary for all subjects
            Map<String, Map<String, Object>> attendanceSummary = new HashMap<>();
            
            for (String subjectCode : subjects.keySet()) {
                // Get attendance records
                List<Attendance> records = attendanceDao.findByStudentSubjectSemesterAndYear(
                        studentId, subjectCode, enrollment.getSemester(), enrollment.getAcademicYear());
                
                int totalClasses = records.size();
                int presentCount = 0;
                int absentCount = 0;
                int leaveCount = 0;
                
                for (Attendance record : records) {
                    switch (record.getStatus()) {
                        case "Present":
                            presentCount++;
                            break;
                        case "Absent":
                            absentCount++;
                            break;
                        case "Leave":
                            leaveCount++;
                            break;
                    }
                }
                
                // Calculate attendance percentage
                double percentage = totalClasses > 0 ? (double) presentCount / totalClasses * 100 : 0;
                
                Map<String, Object> subjectSummary = new HashMap<>();
                subjectSummary.put("totalClasses", totalClasses);
                subjectSummary.put("presentCount", presentCount);
                subjectSummary.put("absentCount", absentCount);
                subjectSummary.put("leaveCount", leaveCount);
                subjectSummary.put("percentage", percentage);
                
                attendanceSummary.put(subjectCode, subjectSummary);
            }
            
            request.setAttribute("student", student);
            request.setAttribute("enrollment", enrollment);
            request.setAttribute("allEnrollments", allEnrollments);
            request.setAttribute("class", cls);
            request.setAttribute("classes", classes);
            request.setAttribute("department", department);
            request.setAttribute("subjects", subjects);
            request.setAttribute("attendanceSummary", attendanceSummary);
            
            request.getRequestDispatcher("/WEB-INF/views/hod/students/view.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while viewing student details (HOD view)", e);
            response.sendRedirect(request.getContextPath() + "/hod/students?error=database");
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student ID");
        }
    }
    
    /**
     * View student attendance records (HOD view)
     */
    private void viewHodStudentAttendance(HttpServletRequest request, HttpServletResponse response, User user, String pathInfo) throws ServletException, IOException {
        try {
            // Get HOD's department
            List<Department> departmentList = departmentDao.findByHod(user.getUserId());
            Department department = departmentList.isEmpty() ? null : departmentList.get(0);
            
            if (department == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not assigned as HOD to any department");
                return;
            }
            
            // Extract user ID from path
            int studentId = extractIdFromPath(pathInfo, "/attendance/");
            
            // Get student details
            User student = userDao.findById(studentId);
            
            if (student == null || !"Student".equals(student.getRole())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Student not found");
                return;
            }
            
            // Get filter parameters
            String subjectCode = request.getParameter("subjectCode");
            String semesterFilter = request.getParameter("semester");
            String academicYearFilter = request.getParameter("academicYear");
            String fromDateStr = request.getParameter("fromDate");
            String toDateStr = request.getParameter("toDate");
            
            // Get current enrollment
            StudentEnrollment currentEnrollment = studentEnrollmentDao.findCurrentEnrollment(studentId);
            
            // Get all enrollments for this student
            List<StudentEnrollment> allEnrollments = studentEnrollmentDao.findByStudent(studentId);
            
            // Filter enrollments to those in HOD's department
            List<StudentEnrollment> departmentEnrollments = new ArrayList<>();
            
            for (StudentEnrollment enrollment : allEnrollments) {
                com.attendance.models.Class cls = classDao.findById(enrollment.getClassId());
                if (cls != null && cls.getDepartmentId() == department.getDepartmentId()) {
                    departmentEnrollments.add(enrollment);
                }
            }
            
            if (departmentEnrollments.isEmpty()) {
                request.setAttribute("error", "Student has no enrollments in your department.");
                request.setAttribute("student", student);
                request.setAttribute("department", department);
                request.getRequestDispatcher("/WEB-INF/views/hod/students/attendance.jsp").forward(request, response);
                return;
            }
            
            // Determine which enrollment to use
            StudentEnrollment enrollmentToUse = currentEnrollment;
            
            if (semesterFilter != null && !semesterFilter.trim().isEmpty() && 
                academicYearFilter != null && !academicYearFilter.trim().isEmpty()) {
                // Find matching enrollment
                for (StudentEnrollment enrollment : departmentEnrollments) {
                    if (semesterFilter.equals(enrollment.getSemester()) && 
                        academicYearFilter.equals(enrollment.getAcademicYear())) {
                        enrollmentToUse = enrollment;
                        break;
                    }
                }
            } else if (currentEnrollment == null || !departmentEnrollments.contains(currentEnrollment)) {
                // Use the most recent department enrollment
                enrollmentToUse = departmentEnrollments.stream()
                    .max(Comparator.comparing(StudentEnrollment::getEnrollmentDate))
                    .orElse(null);
            }
            
            if (enrollmentToUse == null) {
                request.setAttribute("error", "No valid enrollment found for the selected filters.");
                request.setAttribute("student", student);
                request.setAttribute("department", department);
                request.getRequestDispatcher("/WEB-INF/views/hod/students/attendance.jsp").forward(request, response);
                return;
            }
            
            // Get class details
            com.attendance.models.Class cls = classDao.findById(enrollmentToUse.getClassId());
            
            if (cls == null || cls.getDepartmentId() != department.getDepartmentId()) {
                request.setAttribute("error", "Invalid class enrollment or class not in your department.");
                request.setAttribute("student", student);
                request.setAttribute("department", department);
                request.getRequestDispatcher("/WEB-INF/views/hod/students/attendance.jsp").forward(request, response);
                return;
            }
            
            // Get class details for each enrollment
            Map<Integer, com.attendance.models.Class> classes = new HashMap<>();
            
            for (StudentEnrollment enrollment : departmentEnrollments) {
                com.attendance.models.Class classObj = classDao.findById(enrollment.getClassId());
                if (classObj != null) {
                    classes.put(enrollment.getClassId(), classObj);
                }
            }
            
            // Get department subjects for enrollment semester
            List<DepartmentSubject> departmentSubjects = departmentSubjectDao.findByDepartmentAndSemester(
                    department.getDepartmentId(), enrollmentToUse.getSemester());
            
            // Get subject details
            Map<String, Subject> subjects = new HashMap<>();
            for (DepartmentSubject deptSubject : departmentSubjects) {
                Subject subject = subjectDao.findByCode(deptSubject.getSubjectCode());
                if (subject != null) {
                    subjects.put(deptSubject.getSubjectCode(), subject);
                }
            }
            
            // Get attendance records based on filters
            List<Attendance> attendanceRecords;
            
            if (subjectCode != null && !subjectCode.trim().isEmpty() && subjects.containsKey(subjectCode)) {
                // Filter by subject
                attendanceRecords = attendanceDao.findByStudentSubjectSemesterAndYear(
                        studentId, subjectCode, enrollmentToUse.getSemester(), enrollmentToUse.getAcademicYear());
            } else {
                // Get attendance records for all subjects in this semester
                attendanceRecords = new ArrayList<>();
                for (String code : subjects.keySet()) {
                    List<Attendance> records = attendanceDao.findByStudentSubjectSemesterAndYear(
                            studentId, code, enrollmentToUse.getSemester(), enrollmentToUse.getAcademicYear());
                    attendanceRecords.addAll(records);
                }
            }
            
            // Apply date filters if provided
            if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
                LocalDate fromDate = DateUtils.parseDate(fromDateStr);
                if (fromDate != null) {
                    attendanceRecords.removeIf(record -> 
                        record.getAttendanceDate().toLocalDate().isBefore(fromDate));
                }
            }
            
            if (toDateStr != null && !toDateStr.trim().isEmpty()) {
                LocalDate toDate = DateUtils.parseDate(toDateStr);
                if (toDate != null) {
                    attendanceRecords.removeIf(record -> 
                        record.getAttendanceDate().toLocalDate().isAfter(toDate));
                }
            }
            
            // Calculate attendance summary
            Map<String, Map<String, Object>> attendanceSummary = new HashMap<>();
            
            for (String code : subjects.keySet()) {
                // Get attendance records
                List<Attendance> records = attendanceDao.findByStudentSubjectSemesterAndYear(
                        studentId, code, enrollmentToUse.getSemester(), enrollmentToUse.getAcademicYear());
                
                int totalClasses = records.size();
                int presentCount = 0;
                int absentCount = 0;
                int leaveCount = 0;
                
                for (Attendance record : records) {
                    switch (record.getStatus()) {
                        case "Present":
                            presentCount++;
                            break;
                        case "Absent":
                            absentCount++;
                            break;
                        case "Leave":
                            leaveCount++;
                            break;
                    }
                }
                
                // Calculate attendance percentage
                double percentage = totalClasses > 0 ? (double) presentCount / totalClasses * 100 : 0;
                
                Map<String, Object> subjectSummary = new HashMap<>();
                subjectSummary.put("totalClasses", totalClasses);
                subjectSummary.put("presentCount", presentCount);
                subjectSummary.put("absentCount", absentCount);
                subjectSummary.put("leaveCount", leaveCount);
                subjectSummary.put("percentage", percentage);
                
                attendanceSummary.put(code, subjectSummary);
            }
            
            // Get list of semesters and academic years for filters
            Set<String> availableSemesters = new HashSet<>();
            Set<String> availableAcademicYears = new HashSet<>();
            
            for (StudentEnrollment enrollment : departmentEnrollments) {
                availableSemesters.add(enrollment.getSemester());
                availableAcademicYears.add(enrollment.getAcademicYear());
            }
            
            request.setAttribute("student", student);
            request.setAttribute("enrollment", enrollmentToUse);
            request.setAttribute("enrollments", departmentEnrollments);
            request.setAttribute("class", cls);
            request.setAttribute("classes", classes);
            request.setAttribute("department", department);
            request.setAttribute("subjects", subjects);
            request.setAttribute("attendanceRecords", attendanceRecords);
            request.setAttribute("attendanceSummary", attendanceSummary);
            request.setAttribute("availableSemesters", availableSemesters);
            request.setAttribute("availableAcademicYears", availableAcademicYears);
            
            // Set filter parameters for maintaining state
            request.setAttribute("subjectFilter", subjectCode);
            request.setAttribute("semesterFilter", semesterFilter);
            request.setAttribute("academicYearFilter", academicYearFilter);
            request.setAttribute("fromDateFilter", fromDateStr);
            request.setAttribute("toDateFilter", toDateStr);
            
            request.getRequestDispatcher("/WEB-INF/views/hod/students/attendance.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while viewing student attendance (HOD view)", e);
            response.sendRedirect(request.getContextPath() + "/hod/students?error=database");
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student ID");
        }
    }
    
    /**
     * Helper method to extract ID from path
     */
    private int extractIdFromPath(String pathInfo, String prefix) {
        return Integer.parseInt(pathInfo.substring(prefix.length()));
    }
}