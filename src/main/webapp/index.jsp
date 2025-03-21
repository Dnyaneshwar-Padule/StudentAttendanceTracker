<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Attendance Management System</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Custom CSS -->
    <link href="${pageContext.request.contextPath}/assets/css/styles.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container">
        <div class="row justify-content-center mt-5">
            <div class="col-md-8 text-center">
                <div class="card shadow">
                    <div class="card-body p-5">
                        <h1 class="display-4 mb-4">Student Attendance Management</h1>
                        <p class="lead mb-4">A comprehensive system for tracking and managing student attendance</p>
                        
                        <hr class="my-4">
                        
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <a href="${pageContext.request.contextPath}/login" class="btn btn-primary btn-lg w-100">
                                    Login
                                </a>
                            </div>
                            <div class="col-md-6 mb-3">
                                <a href="${pageContext.request.contextPath}/register" class="btn btn-outline-primary btn-lg w-100">
                                    Register
                                </a>
                            </div>
                        </div>
                        
                        <div class="mt-4">
                            <h5>Features:</h5>
                            <ul class="list-group list-group-flush text-start">
                                <li class="list-group-item">Role-based access for students, teachers, HODs, and principals</li>
                                <li class="list-group-item">Track attendance with real-time updates</li>
                                <li class="list-group-item">Generate comprehensive reports</li>
                                <li class="list-group-item">Manage leave applications</li>
                                <li class="list-group-item">View attendance statistics</li>
                            </ul>
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
    <!-- Custom JavaScript -->
    <script src="${pageContext.request.contextPath}/assets/js/scripts.js"></script>
</body>
</html>