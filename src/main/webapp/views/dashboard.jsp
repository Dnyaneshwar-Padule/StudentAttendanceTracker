<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Student Attendance Management System</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
    <!-- Custom CSS -->
    <link href="${pageContext.request.contextPath}/assets/css/styles.css" rel="stylesheet">
</head>
<body>
    <div class="container-fluid">
        <div class="row">
            <!-- Sidebar -->
            <div class="col-md-3 col-lg-2 d-md-block bg-dark sidebar collapse">
                <div class="pt-3 pb-4 px-3">
                    <a href="${pageContext.request.contextPath}/dashboard" class="d-flex align-items-center mb-3 text-white text-decoration-none">
                        <span class="fs-4">SAMS</span>
                    </a>
                    <hr>
                    <ul class="nav flex-column">
                        <li class="nav-item">
                            <a class="nav-link active" href="${pageContext.request.contextPath}/dashboard">
                                <i class="fas fa-tachometer-alt"></i> Dashboard
                            </a>
                        </li>
                        
                        <c:if test="${user.role == 'Admin' || user.role == 'Principal' || user.role == 'HOD'}">
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/users">
                                    <i class="fas fa-users"></i> Users
                                </a>
                            </li>
                        </c:if>
                        
                        <c:if test="${user.role == 'Admin' || user.role == 'Principal' || user.role == 'HOD'}">
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/departments">
                                    <i class="fas fa-building"></i> Departments
                                </a>
                            </li>
                        </c:if>
                        
                        <c:if test="${user.role != 'Student'}">
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/classes">
                                    <i class="fas fa-chalkboard"></i> Classes
                                </a>
                            </li>
                        </c:if>
                        
                        <c:if test="${user.role != 'Student'}">
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/subjects">
                                    <i class="fas fa-book"></i> Subjects
                                </a>
                            </li>
                        </c:if>
                        
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/attendance">
                                <i class="fas fa-calendar-check"></i> Attendance
                            </a>
                        </li>
                        
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/reports">
                                <i class="fas fa-chart-bar"></i> Reports
                            </a>
                        </li>
                        
                        <c:if test="${user.role == 'Student'}">
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/leave">
                                    <i class="fas fa-envelope"></i> Leave Applications
                                </a>
                            </li>
                        </c:if>
                        
                        <c:if test="${user.role == 'Teacher' || user.role == 'ClassTeacher'}">
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/leave/approve">
                                    <i class="fas fa-check-square"></i> Leave Approvals
                                </a>
                            </li>
                        </c:if>
                        
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/profile">
                                <i class="fas fa-user-circle"></i> Profile
                            </a>
                        </li>
                        
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/logout">
                                <i class="fas fa-sign-out-alt"></i> Logout
                            </a>
                        </li>
                    </ul>
                </div>
            </div>
            
            <!-- Main content -->
            <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
                <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                    <h1 class="h2">Dashboard</h1>
                    <div class="btn-toolbar mb-2 mb-md-0">
                        <div class="btn-group me-2">
                            <span class="badge bg-primary">Role: ${user.role}</span>
                        </div>
                    </div>
                </div>
                
                <!-- Welcome message -->
                <div class="alert alert-info">
                    <h4>Welcome, ${user.fullName}!</h4>
                    <p>This is your dashboard for the Student Attendance Management System.</p>
                </div>
                
                <!-- Dashboard content based on role -->
                <div class="row mt-4">
                    <c:choose>
                        <c:when test="${user.role == 'Admin'}">
                            <!-- Admin Dashboard -->
                            <div class="col-md-4 mb-4">
                                <div class="card stat-card h-100 shadow-sm">
                                    <div class="card-body">
                                        <h5 class="card-title"><i class="fas fa-users text-primary"></i> Total Users</h5>
                                        <p class="card-text display-6">${totalUsers}</p>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="col-md-4 mb-4">
                                <div class="card stat-card h-100 shadow-sm">
                                    <div class="card-body">
                                        <h5 class="card-title"><i class="fas fa-user-clock text-warning"></i> Pending Users</h5>
                                        <p class="card-text display-6">${pendingUsers}</p>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="col-md-4 mb-4">
                                <div class="card stat-card h-100 shadow-sm">
                                    <div class="card-body">
                                        <h5 class="card-title"><i class="fas fa-university text-success"></i> Departments</h5>
                                        <p class="card-text display-6">-</p>
                                    </div>
                                </div>
                            </div>
                        </c:when>
                        
                        <c:when test="${user.role == 'Teacher'}">
                            <!-- Teacher Dashboard -->
                            <div class="col-md-6 mb-4">
                                <div class="card h-100 shadow-sm">
                                    <div class="card-header">
                                        <h5><i class="fas fa-envelope-open-text"></i> Pending Leave Applications</h5>
                                    </div>
                                    <div class="card-body">
                                        <c:choose>
                                            <c:when test="${empty pendingLeaveApplications}">
                                                <p class="text-muted">No pending leave applications.</p>
                                            </c:when>
                                            <c:otherwise>
                                                <p class="card-text">You have ${pendingLeaveApplications.size()} pending leave applications to review.</p>
                                                <a href="${pageContext.request.contextPath}/leave/approve" class="btn btn-primary">Review</a>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="col-md-6 mb-4">
                                <div class="card h-100 shadow-sm">
                                    <div class="card-header">
                                        <h5><i class="fas fa-calendar-alt"></i> Today's Classes</h5>
                                    </div>
                                    <div class="card-body">
                                        <p class="text-muted">Mark attendance for your classes today.</p>
                                        <a href="${pageContext.request.contextPath}/attendance/mark" class="btn btn-primary">Mark Attendance</a>
                                    </div>
                                </div>
                            </div>
                        </c:when>
                        
                        <c:when test="${user.role == 'Student'}">
                            <!-- Student Dashboard -->
                            <div class="col-md-6 mb-4">
                                <div class="card h-100 shadow-sm">
                                    <div class="card-header">
                                        <h5><i class="fas fa-chart-pie"></i> Attendance Overview</h5>
                                    </div>
                                    <div class="card-body">
                                        <div class="text-center mb-3">
                                            <h1 class="display-4 
                                                <c:choose>
                                                    <c:when test="${attendancePercentage >= 75}">attendance-high</c:when>
                                                    <c:when test="${attendancePercentage >= 60}">attendance-medium</c:when>
                                                    <c:otherwise>attendance-low</c:otherwise>
                                                </c:choose>
                                            ">${attendancePercentage}%</h1>
                                            <p class="text-muted">Current Attendance Percentage</p>
                                        </div>
                                        <a href="${pageContext.request.contextPath}/attendance/view" class="btn btn-outline-primary">View Details</a>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="col-md-6 mb-4">
                                <div class="card h-100 shadow-sm">
                                    <div class="card-header">
                                        <h5><i class="fas fa-envelope"></i> Leave Applications</h5>
                                    </div>
                                    <div class="card-body">
                                        <c:choose>
                                            <c:when test="${empty leaveApplications}">
                                                <p class="text-muted">No leave applications yet.</p>
                                            </c:when>
                                            <c:otherwise>
                                                <p class="card-text">You have ${leaveApplications.size()} leave applications.</p>
                                            </c:otherwise>
                                        </c:choose>
                                        <a href="${pageContext.request.contextPath}/leave" class="btn btn-primary">Apply for Leave</a>
                                    </div>
                                </div>
                            </div>
                        </c:when>
                        
                        <c:otherwise>
                            <!-- Default Dashboard for other roles -->
                            <div class="col-12">
                                <div class="card shadow-sm">
                                    <div class="card-body">
                                        <h5 class="card-title">Welcome to your dashboard</h5>
                                        <p class="card-text">Use the navigation menu to manage your tasks.</p>
                                    </div>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
                
                <!-- System Announcements -->
                <div class="row mt-4">
                    <div class="col-12">
                        <div class="card shadow-sm">
                            <div class="card-header">
                                <h5><i class="fas fa-bullhorn"></i> Announcements</h5>
                            </div>
                            <div class="card-body">
                                <p>Welcome to the Student Attendance Management System. This system is still under development.</p>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </div>

    <!-- Footer -->
    <footer class="footer mt-auto py-3 bg-light">
        <div class="container text-center">
            <span class="text-muted">&copy; 2025 Student Attendance Management System</span>
        </div>
    </footer>

    <!-- Bootstrap Bundle with Popper -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js"></script>
    <!-- Custom JavaScript -->
    <script src="${pageContext.request.contextPath}/assets/js/scripts.js"></script>
</body>
</html>