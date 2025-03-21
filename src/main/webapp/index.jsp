<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.attendance.utils.SessionUtil" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Attendance Management System</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>
    <%-- Check if user is logged in --%>
    <% if (SessionUtil.isLoggedIn(request)) { 
        // Redirect to dashboard
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return;
    } %>

    <div class="container">
        <div class="row justify-content-center mt-5">
            <div class="col-md-8 text-center">
                <h1 class="display-4">Student Attendance Management System</h1>
                <p class="lead mt-4">Track and manage student attendance across departments, classes, and subjects</p>
                
                <div class="card mt-5">
                    <div class="card-body">
                        <h2 class="card-title mb-4">Welcome to the Attendance System</h2>
                        <p class="card-text">
                            This system helps institutions manage student attendance with comprehensive tracking
                            and reporting features. It supports multiple user roles with different permissions
                            and workflows.
                        </p>
                        
                        <div class="row mt-5">
                            <div class="col-md-6">
                                <a href="${pageContext.request.contextPath}/login" class="btn btn-primary btn-lg btn-block">Login</a>
                            </div>
                            <div class="col-md-6">
                                <a href="${pageContext.request.contextPath}/register" class="btn btn-outline-secondary btn-lg btn-block">Register</a>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="card mt-4">
                    <div class="card-body">
                        <h3 class="card-title">System Features</h3>
                        <ul class="list-group list-group-flush text-left">
                            <li class="list-group-item">Role-based access control: Student, Teacher, Class Teacher, HOD, Principal</li>
                            <li class="list-group-item">Department and class management</li>
                            <li class="list-group-item">Subject-wise attendance tracking</li>
                            <li class="list-group-item">Semester and academic year organization</li>
                            <li class="list-group-item">Enrollment verification workflow</li>
                            <li class="list-group-item">Attendance reports and statistics</li>
                        </ul>
                    </div>
                </div>
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
