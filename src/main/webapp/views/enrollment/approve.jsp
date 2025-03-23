<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- Import Class model with fully qualified name to avoid ambiguity --%>
<%@ page import="com.attendance.models.Class" %>
<%@ page import="com.attendance.models.User" %>
<%@ page import="com.attendance.models.Department" %>
<%@ page import="com.attendance.models.EnrollmentRequest" %>
<%@ page import="com.attendance.models.StudentEnrollment" %>
<%@ page import="com.attendance.utils.SessionUtil" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Approve Enrollment Requests - Attendance Management System</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.1/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>
    <%-- Check if user is logged in and has appropriate role --%>
    <% 
        User user = SessionUtil.getUser(request);
        if (user == null || !("Principal".equals(user.getRole()) || "HOD".equals(user.getRole()) || "Class Teacher".equals(user.getRole()))) {
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
                        <li class="breadcrumb-item active" aria-current="page">Enrollment Requests</li>
                    </ol>
                </nav>
            </div>
        </div>
        
        <% 
            // Check if we're viewing a specific request
            EnrollmentRequest enrollmentRequest = (EnrollmentRequest) request.getAttribute("enrollmentRequest");
            User requester = (User) request.getAttribute("requester");
            Class classObj = (Class) request.getAttribute("classObj");
            Department department = (Department) request.getAttribute("department");
            
            if (enrollmentRequest != null && requester != null) {
                // Display specific request details for approval
        %>
            <div class="row justify-content-center">
                <div class="col-md-8">
                    <div class="card">
                        <div class="card-header bg-primary text-white">
                            <h5 class="card-title mb-0">Enrollment Request Review</h5>
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
                                </div>
                            <% } %>
                            
                            <h5 class="mb-3">Request Details</h5>
                            <dl class="row">
                                <dt class="col-sm-4">Request ID:</dt>
                                <dd class="col-sm-8"><%= enrollmentRequest.getRequestId() %></dd>
                                
                                <dt class="col-sm-4">Requester Name:</dt>
                                <dd class="col-sm-8"><%= requester.getName() %></dd>
                                
                                <dt class="col-sm-4">Email:</dt>
                                <dd class="col-sm-8"><%= requester.getEmail() %></dd>
                                
                                <dt class="col-sm-4">Phone:</dt>
                                <dd class="col-sm-8"><%= requester.getPhoneNo() != null ? requester.getPhoneNo() : "Not provided" %></dd>
                                
                                <dt class="col-sm-4">Requested Role:</dt>
                                <dd class="col-sm-8"><span class="badge badge-info"><%= enrollmentRequest.getRequestedRole() %></span></dd>
                                
                                <dt class="col-sm-4">Submission Date:</dt>
                                <dd class="col-sm-8"><%= enrollmentRequest.getSubmittedOn() %></dd>
                                
                                <dt class="col-sm-4">Status:</dt>
                                <dd class="col-sm-8">
                                    <span class="badge badge-warning"><%= enrollmentRequest.getStatus() %></span>
                                </dd>
                                
                                <% if ("Student".equals(enrollmentRequest.getRequestedRole()) && classObj != null && department != null) { %>
                                    <dt class="col-sm-4">Department:</dt>
                                    <dd class="col-sm-8"><%= department.getDepartmentName() %></dd>
                                    
                                    <dt class="col-sm-4">Class:</dt>
                                    <dd class="col-sm-8"><%= classObj.getClassName() %></dd>
                                    
                                    <dt class="col-sm-4">Enrollment Number:</dt>
                                    <dd class="col-sm-8"><%= enrollmentRequest.getEnrollmentNumber() %></dd>
                                <% } else if (department != null) { %>
                                    <dt class="col-sm-4">Department:</dt>
                                    <dd class="col-sm-8"><%= department.getDepartmentName() %></dd>
                                <% } %>
                            </dl>
                            
                            <hr>
                            
                            <form action="${pageContext.request.contextPath}/enrollment/approve/<%= enrollmentRequest.getRequestId() %>" method="post">
                                <div class="form-group">
                                    <label>Decision</label>
                                    <div class="custom-control custom-radio">
                                        <input type="radio" id="approve" name="decision" value="Approved" class="custom-control-input" required>
                                        <label class="custom-control-label" for="approve">Approve</label>
                                    </div>
                                    <div class="custom-control custom-radio">
                                        <input type="radio" id="reject" name="decision" value="Rejected" class="custom-control-input" required>
                                        <label class="custom-control-label" for="reject">Reject</label>
                                    </div>
                                </div>
                                
                                <div class="form-group text-center mt-4">
                                    <button type="submit" class="btn btn-primary">Submit Decision</button>
                                    <a href="${pageContext.request.contextPath}/enrollment/pending" class="btn btn-secondary ml-2">Back to List</a>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        <% } else { 
            // Display list of pending requests
            List<EnrollmentRequest> pendingRequests = (List<EnrollmentRequest>) request.getAttribute("pendingRequests");
        %>
            <div class="row">
                <div class="col-md-12">
                    <div class="card">
                        <div class="card-header bg-primary text-white">
                            <h5 class="card-title mb-0">Pending Enrollment Requests</h5>
                        </div>
                        <div class="card-body">
                            <% if (request.getAttribute("success") != null) { %>
                                <div class="alert alert-success">
                                    <%= request.getAttribute("success") %>
                                </div>
                            <% } %>
                            
                            <div class="table-responsive">
                                <table class="table table-striped">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Name</th>
                                            <th>Email</th>
                                            <th>Requested Role</th>
                                            <th>Submitted On</th>
                                            <th>Status</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% 
                                            if (pendingRequests != null && !pendingRequests.isEmpty()) {
                                                for (EnrollmentRequest req : pendingRequests) {
                                        %>
                                            <tr>
                                                <td><%= req.getRequestId() %></td>
                                                <td><%= req.getUser().getName() %></td>
                                                <td><%= req.getUser().getEmail() %></td>
                                                <td><span class="badge badge-info"><%= req.getRequestedRole() %></span></td>
                                                <td><%= req.getSubmittedOn() %></td>
                                                <td><span class="badge badge-warning"><%= req.getStatus() %></span></td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/enrollment/approve/<%= req.getRequestId() %>" 
                                                       class="btn btn-sm btn-primary">
                                                        <i class="fas fa-eye"></i> Review
                                                    </a>
                                                </td>
                                            </tr>
                                        <% 
                                                }
                                            } else { 
                                        %>
                                            <tr>
                                                <td colspan="7" class="text-center">No pending enrollment requests found</td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                    
                    <div class="card mt-4">
                        <div class="card-header bg-info text-white">
                            <h5 class="card-title mb-0">Enrollment Verification Guidelines</h5>
                        </div>
                        <div class="card-body">
                            <div class="alert alert-info">
                                <h6><i class="fas fa-info-circle"></i> Your Role as a Verifier</h6>
                                <p>As a <%= user.getRole() %>, you are responsible for verifying:</p>
                                <% if ("Principal".equals(user.getRole())) { %>
                                    <p><strong>HOD requests</strong> - Verify that the person is qualified to be an HOD for the specified department.</p>
                                <% } else if ("HOD".equals(user.getRole())) { %>
                                    <p><strong>Teacher/Class Teacher requests</strong> - Verify that the person is qualified to teach in your department.</p>
                                <% } else if ("Class Teacher".equals(user.getRole())) { %>
                                    <p><strong>Student requests</strong> - Verify that the student belongs to your class and has a valid enrollment number.</p>
                                <% } %>
                            </div>
                            
                            <p>
                                Please verify the identity and qualifications of each requester before approving their enrollment.
                                Rejected requests will notify the user and they may submit a new request.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
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
