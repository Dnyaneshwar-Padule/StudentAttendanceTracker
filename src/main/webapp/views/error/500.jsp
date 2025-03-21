<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>500 - Internal Server Error</title>
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
                        <i class="fas fa-exclamation-triangle"></i> 500
                    </h1>
                    <h2>Internal Server Error</h2>
                    <div class="error-details my-4">
                        <p>Sorry, something went wrong on our servers.</p>
                        <p>We're working to fix the issue as soon as possible.</p>
                        
                        <% if(exception != null) { %>
                            <div class="alert alert-danger mt-4">
                                <h5>Error Details:</h5>
                                <p><%= exception.getMessage() %></p>
                            </div>
                        <% } %>
                    </div>
                    <div class="error-actions">
                        <a href="${pageContext.request.contextPath}/" class="btn btn-primary btn-lg">
                            <i class="fas fa-home"></i> Go to Homepage
                        </a>
                        <a href="javascript:history.back()" class="btn btn-outline-secondary btn-lg ml-2">
                            <i class="fas fa-arrow-left"></i> Go Back
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
