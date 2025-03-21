<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.attendance.utils.SessionUtil" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Student Attendance Management System</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>
    <%-- Check if user is already logged in --%>
    <% if (SessionUtil.isLoggedIn(request)) { 
        // Redirect to dashboard
        SessionUtil.redirectToDashboard(request, response);
        return;
    } %>

    <div class="container">
        <div class="row justify-content-center mt-5">
            <div class="col-md-6">
                <div class="card">
                    <div class="card-header bg-primary text-white">
                        <h3 class="card-title mb-0">Login</h3>
                    </div>
                    <div class="card-body">
                        <% if (request.getAttribute("error") != null) { %>
                            <div class="alert alert-danger">
                                <%= request.getAttribute("error") %>
                            </div>
                        <% } %>
                        
                        <% if (request.getAttribute("success") != null) { %>
                            <div class="alert alert-success">
                                <%= request.getAttribute("success") %>
                            </div>
                        <% } %>
                        
                        <form action="${pageContext.request.contextPath}/login" method="post">
                            <div class="form-group">
                                <label for="email">Email</label>
                                <input type="email" class="form-control" id="email" name="email" required>
                            </div>
                            
                            <div class="form-group">
                                <label for="password">Password</label>
                                <input type="password" class="form-control" id="password" name="password" required>
                            </div>
                            
                            <div class="form-group">
                                <button type="submit" class="btn btn-primary btn-block">Login</button>
                            </div>
                        </form>
                        
                        <div class="text-center mt-3">
                            <p>Don't have an account? <a href="${pageContext.request.contextPath}/register">Register here</a></p>
                        </div>
                    </div>
                </div>
                
                <div class="card mt-4">
                    <div class="card-body">
                        <h5 class="card-title">User Roles</h5>
                        <p class="card-text">The system supports the following roles:</p>
                        <ul>
                            <li><strong>Student:</strong> View attendance, check reports</li>
                            <li><strong>Teacher:</strong> Record attendance for assigned subjects</li>
                            <li><strong>Class Teacher:</strong> Verify student enrollments, manage class attendance</li>
                            <li><strong>HOD:</strong> Approve teacher registrations, department management</li>
                            <li><strong>Principal:</strong> Approve HOD registrations, institution-wide management</li>
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
