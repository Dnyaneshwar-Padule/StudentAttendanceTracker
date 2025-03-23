<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.attendance.models.User" %>
<%@ page import="com.attendance.models.Department" %>
<%@ page import="com.attendance.models.Teacher" %>
<%@ page import="com.attendance.models.Subject" %>
<%@ page import="com.attendance.models.EnrollmentRequest" %>
<%@ page import="com.attendance.utils.SessionUtil" %>
<%@ page import="java.util.*" %>
<%-- Import Class model with fully qualified name to avoid ambiguity --%>
<%@ page import="com.attendance.models.Class" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HOD Dashboard - Attendance Management System</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.1/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>
    <%-- Check if user is logged in and is an HOD --%>
    <% 
        User user = SessionUtil.getUser(request);
        if (user == null || !"HOD".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
    %>

    <%-- Include header --%>
    <jsp:include page="/views/common/header.jsp" />

    <div class="container mt-4">
        <div class="row">
            <div class="col-md-12">
                <h1>HOD Dashboard</h1>
                <p class="lead">Welcome, <%= user.getName() %>!</p>
            </div>
        </div>

        <% 
            Department department = (Department) request.getAttribute("department");
            List<Class> classes = (List<Class>) request.getAttribute("classes");
            List<User> teachers = (List<User>) request.getAttribute("teachers");
            List<EnrollmentRequest> pendingRequests = (List<EnrollmentRequest>) request.getAttribute("pendingRequests");
            List<Subject> subjects = (List<Subject>) request.getAttribute("subjects");
            
            if (department == null) {
        %>
            <div class="row mt-4">
                <div class="col-md-12">
                    <div class="alert alert-warning">
                        <h4 class="alert-heading">Department Assignment Required</h4>
                        <p>
                            You need to be assigned to a department to access the HOD dashboard features.
                            Please contact the Principal to get assigned to a department.
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
                            <p><strong>Role:</strong> <span class="badge badge-info">Head of Department</span></p>
                            
                            <div class="mt-3">
                                <p><i class="fas fa-users"></i> <strong>Teachers:</strong> 
                                    <span class="badge badge-primary"><%= teachers != null ? teachers.size() : 0 %></span>
                                </p>
                                <p><i class="fas fa-school"></i> <strong>Classes:</strong> 
                                    <span class="badge badge-primary"><%= classes != null ? classes.size() : 0 %></span>
                                </p>
                                <p><i class="fas fa-book"></i> <strong>Subjects:</strong> 
                                    <span class="badge badge-primary"><%= subjects != null ? subjects.size() : 0 %></span>
                                </p>
                            </div>
                        </div>
                    </div>
                    
                    <div class="card mt-4">
                        <div class="card-header bg-primary text-white">
                            <h5 class="card-title mb-0">Quick Actions</h5>
                        </div>
                        <div class="card-body">
                            <a href="${pageContext.request.contextPath}/enrollment/pending" class="btn btn-outline-primary btn-block mb-2">
                                <i class="fas fa-user-check"></i> Teacher Enrollment Requests
                                <% if (pendingRequests != null && !pendingRequests.isEmpty()) { %>
                                    <span class="badge badge-danger"><%= pendingRequests.size() %></span>
                                <% } %>
                            </a>
                            <a href="${pageContext.request.contextPath}/assignments/" class="btn btn-outline-primary btn-block mb-2">
                                <i class="fas fa-tasks"></i> Manage Teacher Assignments
                            </a>
                            <a href="${pageContext.request.contextPath}/subjects/" class="btn btn-outline-primary btn-block mb-2">
                                <i class="fas fa-book"></i> Manage Subjects
                            </a>
                            <a href="${pageContext.request.contextPath}/attendance/view" class="btn btn-outline-primary btn-block mb-2">
                                <i class="fas fa-eye"></i> View Department Attendance
                            </a>
                            <a href="#" class="btn btn-outline-primary btn-block">
                                <i class="fas fa-file-alt"></i> Generate Reports
                            </a>
                        </div>
                    </div>
                </div>
                
                <div class="col-md-8">
                    <div class="card">
                        <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                            <h5 class="card-title mb-0">Department Classes</h5>
                            <span class="badge badge-light">
                                <% if (classes != null) { %>
                                    <%= classes.size() %> Classes
                                <% } else { %>
                                    0 Classes
                                <% } %>
                            </span>
                        </div>
                        <div class="card-body">
                            <div class="table-responsive">
                                <table class="table">
                                    <thead>
                                        <tr>
                                            <th>Class</th>
                                            <th>Class Teacher</th>
                                            <th>Total Subjects</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% 
                                            if (classes != null && !classes.isEmpty()) {
                                                for (Class classObj : classes) {
                                        %>
                                            <tr>
                                                <td><%= classObj.getClassName() %></td>
                                                <td>
                                                    <!-- In a real application, this would be filled from teacher assignments -->
                                                    <span class="text-muted">Not assigned</span>
                                                </td>
                                                <td>
                                                    <!-- In a real application, this would be filled from subject assignments -->
                                                    <span class="badge badge-info">0</span>
                                                </td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/attendance/view?classId=<%= classObj.getClassId() %>" 
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
                                                <td colspan="4" class="text-center">No classes found in your department</td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                    
                    <div class="card mt-4">
                        <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                            <h5 class="card-title mb-0">Department Teachers</h5>
                            <span class="badge badge-light">
                                <% if (teachers != null) { %>
                                    <%= teachers.size() %> Teachers
                                <% } else { %>
                                    0 Teachers
                                <% } %>
                            </span>
                        </div>
                        <div class="card-body">
                            <div class="table-responsive">
                                <table class="table">
                                    <thead>
                                        <tr>
                                            <th>Teacher Name</th>
                                            <th>Email</th>
                                            <th>Role</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% 
                                            if (teachers != null && !teachers.isEmpty()) {
                                                for (User teacher : teachers) {
                                        %>
                                            <tr>
                                                <td><%= teacher.getName() %></td>
                                                <td><%= teacher.getEmail() %></td>
                                                <td><%= teacher.getRole() %></td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/assignments/add?teacherId=<%= teacher.getUserId() %>" 
                                                       class="btn btn-sm btn-primary">
                                                        <i class="fas fa-tasks"></i> Assign
                                                    </a>
                                                </td>
                                            </tr>
                                        <% 
                                                }
                                            } else {
                                        %>
                                            <tr>
                                                <td colspan="4" class="text-center">No teachers found in your department</td>
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
                            <h5 class="card-title mb-0">Pending Teacher Enrollment Requests</h5>
                            <span class="badge badge-light"><%= pendingRequests.size() %> Requests</span>
                        </div>
                        <div class="card-body">
                            <div class="table-responsive">
                                <table class="table">
                                    <thead>
                                        <tr>
                                            <th>Name</th>
                                            <th>Email</th>
                                            <th>Requested Role</th>
                                            <th>Submitted On</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (EnrollmentRequest request1 : pendingRequests) { %>
                                            <tr>
                                                <td><%= request1.getUser().getName() %></td>
                                                <td><%= request1.getUser().getEmail() %></td>
                                                <td><%= request1.getRequestedRole() %></td>
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
