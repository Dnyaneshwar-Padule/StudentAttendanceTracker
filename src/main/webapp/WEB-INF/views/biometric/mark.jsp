<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mark Attendance - Student Attendance System</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.8.1/font/bootstrap-icons.css" rel="stylesheet">
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
        .subject-card {
            transition: transform 0.2s;
            border-left: 5px solid #0d6efd;
        }
        .subject-card:hover {
            transform: translateY(-5px);
        }
        .subject-card.marked {
            border-left: 5px solid #198754;
            background-color: #f8f9fa;
        }
        .subject-card.marked .card-title {
            color: #198754;
        }
        .status-badge {
            position: absolute;
            top: 10px;
            right: 10px;
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
                        <h4 class="mb-0">Mark Attendance</h4>
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
                        
                        <div class="mb-4">
                            <h5>Date: ${today}</h5>
                            <p>Class: ${class.className} | Semester: ${enrollment.semester}</p>
                        </div>
                        
                        <c:choose>
                            <c:when test="${empty subjects}">
                                <div class="alert alert-warning" role="alert">
                                    No subjects found for your class and semester.
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="mb-4">
                                    <h5>Select a Subject to Mark Attendance:</h5>
                                    <div class="row row-cols-1 row-cols-md-2 g-4 mt-2">
                                        <c:forEach var="subjectEntry" items="${subjects}">
                                            <div class="col">
                                                <div class="card h-100 subject-card ${attendanceMarked[subjectEntry.key] ? 'marked' : ''}">
                                                    <div class="card-body">
                                                        <h5 class="card-title">${subjectEntry.value.subjectName}</h5>
                                                        <h6 class="card-subtitle mb-2 text-muted">${subjectEntry.key}</h6>
                                                        <p class="card-text">
                                                            ${not empty subjectEntry.value.description ? subjectEntry.value.description : 'No description available'}
                                                        </p>
                                                        
                                                        <c:choose>
                                                            <c:when test="${attendanceMarked[subjectEntry.key]}">
                                                                <span class="badge bg-success status-badge">Marked</span>
                                                                <button class="btn btn-outline-success" disabled>
                                                                    <i class="bi bi-check-circle-fill"></i> Attendance Marked
                                                                </button>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <button class="btn btn-primary mark-attendance-btn" 
                                                                        data-subject-code="${subjectEntry.key}"
                                                                        data-subject-name="${subjectEntry.value.subjectName}">
                                                                    <i class="bi bi-camera-fill"></i> Mark Attendance
                                                                </button>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </div>
                                
                                <!-- Attendance Capture Modal -->
                                <div class="modal fade" id="attendanceModal" tabindex="-1" aria-labelledby="attendanceModalLabel" aria-hidden="true">
                                    <div class="modal-dialog modal-lg">
                                        <div class="modal-content">
                                            <div class="modal-header">
                                                <h5 class="modal-title" id="attendanceModalLabel">Mark Attendance for <span id="modalSubjectName"></span></h5>
                                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                            </div>
                                            <div class="modal-body">
                                                <p class="text-muted">
                                                    Please position your face properly within the circle guide and ensure good lighting.
                                                </p>
                                                
                                                <form id="attendanceForm" action="${pageContext.request.contextPath}/biometric/mark" method="post" enctype="multipart/form-data">
                                                    <input type="hidden" name="subjectCode" id="subjectCode">
                                                    
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
                                                                <i class="bi bi-check-circle-fill"></i> Mark Present
                                                            </button>
                                                        </div>
                                                        
                                                        <input type="hidden" name="image" id="image-data">
                                                    </div>
                                                </form>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
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
            // Variables for video elements
            const video = document.getElementById('video');
            const canvas = document.getElementById('canvas');
            const captureBtn = document.getElementById('capture-btn');
            const submitBtn = document.getElementById('submit-btn');
            const imageData = document.getElementById('image-data');
            
            // Variables for modal
            const attendanceModal = new bootstrap.Modal(document.getElementById('attendanceModal'));
            const modalSubjectName = document.getElementById('modalSubjectName');
            const subjectCodeInput = document.getElementById('subjectCode');
            
            // Initialize mark attendance buttons
            const markButtons = document.querySelectorAll('.mark-attendance-btn');
            markButtons.forEach(button => {
                button.addEventListener('click', function() {
                    const subjectCode = this.dataset.subjectCode;
                    const subjectName = this.dataset.subjectName;
                    
                    // Set modal data
                    modalSubjectName.textContent = subjectName;
                    subjectCodeInput.value = subjectCode;
                    
                    // Reset video/canvas
                    video.style.display = 'block';
                    canvas.style.display = 'none';
                    submitBtn.disabled = true;
                    
                    // Show modal
                    attendanceModal.show();
                    
                    // Initialize camera when modal is shown
                    initCamera();
                });
            });
            
            // Initialize camera
            function initCamera() {
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
            }
            
            // Stop camera when modal is closed
            document.getElementById('attendanceModal').addEventListener('hidden.bs.modal', function() {
                if (video.srcObject) {
                    video.srcObject.getTracks().forEach(track => track.stop());
                }
            });
            
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
            document.getElementById('attendanceForm').addEventListener('submit', function(event) {
                if (!imageData.value) {
                    event.preventDefault();
                    alert('Please capture your photo first.');
                }
            });
        });
    </script>
</body>
</html>