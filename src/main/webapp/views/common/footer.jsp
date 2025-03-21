<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<footer class="bg-dark text-white mt-5 py-4">
    <div class="container">
        <div class="row">
            <div class="col-md-6">
                <h5>Student Attendance Management System</h5>
                <p class="text-muted">
                    A comprehensive system for tracking and managing student attendance
                    across departments, classes, and subjects.
                </p>
            </div>
            <div class="col-md-3">
                <h5>Links</h5>
                <ul class="list-unstyled">
                    <li><a href="${pageContext.request.contextPath}/" class="text-muted">Home</a></li>
                    <li><a href="${pageContext.request.contextPath}/login" class="text-muted">Login</a></li>
                    <li><a href="${pageContext.request.contextPath}/register" class="text-muted">Register</a></li>
                </ul>
            </div>
            <div class="col-md-3">
                <h5>Support</h5>
                <ul class="list-unstyled">
                    <li><a href="#" class="text-muted">Help Center</a></li>
                    <li><a href="#" class="text-muted">Contact Us</a></li>
                    <li><a href="#" class="text-muted">FAQ</a></li>
                </ul>
            </div>
        </div>
        <hr class="bg-secondary">
        <div class="row">
            <div class="col-md-12 text-center">
                <p class="mb-0">&copy; <%= java.time.Year.now().getValue() %> Student Attendance Management System. All rights reserved.</p>
            </div>
        </div>
    </div>
</footer>
