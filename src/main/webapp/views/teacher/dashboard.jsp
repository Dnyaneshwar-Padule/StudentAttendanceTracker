<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- Import Class model with fully qualified name to avoid ambiguity --%>
<%@ page import="com.attendance.models.Class" %>
<%@ page import="com.attendance.models.User" %>
<%@ page import="com.attendance.models.Department" %>
<%@ page import="com.attendance.models.Subject" %>
<%@ page import="com.attendance.models.TeacherAssignment" %>
<%@ page import="com.attendance.utils.SessionUtil" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Teacher Dashboard - Attendance Management System</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.1/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>
    <%-- Check if user is logged in and is a teacher --%>
    <% 
        User user = SessionUtil.getUser(request);
        if (user == null || !"Teacher".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
    %>

    <%-- Include header --%>
    <jsp:include page="/views/common/header.jsp" />

    <div class="container mt-4">
        <div class="row">
            <div class="col-md-12">
                <h1>Teacher Dashboard</h1>
                <p class="lead">Welcome, <%= user.getName() %>!</p>
            </div>
        </div>

        <% 
            Department department = (Department) request.getAttribute("department");
            List<TeacherAssignment> assignments = (List<TeacherAssignment>) request.getAttribute("assignments");
            Boolean isClassTeacher = (Boolean) request.getAttribute("isClassTeacher");
            
            if (department == null) {
        %>
            <div class="row mt-4">
                <div class="col-md-12">
                    <div class="alert alert-warning">
                        <h4 class="alert-heading">Department Assignment Required</h4>
                        <p>
                            You need to be assigned to a department to access the teacher dashboard features.
                            Please contact the HOD to get assigned to a department.
                        </p>
                    </div>
                </div>
            </div>
        <% } else { %>
            <div class="row mt-4">
                <div class="col-md-4">
                    <div class="card">
                        <div class="card-header bg-primary text-white">
                            <h5 class="card-title mb-0">Department Information</h5>
                        </div>
                        <div class="card-body">
                            <p><strong>Department:</strong> <%= department.getDepartmentName() %></p>
                            <p><strong>Role:</strong> <%= user.getRole() %></p>
                            <% if (isClassTeacher != null && isClassTeacher) { %>
                                <p><span class="badge badge-info">Class Teacher</span></p>
                            <% } %>
                        </div>
                    </div>
                    
                    <div class="card mt-4">
                        <div class="card-header bg-primary text-white">
                            <h5 class="card-title mb-0">Quick Actions</h5>
                        </div>
                        <div class="card-body">
                            <a href="${pageContext.request.contextPath}/attendance/mark" class="btn btn-outline-primary btn-block mb-2">
                                <i class="fas fa-clipboard-check"></i> Mark Attendance
                            </a>
                            <a href="${pageContext.request.contextPath}/attendance/view" class="btn btn-outline-primary btn-block mb-2">
                                <i class="fas fa-eye"></i> View Attendance
                            </a>
                            <% if (isClassTeacher != null && isClassTeacher) { %>
                                <a href="${pageContext.request.contextPath}/enrollment/pending" class="btn btn-outline-primary btn-block mb-2">
                                    <i class="fas fa-user-check"></i> Student Enrollment Requests
                                </a>
                            <% } %>
                            <a href="#" class="btn btn-outline-primary btn-block">
                                <i class="fas fa-file-alt"></i> Generate Reports
                            </a>
                        </div>
                    </div>
                </div>
                
                <div class="col-md-8">
                    <div class="card">
                        <div class="card-header bg-primary text-white">
                            <h5 class="card-title mb-0">Your Subject Assignments</h5>
                        </div>
                        <div class="card-body">
                            <div class="table-responsive">
                                <table class="table">
                                    <thead>
                                        <tr>
                                            <th>Class</th>
                                            <th>Subject</th>
                                            <th>Assignment Type</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% 
                                            if (assignments != null && !assignments.isEmpty()) {
                                                for (TeacherAssignment assignment : assignments) {
                                        %>
                                            <tr>
                                                <td><%= assignment.getClassObj().getClassName() %></td>
                                                <td><%= assignment.getSubject().getSubjectName() %> (<%= assignment.getSubjectCode() %>)</td>
                                                <td><%= assignment.getAssignmentType() %></td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/attendance/mark?classId=<%= assignment.getClassId() %>&subjectCode=<%= assignment.getSubjectCode() %>" 
                                                       class="btn btn-sm btn-primary">
                                                        <i class="fas fa-edit"></i> Mark Attendance
                                                    </a>
                                                </td>
                                            </tr>
                                        <% 
                                                }
                                            } else {
                                        %>
                                            <tr>
                                                <td colspan="4" class="text-center">No subject assignments found</td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                    
                    <div class="card mt-4">
                        <div class="card-header bg-primary text-white">
                            <h5 class="card-title mb-0">Recent Activity</h5>
                        </div>
                        <div class="card-body">
                            <ul class="list-group">
                                <li class="list-group-item">
                                    <span class="text-muted mr-2"><i class="fas fa-clock"></i> Today</span>
                                    You marked attendance for FY Computer Science - Programming
                                </li>
                                <li class="list-group-item">
                                    <span class="text-muted mr-2"><i class="fas fa-clock"></i> Yesterday</span>
                                    You marked attendance for SY Electronics - Digital Circuits
                                </li>
                                <li class="list-group-item">
                                    <span class="text-muted mr-2"><i class="fas fa-clock"></i> 3 days ago</span>
                                    You were assigned to a new subject: Computer Networks
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        <% } %>
    </div>

    <%-- Include footer --%>
    <jsp:include page="/views/common/footer.jsp" />

    <script src="https://code.jquery.com/jquery-3.5.1.slim.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/scripts.js"></script>
</body>
</html>
