<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.attendance.models.*" %>
<%@ page import="com.attendance.utils.SessionUtil" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Subjects - Attendance Management System</title>
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
                        <li class="breadcrumb-item active" aria-current="page">Subjects</li>
                    </ol>
                </nav>
                
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2>Subject Management</h2>
                    <div>
                        <a href="${pageContext.request.contextPath}/subjects/add" class="btn btn-success">
                            <i class="fas fa-plus"></i> Add Subject
                        </a>
                        <a href="${pageContext.request.contextPath}/subjects/assign" class="btn btn-primary ml-2">
                            <i class="fas fa-link"></i> Assign Subject
                        </a>
                    </div>
                </div>
            </div>
        </div>
        
        <% if (request.getAttribute("error") != null) { %>
            <div class="row">
                <div class="col-md-12">
                    <div class="alert alert-danger">
                        <%= request.getAttribute("error") %>
                    </div>
                </div>
            </div>
        <% } %>
        
        <% if (request.getAttribute("success") != null) { %>
            <div class="row">
                <div class="col-md-12">
                    <div class="alert alert-success">
                        <%= request.getAttribute("success") %>
                    </div>
                </div>
            </div>
        <% } %>
        
        <div class="row">
            <div class="col-md-12">
                <div class="card">
                    <div class="card-header bg-primary text-white">
                        <h5 class="card-title mb-0">All Subjects</h5>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-striped">
                                <thead>
                                    <tr>
                                        <th>Subject Code</th>
                                        <th>Subject Name</th>
                                        <th>Assigned To</th>
                                        <th>Actions</th>
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
                                                <!-- In a real app, this would show department/class assignments -->
                                                <span class="badge badge-info">Multiple</span>
                                            </td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/subjects/edit/<%= subject.getSubjectCode() %>" class="btn btn-sm btn-primary">
                                                    <i class="fas fa-edit"></i> Edit
                                                </a>
                                                <a href="#" class="btn btn-sm btn-info" data-toggle="modal" data-target="#viewAssignmentsModal<%= subject.getSubjectCode() %>">
                                                    <i class="fas fa-eye"></i> View Assignments
                                                </a>
                                            </td>
                                        </tr>
                                        
                                        <!-- Modal for viewing subject assignments -->
                                        <div class="modal fade" id="viewAssignmentsModal<%= subject.getSubjectCode() %>" tabindex="-1" role="dialog" aria-labelledby="assignmentsModalLabel" aria-hidden="true">
                                            <div class="modal-dialog modal-lg" role="document">
                                                <div class="modal-content">
                                                    <div class="modal-header bg-primary text-white">
                                                        <h5 class="modal-title" id="assignmentsModalLabel">
                                                            Assignments for <%= subject.getSubjectName() %> (<%= subject.getSubjectCode() %>)
                                                        </h5>
                                                        <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                                                            <span aria-hidden="true">&times;</span>
                                                        </button>
                                                    </div>
                                                    <div class="modal-body">
                                                        <!-- In a real app, this would show actual department/class assignments -->
                                                        <p class="text-muted">No assignments found for this subject.</p>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                                                        <a href="${pageContext.request.contextPath}/subjects/assign?subjectCode=<%= subject.getSubjectCode() %>" class="btn btn-primary">Add Assignment</a>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    <% 
                                            }
                                        } else {
                                    %>
                                        <tr>
                                            <td colspan="4" class="text-center">No subjects found</td>
                                        </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="row mt-4">
            <div class="col-md-12">
                <div class="card">
                    <div class="card-header bg-info text-white">
                        <h5 class="card-title mb-0">Subject-Department Assignments</h5>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-striped">
                                <thead>
                                    <tr>
                                        <th>Department</th>
                                        <th>Class</th>
                                        <th>Subject</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <!-- In a real app, this would be populated with actual department-subject mappings -->
                                    <tr>
                                        <td colspan="4" class="text-center">No subject assignments found</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
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
