package com.attendance.dao;

import com.attendance.models.EnrollmentRequest;
import com.attendance.models.User;
import com.attendance.models.Class;
import com.attendance.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for EnrollmentRequest-related database operations
 */
public class EnrollmentRequestDAO {

    private UserDAO userDAO = new UserDAO();
    private ClassDAO classDAO = new ClassDAO();

    /**
     * Create a new enrollment request
     * @param request The enrollment request to create
     * @return The request ID if successful, -1 otherwise
     */
    public int createEnrollmentRequest(EnrollmentRequest request) {
        String sql = "INSERT INTO EnrollmentRequest (user_id, requested_role, class_id, enrollment_number) " +
                    "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, request.getUserId());
            pstmt.setString(2, request.getRequestedRole());
            
            if (request.getClassId() > 0) {
                pstmt.setInt(3, request.getClassId());
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }
            
            pstmt.setString(4, request.getEnrollmentNumber());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }

    /**
     * Get an enrollment request by ID
     * @param requestId The request ID
     * @return EnrollmentRequest object if found, null otherwise
     */
    public EnrollmentRequest getRequestById(int requestId) {
        String sql = "SELECT * FROM EnrollmentRequest WHERE request_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, requestId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    EnrollmentRequest request = new EnrollmentRequest();
                    request.setRequestId(rs.getInt("request_id"));
                    request.setUserId(rs.getInt("user_id"));
                    request.setRequestedRole(rs.getString("requested_role"));
                    
                    int classId = rs.getInt("class_id");
                    if (!rs.wasNull()) {
                        request.setClassId(classId);
                    }
                    
                    request.setEnrollmentNumber(rs.getString("enrollment_number"));
                    request.setSubmittedOn(rs.getTimestamp("submitted_on"));
                    request.setStatus(rs.getString("status"));
                    
                    // verifiedBy might be null
                    int verifiedBy = rs.getInt("verified_by");
                    if (!rs.wasNull()) {
                        request.setVerifiedBy(verifiedBy);
                    }
                    
                    request.setVerifiedOn(rs.getTimestamp("verified_on"));
                    
                    // Load related objects
                    request.setUser(userDAO.getUserById(request.getUserId()));
                    
                    if (request.getVerifiedBy() != null) {
                        request.setVerifier(userDAO.getUserById(request.getVerifiedBy()));
                    }
                    
                    if (request.getClassId() > 0) {
                        request.setClassObj(classDAO.getClassById(request.getClassId()));
                    }
                    
                    return request;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Get all enrollment requests with a specific status
     * @param status The status to filter by (e.g., "Pending", "Approved", "Rejected")
     * @return List of enrollment requests
     */
    public List<EnrollmentRequest> getRequestsByStatus(String status) {
        List<EnrollmentRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM EnrollmentRequest WHERE status = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    EnrollmentRequest request = new EnrollmentRequest();
                    request.setRequestId(rs.getInt("request_id"));
                    request.setUserId(rs.getInt("user_id"));
                    request.setRequestedRole(rs.getString("requested_role"));
                    
                    int classId = rs.getInt("class_id");
                    if (!rs.wasNull()) {
                        request.setClassId(classId);
                    }
                    
                    request.setEnrollmentNumber(rs.getString("enrollment_number"));
                    request.setSubmittedOn(rs.getTimestamp("submitted_on"));
                    request.setStatus(rs.getString("status"));
                    
                    // verifiedBy might be null
                    int verifiedBy = rs.getInt("verified_by");
                    if (!rs.wasNull()) {
                        request.setVerifiedBy(verifiedBy);
                    }
                    
                    request.setVerifiedOn(rs.getTimestamp("verified_on"));
                    
                    // Load user info
                    request.setUser(userDAO.getUserById(request.getUserId()));
                    
                    requests.add(request);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return requests;
    }

    /**
     * Get enrollment requests by department ID
     * @param departmentId The department ID
     * @param status The request status (optional, can be null)
     * @return List of enrollment requests
     */
    public List<EnrollmentRequest> getRequestsByDepartment(int departmentId, String status) {
        List<EnrollmentRequest> requests = new ArrayList<>();
        
        String sql = "SELECT er.* FROM EnrollmentRequest er " +
                    "JOIN Users u ON er.user_id = u.user_id " +
                    "WHERE u.department_id = ?";
        
        if (status != null && !status.isEmpty()) {
            sql += " AND er.status = ?";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, departmentId);
            
            if (status != null && !status.isEmpty()) {
                pstmt.setString(2, status);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    EnrollmentRequest request = new EnrollmentRequest();
                    request.setRequestId(rs.getInt("request_id"));
                    request.setUserId(rs.getInt("user_id"));
                    request.setRequestedRole(rs.getString("requested_role"));
                    
                    int classId = rs.getInt("class_id");
                    if (!rs.wasNull()) {
                        request.setClassId(classId);
                    }
                    
                    request.setEnrollmentNumber(rs.getString("enrollment_number"));
                    request.setSubmittedOn(rs.getTimestamp("submitted_on"));
                    request.setStatus(rs.getString("status"));
                    
                    // verifiedBy might be null
                    int verifiedBy = rs.getInt("verified_by");
                    if (!rs.wasNull()) {
                        request.setVerifiedBy(verifiedBy);
                    }
                    
                    request.setVerifiedOn(rs.getTimestamp("verified_on"));
                    
                    // Load user info
                    request.setUser(userDAO.getUserById(request.getUserId()));
                    
                    requests.add(request);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return requests;
    }

    /**
     * Update the status of an enrollment request
     * @param requestId The request ID
     * @param status The new status
     * @param verifiedBy The ID of the user who verified the request
     * @return true if successful, false otherwise
     */
    public boolean updateRequestStatus(int requestId, String status, int verifiedBy) {
        String sql = "UPDATE EnrollmentRequest SET status = ?, verified_by = ?, verified_on = CURRENT_TIMESTAMP " +
                    "WHERE request_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, verifiedBy);
            pstmt.setInt(3, requestId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get pending enrollment requests for a verifier based on role
     * @param verifierRole The role of the verifier
     * @param departmentId The department ID (can be null for Principal)
     * @return List of pending enrollment requests
     */
    public List<EnrollmentRequest> getPendingRequestsForVerifier(String verifierRole, Integer departmentId) {
        List<EnrollmentRequest> requests = new ArrayList<>();
        
        String sql = "SELECT er.* FROM EnrollmentRequest er " +
                    "JOIN Users u ON er.user_id = u.user_id " +
                    "WHERE er.status = 'Pending'";
        
        if (verifierRole.equals("Principal")) {
            sql += " AND er.requested_role = 'HOD'";
        } else if (verifierRole.equals("HOD") && departmentId != null) {
            sql += " AND er.requested_role IN ('Teacher', 'Class Teacher') AND u.department_id = ?";
        } else if (verifierRole.equals("Class Teacher") && departmentId != null) {
            sql += " AND er.requested_role = 'Student' AND u.department_id = ?";
        } else {
            // Invalid verifier role or missing department
            return requests;
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if ((verifierRole.equals("HOD") || verifierRole.equals("Class Teacher")) && departmentId != null) {
                pstmt.setInt(1, departmentId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    EnrollmentRequest request = new EnrollmentRequest();
                    request.setRequestId(rs.getInt("request_id"));
                    request.setUserId(rs.getInt("user_id"));
                    request.setRequestedRole(rs.getString("requested_role"));
                    
                    int classId = rs.getInt("class_id");
                    if (!rs.wasNull()) {
                        request.setClassId(classId);
                    }
                    
                    request.setEnrollmentNumber(rs.getString("enrollment_number"));
                    request.setSubmittedOn(rs.getTimestamp("submitted_on"));
                    request.setStatus(rs.getString("status"));
                    
                    // Load user info
                    request.setUser(userDAO.getUserById(request.getUserId()));
                    
                    if (request.getClassId() > 0) {
                        request.setClassObj(classDAO.getClassById(request.getClassId()));
                    }
                    
                    requests.add(request);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return requests;
    }
}
