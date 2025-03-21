<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.attendance.models.*" %>
<%@ page import="com.attendance.utils.SessionUtil" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Teacher Assignments - Attendance Management System</title>
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
                        <li class="breadcrumb-item active" aria-current="page">Teacher Assignments</li>
                    </ol>
                </nav>
                
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2>Teacher Assignment Management</h2>
                    <a href="${pageContext.request.contextPath}/assignments/add" class="btn btn-success">
                        <i class="fas fa-plus"></i> Add Assignment
                    </a>
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
                        <h5 class="card-title mb-0">Current Teacher Assignments</h5>
                    </div>
                    <div class="card-body">
                        <% if ("Principal".equals(user.getRole())) { %>
                            <div class="form-group">
                                <label for="departmentFilter">Filter by Department</label>
                                <select class="form-control" id="departmentFilter">
                                    <option value="">All Departments</option>
                                    <% 
                                        List<Department> departments = (List<Department>) request.getAttribute("departments");
                                        if (departments != null) {
                                            for (Department department : departments) {
                                    %>
                                        <option value="<%= department.getDepartmentId() %>"><%= department.getDepartmentName() %></option>
                                    <% 
                                            }
                                        }
                                    %>
                                </select>
                            </div>
                        <% } %>
                        
                        <div class="table-responsive">
                            <table class="table table-striped">
                                <thead>
                                    <tr>
                                        <th>Teacher</th>
                                        <th>Department</th>
                                        <th>Class</th>
                                        <th>Subject</th>
                                        <th>Assignment Type</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% 
                                        List<TeacherAssignment> assignments = (List<TeacherAssignment>) request.getAttribute("assignments");
                                        if (assignments != null && !assignments.isEmpty()) {
                                            for (TeacherAssignment assignment : assignments) {
                                    %>
                                        <tr class="department-row" data-department="<%= assignment.getTeacher().getDepartmentId() %>">
                                            <td><%= assignment.getTeacher().getName() %></td>
                                            <td>
                                                <% 
                                                    if ("Principal".equals(user.getRole())) {
                                                        // For Principal, show departments from the full list
                                                        if (departments != null) {
                                                            for (Department dept : departments) {
                                                                if (dept.getDepartmentId() == assignment.getTeacher().getDepartmentId()) {
                                                %>
                                                                    <%= dept.getDepartmentName() %>
                                                <% 
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        // For HOD, just show their department
                                                        Department department = (Department) request.getAttribute("department");
                                                        if (department != null) {
                                                %>
                                                            <%= department.getDepartmentName() %>
                                                <% 
                                                        }
                                                    }
                                                %>
                                            </td>
                                            <td><%= assignment.getClassObj().getClassName() %></td>
                                            <td><%= assignment.getSubject().getSubjectName() %> (<%= assignment.getSubjectCode() %>)</td>
                                            <td>
                                                <span class="badge <%= "Class Teacher".equals(assignment.getAssignmentType()) ? "badge-info" : "badge-secondary" %>">
                                                    <%= assignment.getAssignmentType() %>
                                                </span>
                                            </td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/assignments/edit/<%= assignment.getTeacherId() %>_<%= assignment.getSubjectCode() %>_<%= assignment.getClassId() %>" 
                                                   class="btn btn-sm btn-primary">
                                                    <i class="fas fa-edit"></i> Edit
                                                </a>
                                                <button type="button" class="btn btn-sm btn-danger" data-toggle="modal" 
                                                        data-target="#removeModal<%= assignment.getTeacherId() %>_<%= assignment.getSubjectCode() %>_<%= assignment.getClassId() %>">
                                                    <i class="fas fa-trash"></i> Remove
                                                </button>
                                            </td>
                                        </tr>
                                        
                                        <!-- Modal for removal confirmation -->
                                        <div class="modal fade" id="removeModal<%= assignment.getTeacherId() %>_<%= assignment.getSubjectCode() %>_<%= assignment.getClassId() %>" 
                                             tabindex="-1" role="dialog" aria-labelledby="removeModalLabel" aria-hidden="true">
                                            <div class="modal-dialog" role="document">
                                                <div class="modal-content">
                                                    <div class="modal-header bg-danger text-white">
                                                        <h5 class="modal-title" id="removeModalLabel">Confirm Removal</h5>
                                                        <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                                                            <span aria-hidden="true">&times;</span>
                                                        </button>
                                                    </div>
                                                    <div class="modal-body">
                                                        <p>Are you sure you want to remove this assignment?</p>
                                                        <p><strong>Teacher:</strong> <%= assignment.getTeacher().getName() %></p>
                                                        <p><strong>Subject:</strong> <%= assignment.getSubject().getSubjectName() %></p>
                                                        <p><strong>Class:</strong> <%= assignment.getClassObj().getClassName() %></p>
                                                        <p><strong>Assignment Type:</strong> <%= assignment.getAssignmentType() %></p>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                                                        <form action="${pageContext.request.contextPath}/assignments/remove" method="post">
                                                            <input type="hidden" name="teacherId" value="<%= assignment.getTeacherId() %>">
                                                            <input type="hidden" name="subjectCode" value="<%= assignment.getSubjectCode() %>">
                                                            <input type="hidden" name="classId" value="<%= assignment.getClassId() %>">
                                                            <button type="submit" class="btn btn-danger">Remove</button>
                                                        </form>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    <% 
                                            }
                                        } else {
                                    %>
                                        <tr>
                                            <td colspan="6" class="text-center">No teacher assignments found</td>
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
                        <h5 class="card-title mb-0">Class Teachers</h5>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-striped">
                                <thead>
                                    <tr>
                                        <th>Class</th>
                                        <th>Department</th>
                                        <th>Class Teacher</th>
                                        <th>Email</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <!-- This would be populated with a list of all classes and their assigned class teachers -->
                                    <tr>
                                        <td colspan="4" class="text-center">No class teacher assignments found</td>
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
    
    <script>
        $(document).ready(function() {
            // Department filter for Principal
            $('#departmentFilter').change(function() {
                var departmentId = $(this).val();
                
                if (departmentId === '') {
                    // Show all rows
                    $('.department-row').show();
                } else {
                    // Hide all rows first
                    $('.department-row').hide();
                    
                    // Show rows matching the selected department
                    $('.department-row[data-department="' + departmentId + '"]').show();
                }
            });
        });
    </script>
</body>
</html>
