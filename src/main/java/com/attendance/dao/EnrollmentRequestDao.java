package com.attendance.dao;

import com.attendance.models.EnrollmentRequest;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for EnrollmentRequest entities
 */
public interface EnrollmentRequestDao extends BaseDao<EnrollmentRequest, Integer> {
    
    /**
     * Find enrollment requests by user
     * @param userId The user ID
     * @return List of enrollment requests for the specified user
     * @throws SQLException If a database error occurs
     */
    List<EnrollmentRequest> findByUser(int userId) throws SQLException;
    
    /**
     * Find enrollment requests by class
     * @param classId The class ID
     * @return List of enrollment requests for the specified class
     * @throws SQLException If a database error occurs
     */
    List<EnrollmentRequest> findByClass(int classId) throws SQLException;
    
    /**
     * Find enrollment requests by academic year
     * @param academicYear The academic year
     * @return List of enrollment requests for the specified academic year
     * @throws SQLException If a database error occurs
     */
    List<EnrollmentRequest> findByAcademicYear(String academicYear) throws SQLException;
    
    /**
     * Find enrollment requests by status
     * @param status The request status
     * @return List of enrollment requests with the specified status
     * @throws SQLException If a database error occurs
     */
    List<EnrollmentRequest> findByStatus(String status) throws SQLException;
    
    /**
     * Find enrollment requests by approver
     * @param approverId The approver user ID
     * @return List of enrollment requests for the specified approver
     * @throws SQLException If a database error occurs
     */
    List<EnrollmentRequest> findByApprover(int approverId) throws SQLException;
    
    /**
     * Find enrollment requests by department
     * @param departmentId The department ID
     * @return List of enrollment requests for the specified department
     * @throws SQLException If a database error occurs
     */
    List<EnrollmentRequest> findByDepartment(int departmentId) throws SQLException;
    
    /**
     * Update enrollment request status
     * @param requestId The request ID
     * @param status The new status
     * @param approverId The approver user ID
     * @param comments Approval/rejection comments
     * @return true if updated, false otherwise
     * @throws SQLException If a database error occurs
     */
    boolean updateStatus(int requestId, String status, int approverId, String comments) throws SQLException;
    
    /**
     * Update enrollment request status (alias for updateStatus with empty comments)
     * @param requestId The request ID
     * @param status The new status
     * @param approverId The approver user ID
     * @return true if updated, false otherwise
     * @throws SQLException If a database error occurs
     */
    default boolean updateRequestStatus(int requestId, String status, int approverId) throws SQLException {
        return updateStatus(requestId, status, approverId, "");
    }
    
    /**
     * Get request by ID
     * @param requestId The request ID
     * @return The enrollment request, or null if not found
     * @throws SQLException If a database error occurs
     */
    EnrollmentRequest getRequestById(int requestId) throws SQLException;
    
    /**
     * Get pending requests for a specific verifier role
     * @param verifierRole The role of the verifier (HOD, Class Teacher, etc)
     * @param departmentId The department ID of the verifier
     * @return List of pending enrollment requests for the specified verifier
     * @throws SQLException If a database error occurs
     */
    List<EnrollmentRequest> getPendingRequestsForVerifier(String verifierRole, Integer departmentId) throws SQLException;
}