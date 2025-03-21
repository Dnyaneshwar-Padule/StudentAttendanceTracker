<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>404 Not Found - Student Attendance Management System</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
    <!-- Custom CSS -->
    <link href="${pageContext.request.contextPath}/assets/css/styles.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container">
        <div class="row justify-content-center mt-5">
            <div class="col-md-8 text-center">
                <div class="card shadow">
                    <div class="card-body p-5">
                        <div class="display-1 text-danger mb-4">
                            <i class="fas fa-exclamation-triangle"></i> 404
                        </div>
                        <h1 class="mb-4">Page Not Found</h1>
                        <p class="lead mb-4">The page you are looking for might have been removed, had its name changed, or is temporarily unavailable.</p>
                        
                        <hr class="my-4">
                        
                        <div class="d-grid gap-2 col-6 mx-auto">
                            <a href="${pageContext.request.contextPath}/" class="btn btn-primary">
                                <i class="fas fa-home"></i> Back to Home
                            </a>
                            <button onclick="window.history.back()" class="btn btn-outline-secondary">
                                <i class="fas fa-arrow-left"></i> Go Back
                            </button>
                        </div>
                    </div>
                </div>
                
                <div class="mt-3 text-muted">
                    <small>&copy; 2025 Student Attendance Management System</small>
                </div>
            </div>
        </div>
    </div>

    <!-- Bootstrap Bundle with Popper -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>