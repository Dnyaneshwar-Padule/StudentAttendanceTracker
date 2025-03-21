<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.attendance.utils.SessionUtil" %>
<%@ page import="com.attendance.models.Department" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register - Student Attendance Management System</title>
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
            <div class="col-md-8">
                <div class="card">
                    <div class="card-header bg-primary text-white">
                        <h3 class="card-title mb-0">Register</h3>
                    </div>
                    <div class="card-body">
                        <% if (request.getAttribute("error") != null) { %>
                            <div class="alert alert-danger">
                                <%= request.getAttribute("error") %>
                            </div>
                        <% } %>
                        
                        <form action="${pageContext.request.contextPath}/register" method="post">
                            <div class="form-group">
                                <label for="name">Full Name *</label>
                                <input type="text" class="form-control" id="name" name="name" required>
                            </div>
                            
                            <div class="form-group">
                                <label for="email">Email *</label>
                                <input type="email" class="form-control" id="email" name="email" required>
                            </div>
                            
                            <div class="form-group">
                                <label for="phoneNo">Phone Number</label>
                                <input type="tel" class="form-control" id="phoneNo" name="phoneNo">
                            </div>
                            
                            <div class="form-group">
                                <label for="password">Password *</label>
                                <input type="password" class="form-control" id="password" name="password" required
                                       minlength="6" maxlength="20">
                                <small class="form-text text-muted">Password must be 6-20 characters long</small>
                            </div>
                            
                            <div class="form-group">
                                <label for="confirmPassword">Confirm Password *</label>
                                <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required>
                            </div>
                            
                            <div class="form-group">
                                <label for="role">Role *</label>
                                <select class="form-control" id="role" name="role" required>
                                    <option value="">Select Role</option>
                                    <option value="Student">Student</option>
                                    <option value="Teacher">Teacher</option>
                                    <option value="Class Teacher">Class Teacher</option>
                                    <option value="HOD">Head of Department (HOD)</option>
                                    <option value="Principal">Principal</option>
                                </select>
                                <small class="form-text text-muted">
                                    Note: Your role will need to be verified by the appropriate authority
                                </small>
                            </div>
                            
                            <div class="form-group" id="departmentGroup">
                                <label for="departmentId">Department</label>
                                <select class="form-control" id="departmentId" name="departmentId">
                                    <option value="">Select Department</option>
                                    <% 
                                        List<Department> departments = (List<Department>) request.getAttribute("departments");
                                        if (departments != null) {
                                            for (Department dept : departments) {
                                    %>
                                            <option value="<%= dept.getDepartmentId() %>"><%= dept.getDepartmentName() %></option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                                <small class="form-text text-muted">
                                    Required for Teachers, Class Teachers, and HODs
                                </small>
                            </div>
                            
                            <div class="form-group">
                                <button type="submit" class="btn btn-primary btn-block">Register</button>
                            </div>
                        </form>
                        
                        <div class="text-center mt-3">
                            <p>Already have an account? <a href="${pageContext.request.contextPath}/login">Login here</a></p>
                        </div>
                    </div>
                </div>
                
                <div class="card mt-4">
                    <div class="card-body">
                        <h5 class="card-title">Registration Process</h5>
                        <p class="card-text">After registration, you will need to:</p>
                        <ol>
                            <li>Submit an enrollment request with your role details</li>
                            <li>Wait for approval from the appropriate authority:
                                <ul>
                                    <li>Students are approved by Class Teachers</li>
                                    <li>Teachers/Class Teachers are approved by HODs</li>
                                    <li>HODs are approved by the Principal</li>
                                    <li>Principal is approved by the System Admin</li>
                                </ul>
                            </li>
                            <li>Once approved, you can access your role-specific dashboard</li>
                        </ol>
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
        // Show/hide department field based on role selection
        $(document).ready(function() {
            $('#role').change(function() {
                var selectedRole = $(this).val();
                if (selectedRole === 'Teacher' || selectedRole === 'Class Teacher' || selectedRole === 'HOD') {
                    $('#departmentGroup').show();
                } else {
                    $('#departmentGroup').hide();
                }
            });
            
            // Trigger change event on page load
            $('#role').trigger('change');
        });
    </script>
</body>
</html>
