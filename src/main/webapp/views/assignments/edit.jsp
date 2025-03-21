<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.attendance.models.*" %>
<%@ page import="com.attendance.utils.SessionUtil" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Teacher Assignment - Attendance Management System</title>
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
        
        TeacherAssignment assignment = (TeacherAssignment) request.getAttribute("assignment");
        User teacher = (User) request.getAttribute("teacher");
        Subject subject = (Subject) request.getAttribute("subject");
        Class classObj = (Class) request.getAttribute("classObj");
        
        if (assignment == null || teacher == null || subject == null || classObj == null) {
            response.sendRedirect(request.getContextPath() + "/assignments/");
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
                        <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/assignments/">Teacher Assignments</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Edit Assignment</li>
                    </ol>
                </nav>
            </div>
        </div>
        
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card">
                    <div class="card-header bg-primary text-white">
                        <h5 class="card-title mb-0">Edit Teacher Assignment</h5>
                    </div>
                    <div class="card-body">
                        <% if (request.getAttribute("error") != null) { %>
                            <div class="alert alert-danger">
                                <%= request.getAttribute("error") %>
                            </div>
                        <% } %>
                        
                        <form action="${pageContext.request.contextPath}/assignments/edit" method="post">
                            <input type="hidden" name="teacherId" value="<%= teacher.getUserId() %>">
                            <input type="hidden" name="subjectCode" value="<%= subject.getSubjectCode() %>">
                            <input type="hidden" name="classId" value="<%= classObj.getClassId() %>">
                            
                            <div class="form-group">
                                <label>Teacher</label>
                                <input type="text" class="form-control" value="<%= teacher.getName() %>" readonly>
                            </div>
                            
                            <div class="form-group">
                                <label>Subject</label>
                                <input type="text" class="form-control" value="<%= subject.getSubjectName() %> (<%= subject.getSubjectCode() %>)" readonly>
                            </div>
                            
                            <div class="form-group">
                                <label>Class</label>
                                <input type="text" class="form-control" value="<%= classObj.getClassName() %>" readonly>
                            </div>
                            
                            <div class="form-group">
                                <label for="assignmentType">Assignment Type *</label>
                                <select class="form-control" id="assignmentType" name="assignmentType" required>
                                    <option value="Subject Teacher" <%= "Subject Teacher".equals(assignment.getAssignmentType()) ? "selected" : "" %>>Subject Teacher</option>
                                    <option value="Class Teacher" <%= "Class Teacher".equals(assignment.getAssignmentType()) ? "selected" : "" %>>Class Teacher</option>
                                </select>
                                <small class="form-text text-muted">
                                    Note: Only one Class Teacher can be assigned per class
                                </small>
                            </div>
                            
                            <div class="form-group text-center mt-4">
                                <button type="submit" class="btn btn-primary">
                                    <i class="fas fa-save"></i> Update Assignment
                                </button>
                                <a href="${pageContext.request.contextPath}/assignments/" class="btn btn-secondary ml-2">
                                    <i class="fas fa-arrow-left"></i> Back to List
                                </a>
                            </div>
                        </form>
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
