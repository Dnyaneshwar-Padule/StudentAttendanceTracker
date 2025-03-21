<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.attendance.models.*" %>
<%@ page import="com.attendance.utils.SessionUtil" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Enrollment Request - Attendance Management System</title>
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
    %>

    <%-- Include header --%>
    <jsp:include page="/views/common/header.jsp" />

    <div class="container mt-4">
        <div class="row">
            <div class="col-md-12">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Enrollment Request</li>
                    </ol>
                </nav>
            </div>
        </div>
        
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card">
                    <div class="card-header bg-primary text-white">
                        <h5 class="card-title mb-0">Submit Enrollment Request</h5>
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
                                <p class="mt-2">
                                    Your enrollment request has been submitted and is pending approval. 
                                    You will be notified once it's approved.
                                </p>
                                <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary mt-3">
                                    Return to Dashboard
                                </a>
                            </div>
                        <% } else { %>
                            <form action="${pageContext.request.contextPath}/enrollment/" method="post">
                                <div class="form-group">
                                    <label for="requestedRole">Requested Role *</label>
                                    <select class="form-control" id="requestedRole" name="requestedRole" required>
                                        <option value="">Select Role</option>
                                        <option value="Student" <%= "Student".equals(user.getRole()) ? "selected" : "" %>>Student</option>
                                        <option value="Teacher" <%= "Teacher".equals(user.getRole()) ? "selected" : "" %>>Teacher</option>
                                        <option value="Class Teacher" <%= "Class Teacher".equals(user.getRole()) ? "selected" : "" %>>Class Teacher</option>
                                        <option value="HOD" <%= "HOD".equals(user.getRole()) ? "selected" : "" %>>Head of Department (HOD)</option>
                                        <option value="Principal" <%= "Principal".equals(user.getRole()) ? "selected" : "" %>>Principal</option>
                                    </select>
                                    <small class="form-text text-muted">
                                        Your request will be verified by the appropriate authority
                                    </small>
                                </div>
                                
                                <div class="form-group" id="departmentGroup">
                                    <label for="departmentId">Department *</label>
                                    <select class="form-control" id="departmentId" name="departmentId">
                                        <option value="">Select Department</option>
                                        <% 
                                            List<Department> departments = (List<Department>) request.getAttribute("departments");
                                            if (departments != null) {
                                                for (Department dept : departments) {
                                        %>
                                                <option value="<%= dept.getDepartmentId() %>" 
                                                        <%= user.getDepartmentId() == dept.getDepartmentId() ? "selected" : "" %>>
                                                    <%= dept.getDepartmentName() %>
                                                </option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                    <small class="form-text text-muted">
                                        Required for Teachers, Class Teachers, and HODs
                                    </small>
                                </div>
                                
                                <div class="form-group" id="classGroup" style="display: none;">
                                    <label for="classId">Class *</label>
                                    <select class="form-control" id="classId" name="classId">
                                        <option value="">Select Class</option>
                                        <!-- This would be populated dynamically based on department selection -->
                                        <option value="1">FY</option>
                                        <option value="2">SY</option>
                                        <option value="3">TY</option>
                                    </select>
                                    <small class="form-text text-muted">
                                        Required for Students
                                    </small>
                                </div>
                                
                                <div class="form-group" id="enrollmentGroup" style="display: none;">
                                    <label for="enrollmentNumber">Enrollment Number *</label>
                                    <input type="text" class="form-control" id="enrollmentNumber" name="enrollmentNumber">
                                    <small class="form-text text-muted">
                                        Enter your official enrollment/registration number
                                    </small>
                                </div>
                                
                                <div class="form-group text-center mt-4">
                                    <button type="submit" class="btn btn-primary">Submit Request</button>
                                    <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-secondary ml-2">Cancel</a>
                                </div>
                            </form>
                        <% } %>
                    </div>
                </div>
                
                <div class="card mt-4">
                    <div class="card-header bg-info text-white">
                        <h5 class="card-title mb-0">Enrollment Verification Process</h5>
                    </div>
                    <div class="card-body">
                        <div class="alert alert-info">
                            <h6><i class="fas fa-info-circle"></i> Verification Flow</h6>
                            <ul class="mb-0">
                                <li>Students are verified by Class Teachers</li>
                                <li>Teachers/Class Teachers are verified by HODs</li>
                                <li>HODs are verified by the Principal</li>
                                <li>Principal is verified by System Admin</li>
                            </ul>
                        </div>
                        
                        <p>
                            After submitting your enrollment request, you'll need to wait for approval from the
                            appropriate authority. Once your request is approved, you'll have access to the
                            appropriate dashboard and functionality.
                        </p>
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
    
    <script>
        $(document).ready(function() {
            // Show/hide fields based on role selection
            $('#requestedRole').change(function() {
                var selectedRole = $(this).val();
                
                if (selectedRole === 'Teacher' || selectedRole === 'Class Teacher' || selectedRole === 'HOD') {
                    $('#departmentGroup').show();
                    $('#classGroup').hide();
                    $('#enrollmentGroup').hide();
                } else if (selectedRole === 'Student') {
                    $('#departmentGroup').show();
                    $('#classGroup').show();
                    $('#enrollmentGroup').show();
                } else {
                    $('#departmentGroup').hide();
                    $('#classGroup').hide();
                    $('#enrollmentGroup').hide();
                }
            });
            
            // Trigger change event on page load
            $('#requestedRole').trigger('change');
        });
    </script>
</body>
</html>
