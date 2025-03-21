<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.attendance.models.*" %>
<%@ page import="com.attendance.utils.SessionUtil" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Class Teacher Dashboard - Attendance Management System</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.1/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>
    <%-- Check if user is logged in and is a class teacher --%>
    <% 
        User user = SessionUtil.getUser(request);
        if (user == null || !"Class Teacher".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
    %>

    <%-- Include header --%>
    <jsp:include page="/views/common/header.jsp" />

    <div class="container mt-4">
        <div class="row">
            <div class="col-md-12">
                <h1>Class Teacher Dashboard</h1>
                <p class="lead">Welcome, <%= user.getName() %>!</p>
            </div>
        </div>

        <% 
            Department department = (Department) request.getAttribute("department");
            List<TeacherAssignment> assignments = (List<TeacherAssignment>) request.getAttribute("assignments");
            List<EnrollmentRequest> pendingRequests = (List<EnrollmentRequest>) request.getAttribute("pendingRequests");
            List<StudentEnrollment> enrollments = (List<StudentEnrollment>) request.getAttribute("enrollments");
            Class classObj = (Class) request.getAttribute("classObj");
            
            if (department == null || classObj == null) {
        %>
            <div class="row mt-4">
                <div class="col-md-12">
                    <div class="alert alert-warning">
                        <h4 class="alert-heading">Class Assignment Required</h4>
                        <p>
                            You need to be assigned as a class teacher to a specific class to access the class teacher dashboard features.
                            Please contact the HOD to get assigned to a class.
                        </p>
                    </div>
                </div>
            </div>
        <% } else { %>
            <div class="row mt-4">
                <div class="col-md-4">
                    <div class="card">
                        <div class="card-header bg-primary text-white">
                            <h5 class="card-title mb-0">Class Information</h5>
                        </div>
                        <div class="card-body">
                            <p><strong>Department:</strong> <%= department.getDepartmentName() %></p>
                            <p><strong>Class:</strong> <%= classObj.getClassName() %></p>
                            <p><strong>Role:</strong> <span class="badge badge-info">Class Teacher</span></p>
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
                            <a href="${pageContext.request.contextPath}/enrollment/pending" class="btn btn-outline-primary btn-block mb-2">
                                <i class="fas fa-user-check"></i> Enrollment Requests 
                                <% if (pendingRequests != null && !pendingRequests.isEmpty()) { %>
                                    <span class="badge badge-danger"><%= pendingRequests.size() %></span>
                                <% } %>
                            </a>
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
                        <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                            <h5 class="card-title mb-0">Students Enrolled in Your Class</h5>
                            <span class="badge badge-light">
                                <% if (enrollments != null) { %>
                                    <%= enrollments.size() %> Students
                                <% } else { %>
                                    0 Students
                                <% } %>
                            </span>
                        </div>
                        <div class="card-body">
                            <div class="table-responsive">
                                <table class="table">
                                    <thead>
                                        <tr>
                                            <th>Enrollment ID</th>
                                            <th>Student Name</th>
                                            <th>Academic Year</th>
                                            <th>Status</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% 
                                            if (enrollments != null && !enrollments.isEmpty()) {
                                                for (StudentEnrollment enrollment : enrollments) {
                                        %>
                                            <tr>
                                                <td><%= enrollment.getEnrollmentId() %></td>
                                                <td><%= enrollment.getUser().getName() %></td>
                                                <td><%= enrollment.getAcademicYear() %></td>
                                                <td>
                                                    <span class="badge badge-<%= "Active".equals(enrollment.getEnrollmentStatus()) ? "success" : "secondary" %>">
                                                        <%= enrollment.getEnrollmentStatus() %>
                                                    </span>
                                                </td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/attendance/view?studentId=<%= enrollment.getUserId() %>" 
                                                       class="btn btn-sm btn-info">
                                                        <i class="fas fa-eye"></i> View Attendance
                                                    </a>
                                                </td>
                                            </tr>
                                        <% 
                                                }
                                            } else {
                                        %>
                                            <tr>
                                                <td colspan="5" class="text-center">No students enrolled in your class</td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                    
                    <% if (pendingRequests != null && !pendingRequests.isEmpty()) { %>
                    <div class="card mt-4">
                        <div class="card-header bg-warning text-dark d-flex justify-content-between align-items-center">
                            <h5 class="card-title mb-0">Pending Enrollment Requests</h5>
                            <span class="badge badge-light"><%= pendingRequests.size() %> Requests</span>
                        </div>
                        <div class="card-body">
                            <div class="table-responsive">
                                <table class="table">
                                    <thead>
                                        <tr>
                                            <th>Student Name</th>
                                            <th>Enrollment Number</th>
                                            <th>Submitted On</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (EnrollmentRequest request1 : pendingRequests) { %>
                                            <tr>
                                                <td><%= request1.getUser().getName() %></td>
                                                <td><%= request1.getEnrollmentNumber() %></td>
                                                <td><%= request1.getSubmittedOn() %></td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/enrollment/approve/<%= request1.getRequestId() %>" 
                                                       class="btn btn-sm btn-success">
                                                        <i class="fas fa-check"></i> Review
                                                    </a>
                                                </td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                    <% } %>
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
