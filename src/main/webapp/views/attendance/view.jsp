<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.attendance.models.User" %>
<%@ page import="com.attendance.models.Attendance" %>
<%@ page import="com.attendance.models.Subject" %>
<%@ page import="com.attendance.models.StudentEnrollment" %>
<%@ page import="com.attendance.utils.SessionUtil" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.Date" %>
<%-- Import Class model with fully qualified name to avoid ambiguity --%>
<%@ page import="com.attendance.models.Class" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>View Attendance - Attendance Management System</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.1/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>
    <%-- Check if user is logged in --%>
    <% 
        User user = SessionUtil.getUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        String userRole = user.getRole();
    %>

    <%-- Include header --%>
    <jsp:include page="/views/common/header.jsp" />

    <div class="container mt-4">
        <div class="row">
            <div class="col-md-12">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
                        <li class="breadcrumb-item active" aria-current="page">View Attendance</li>
                    </ol>
                </nav>
                
                <h2>View Attendance</h2>
                <p class="lead">Check and analyze attendance records</p>
            </div>
        </div>
        
        <% if (request.getAttribute("error") != null) { %>
            <div class="row">
                <div class="col-md-12">
                    <div class="alert alert-danger">
                        <%= request.getAttribute("error") %>
                    </div>
                </div>
            </div>
        <% } %>
        
        <div class="row">
            <div class="col-md-4">
                <div class="card">
                    <div class="card-header bg-primary text-white">
                        <h5 class="card-title mb-0">Filter Options</h5>
                    </div>
                    <div class="card-body">
                        <!-- Different filter forms based on user role -->
                        <% if ("Student".equals(userRole)) { %>
                            <!-- Student Filter Form -->
                            <form action="${pageContext.request.contextPath}/attendance/view" method="get">
                                <div class="form-group">
                                    <label for="subjectCode">Subject</label>
                                    <select class="form-control" id="subjectCode" name="subjectCode">
                                        <option value="">All Subjects</option>
                                        <% 
                                            List<Subject> subjects = (List<Subject>) request.getAttribute("subjects");
                                            String selectedSubject = request.getParameter("subjectCode");
                                            
                                            if (subjects != null) {
                                                for (Subject subject : subjects) {
                                        %>
                                            <option value="<%= subject.getSubjectCode() %>" 
                                                    <%= (selectedSubject != null && selectedSubject.equals(subject.getSubjectCode())) ? "selected" : "" %>>
                                                <%= subject.getSubjectName() %> (<%= subject.getSubjectCode() %>)
                                            </option>
                                        <% 
                                                }
                                            }
                                        %>
                                    </select>
                                </div>
                                
                                <div class="form-group">
                                    <label for="fromDate">From Date</label>
                                    <input type="date" class="form-control" id="fromDate" name="fromDate" 
                                           value="<%= request.getParameter("fromDate") != null ? request.getParameter("fromDate") : "" %>">
                                </div>
                                
                                <div class="form-group">
                                    <label for="toDate">To Date</label>
                                    <input type="date" class="form-control" id="toDate" name="toDate" 
                                           value="<%= request.getParameter("toDate") != null ? request.getParameter("toDate") : "" %>">
                                </div>
                                
                                <button type="submit" class="btn btn-primary btn-block">
                                    <i class="fas fa-search"></i> Apply Filters
                                </button>
                            </form>
                        <% } else if ("Teacher".equals(userRole) || "Class Teacher".equals(userRole)) { %>
                            <!-- Teacher Filter Form -->
                            <form action="${pageContext.request.contextPath}/attendance/view" method="get">
                                <div class="form-group">
                                    <label for="classId">Class</label>
                                    <select class="form-control" id="classId" name="classId" required>
                                        <option value="">Select Class</option>
                                        <% 
                                            Collection<Class> classes = (Collection<Class>) request.getAttribute("classes");
                                            String selectedClassId = request.getParameter("classId");
                                            
                                            if (classes != null) {
                                                for (Class classObj : classes) {
                                        %>
                                            <option value="<%= classObj.getClassId() %>" 
                                                    <%= (selectedClassId != null && selectedClassId.equals(String.valueOf(classObj.getClassId()))) ? "selected" : "" %>>
                                                <%= classObj.getClassName() %> - <%= classObj.getDepartment().getDepartmentName() %>
                                            </option>
                                        <% 
                                                }
                                            }
                                        %>
                                    </select>
                                </div>
                                
                                <div class="form-group">
                                    <label for="subjectCode">Subject</label>
                                    <select class="form-control" id="subjectCode" name="subjectCode" required>
                                        <option value="">Select Subject</option>
                                        <% 
                                            Collection<Subject> teacherSubjects = (Collection<Subject>) request.getAttribute("subjects");
                                            String selectedTeacherSubject = request.getParameter("subjectCode");
                                            
                                            if (teacherSubjects != null) {
                                                for (Subject subject : teacherSubjects) {
                                        %>
                                            <option value="<%= subject.getSubjectCode() %>" 
                                                    <%= (selectedTeacherSubject != null && selectedTeacherSubject.equals(subject.getSubjectCode())) ? "selected" : "" %>>
                                                <%= subject.getSubjectName() %> (<%= subject.getSubjectCode() %>)
                                            </option>
                                        <% 
                                                }
                                            }
                                        %>
                                    </select>
                                </div>
                                
                                <div class="form-group">
                                    <label for="date">Date</label>
                                    <input type="date" class="form-control" id="date" name="date" required
                                           value="<%= request.getParameter("date") != null ? request.getParameter("date") : new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()) %>">
                                </div>
                                
                                <div class="form-group">
                                    <label for="semester">Semester</label>
                                    <select class="form-control" id="semester" name="semester" required>
                                        <option value="">Select Semester</option>
                                        <option value="1" <%= "1".equals(request.getParameter("semester")) ? "selected" : "" %>>Semester 1</option>
                                        <option value="2" <%= "2".equals(request.getParameter("semester")) ? "selected" : "" %>>Semester 2</option>
                                        <option value="3" <%= "3".equals(request.getParameter("semester")) ? "selected" : "" %>>Semester 3</option>
                                        <option value="4" <%= "4".equals(request.getParameter("semester")) ? "selected" : "" %>>Semester 4</option>
                                        <option value="5" <%= "5".equals(request.getParameter("semester")) ? "selected" : "" %>>Semester 5</option>
                                        <option value="6" <%= "6".equals(request.getParameter("semester")) ? "selected" : "" %>>Semester 6</option>
                                    </select>
                                </div>
                                
                                <button type="submit" class="btn btn-primary btn-block">
                                    <i class="fas fa-search"></i> View Attendance
                                </button>
                            </form>
                        <% } else if ("HOD".equals(userRole)) { %>
                            <!-- HOD Filter Form -->
                            <form action="${pageContext.request.contextPath}/attendance/view" method="get">
                                <div class="form-group">
                                    <label for="classId">Class</label>
                                    <select class="form-control" id="classId" name="classId" required>
                                        <option value="">Select Class</option>
                                        <% 
                                            List<Class> hodClasses = (List<Class>) request.getAttribute("classes");
                                            String selectedHodClassId = request.getParameter("classId");
                                            
                                            if (hodClasses != null) {
                                                for (Class classObj : hodClasses) {
                                        %>
                                            <option value="<%= classObj.getClassId() %>" 
                                                    <%= (selectedHodClassId != null && selectedHodClassId.equals(String.valueOf(classObj.getClassId()))) ? "selected" : "" %>>
                                                <%= classObj.getClassName() %>
                                            </option>
                                        <% 
                                                }
                                            }
                                        %>
                                    </select>
                                </div>
                                
                                <div class="form-group">
                                    <label for="subjectCode">Subject</label>
                                    <select class="form-control" id="subjectCode" name="subjectCode" required>
                                        <option value="">Select Subject</option>
                                        <% 
                                            List<Subject> hodSubjects = (List<Subject>) request.getAttribute("subjects");
                                            String selectedHodSubject = request.getParameter("subjectCode");
                                            
                                            if (hodSubjects != null) {
                                                for (Subject subject : hodSubjects) {
                                        %>
                                            <option value="<%= subject.getSubjectCode() %>" 
                                                    <%= (selectedHodSubject != null && selectedHodSubject.equals(subject.getSubjectCode())) ? "selected" : "" %>>
                                                <%= subject.getSubjectName() %> (<%= subject.getSubjectCode() %>)
                                            </option>
                                        <% 
                                                }
                                            }
                                        %>
                                    </select>
                                </div>
                                
                                <div class="form-group">
                                    <label for="date">Date</label>
                                    <input type="date" class="form-control" id="date" name="date" required
                                           value="<%= request.getParameter("date") != null ? request.getParameter("date") : new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()) %>">
                                </div>
                                
                                <div class="form-group">
                                    <label for="semester">Semester</label>
                                    <select class="form-control" id="semester" name="semester" required>
                                        <option value="">Select Semester</option>
                                        <option value="1" <%= "1".equals(request.getParameter("semester")) ? "selected" : "" %>>Semester 1</option>
                                        <option value="2" <%= "2".equals(request.getParameter("semester")) ? "selected" : "" %>>Semester 2</option>
                                        <option value="3" <%= "3".equals(request.getParameter("semester")) ? "selected" : "" %>>Semester 3</option>
                                        <option value="4" <%= "4".equals(request.getParameter("semester")) ? "selected" : "" %>>Semester 4</option>
                                        <option value="5" <%= "5".equals(request.getParameter("semester")) ? "selected" : "" %>>Semester 5</option>
                                        <option value="6" <%= "6".equals(request.getParameter("semester")) ? "selected" : "" %>>Semester 6</option>
                                    </select>
                                </div>
                                
                                <button type="submit" class="btn btn-primary btn-block">
                                    <i class="fas fa-search"></i> View Attendance
                                </button>
                            </form>
                        <% } else if ("Principal".equals(userRole)) { %>
                            <!-- Principal Filter Form -->
                            <form action="${pageContext.request.contextPath}/attendance/view" method="get">
                                <div class="form-group">
                                    <label for="departmentId">Department</label>
                                    <select class="form-control" id="departmentId" name="departmentId" required>
                                        <option value="">Select Department</option>
                                        <% 
                                            List<Department> departments = (List<Department>) request.getAttribute("departments");
                                            String selectedDepartmentId = request.getParameter("departmentId");
                                            
                                            if (departments != null) {
                                                for (Department department : departments) {
                                        %>
                                            <option value="<%= department.getDepartmentId() %>" 
                                                    <%= (selectedDepartmentId != null && selectedDepartmentId.equals(String.valueOf(department.getDepartmentId()))) ? "selected" : "" %>>
                                                <%= department.getDepartmentName() %>
                                            </option>
                                        <% 
                                                }
                                            }
                                        %>
                                    </select>
                                </div>
                                
                                <% if (request.getParameter("departmentId") != null) { %>
                                <div class="form-group">
                                    <label for="classId">Class</label>
                                    <select class="form-control" id="classId" name="classId" required>
                                        <option value="">Select Class</option>
                                        <% 
                                            List<Class> principalClasses = (List<Class>) request.getAttribute("classes");
                                            String selectedPrincipalClassId = request.getParameter("classId");
                                            
                                            if (principalClasses != null) {
                                                for (Class classObj : principalClasses) {
                                        %>
                                            <option value="<%= classObj.getClassId() %>" 
                                                    <%= (selectedPrincipalClassId != null && selectedPrincipalClassId.equals(String.valueOf(classObj.getClassId()))) ? "selected" : "" %>>
                                                <%= classObj.getClassName() %>
                                            </option>
                                        <% 
                                                }
                                            }
                                        %>
                                    </select>
                                </div>
                                <% } %>
                                
                                <% if (request.getParameter("classId") != null) { %>
                                <div class="form-group">
                                    <label for="subjectCode">Subject</label>
                                    <select class="form-control" id="subjectCode" name="subjectCode" required>
                                        <option value="">Select Subject</option>
                                        <% 
                                            List<Subject> principalSubjects = (List<Subject>) request.getAttribute("subjects");
                                            String selectedPrincipalSubject = request.getParameter("subjectCode");
                                            
                                            if (principalSubjects != null) {
                                                for (Subject subject : principalSubjects) {
                                        %>
                                            <option value="<%= subject.getSubjectCode() %>" 
                                                    <%= (selectedPrincipalSubject != null && selectedPrincipalSubject.equals(subject.getSubjectCode())) ? "selected" : "" %>>
                                                <%= subject.getSubjectName() %> (<%= subject.getSubjectCode() %>)
                                            </option>
                                        <% 
                                                }
                                            }
                                        %>
                                    </select>
                                </div>
                                
                                <div class="form-group">
                                    <label for="date">Date</label>
                                    <input type="date" class="form-control" id="date" name="date" required
                                           value="<%= request.getParameter("date") != null ? request.getParameter("date") : new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()) %>">
                                </div>
                                
                                <div class="form-group">
                                    <label for="semester">Semester</label>
                                    <select class="form-control" id="semester" name="semester" required>
                                        <option value="">Select Semester</option>
                                        <option value="1" <%= "1".equals(request.getParameter("semester")) ? "selected" : "" %>>Semester 1</option>
                                        <option value="2" <%= "2".equals(request.getParameter("semester")) ? "selected" : "" %>>Semester 2</option>
                                        <option value="3" <%= "3".equals(request.getParameter("semester")) ? "selected" : "" %>>Semester 3</option>
                                        <option value="4" <%= "4".equals(request.getParameter("semester")) ? "selected" : "" %>>Semester 4</option>
                                        <option value="5" <%= "5".equals(request.getParameter("semester")) ? "selected" : "" %>>Semester 5</option>
                                        <option value="6" <%= "6".equals(request.getParameter("semester")) ? "selected" : "" %>>Semester 6</option>
                                    </select>
                                </div>
                                <% } %>
                                
                                <button type="submit" class="btn btn-primary btn-block">
                                    <% if (request.getParameter("classId") != null && request.getParameter("departmentId") != null) { %>
                                        <i class="fas fa-search"></i> View Attendance
                                    <% } else { %>
                                        <i class="fas fa-arrow-right"></i> Continue
                                    <% } %>
                                </button>
                            </form>
                        <% } %>
                    </div>
                </div>
                
                <% if ("Student".equals(userRole)) { 
                    // Show attendance summary for students
                    Map<String, Integer> attendanceSummary = (Map<String, Integer>) request.getAttribute("attendanceSummary");
                    Double overallPercentage = (Double) request.getAttribute("overallPercentage");
                    
                    if (attendanceSummary != null || overallPercentage != null) {
                %>
                <div class="card mt-4">
                    <div class="card-header bg-info text-white">
                        <h5 class="card-title mb-0">Attendance Summary</h5>
                    </div>
                    <div class="card-body">
                        <% if (attendanceSummary != null) { %>
                            <h6>Subject Attendance</h6>
                            <div class="row text-center">
                                <div class="col-4">
                                    <div class="counter-box bg-success text-white p-2 rounded">
                                        <h3><%= attendanceSummary.get("Present") != null ? attendanceSummary.get("Present") : 0 %></h3>
                                        <p>Present</p>
                                    </div>
                                </div>
                                <div class="col-4">
                                    <div class="counter-box bg-danger text-white p-2 rounded">
                                        <h3><%= attendanceSummary.get("Absent") != null ? attendanceSummary.get("Absent") : 0 %></h3>
                                        <p>Absent</p>
                                    </div>
                                </div>
                                <div class="col-4">
                                    <div class="counter-box bg-warning text-dark p-2 rounded">
                                        <h3><%= attendanceSummary.get("Leave") != null ? attendanceSummary.get("Leave") : 0 %></h3>
                                        <p>Leave</p>
                                    </div>
                                </div>
                            </div>
                            
                            <% 
                                int total = attendanceSummary.get("Total") != null ? attendanceSummary.get("Total") : 0;
                                int present = attendanceSummary.get("Present") != null ? attendanceSummary.get("Present") : 0;
                                double percentage = total > 0 ? (present * 100.0 / total) : 0;
                                String badgeClass = percentage >= 75 ? "success" : (percentage >= 60 ? "warning" : "danger");
                            %>
                            
                            <div class="mt-3 text-center">
                                <h5>Attendance Percentage: 
                                    <span class="badge badge-<%= badgeClass %>"><%= String.format("%.2f", percentage) %>%</span>
                                </h5>
                                <div class="progress mt-2">
                                    <div class="progress-bar bg-<%= badgeClass %>" role="progressbar" style="width: <%= percentage %>%;" 
                                         aria-valuenow="<%= percentage %>" aria-valuemin="0" aria-valuemax="100">
                                        <%= String.format("%.2f", percentage) %>%
                                    </div>
                                </div>
                            </div>
                        <% } else if (overallPercentage != null) { %>
                            <h6>Overall Attendance</h6>
                            <% 
                                String badgeClass = overallPercentage >= 75 ? "success" : (overallPercentage >= 60 ? "warning" : "danger");
                            %>
                            <div class="text-center">
                                <h5>Attendance Percentage: 
                                    <span class="badge badge-<%= badgeClass %>"><%= String.format("%.2f", overallPercentage) %>%</span>
                                </h5>
                                <div class="progress mt-2">
                                    <div class="progress-bar bg-<%= badgeClass %>" role="progressbar" style="width: <%= overallPercentage %>%;" 
                                         aria-valuenow="<%= overallPercentage %>" aria-valuemin="0" aria-valuemax="100">
                                        <%= String.format("%.2f", overallPercentage) %>%
                                    </div>
                                </div>
                            </div>
                        <% } %>
                    </div>
                </div>
                <% } %>
            </div>
            
            <div class="col-md-8">
                <div class="card">
                    <div class="card-header bg-primary text-white">
                        <h5 class="card-title mb-0">Attendance Records</h5>
                    </div>
                    <div class="card-body">
                        <% 
                            if ("Student".equals(userRole)) {
                                // Student view - Show attendance list for the student
                                List<Attendance> attendanceList = (List<Attendance>) request.getAttribute("attendanceList");
                                
                                if (attendanceList != null && !attendanceList.isEmpty()) {
                        %>
                            <div class="table-responsive">
                                <table class="table table-striped">
                                    <thead>
                                        <tr>
                                            <th>Date</th>
                                            <th>Subject</th>
                                            <th>Semester</th>
                                            <th>Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (Attendance attendance : attendanceList) { %>
                                            <tr>
                                                <td><%= attendance.getAttendanceDate() %></td>
                                                <td><%= attendance.getSubject().getSubjectName() %> (<%= attendance.getSubjectCode() %>)</td>
                                                <td>Semester <%= attendance.getSemester() %></td>
                                                <td>
                                                    <span class="badge badge-<%= "Present".equals(attendance.getStatus()) ? "success" : 
                                                                                ("Absent".equals(attendance.getStatus()) ? "danger" : "warning") %>">
                                                        <%= attendance.getStatus() %>
                                                    </span>
                                                </td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        <% } else { %>
                            <div class="alert alert-info">
                                <h5><i class="fas fa-info-circle"></i> No Attendance Records</h5>
                                <p>No attendance records found matching the selected criteria.</p>
                            </div>
                        <% 
                                }
                            } else {
                                // Teacher/HOD/Principal view - Show class attendance
                                List<Attendance> classAttendance = (List<Attendance>) request.getAttribute("classAttendance");
                                
                                if (classAttendance != null && !classAttendance.isEmpty()) {
                        %>
                            <div class="table-responsive">
                                <table class="table table-striped">
                                    <thead>
                                        <tr>
                                            <th>Student Name</th>
                                            <th>Enrollment ID</th>
                                            <th>Status</th>
                                            <% if ("HOD".equals(userRole) || "Principal".equals(userRole)) { %>
                                                <th>Marked By</th>
                                            <% } %>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (Attendance attendance : classAttendance) { %>
                                            <tr>
                                                <td><%= attendance.getStudent().getName() %></td>
                                                <td>
                                                    <!-- In a real application, this would come from the student enrollment -->
                                                    <%= attendance.getStudent().getUserId() %>
                                                </td>
                                                <td>
                                                    <span class="badge badge-<%= "Present".equals(attendance.getStatus()) ? "success" : 
                                                                                ("Absent".equals(attendance.getStatus()) ? "danger" : "warning") %>">
                                                        <%= attendance.getStatus() %>
                                                    </span>
                                                </td>
                                                <% if ("HOD".equals(userRole) || "Principal".equals(userRole)) { %>
                                                    <td>
                                                        <!-- In a real application, this would show the teacher who marked attendance -->
                                                        <span class="text-muted">System</span>
                                                    </td>
                                                <% } %>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        <% } else if (request.getParameter("classId") != null && request.getParameter("subjectCode") != null && 
                                    request.getParameter("date") != null && request.getParameter("semester") != null) { %>
                            <div class="alert alert-info">
                                <h5><i class="fas fa-info-circle"></i> No Attendance Records</h5>
                                <p>No attendance records found for the selected class, subject, date, and semester.</p>
                            </div>
                        <% } else { %>
                            <div class="alert alert-info">
                                <h5><i class="fas fa-info-circle"></i> Select Filters</h5>
                                <p>Please select the filters on the left to view attendance records.</p>
                            </div>
                        <% 
                                }
                            }
                        %>
                    </div>
                </div>
                
                <% if (!"Student".equals(userRole) && request.getParameter("classId") != null && 
                     request.getParameter("subjectCode") != null && request.getParameter("date") != null) { %>
                <div class="card mt-4">
                    <div class="card-header bg-info text-white">
                        <h5 class="card-title mb-0">Attendance Summary</h5>
                    </div>
                    <div class="card-body">
                        <% 
                            List<Attendance> classAttendance = (List<Attendance>) request.getAttribute("classAttendance");
                            if (classAttendance != null) {
                                int total = classAttendance.size();
                                int present = 0;
                                int absent = 0;
                                int leave = 0;
                                
                                for (Attendance attendance : classAttendance) {
                                    if ("Present".equals(attendance.getStatus())) {
                                        present++;
                                    } else if ("Absent".equals(attendance.getStatus())) {
                                        absent++;
                                    } else if ("Leave".equals(attendance.getStatus())) {
                                        leave++;
                                    }
                                }
                                
                                double presentPercentage = total > 0 ? (present * 100.0 / total) : 0;
                                double absentPercentage = total > 0 ? (absent * 100.0 / total) : 0;
                                double leavePercentage = total > 0 ? (leave * 100.0 / total) : 0;
                        %>
                            <div class="row text-center">
                                <div class="col-md-4">
                                    <div class="counter-box bg-success text-white p-2 rounded">
                                        <h3><%= present %></h3>
                                        <p>Present</p>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="counter-box bg-danger text-white p-2 rounded">
                                        <h3><%= absent %></h3>
                                        <p>Absent</p>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="counter-box bg-warning text-dark p-2 rounded">
                                        <h3><%= leave %></h3>
                                        <p>Leave</p>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="mt-3">
                                <h6>Class Attendance Rate: <%= String.format("%.2f", presentPercentage) %>%</h6>
                                <div class="progress">
                                    <div class="progress-bar bg-success" role="progressbar" style="width: <%= presentPercentage %>%;" 
                                         aria-valuenow="<%= presentPercentage %>" aria-valuemin="0" aria-valuemax="100">
                                        <%= String.format("%.2f", presentPercentage) %>%
                                    </div>
                                </div>
                            </div>
                        <% } %>
                    </div>
                </div>
                <% } %>
            </div>
        </div>
    </div>

    <%-- Include footer --%>
    <jsp:include page="/views/common/footer.jsp" />

    <script src="https://code.jquery.com/jquery-3.5.1.slim.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/scripts.js"></script>
    
    <script>
        // Dynamic form handling for Principal's cascading dropdowns
        $(document).ready(function() {
            // Auto-submit department selection for Principal
            $('#departmentId').change(function() {
                if ($(this).val() !== '') {
                    $(this).closest('form').submit();
                }
            });
            
            // Auto-submit class selection for Principal
            $('#classId').change(function() {
                if ($(this).val() !== '') {
                    $(this).closest('form').submit();
                }
            });
        });
    </script>
</body>
</html>
