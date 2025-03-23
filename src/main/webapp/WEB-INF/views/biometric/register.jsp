<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Face Registration - Student Attendance System</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Custom CSS -->
    <link href="${pageContext.request.contextPath}/assets/css/styles.css" rel="stylesheet">
    <style>
        #video-container {
            width: 100%;
            max-width: 640px;
            margin: 0 auto;
            position: relative;
        }
        #video {
            width: 100%;
            border: 1px solid #ddd;
            border-radius: 8px;
            background-color: #f8f8f8;
        }
        #canvas {
            display: none;
        }
        #capture-btn {
            margin-top: 15px;
        }
        .face-guide {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 250px;
            height: 250px;
            border: 2px dashed #198754;
            border-radius: 50%;
            pointer-events: none;
            z-index: 10;
        }
        .registration-status {
            margin-top: 20px;
            padding: 15px;
            border-radius: 8px;
        }
        .registered {
            background-color: #d1e7dd;
            border: 1px solid #badbcc;
            color: #0f5132;
        }
        .not-registered {
            background-color: #f8d7da;
            border: 1px solid #f5c2c7;
            color: #842029;
        }
    </style>
</head>
<body>
    <jsp:include page="../common/header.jsp" />
    
    <div class="container mt-4">
        <div class="row">
            <div class="col-lg-8 offset-lg-2">
                <div class="card shadow">
                    <div class="card-header bg-primary text-white">
                        <h4 class="mb-0">Face Registration</h4>
                    </div>
                    <div class="card-body">
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger" role="alert">
                                ${error}
                            </div>
                        </c:if>
                        <c:if test="${not empty success}">
                            <div class="alert alert-success" role="alert">
                                ${success}
                            </div>
                        </c:if>
                        <c:if test="${not empty info}">
                            <div class="alert alert-info" role="alert">
                                ${info}
                            </div>
                        </c:if>
                        
                        <div class="registration-status ${isFaceRegistered ? 'registered' : 'not-registered'}">
                            <h5 class="mb-0">
                                <c:choose>
                                    <c:when test="${isFaceRegistered}">
                                        <i class="bi bi-check-circle-fill"></i> Your face is already registered
                                    </c:when>
                                    <c:otherwise>
                                        <i class="bi bi-exclamation-circle-fill"></i> Your face is not registered yet
                                    </c:otherwise>
                                </c:choose>
                            </h5>
                        </div>
                        
                        <div class="mt-4">
                            <p class="text-muted">
                                Please follow these instructions for a successful face registration:
                            </p>
                            <ul class="text-muted">
                                <li>Make sure you are in a well-lit area</li>
                                <li>Position your face within the circle guide</li>
                                <li>Remove glasses, masks, or any face coverings</li>
                                <li>Look directly at the camera with a neutral expression</li>
                            </ul>
                        </div>
                        
                        <form id="registrationForm" action="${pageContext.request.contextPath}/biometric/register" method="post" enctype="multipart/form-data">
                            <div class="mt-4">
                                <div id="video-container" class="mb-3">
                                    <div class="face-guide"></div>
                                    <video id="video" autoplay></video>
                                    <canvas id="canvas"></canvas>
                                </div>
                                
                                <div class="d-grid gap-2">
                                    <button type="button" id="capture-btn" class="btn btn-success">
                                        <i class="bi bi-camera-fill"></i> Capture Photo
                                    </button>
                                    <button type="submit" id="submit-btn" class="btn btn-primary" disabled>
                                        <i class="bi bi-check-circle-fill"></i> Register Face
                                    </button>
                                </div>
                                
                                <input type="hidden" name="image" id="image-data">
                            </div>
                        </form>
                        
                        <div class="mt-4">
                            <p><strong>Note:</strong> Your face data will be used solely for attendance purposes and is securely stored in the system.</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <jsp:include page="../common/footer.jsp" />
    
    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
    <!-- Custom JS -->
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const video = document.getElementById('video');
            const canvas = document.getElementById('canvas');
            const captureBtn = document.getElementById('capture-btn');
            const submitBtn = document.getElementById('submit-btn');
            const imageData = document.getElementById('image-data');
            
            // Access webcam
            if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
                navigator.mediaDevices.getUserMedia({ video: true })
                    .then(function(stream) {
                        video.srcObject = stream;
                    })
                    .catch(function(error) {
                        console.error("Error accessing the camera: ", error);
                        alert("Could not access the camera. Please make sure you have granted camera permissions.");
                    });
            } else {
                alert("Sorry, your browser does not support accessing the camera.");
            }
            
            // Capture photo
            captureBtn.addEventListener('click', function() {
                const context = canvas.getContext('2d');
                canvas.width = video.videoWidth;
                canvas.height = video.videoHeight;
                context.drawImage(video, 0, 0, canvas.width, canvas.height);
                
                // Convert canvas to base64 image data (JPEG format)
                const dataURL = canvas.toDataURL('image/jpeg', 0.8);
                imageData.value = dataURL.split(',')[1]; // Remove the "data:image/jpeg;base64," part
                
                submitBtn.disabled = false;
                
                // Preview the captured image
                video.style.display = 'none';
                canvas.style.display = 'block';
            });
            
            // Allow retaking the photo
            canvas.addEventListener('click', function() {
                video.style.display = 'block';
                canvas.style.display = 'none';
                submitBtn.disabled = true;
            });
            
            // Form validation
            document.getElementById('registrationForm').addEventListener('submit', function(event) {
                if (!imageData.value) {
                    event.preventDefault();
                    alert('Please capture your photo first.');
                }
            });
        });
    </script>
</body>
</html>