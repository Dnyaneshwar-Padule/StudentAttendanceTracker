<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.attendance.models.*" %>
<%@ page import="com.attendance.utils.SessionUtil" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Principal Dashboard - Attendance Management System</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.1/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>
    <%-- Check if user is logged in and is a principal --%>
    <% 
        User user = SessionUtil.getUser(request);
        if (user == null || !"Principal".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
    %>

    <%-- Include header --%>
    <jsp:include page="/views/common/header.jsp" />

    <div class="container mt-4">
        <div class="row">
            <div class="col-md-12">
                <h1>Principal Dashboard</h1>
                <p class="lead">Welcome, <%= user.getName() %>!</p>
            </div>
        </div>

        <% 
            List<Department> departments = (List<Department>) request.getAttribute("departments");
            List<User> hods = (List<User>) request.getAttribute("hods");
            List<EnrollmentRequest> pendingRequests = (List<EnrollmentRequest>) request.getAttribute("pendingRequests");
            Integer totalStudents = (Integer) request.getAttribute("totalStudents");
            Integer totalTeachers = (Integer) request.getAttribute("totalTeachers");
            Integer totalClassTeachers = (Integer) request.getAttribute("totalClassTeachers");
        %>
        
        <div class="row mt-4">
            <div class="col-md-4">
                <div class="card">
                    <div class="card-header bg-primary text-white">
                        <h5 class="card-title mb-0">Institution Overview</h5>
                    </div>
                    <div class="card-body">
                        <p><strong>Role:</strong> <span class="badge badge-info">Principal</span></p>
                        
                        <div class="mt-3">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <p class="mb-0"><i class="fas fa-building"></i> <strong>Departments:</strong></p>
                                <span class="badge badge-primary"><%= departments != null ? departments.size() : 0 %></span>
                            </div>
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <p class="mb-0"><i class="fas fa-user-tie"></i> <strong>HODs:</strong></p>
                                <span class="badge badge-primary"><%= hods != null ? hods.size() : 0 %></span>
                            </div>
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <p class="mb-0"><i class="fas fa-chalkboard-teacher"></i> <strong>Teachers:</strong></p>
                                <span class="badge badge-primary"><%= totalTeachers != null ? totalTeachers : 0 %></span>
                            </div>
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <p class="mb-0"><i class="fas fa-user-graduate"></i> <strong>Students:</strong></p>
                                <span class="badge badge-primary"><%= totalStudents != null ? totalStudents : 0 %></span>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="card mt-4">
                    <div class="card-header bg-primary text-white">
                        <h5 class="card-title mb-0">Quick Actions</h5>
                    </div>
                    <div class="card-body">
                        <a href="${pageContext.request.contextPath}/enrollment/pending" class="btn btn-outline-primary btn-block mb-2">
                            <i class="fas fa-user-check"></i> HOD Enrollment Requests
                            <% if (pendingRequests != null && !pendingRequests.isEmpty()) { %>
                                <span class="badge badge-danger"><%= pendingRequests.size() %></span>
                            <% } %>
                        </a>
                        <a href="#" class="btn btn-outline-primary btn-block mb-2">
                            <i class="fas fa-building"></i> Manage Departments
                        </a>
                        <a href="${pageContext.request.contextPath}/attendance/view" class="btn btn-outline-primary btn-block mb-2">
                            <i class="fas fa-eye"></i> View Institution Attendance
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
                        <h5 class="card-title mb-0">Departments</h5>
                        <span class="badge badge-light">
                            <% if (departments != null) { %>
                                <%= departments.size() %> Departments
                            <% } else { %>
                                0 Departments
                            <% } %>
                        </span>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table">
                                <thead>
                                    <tr>
                                        <th>Department Name</th>
                                        <th>HOD</th>
                                        <th>Teachers</th>
                                        <th>Students</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% 
                                        if (departments != null && !departments.isEmpty()) {
                                            for (Department department : departments) {
                                                User hod = null;
                                                if (hods != null) {
                                                    for (User h : hods) {
                                                        if (h.getDepartmentId() == department.getDepartmentId()) {
                                                            hod = h;
                                                            break;
                                                        }
                                                    }
                                                }
                                    %>
                                        <tr>
                                            <td><%= department.getDepartmentName() %></td>
                                            <td>
                                                <% if (hod != null) { %>
                                                    <%= hod.getName() %>
                                                <% } else { %>
                                                    <span class="text-danger">Not assigned</span>
                                                <% } %>
                                            </td>
                                            <td>
                                                <!-- Placeholder for teacher count -->
                                                <span class="badge badge-info">0</span>
                                            </td>
                                            <td>
                                                <!-- Placeholder for student count -->
                                                <span class="badge badge-info">0</span>
                                            </td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/attendance/view?departmentId=<%= department.getDepartmentId() %>" 
                                                   class="btn btn-sm btn-info">
                                                    <i class="fas fa-eye"></i> View
                                                </a>
                                            </td>
                                        </tr>
                                    <% 
                                            }
                                        } else {
                                    %>
                                        <tr>
                                            <td colspan="5" class="text-center">No departments found</td>
                                        </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                
                <div class="card mt-4">
                    <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                        <h5 class="card-title mb-0">Heads of Departments</h5>
                        <span class="badge badge-light">
                            <% if (hods != null) { %>
                                <%= hods.size() %> HODs
                            <% } else { %>
                                0 HODs
                            <% } %>
                        </span>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table">
                                <thead>
                                    <tr>
                                        <th>Name</th>
                                        <th>Email</th>
                                        <th>Department</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% 
                                        if (hods != null && !hods.isEmpty()) {
                                            for (User hod : hods) {
                                                String departmentName = "Unknown";
                                                if (departments != null) {
                                                    for (Department d : departments) {
                                                        if (d.getDepartmentId() == hod.getDepartmentId()) {
                                                            departmentName = d.getDepartmentName();
                                                            break;
                                                        }
                                                    }
                                                }
                                    %>
                                        <tr>
                                            <td><%= hod.getName() %></td>
                                            <td><%= hod.getEmail() %></td>
                                            <td><%= departmentName %></td>
                                            <td>
                                                <a href="#" class="btn btn-sm btn-primary">
                                                    <i class="fas fa-edit"></i> Edit
                                                </a>
                                            </td>
                                        </tr>
                                    <% 
                                            }
                                        } else {
                                    %>
                                        <tr>
                                            <td colspan="4" class="text-center">No HODs found</td>
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
                        <h5 class="card-title mb-0">Pending HOD Enrollment Requests</h5>
                        <span class="badge badge-light"><%= pendingRequests.size() %> Requests</span>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table">
                                <thead>
                                    <tr>
                                        <th>Name</th>
                                        <th>Email</th>
                                        <th>Department</th>
                                        <th>Submitted On</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (EnrollmentRequest request1 : pendingRequests) { 
                                        String departmentName = "Unknown";
                                        if (departments != null) {
                                            for (Department d : departments) {
                                                if (d.getDepartmentId() == request1.getUser().getDepartmentId()) {
                                                    departmentName = d.getDepartmentName();
                                                    break;
                                                }
                                            }
                                        }
                                    %>
                                        <tr>
                                            <td><%= request1.getUser().getName() %></td>
                                            <td><%= request1.getUser().getEmail() %></td>
                                            <td><%= departmentName %></td>
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
    </div>

    <%-- Include footer --%>
    <jsp:include page="/views/common/footer.jsp" />

    <script src="https://code.jquery.com/jquery-3.5.1.slim.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/scripts.js"></script>
</body>
</html>
