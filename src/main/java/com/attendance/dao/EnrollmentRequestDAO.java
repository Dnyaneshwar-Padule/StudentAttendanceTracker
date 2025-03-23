package com.attendance.dao;

import com.attendance.models.EnrollmentRequest;
import java.util.List;

/**
 * Data Access Object interface for EnrollmentRequest entity
 */
public interface EnrollmentRequestDAO {
    
    /**
     * Create a new enrollment request in the database
     * 
     * @param request The enrollment request to create
     * @return The ID of the created enrollment request
     */
    int createEnrollmentRequest(EnrollmentRequest request);
    
    /**
     * Get an enrollment request by ID
     * 
     * @param requestId The ID of the enrollment request
     * @return The enrollment request, or null if not found
     */
    EnrollmentRequest getEnrollmentRequestById(int requestId);
    
    /**
     * Get enrollment requests by student ID
     * 
     * @param studentId The ID of the student
     * @return List of enrollment requests for the specified student
     */
    List<EnrollmentRequest> getEnrollmentRequestsByStudentId(int studentId);
    
    /**
     * Get enrollment requests by class ID
     * 
     * @param classId The ID of the class
     * @return List of enrollment requests for the specified class
     */
    List<EnrollmentRequest> getEnrollmentRequestsByClassId(int classId);
    
    /**
     * Get enrollment requests by status
     * 
     * @param status The status of the enrollment request
     * @return List of enrollment requests with the specified status
     */
    List<EnrollmentRequest> getEnrollmentRequestsByStatus(String status);
    
    /**
     * Update an existing enrollment request
     * 
     * @param request The enrollment request with updated information
     * @return true if successful, false otherwise
     */
    boolean updateEnrollmentRequest(EnrollmentRequest request);
    
    /**
     * Delete an enrollment request by ID
     * 
     * @param requestId The ID of the enrollment request to delete
     * @return true if successful, false otherwise
     */
    boolean deleteEnrollmentRequest(int requestId);
    
    /**
     * Get all enrollment requests
     * 
     * @return List of all enrollment requests
     */
    List<EnrollmentRequest> getAllEnrollmentRequests();
    
    /**
     * Approve an enrollment request
     * 
     * @param requestId The ID of the enrollment request to approve
     * @param approverUserId The ID of the user approving the request
     * @return true if successful, false otherwise
     */
    boolean approveEnrollmentRequest(int requestId, int approverUserId);
    
    /**
     * Reject an enrollment request
     * 
     * @param requestId The ID of the enrollment request to reject
     * @param approverUserId The ID of the user rejecting the request
     * @param reason The reason for rejection
     * @return true if successful, false otherwise
     */
    boolean rejectEnrollmentRequest(int requestId, int approverUserId, String reason);
    
    /**
     * Get pending enrollment requests for a department
     * 
     * @param departmentId The ID of the department
     * @return List of pending enrollment requests for the specified department
     */
    List<EnrollmentRequest> getPendingEnrollmentRequestsByDepartmentId(int departmentId);
}