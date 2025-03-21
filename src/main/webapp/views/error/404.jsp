<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>404 - Page Not Found</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.1/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-8 text-center">
                <div class="error-template">
                    <h1 class="display-1 text-danger">
                        <i class="fas fa-exclamation-circle"></i> 404
                    </h1>
                    <h2>Page Not Found</h2>
                    <div class="error-details my-4">
                        <p>Sorry, the page you requested could not be found.</p>
                        <p>The page might have been removed, had its name changed, or is temporarily unavailable.</p>
                    </div>
                    <div class="error-actions">
                        <a href="${pageContext.request.contextPath}/" class="btn btn-primary btn-lg">
                            <i class="fas fa-home"></i> Go to Homepage
                        </a>
                        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-outline-secondary btn-lg ml-2">
                            <i class="fas fa-tachometer-alt"></i> Go to Dashboard
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://code.jquery.com/jquery-3.5.1.slim.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
</body>
</html>
