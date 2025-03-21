<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.attendance.models.User" %>
<%@ page import="com.attendance.utils.SessionUtil" %>

<header>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" href="${pageContext.request.contextPath}/">
                <i class="fas fa-calendar-check"></i> Attendance Management
            </a>
            <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" 
                    aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            
            <div class="collapse navbar-collapse" id="navbarNav">
                <% User currentUser = SessionUtil.getUser(request); %>
                
                <% if (currentUser != null) { %>
                    <ul class="navbar-nav mr-auto">
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/dashboard">
                                <i class="fas fa-tachometer-alt"></i> Dashboard
                            </a>
                        </li>
                        
                        <% if ("Student".equals(currentUser.getRole())) { %>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/attendance/view">
                                    <i class="fas fa-eye"></i> View Attendance
                                </a>
                            </li>
                        <% } %>
                        
                        <% if ("Teacher".equals(currentUser.getRole()) || "Class Teacher".equals(currentUser.getRole())) { %>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/attendance/mark">
                                    <i class="fas fa-clipboard-check"></i> Mark Attendance
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/attendance/view">
                                    <i class="fas fa-eye"></i> View Attendance
                                </a>
                            </li>
                        <% } %>
                        
                        <% if ("Class Teacher".equals(currentUser.getRole())) { %>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/enrollment/pending">
                                    <i class="fas fa-user-check"></i> Enrollment Requests
                                </a>
                            </li>
                        <% } %>
                        
                        <% if ("HOD".equals(currentUser.getRole())) { %>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/enrollment/pending">
                                    <i class="fas fa-user-check"></i> Enrollment Requests
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/subjects/">
                                    <i class="fas fa-book"></i> Subjects
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/assignments/">
                                    <i class="fas fa-tasks"></i> Teacher Assignments
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/attendance/view">
                                    <i class="fas fa-chart-bar"></i> Attendance Reports
                                </a>
                            </li>
                        <% } %>
                        
                        <% if ("Principal".equals(currentUser.getRole())) { %>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/enrollment/pending">
                                    <i class="fas fa-user-check"></i> HOD Enrollment Requests
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/attendance/view">
                                    <i class="fas fa-chart-bar"></i> Institution Reports
                                </a>
                            </li>
                        <% } %>
                    </ul>
                    
                    <ul class="navbar-nav ml-auto">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#" id="userDropdown" role="button" 
                               data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                <i class="fas fa-user-circle"></i> <%= currentUser.getName() %> 
                                <span class="badge badge-pill badge-light"><%= currentUser.getRole() %></span>
                            </a>
                            <div class="dropdown-menu dropdown-menu-right" aria-labelledby="userDropdown">
                                <a class="dropdown-item" href="#">
                                    <i class="fas fa-user-cog"></i> Profile
                                </a>
                                <a class="dropdown-item" href="#">
                                    <i class="fas fa-key"></i> Change Password
                                </a>
                                <div class="dropdown-divider"></div>
                                <a class="dropdown-item" href="${pageContext.request.contextPath}/logout">
                                    <i class="fas fa-sign-out-alt"></i> Logout
                                </a>
                            </div>
                        </li>
                    </ul>
                <% } else { %>
                    <ul class="navbar-nav ml-auto">
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/login">
                                <i class="fas fa-sign-in-alt"></i> Login
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/register">
                                <i class="fas fa-user-plus"></i> Register
                            </a>
                        </li>
                    </ul>
                <% } %>
            </div>
        </div>
    </nav>
</header>
