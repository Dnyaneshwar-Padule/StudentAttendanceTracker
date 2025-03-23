package com.attendance.controllers;

import com.attendance.dao.UserDAO;
import com.attendance.dao.impl.UserDAOImpl;
import com.attendance.models.Attendance;
import com.attendance.models.BiometricData;
import com.attendance.models.User;
import com.attendance.utils.FaceRecognitionService;
import com.attendance.utils.EmailNotificationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for biometric attendance
 */
@WebServlet(urlPatterns = {
    "/biometric/register", 
    "/biometric/verify", 
    "/biometric/attendance"
})
public class BiometricAttendanceController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(BiometricAttendanceController.class.getName());
    
    private final UserDAO userDAO = new UserDAOImpl();
    private final FaceRecognitionService faceService = FaceRecognitionService.getInstance();
    private final EmailNotificationService emailService = EmailNotificationService.getInstance();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getServletPath();
        
        switch (path) {
            case "/biometric/register":
                // Show face registration page
                showRegistrationPage(request, response);
                break;
                
            case "/biometric/verify":
                // Show face verification page
                showVerificationPage(request, response);
                break;
                
            case "/biometric/attendance":
                // Show biometric attendance page
                showAttendancePage(request, response);
                break;
                
            default:
                response.sendRedirect(request.getContextPath() + "/dashboard");
                break;
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getServletPath();
        
        switch (path) {
            case "/biometric/register":
                // Handle face registration
                handleFaceRegistration(request, response);
                break;
                
            case "/biometric/verify":
                // Handle face verification
                handleFaceVerification(request, response);
                break;
                
            case "/biometric/attendance":
                // Handle biometric attendance
                handleBiometricAttendance(request, response);
                break;
                
            default:
                response.sendRedirect(request.getContextPath() + "/dashboard");
                break;
        }
    }
    
    /**
     * Show the face registration page
     */
    private void showRegistrationPage(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        // Only students can register their face
        if (!user.isStudent()) {
            request.setAttribute("errorMessage", "Only students can register for biometric attendance");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            return;
        }
        
        // Check if face is already registered
        boolean faceRegistered = faceService.isFaceRegistered(user.getUserId());
        request.setAttribute("faceRegistered", faceRegistered);
        
        request.getRequestDispatcher("/WEB-INF/views/biometric/register.jsp").forward(request, response);
    }
    
    /**
     * Handle face registration
     */
    private void handleFaceRegistration(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        try {
            // Attempt to register the face
            boolean success = faceService.registerFace(user.getUserId());
            
            if (success) {
                // Update biometric data
                BiometricData bioData = new BiometricData();
                bioData.setStudentId(user.getUserId());
                bioData.setFaceRegistered(true);
                bioData.setRegistrationStatus("Registered");
                bioData.setLastRegistrationDate(new java.sql.Timestamp(System.currentTimeMillis()));
                
                // Send email notification
                emailService.sendBiometricRegistrationNotification(user, true);
                
                request.setAttribute("successMessage", "Face registered successfully! You can now use biometric attendance.");
                
                LOGGER.info("Face registration successful for student: " + user.getUserId());
            } else {
                request.setAttribute("errorMessage", "Failed to register face. Please try again.");
                LOGGER.warning("Face registration failed for student: " + user.getUserId());
            }
            
            request.setAttribute("faceRegistered", success);
            request.getRequestDispatcher("/WEB-INF/views/biometric/register.jsp").forward(request, response);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during face registration", e);
            request.setAttribute("errorMessage", "An error occurred during face registration: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/biometric/register.jsp").forward(request, response);
        }
    }
    
    /**
     * Show the face verification page
     */
    private void showVerificationPage(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        // Check if face is registered
        boolean faceRegistered = faceService.isFaceRegistered(user.getUserId());
        
        if (!faceRegistered) {
            request.setAttribute("errorMessage", "You need to register your face first");
            request.getRequestDispatcher("/WEB-INF/views/biometric/register.jsp").forward(request, response);
            return;
        }
        
        request.getRequestDispatcher("/WEB-INF/views/biometric/verify.jsp").forward(request, response);
    }
    
    /**
     * Handle face verification
     */
    private void handleFaceVerification(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        try {
            // Attempt to verify the face
            boolean success = faceService.verifyFace(user.getUserId());
            
            if (success) {
                request.setAttribute("successMessage", "Face verification successful!");
                LOGGER.info("Face verification successful for student: " + user.getUserId());
            } else {
                request.setAttribute("errorMessage", "Face verification failed. Please try again.");
                LOGGER.warning("Face verification failed for student: " + user.getUserId());
            }
            
            request.getRequestDispatcher("/WEB-INF/views/biometric/verify.jsp").forward(request, response);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during face verification", e);
            request.setAttribute("errorMessage", "An error occurred during face verification: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/biometric/verify.jsp").forward(request, response);
        }
    }
    
    /**
     * Show the biometric attendance page
     */
    private void showAttendancePage(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        // Check if the user is a student
        if (!user.isStudent()) {
            request.setAttribute("errorMessage", "Only students can mark biometric attendance");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            return;
        }
        
        // Check if face is registered
        boolean faceRegistered = faceService.isFaceRegistered(user.getUserId());
        
        if (!faceRegistered) {
            request.setAttribute("errorMessage", "You need to register your face first");
            request.getRequestDispatcher("/WEB-INF/views/biometric/register.jsp").forward(request, response);
            return;
        }
        
        request.getRequestDispatcher("/WEB-INF/views/biometric/attendance.jsp").forward(request, response);
    }
    
    /**
     * Handle biometric attendance
     */
    private void handleBiometricAttendance(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        
        String subjectCode = request.getParameter("subjectCode");
        String semester = request.getParameter("semester");
        String academicYear = request.getParameter("academicYear");
        
        if (subjectCode == null || subjectCode.isEmpty() || 
            semester == null || semester.isEmpty() || 
            academicYear == null || academicYear.isEmpty()) {
            
            request.setAttribute("errorMessage", "Subject, semester, and academic year are required");
            request.getRequestDispatcher("/WEB-INF/views/biometric/attendance.jsp").forward(request, response);
            return;
        }
        
        try {
            // Verify face before marking attendance
            boolean verified = faceService.verifyFace(user.getUserId());
            
            if (!verified) {
                request.setAttribute("errorMessage", "Face verification failed. Cannot mark attendance.");
                request.getRequestDispatcher("/WEB-INF/views/biometric/attendance.jsp").forward(request, response);
                return;
            }
            
            // Create attendance record
            Attendance attendance = new Attendance();
            attendance.setStudentId(user.getUserId());
            attendance.setSubjectCode(subjectCode);
            attendance.setAttendanceDate(Date.valueOf(LocalDate.now()));
            attendance.setStatus("Present");  // Present since face is verified
            // Convert semester string to integer
            attendance.setSemester(Integer.parseInt(semester));
            attendance.setAcademicYear(academicYear);
            attendance.setMarkedBy("Biometric System");
            attendance.setRemarks("Attendance marked via face recognition");
            
            // Attendance would be saved to the database here
            // TODO: Implement AttendanceDAO and save the attendance
            
            // Send email notification
            // Since we don't have the subject details yet, we'll use the code
            emailService.sendAttendanceNotification(user, attendance, subjectCode);
            
            request.setAttribute("successMessage", "Attendance marked successfully for " + subjectCode);
            request.getRequestDispatcher("/WEB-INF/views/biometric/attendance.jsp").forward(request, response);
            
            LOGGER.info("Biometric attendance marked for student: " + user.getUserId() + ", subject: " + subjectCode);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error marking biometric attendance", e);
            request.setAttribute("errorMessage", "An error occurred: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/biometric/attendance.jsp").forward(request, response);
        }
    }
}