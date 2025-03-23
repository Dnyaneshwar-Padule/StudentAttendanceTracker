<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- Import Class model with fully qualified name to avoid ambiguity --%>
<%@ page import="com.attendance.models.Class" %>
<%@ page import="com.attendance.models.User" %>
<%@ page import="com.attendance.models.Attendance" %>
<%@ page import="com.attendance.models.Subject" %>
<%@ page import="com.attendance.utils.SessionUtil" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Dashboard - Attendance Management System</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.1/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>
    <%-- Check if user is logged in and is a student --%>
    <% 
        User user = SessionUtil.getUser(request);
        if (user == null || !"Student".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
    %>

    <%-- Include header --%>
    <jsp:include page="/views/common/header.jsp" />

    <div class="container mt-4">
        <div class="row">
            <div class="col-md-12">
                <h1>Student Dashboard</h1>
                <p class="lead">Welcome, <%= user.getName() %>!</p>
            </div>
        </div>

        <% 
            // Check if student needs enrollment
            Boolean needsEnrollment = (Boolean) request.getAttribute("needsEnrollment");
            if (needsEnrollment != null && needsEnrollment) {
        %>
            <div class="row mt-4">
                <div class="col-md-12">
                    <div class="alert alert-info">
                        <h4 class="alert-heading">Enrollment Required</h4>
                        <p>
                            You need to submit an enrollment request to access the student dashboard.
                            This request will be verified by your class teacher.
                        </p>
                        <hr>
                        <a href="${pageContext.request.contextPath}/enrollment/" class="btn btn-primary">
                            Submit Enrollment Request
                        </a>
                    </div>
                </div>
            </div>
        <% } else { %>
            <%-- Display enrollment details --%>
            <% 
                StudentEnrollment enrollment = (StudentEnrollment) request.getAttribute("enrollment");
                Class classObj = (Class) request.getAttribute("classObj");
                Department department = (Department) request.getAttribute("department");
                String semester = (String) request.getAttribute("semester");
                
                if (enrollment != null && classObj != null && department != null) {
            %>
                <div class="row mt-4">
                    <div class="col-md-4">
                        <div class="card">
                            <div class="card-header bg-primary text-white">
                                <h5 class="card-title mb-0">Enrollment Details</h5>
                            </div>
                            <div class="card-body">
                                <p><strong>Enrollment ID:</strong> <%= enrollment.getEnrollmentId() %></p>
                                <p><strong>Department:</strong> <%= department.getDepartmentName() %></p>
                                <p><strong>Class:</strong> <%= classObj.getClassName() %></p>
                                <p><strong>Semester:</strong> <%= semester %></p>
                                <p><strong>Academic Year:</strong> <%= enrollment.getAcademicYear() %></p>
                                <p><strong>Status:</strong> <span class="badge badge-success"><%= enrollment.getEnrollmentStatus() %></span></p>
                            </div>
                        </div>
                    </div>
                    
                    <div class="col-md-8">
                        <div class="card">
                            <div class="card-header bg-primary text-white">
                                <h5 class="card-title mb-0">Attendance Overview</h5>
                            </div>
                            <div class="card-body">
                                <% 
                                    Double attendancePercentage = (Double) request.getAttribute("attendancePercentage");
                                    if (attendancePercentage != null) {
                                        String badgeClass = "badge-danger";
                                        if (attendancePercentage >= 75) {
                                            badgeClass = "badge-success";
                                        } else if (attendancePercentage >= 60) {
                                            badgeClass = "badge-warning";
                                        }
                                %>
                                    <h3>Overall Attendance: <span class="badge <%= badgeClass %>"><%= String.format("%.2f", attendancePercentage) %>%</span></h3>
                                <% } %>
                                
                                <div class="mt-4">
                                    <h5>Attendance by Subject</h5>
                                    <div class="table-responsive">
                                        <table class="table table-bordered">
                                            <thead>
                                                <tr>
                                                    <th>Subject Code</th>
                                                    <th>Subject Name</th>
                                                    <th>Attendance</th>
                                                    <th>Action</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <% 
                                                    List<Subject> subjects = (List<Subject>) request.getAttribute("subjects");
                                                    if (subjects != null && !subjects.isEmpty()) {
                                                        for (Subject subject : subjects) {
                                                %>
                                                    <tr>
                                                        <td><%= subject.getSubjectCode() %></td>
                                                        <td><%= subject.getSubjectName() %></td>
                                                        <td>
                                                            <div class="progress">
                                                                <div class="progress-bar" role="progressbar" style="width: 75%;" aria-valuenow="75" aria-valuemin="0" aria-valuemax="100">75%</div>
                                                            </div>
                                                        </td>
                                                        <td>
                                                            <a href="${pageContext.request.contextPath}/attendance/view?subjectCode=<%= subject.getSubjectCode() %>" class="btn btn-sm btn-info">
                                                                <i class="fas fa-eye"></i> View Details
                                                            </a>
                                                        </td>
                                                    </tr>
                                                <% 
                                                        }
                                                    } else {
                                                %>
                                                    <tr>
                                                        <td colspan="4" class="text-center">No subjects assigned to your class</td>
                                                    </tr>
                                                <% } %>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="row mt-4">
                    <div class="col-md-12">
                        <div class="card">
                            <div class="card-header bg-primary text-white">
                                <h5 class="card-title mb-0">Quick Actions</h5>
                            </div>
                            <div class="card-body">
                                <div class="row">
                                    <div class="col-md-4 mb-3">
                                        <a href="${pageContext.request.contextPath}/attendance/view" class="btn btn-outline-primary btn-block">
                                            <i class="fas fa-calendar-check"></i> View Attendance
                                        </a>
                                    </div>
                                    <div class="col-md-4 mb-3">
                                        <a href="#" class="btn btn-outline-primary btn-block">
                                            <i class="fas fa-file-alt"></i> Attendance Reports
                                        </a>
                                    </div>
                                    <div class="col-md-4 mb-3">
                                        <a href="#" class="btn btn-outline-primary btn-block">
                                            <i class="fas fa-user-cog"></i> Profile Settings
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            <% } %>
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
