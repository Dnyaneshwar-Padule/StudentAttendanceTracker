<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.attendance.models.User" %>
<%@ page import="com.attendance.models.Attendance" %>
<%@ page import="com.attendance.models.StudentEnrollment" %>
<%@ page import="com.attendance.models.Subject" %>
<%@ page import="com.attendance.utils.SessionUtil" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.Date" %>
<%-- Import Class model with fully qualified name to avoid ambiguity --%>
<%@ page import="com.attendance.models.Class" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mark Attendance - Attendance Management System</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.1/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>
    <%-- Check if user is logged in and is a teacher or class teacher --%>
    <% 
        User user = SessionUtil.getUser(request);
        if (user == null || !("Teacher".equals(user.getRole()) || "Class Teacher".equals(user.getRole()))) {
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
                        <li class="breadcrumb-item active" aria-current="page">Mark Attendance</li>
                    </ol>
                </nav>
                
                <h2>Mark Attendance</h2>
                <p class="lead">Record attendance for students in your classes</p>
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
            <div class="col-md-4">
                <div class="card">
                    <div class="card-header bg-primary text-white">
                        <h5 class="card-title mb-0">Select Class and Subject</h5>
                    </div>
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/attendance/mark" method="get">
                            <div class="form-group">
                                <label for="classId">Class *</label>
                                <select class="form-control" id="classId" name="classId" required>
                                    <option value="">Select Class</option>
                                    <% 
                                        Collection<Class> classes = (Collection<Class>) request.getAttribute("classes");
                                        Integer selectedClassId = (Integer) request.getAttribute("selectedClassId");
                                        
                                        if (classes != null && !classes.isEmpty()) {
                                            for (Class classObj : classes) {
                                    %>
                                        <option value="<%= classObj.getClassId() %>" 
                                                <%= (selectedClassId != null && selectedClassId == classObj.getClassId()) ? "selected" : "" %>>
                                            <%= classObj.getClassName() %> - <%= classObj.getDepartment().getDepartmentName() %>
                                        </option>
                                    <% 
                                            }
                                        }
                                    %>
                                </select>
                            </div>
                            
                            <div class="form-group">
                                <label for="subjectCode">Subject *</label>
                                <select class="form-control" id="subjectCode" name="subjectCode" required>
                                    <option value="">Select Subject</option>
                                    <% 
                                        Collection<Subject> subjects = (Collection<Subject>) request.getAttribute("subjects");
                                        String selectedSubject = (String) request.getAttribute("selectedSubject");
                                        
                                        if (subjects != null && !subjects.isEmpty()) {
                                            for (Subject subject : subjects) {
                                    %>
                                        <option value="<%= subject.getSubjectCode() %>" 
                                                <%= (selectedSubject != null && selectedSubject.equals(subject.getSubjectCode())) ? "selected" : "" %>>
                                            <%= subject.getSubjectName() %> (<%= subject.getSubjectCode() %>)
                                        </option>
                                    <% 
                                            }
                                        }
                                    %>
                                </select>
                            </div>
                            
                            <div class="form-group">
                                <label for="date">Date *</label>
                                <input type="date" class="form-control" id="date" name="date" 
                                       value="<%= request.getAttribute("selectedDate") != null ? request.getAttribute("selectedDate") : new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()) %>" 
                                       required>
                            </div>
                            
                            <div class="form-group">
                                <label for="semester">Semester *</label>
                                <select class="form-control" id="semester" name="semester" required>
                                    <option value="">Select Semester</option>
                                    <option value="1" <%= "1".equals(request.getAttribute("selectedSemester")) ? "selected" : "" %>>Semester 1</option>
                                    <option value="2" <%= "2".equals(request.getAttribute("selectedSemester")) ? "selected" : "" %>>Semester 2</option>
                                    <option value="3" <%= "3".equals(request.getAttribute("selectedSemester")) ? "selected" : "" %>>Semester 3</option>
                                    <option value="4" <%= "4".equals(request.getAttribute("selectedSemester")) ? "selected" : "" %>>Semester 4</option>
                                    <option value="5" <%= "5".equals(request.getAttribute("selectedSemester")) ? "selected" : "" %>>Semester 5</option>
                                    <option value="6" <%= "6".equals(request.getAttribute("selectedSemester")) ? "selected" : "" %>>Semester 6</option>
                                </select>
                            </div>
                            
                            <button type="submit" class="btn btn-primary btn-block">
                                <i class="fas fa-search"></i> Get Students
                            </button>
                        </form>
                    </div>
                </div>
                
                <div class="card mt-4">
                    <div class="card-header bg-info text-white">
                        <h5 class="card-title mb-0">Attendance Legend</h5>
                    </div>
                    <div class="card-body">
                        <div class="d-flex justify-content-between mb-2">
                            <span class="badge badge-success p-2">Present</span>
                            <span>Student is present in class</span>
                        </div>
                        <div class="d-flex justify-content-between mb-2">
                            <span class="badge badge-danger p-2">Absent</span>
                            <span>Student is not present in class</span>
                        </div>
                        <div class="d-flex justify-content-between">
                            <span class="badge badge-warning p-2">Leave</span>
                            <span>Student is on authorized leave</span>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="col-md-8">
                <% 
                    List<StudentEnrollment> enrollments = (List<StudentEnrollment>) request.getAttribute("enrollments");
                    Map<Integer, Attendance> attendanceMap = (Map<Integer, Attendance>) request.getAttribute("attendanceMap");
                    
                    if (enrollments != null && !enrollments.isEmpty() && 
                        selectedClassId != null && selectedSubject != null && 
                        request.getAttribute("selectedDate") != null && request.getAttribute("selectedSemester") != null) {
                %>
                    <div class="card">
                        <div class="card-header bg-primary text-white">
                            <h5 class="card-title mb-0">Mark Attendance</h5>
                        </div>
                        <div class="card-body">
                            <form action="${pageContext.request.contextPath}/attendance/mark" method="post">
                                <input type="hidden" name="classId" value="<%= selectedClassId %>">
                                <input type="hidden" name="subjectCode" value="<%= selectedSubject %>">
                                <input type="hidden" name="date" value="<%= request.getAttribute("selectedDate") %>">
                                <input type="hidden" name="semester" value="<%= request.getAttribute("selectedSemester") %>">
                                
                                <div class="table-responsive">
                                    <table class="table table-striped">
                                        <thead>
                                            <tr>
                                                <th>Enrollment ID</th>
                                                <th>Student Name</th>
                                                <th>Attendance Status</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <% for (StudentEnrollment enrollment : enrollments) { 
                                                User student = enrollment.getUser();
                                                String currentStatus = "Absent"; // Default
                                                
                                                if (attendanceMap != null && attendanceMap.containsKey(student.getUserId())) {
                                                    Attendance existingAttendance = attendanceMap.get(student.getUserId());
                                                    if (existingAttendance != null) {
                                                        currentStatus = existingAttendance.getStatus();
                                                    }
                                                }
                                            %>
                                                <tr>
                                                    <td><%= enrollment.getEnrollmentId() %></td>
                                                    <td><%= student.getName() %></td>
                                                    <td>
                                                        <div class="btn-group btn-group-toggle" data-toggle="buttons">
                                                            <label class="btn btn-outline-success <%= "Present".equals(currentStatus) ? "active" : "" %>">
                                                                <input type="radio" name="status_<%= student.getUserId() %>" value="Present" 
                                                                       <%= "Present".equals(currentStatus) ? "checked" : "" %>> Present
                                                            </label>
                                                            <label class="btn btn-outline-danger <%= "Absent".equals(currentStatus) ? "active" : "" %>">
                                                                <input type="radio" name="status_<%= student.getUserId() %>" value="Absent" 
                                                                       <%= "Absent".equals(currentStatus) ? "checked" : "" %>> Absent
                                                            </label>
                                                            <label class="btn btn-outline-warning <%= "Leave".equals(currentStatus) ? "active" : "" %>">
                                                                <input type="radio" name="status_<%= student.getUserId() %>" value="Leave" 
                                                                       <%= "Leave".equals(currentStatus) ? "checked" : "" %>> Leave
                                                            </label>
                                                        </div>
                                                    </td>
                                                </tr>
                                            <% } %>
                                        </tbody>
                                    </table>
                                </div>
                                
                                <div class="form-group text-center mt-4">
                                    <button type="submit" class="btn btn-primary">
                                        <i class="fas fa-save"></i> Save Attendance
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                <% } else if (request.getParameter("classId") != null || request.getParameter("subjectCode") != null) { %>
                    <div class="card">
                        <div class="card-body">
                            <div class="alert alert-info">
                                <h5><i class="fas fa-info-circle"></i> No Students Found</h5>
                                <p>
                                    There are no students enrolled in the selected class, or you may not have permission
                                    to mark attendance for this class/subject combination.
                                </p>
                                <p>Please select a different class or subject.</p>
                            </div>
                        </div>
                    </div>
                <% } else { %>
                    <div class="card">
                        <div class="card-body">
                            <div class="alert alert-info">
                                <h5><i class="fas fa-info-circle"></i> Mark Attendance</h5>
                                <p>
                                    Please select a class, subject, date, and semester from the form on the left
                                    to view the student list and mark attendance.
                                </p>
                                <p>
                                    You can only mark attendance for classes and subjects that you are assigned to teach.
                                </p>
                            </div>
                        </div>
                    </div>
                <% } %>
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
