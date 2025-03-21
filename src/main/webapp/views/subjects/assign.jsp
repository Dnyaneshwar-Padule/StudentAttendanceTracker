<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.attendance.models.*" %>
<%@ page import="com.attendance.utils.SessionUtil" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Assign Subject - Attendance Management System</title>
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
                        <li class="breadcrumb-item active" aria-current="page">Assign Subject</li>
                    </ol>
                </nav>
            </div>
        </div>
        
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card">
                    <div class="card-header bg-primary text-white">
                        <h5 class="card-title mb-0">Assign Subject to Department and Class</h5>
                    </div>
                    <div class="card-body">
                        <% if (request.getAttribute("error") != null) { %>
                            <div class="alert alert-danger">
                                <%= request.getAttribute("error") %>
                            </div>
                        <% } %>
                        
                        <form action="${pageContext.request.contextPath}/subjects/assign" method="post">
                            <div class="form-group">
                                <label for="subjectCode">Subject *</label>
                                <select class="form-control" id="subjectCode" name="subjectCode" required>
                                    <option value="">Select Subject</option>
                                    <% 
                                        List<Subject> subjects = (List<Subject>) request.getAttribute("subjects");
                                        String selectedSubjectCode = request.getParameter("subjectCode");
                                        
                                        if (subjects != null) {
                                            for (Subject subject : subjects) {
                                    %>
                                        <option value="<%= subject.getSubjectCode() %>" 
                                                <%= (selectedSubjectCode != null && selectedSubjectCode.equals(subject.getSubjectCode())) ? "selected" : "" %>>
                                            <%= subject.getSubjectName() %> (<%= subject.getSubjectCode() %>)
                                        </option>
                                    <% 
                                            }
                                        }
                                    %>
                                </select>
                            </div>
                            
                            <div class="form-group">
                                <label for="departmentId">Department *</label>
                                <select class="form-control" id="departmentId" name="departmentId" required>
                                    <option value="">Select Department</option>
                                    <% 
                                        List<Department> departments = (List<Department>) request.getAttribute("departments");
                                        Integer selectedDepartmentId = null;
                                        
                                        if (request.getParameter("departmentId") != null) {
                                            try {
                                                selectedDepartmentId = Integer.parseInt(request.getParameter("departmentId"));
                                            } catch (NumberFormatException e) {
                                                // Invalid department ID
                                            }
                                        }
                                        
                                        if (departments != null) {
                                            for (Department department : departments) {
                                    %>
                                        <option value="<%= department.getDepartmentId() %>" 
                                                <%= (selectedDepartmentId != null && selectedDepartmentId == department.getDepartmentId()) ? "selected" : "" %>>
                                            <%= department.getDepartmentName() %>
                                        </option>
                                    <% 
                                            }
                                        }
                                    %>
                                </select>
                            </div>
                            
                            <div class="form-group">
                                <label for="classId">Class *</label>
                                <select class="form-control" id="classId" name="classId" required>
                                    <option value="">Select Class</option>
                                    <% 
                                        List<Class> classes = (List<Class>) request.getAttribute("classes");
                                        Integer selectedClassId = null;
                                        
                                        if (request.getParameter("classId") != null) {
                                            try {
                                                selectedClassId = Integer.parseInt(request.getParameter("classId"));
                                            } catch (NumberFormatException e) {
                                                // Invalid class ID
                                            }
                                        }
                                        
                                        if (classes != null) {
                                            for (Class classObj : classes) {
                                    %>
                                        <option value="<%= classObj.getClassId() %>" 
                                                <%= (selectedClassId != null && selectedClassId == classObj.getClassId()) ? "selected" : "" %>>
                                            <%= classObj.getClassName() %>
                                        </option>
                                    <% 
                                            }
                                        }
                                    %>
                                </select>
                                <small class="form-text text-muted">
                                    If no classes are shown, first select a department
                                </small>
                            </div>
                            
                            <div class="form-group text-center mt-4">
                                <button type="submit" class="btn btn-primary">
                                    <i class="fas fa-link"></i> Assign Subject
                                </button>
                                <a href="${pageContext.request.contextPath}/subjects/" class="btn btn-secondary ml-2">
                                    <i class="fas fa-times"></i> Cancel
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
    
    <script>
        $(document).ready(function() {
            // When department is selected, submit form to load classes
            $('#departmentId').change(function() {
                if ($(this).val() !== '') {
                    // Create a form and submit it
                    var form = $('<form></form>');
                    form.attr('method', 'get');
                    form.attr('action', '${pageContext.request.contextPath}/subjects/assign');
                    
                    var subjectInput = $('<input>').attr({
                        type: 'hidden',
                        name: 'subjectCode',
                        value: $('#subjectCode').val()
                    });
                    
                    var departmentInput = $('<input>').attr({
                        type: 'hidden',
                        name: 'departmentId',
                        value: $(this).val()
                    });
                    
                    form.append(subjectInput);
                    form.append(departmentInput);
                    
                    $('body').append(form);
                    form.submit();
                }
            });
        });
    </script>
</body>
</html>
