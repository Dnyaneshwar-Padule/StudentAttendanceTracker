<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.attendance.models.*" %>
<%@ page import="com.attendance.utils.SessionUtil" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Subject - Attendance Management System</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.1/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>
    <%-- Check if user is logged in and is HOD or Principal --%>
    <% 
        User user = SessionUtil.getUser(request);
        if (user == null || !("HOD".equals(user.getRole()) || "Principal".equals(user.getRole()))) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        Subject subject = (Subject) request.getAttribute("subject");
        if (subject == null) {
            response.sendRedirect(request.getContextPath() + "/subjects/");
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
                        <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/subjects/">Subjects</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Edit Subject</li>
                    </ol>
                </nav>
            </div>
        </div>
        
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card">
                    <div class="card-header bg-primary text-white">
                        <h5 class="card-title mb-0">Edit Subject: <%= subject.getSubjectName() %></h5>
                    </div>
                    <div class="card-body">
                        <% if (request.getAttribute("error") != null) { %>
                            <div class="alert alert-danger">
                                <%= request.getAttribute("error") %>
                            </div>
                        <% } %>
                        
                        <form action="${pageContext.request.contextPath}/subjects/edit" method="post">
                            <div class="form-group">
                                <label for="subjectCode">Subject Code</label>
                                <input type="text" class="form-control" id="subjectCode" name="subjectCode" 
                                       value="<%= subject.getSubjectCode() %>" readonly>
                                <small class="form-text text-muted">
                                    Subject code cannot be changed once created.
                                </small>
                            </div>
                            
                            <div class="form-group">
                                <label for="subjectName">Subject Name *</label>
                                <input type="text" class="form-control" id="subjectName" name="subjectName" required
                                       value="<%= subject.getSubjectName() %>">
                            </div>
                            
                            <div class="form-group text-center mt-4">
                                <button type="submit" class="btn btn-primary">
                                    <i class="fas fa-save"></i> Update Subject
                                </button>
                                <a href="${pageContext.request.contextPath}/subjects/" class="btn btn-secondary ml-2">
                                    <i class="fas fa-arrow-left"></i> Back to List
                                </a>
                            </div>
                        </form>
                    </div>
                </div>
                
                <div class="card mt-4">
                    <div class="card-header bg-info text-white">
                        <h5 class="card-title mb-0">Subject Assignments</h5>
                    </div>
                    <div class="card-body">
                        <!-- This section would show existing department/class assignments -->
                        <p class="text-muted">No department or class assignments found for this subject.</p>
                        <a href="${pageContext.request.contextPath}/subjects/assign?subjectCode=<%= subject.getSubjectCode() %>" class="btn btn-info btn-sm">
                            <i class="fas fa-link"></i> Assign to Department/Class
                        </a>
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
