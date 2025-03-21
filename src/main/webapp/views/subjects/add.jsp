<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.attendance.models.*" %>
<%@ page import="com.attendance.utils.SessionUtil" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add Subject - Attendance Management System</title>
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
                        <li class="breadcrumb-item active" aria-current="page">Add Subject</li>
                    </ol>
                </nav>
            </div>
        </div>
        
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card">
                    <div class="card-header bg-primary text-white">
                        <h5 class="card-title mb-0">Add New Subject</h5>
                    </div>
                    <div class="card-body">
                        <% if (request.getAttribute("error") != null) { %>
                            <div class="alert alert-danger">
                                <%= request.getAttribute("error") %>
                            </div>
                        <% } %>
                        
                        <form action="${pageContext.request.contextPath}/subjects/add" method="post">
                            <div class="form-group">
                                <label for="subjectCode">Subject Code *</label>
                                <input type="text" class="form-control" id="subjectCode" name="subjectCode" required
                                       placeholder="e.g., CS101, MATH201">
                                <small class="form-text text-muted">
                                    Enter a unique subject code. This will be used as an identifier.
                                </small>
                            </div>
                            
                            <div class="form-group">
                                <label for="subjectName">Subject Name *</label>
                                <input type="text" class="form-control" id="subjectName" name="subjectName" required
                                       placeholder="e.g., Introduction to Computer Science">
                            </div>
                            
                            <div class="form-group text-center mt-4">
                                <button type="submit" class="btn btn-primary">
                                    <i class="fas fa-save"></i> Add Subject
                                </button>
                                <a href="${pageContext.request.contextPath}/subjects/" class="btn btn-secondary ml-2">
                                    <i class="fas fa-times"></i> Cancel
                                </a>
                            </div>
                        </form>
                    </div>
                </div>
                
                <div class="card mt-4">
                    <div class="card-header bg-info text-white">
                        <h5 class="card-title mb-0">Subject Guidelines</h5>
                    </div>
                    <div class="card-body">
                        <ul class="mb-0">
                            <li>Subject code should be unique and follow a standardized format</li>
                            <li>Make sure the subject code and name are clear and descriptive</li>
                            <li>After adding a subject, you can assign it to specific departments and classes</li>
                            <li>Teachers can be assigned to teach subjects via the Teacher Assignment menu</li>
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
