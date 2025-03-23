package com.attendance.models;

import java.time.LocalDateTime;

/**
 * Represents an enrollment request in the attendance management system
 */
public class EnrollmentRequest {
    private int id;
    private int studentId; // User ID of the student
    private int userId; // Alias for studentId for compatibility
    private int classId;
    private String status; // "Pending", "Approved", "Rejected"
    private String requestReason;
    private String rejectionReason;
    private String comments; // Additional comments
    private int departmentId; // Department ID
    private String academicYear; // Academic year (e.g., "2023-24")
    private int approverUserId; // User ID of the approver
    private int approverId; // Alias for approverUserId for compatibility
    private LocalDateTime requestDate; // When the request was made
    private LocalDateTime approvalDate; // When the request was approved/rejected
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public EnrollmentRequest() {
        this.status = "Pending";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.requestDate = LocalDateTime.now();
    }
    
    /**
     * Constructor with essential fields
     */
    public EnrollmentRequest(int studentId, int classId, String requestReason) {
        this();
        this.studentId = studentId;
        this.classId = classId;
        this.requestReason = requestReason;
    }
    
    /**
     * Full constructor
     */
    public EnrollmentRequest(int id, int studentId, int classId, String status, 
                             String requestReason, String rejectionReason, int approverUserId,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.studentId = studentId;
        this.classId = classId;
        this.status = status;
        this.requestReason = requestReason;
        this.rejectionReason = rejectionReason;
        this.approverUserId = approverUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Alias for getId() to maintain compatibility with existing code
     * 
     * @return The enrollment request ID
     */
    public int getRequestId() {
        return id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
        this.userId = studentId; // Update userId to match studentId
    }
    
    /**
     * Alias for getStudentId() to maintain compatibility with existing code
     * 
     * @return The user ID
     */
    public int getUserId() {
        return studentId;
    }
    
    /**
     * Alias for setStudentId() to maintain compatibility with existing code
     * 
     * @param userId The user ID
     */
    public void setUserId(int userId) {
        this.userId = userId;
        this.studentId = userId; // Update studentId to match userId
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestReason() {
        return requestReason;
    }

    public void setRequestReason(String requestReason) {
        this.requestReason = requestReason;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public int getApproverUserId() {
        return approverUserId;
    }

    public void setApproverUserId(int approverUserId) {
        this.approverUserId = approverUserId;
        this.approverId = approverUserId; // Update approverId to match approverUserId
    }
    
    /**
     * Alias for getApproverUserId() to maintain compatibility with existing code
     * 
     * @return The approver ID
     */
    public int getApproverId() {
        return approverUserId;
    }
    
    /**
     * Alias for setApproverUserId() to maintain compatibility with existing code
     * 
     * @param approverId The approver ID
     */
    public void setApproverId(int approverId) {
        this.approverId = approverId;
        this.approverUserId = approverId; // Update approverUserId to match approverId
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public int getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getAcademicYear() {
        return academicYear;
    }
    
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
    
    public LocalDateTime getRequestDate() {
        return requestDate;
    }
    
    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }
    
    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }
    
    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }
    
    /**
     * Alias for setId() to maintain compatibility with existing code
     * 
     * @param requestId The request ID
     */
    public void setRequestId(int requestId) {
        this.id = requestId;
    }
    
    /**
     * Check if the request is pending
     * 
     * @return true if pending, false otherwise
     */
    public boolean isPending() {
        return "Pending".equalsIgnoreCase(status);
    }
    
    /**
     * Check if the request is approved
     * 
     * @return true if approved, false otherwise
     */
    public boolean isApproved() {
        return "Approved".equalsIgnoreCase(status);
    }
    
    /**
     * Check if the request is rejected
     * 
     * @return true if rejected, false otherwise
     */
    public boolean isRejected() {
        return "Rejected".equalsIgnoreCase(status);
    }
    
    /**
     * Update the updatedAt timestamp to the current time
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "EnrollmentRequest [id=" + id + ", studentId=" + studentId + ", classId=" + classId 
                + ", status=" + status + "]";
    }
}