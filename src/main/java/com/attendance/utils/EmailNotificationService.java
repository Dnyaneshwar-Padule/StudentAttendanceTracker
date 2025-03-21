package com.attendance.utils;

import com.attendance.models.Attendance;
import com.attendance.models.User;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Email notification service for sending attendance updates and alerts
 */
public class EmailNotificationService {
    private static final Logger LOGGER = Logger.getLogger(EmailNotificationService.class.getName());
    
    // Singleton instance
    private static EmailNotificationService instance;
    
    // Email configuration
    private final String host;
    private final String port;
    private final String username;
    private final String password;
    private final String fromEmail;
    private final boolean isEnabled;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    
    /**
     * Private constructor for singleton pattern
     */
    private EmailNotificationService() {
        // Load email configuration from environment variables
        this.host = System.getenv("MAIL_HOST");
        this.port = System.getenv("MAIL_PORT");
        this.username = System.getenv("MAIL_USERNAME");
        this.password = System.getenv("MAIL_PASSWORD");
        this.fromEmail = System.getenv("MAIL_FROM");
        
        // Enable email notifications only if all required parameters are set
        this.isEnabled = (host != null && !host.isEmpty() &&
                        port != null && !port.isEmpty() &&
                        username != null && !username.isEmpty() &&
                        password != null && !password.isEmpty() &&
                        fromEmail != null && !fromEmail.isEmpty());
        
        if (isEnabled) {
            LOGGER.info("Email notification service initialized successfully");
        } else {
            LOGGER.warning("Email notification service is disabled due to missing configuration");
        }
    }
    
    /**
     * Get the singleton instance
     * 
     * @return the EmailNotificationService instance
     */
    public static synchronized EmailNotificationService getInstance() {
        if (instance == null) {
            instance = new EmailNotificationService();
        }
        return instance;
    }
    
    /**
     * Send an attendance notification to a student
     * 
     * @param student the student to notify
     * @param attendance the attendance record
     * @param subjectName the subject name
     */
    public void sendAttendanceNotification(User student, Attendance attendance, String subjectName) {
        if (!isEnabled) {
            LOGGER.info("Email notifications are disabled. Skipping notification for student: " + student.getFullName());
            return;
        }
        
        if (student.getEmail() == null || student.getEmail().isEmpty()) {
            LOGGER.warning("Cannot send email: Student has no email address: " + student.getFullName());
            return;
        }
        
        try {
            // Set mail properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            
            // Create a Session object
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            
            // Create a MimeMessage
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(student.getEmail()));
            
            // Set subject and content based on attendance status
            String status = attendance.getStatus();
            String date = dateFormat.format(attendance.getAttendanceDate());
            
            message.setSubject("Attendance Update for " + subjectName);
            
            String emailContent = "Dear " + student.getFullName() + ",\n\n";
            
            if ("Present".equalsIgnoreCase(status)) {
                emailContent += "Your attendance has been marked as PRESENT for " + subjectName + 
                               " on " + date + ".\n\n";
            } else if ("Absent".equalsIgnoreCase(status)) {
                emailContent += "Your attendance has been marked as ABSENT for " + subjectName + 
                               " on " + date + ".\n\n";
                emailContent += "Please contact your subject teacher or class teacher if you believe this is an error.\n\n";
            } else if ("Leave".equalsIgnoreCase(status)) {
                emailContent += "Your attendance has been marked as ON LEAVE for " + subjectName + 
                               " on " + date + ".\n\n";
            }
            
            emailContent += "Attendance details:\n";
            emailContent += "- Subject: " + subjectName + "\n";
            emailContent += "- Date: " + date + "\n";
            emailContent += "- Status: " + status + "\n";
            emailContent += "- Marked by: " + attendance.getMarkedBy() + "\n";
            
            if (attendance.getRemarks() != null && !attendance.getRemarks().isEmpty()) {
                emailContent += "- Remarks: " + attendance.getRemarks() + "\n";
            }
            
            emailContent += "\nThis is an automated notification. Please do not reply to this email.\n\n";
            emailContent += "Regards,\nAttendance Management System";
            
            message.setText(emailContent);
            
            // Send the message
            Transport.send(message);
            
            LOGGER.info("Attendance notification email sent to " + student.getEmail());
            
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Failed to send email notification", e);
        }
    }
    
    /**
     * Send a low attendance alert to a student
     * 
     * @param student the student to notify
     * @param subjectName the subject name
     * @param attendancePercentage the attendance percentage
     * @param semester the semester
     */
    public void sendLowAttendanceAlert(User student, String subjectName, 
                                     double attendancePercentage, String semester) {
        if (!isEnabled) {
            LOGGER.info("Email notifications are disabled. Skipping low attendance alert for student: " + 
                      student.getFullName());
            return;
        }
        
        if (student.getEmail() == null || student.getEmail().isEmpty()) {
            LOGGER.warning("Cannot send email: Student has no email address: " + student.getFullName());
            return;
        }
        
        try {
            // Set mail properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            
            // Create a Session object
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            
            // Create a MimeMessage
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(student.getEmail()));
            
            // Set subject and content
            message.setSubject("ALERT: Low Attendance in " + subjectName);
            
            String emailContent = "Dear " + student.getFullName() + ",\n\n";
            emailContent += "This is to inform you that your attendance in " + subjectName + 
                           " for semester " + semester + " is currently at " + 
                           String.format("%.2f", attendancePercentage) + "%, which is below the required threshold.\n\n";
            
            emailContent += "Please be advised that a minimum attendance of 75% is required to be eligible " +
                           "for the final examinations.\n\n";
            
            emailContent += "We recommend you to attend all upcoming classes to improve your attendance percentage.\n\n";
            
            emailContent += "If you have any concerns, please contact your subject teacher or class teacher.\n\n";
            
            emailContent += "Regards,\nAttendance Management System";
            
            message.setText(emailContent);
            
            // Send the message
            Transport.send(message);
            
            LOGGER.info("Low attendance alert email sent to " + student.getEmail());
            
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Failed to send low attendance alert email", e);
        }
    }
    
    /**
     * Send a weekly attendance report to a student
     * 
     * @param student the student to notify
     * @param reportContent the HTML content of the report
     */
    public void sendWeeklyAttendanceReport(User student, String reportContent) {
        if (!isEnabled) {
            LOGGER.info("Email notifications are disabled. Skipping weekly report for student: " + 
                      student.getFullName());
            return;
        }
        
        if (student.getEmail() == null || student.getEmail().isEmpty()) {
            LOGGER.warning("Cannot send email: Student has no email address: " + student.getFullName());
            return;
        }
        
        try {
            // Set mail properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            
            // Create a Session object
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            
            // Create a MimeMessage
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(student.getEmail()));
            
            // Set subject and content
            message.setSubject("Weekly Attendance Report");
            message.setContent(reportContent, "text/html; charset=utf-8");
            
            // Send the message
            Transport.send(message);
            
            LOGGER.info("Weekly attendance report email sent to " + student.getEmail());
            
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Failed to send weekly attendance report email", e);
        }
    }
}