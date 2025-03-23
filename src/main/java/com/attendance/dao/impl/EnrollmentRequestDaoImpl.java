package com.attendance.dao.impl;

import com.attendance.dao.EnrollmentRequestDao;
import com.attendance.dao.EnrollmentRequestDAO;
import com.attendance.models.EnrollmentRequest;
import com.attendance.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of EnrollmentRequestDao interface for database operations
 */
public class EnrollmentRequestDaoImpl implements EnrollmentRequestDao, EnrollmentRequestDAO {
    private static final Logger LOGGER = Logger.getLogger(EnrollmentRequestDaoImpl.class.getName());

    @Override
    public EnrollmentRequest findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM EnrollmentRequests WHERE request_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEnrollmentRequest(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding enrollment request by ID: " + id, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public List<EnrollmentRequest> findAll() throws SQLException {
        List<EnrollmentRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM EnrollmentRequests";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                requests.add(mapResultSetToEnrollmentRequest(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all enrollment requests", e);
            throw e;
        }
        
        return requests;
    }

    @Override
    public EnrollmentRequest save(EnrollmentRequest request) throws SQLException {
        String sql = "INSERT INTO EnrollmentRequests (user_id, class_id, academic_year, department_id, request_date, " +
                     "status, approver_id, approval_date, comments) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING request_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, request.getUserId());
            stmt.setInt(2, request.getClassId());
            stmt.setString(3, request.getAcademicYear());
            stmt.setInt(4, request.getDepartmentId());
            stmt.setDate(5, request.getRequestDate_SqlDate());
            stmt.setString(6, request.getStatus());
            
            if (request.getApproverId() != 0) {
                stmt.setInt(7, request.getApproverId());
            } else {
                stmt.setNull(7, java.sql.Types.INTEGER);
            }
            
            if (request.getApprovalDate() != null) {
                stmt.setDate(8, request.getApprovalDate_SqlDate());
            } else {
                stmt.setNull(8, java.sql.Types.DATE);
            }
            
            stmt.setString(9, request.getComments());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    request.setRequestId(rs.getInt(1));
                    return request;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving enrollment request: " + request, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public EnrollmentRequest update(EnrollmentRequest request) throws SQLException {
        String sql = "UPDATE EnrollmentRequests SET user_id = ?, class_id = ?, academic_year = ?, department_id = ?, " +
                     "request_date = ?, status = ?, approver_id = ?, approval_date = ?, comments = ? " +
                     "WHERE request_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, request.getUserId());
            stmt.setInt(2, request.getClassId());
            stmt.setString(3, request.getAcademicYear());
            stmt.setInt(4, request.getDepartmentId());
            stmt.setDate(5, request.getRequestDate_SqlDate());
            stmt.setString(6, request.getStatus());
            
            if (request.getApproverId() != 0) {
                stmt.setInt(7, request.getApproverId());
            } else {
                stmt.setNull(7, java.sql.Types.INTEGER);
            }
            
            if (request.getApprovalDate() != null) {
                stmt.setDate(8, request.getApprovalDate_SqlDate());
            } else {
                stmt.setNull(8, java.sql.Types.DATE);
            }
            
            stmt.setString(9, request.getComments());
            stmt.setInt(10, request.getRequestId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return request;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating enrollment request: " + request, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM EnrollmentRequests WHERE request_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting enrollment request with ID: " + id, e);
            throw e;
        }
    }

    @Override
    public List<EnrollmentRequest> findByUser(int userId) throws SQLException {
        List<EnrollmentRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM EnrollmentRequests WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapResultSetToEnrollmentRequest(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding enrollment requests by user ID: " + userId, e);
            throw e;
        }
        
        return requests;
    }

    @Override
    public List<EnrollmentRequest> findByClass(int classId) throws SQLException {
        List<EnrollmentRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM EnrollmentRequests WHERE class_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapResultSetToEnrollmentRequest(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding enrollment requests by class ID: " + classId, e);
            throw e;
        }
        
        return requests;
    }

    @Override
    public List<EnrollmentRequest> findByAcademicYear(String academicYear) throws SQLException {
        List<EnrollmentRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM EnrollmentRequests WHERE academic_year = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, academicYear);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapResultSetToEnrollmentRequest(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding enrollment requests by academic year: " + academicYear, e);
            throw e;
        }
        
        return requests;
    }

    @Override
    public List<EnrollmentRequest> findByStatus(String status) throws SQLException {
        List<EnrollmentRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM EnrollmentRequests WHERE status = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapResultSetToEnrollmentRequest(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding enrollment requests by status: " + status, e);
            throw e;
        }
        
        return requests;
    }

    @Override
    public List<EnrollmentRequest> findByApprover(int approverId) throws SQLException {
        List<EnrollmentRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM EnrollmentRequests WHERE approver_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, approverId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapResultSetToEnrollmentRequest(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding enrollment requests by approver ID: " + approverId, e);
            throw e;
        }
        
        return requests;
    }

    @Override
    public List<EnrollmentRequest> findByDepartment(int departmentId) throws SQLException {
        List<EnrollmentRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM EnrollmentRequests WHERE department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, departmentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapResultSetToEnrollmentRequest(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding enrollment requests by department ID: " + departmentId, e);
            throw e;
        }
        
        return requests;
    }

    @Override
    public boolean updateStatus(int requestId, String status, int approverId, String comments) throws SQLException {
        String sql = "UPDATE EnrollmentRequests SET status = ?, approver_id = ?, approval_date = CURRENT_DATE, " +
                     "comments = ? WHERE request_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, approverId);
            stmt.setString(3, comments);
            stmt.setInt(4, requestId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating enrollment request status: " + requestId, e);
            throw e;
        }
    }
    
    // Implementations without SQLExceptions for EnrollmentRequestDAO interface
    @Override
    public EnrollmentRequest getRequestById(int requestId) {
        try {
            return findById(requestId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting request by ID: " + requestId, e);
            return null;
        }
    }
    
    @Override
    public boolean updateRequestStatus(int requestId, String status, int approverId) {
        String sql = "UPDATE EnrollmentRequests SET status = ?, approver_id = ?, approval_date = CURRENT_DATE WHERE request_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, approverId);
            stmt.setInt(3, requestId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating request status for request ID: " + requestId, e);
            return false;
        }
    }
    
    @Override
    public List<EnrollmentRequest> getPendingRequestsForVerifier(String verifierRole, Integer departmentId) {
        List<EnrollmentRequest> requests = new ArrayList<>();
        String sql;
        
        if (departmentId == null) {
            // For admin/principal who can see all pending requests
            sql = "SELECT * FROM EnrollmentRequests WHERE status = 'Pending'";
            
            try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    requests.add(mapResultSetToEnrollmentRequest(rs));
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error finding pending requests for " + verifierRole, e);
                return new ArrayList<>(); // Return empty list instead of throwing exception
            }
        } else {
            // For HOD or ClassTeacher who can only see requests from their department
            sql = "SELECT er.* FROM EnrollmentRequests er " +
                  "JOIN Classes c ON er.class_id = c.class_id " +
                  "WHERE er.status = 'Pending' AND er.department_id = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, departmentId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        requests.add(mapResultSetToEnrollmentRequest(rs));
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error finding pending requests for " + verifierRole + " in department " + departmentId, e);
                return new ArrayList<>(); // Return empty list instead of throwing exception
            }
        }
        
        return requests;
    }
    
    /**
     * Maps a database result set to an EnrollmentRequest object
     * @param rs The result set positioned at the current row
     * @return A populated EnrollmentRequest object
     * @throws SQLException If a database error occurs
     */
    // EnrollmentRequestDAO interface implementations
    
    @Override
    public int createEnrollmentRequest(EnrollmentRequest request) {
        try {
            EnrollmentRequest saved = save(request);
            return saved != null ? saved.getRequestId() : -1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating enrollment request", e);
            return -1;
        }
    }
    
    @Override
    public EnrollmentRequest getEnrollmentRequestById(int requestId) {
        try {
            return findById(requestId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting enrollment request by ID: " + requestId, e);
            return null;
        }
    }
    
    @Override
    public List<EnrollmentRequest> getEnrollmentRequestsByStudentId(int studentId) {
        try {
            return findByUser(studentId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting enrollment requests by student ID: " + studentId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<EnrollmentRequest> getEnrollmentRequestsByClassId(int classId) {
        try {
            return findByClass(classId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting enrollment requests by class ID: " + classId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<EnrollmentRequest> getEnrollmentRequestsByStatus(String status) {
        try {
            return findByStatus(status);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting enrollment requests by status: " + status, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean updateEnrollmentRequest(EnrollmentRequest request) {
        try {
            return update(request) != null;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating enrollment request", e);
            return false;
        }
    }
    
    @Override
    public boolean deleteEnrollmentRequest(int requestId) {
        try {
            return delete(requestId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting enrollment request with ID: " + requestId, e);
            return false;
        }
    }
    
    @Override
    public List<EnrollmentRequest> getAllEnrollmentRequests() {
        try {
            return findAll();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all enrollment requests", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean approveEnrollmentRequest(int requestId, int approverUserId) {
        try {
            return updateRequestStatus(requestId, "Approved", approverUserId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error approving enrollment request with ID: " + requestId, e);
            return false;
        }
    }
    
    @Override
    public boolean rejectEnrollmentRequest(int requestId, int approverUserId, String reason) {
        try {
            EnrollmentRequest request = findById(requestId);
            if (request != null) {
                request.setStatus("Rejected");
                request.setApproverId(approverUserId);
                request.setComments(reason);
                return update(request) != null;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error rejecting enrollment request with ID: " + requestId, e);
            return false;
        }
    }
    
    @Override
    public List<EnrollmentRequest> getPendingEnrollmentRequestsByDepartmentId(int departmentId) {
        try {
            List<EnrollmentRequest> requests = new ArrayList<>();
            List<EnrollmentRequest> allPending = findByStatus("Pending");
            
            for (EnrollmentRequest request : allPending) {
                if (request.getDepartmentId() == departmentId) {
                    requests.add(request);
                }
            }
            
            return requests;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting pending enrollment requests for department: " + departmentId, e);
            return new ArrayList<>();
        }
    }
    
    private EnrollmentRequest mapResultSetToEnrollmentRequest(ResultSet rs) throws SQLException {
        EnrollmentRequest request = new EnrollmentRequest();
        request.setRequestId(rs.getInt("request_id"));
        request.setUserId(rs.getInt("user_id"));
        request.setClassId(rs.getInt("class_id"));
        request.setAcademicYear(rs.getString("academic_year"));
        request.setDepartmentId(rs.getInt("department_id"));
        request.setRequestDate(rs.getDate("request_date"));
        request.setStatus(rs.getString("status"));
        
        // Handle nullable columns
        int approverId = rs.getInt("approver_id");
        if (!rs.wasNull()) {
            request.setApproverId(approverId);
        }
        
        Date approvalDate = rs.getDate("approval_date");
        if (approvalDate != null) {
            request.setApprovalDate(approvalDate);
        }
        
        request.setComments(rs.getString("comments"));
        
        // Get the requested role and enrollment number if available
        try {
            String requestedRole = rs.getString("requested_role");
            if (requestedRole != null) {
                request.setRequestedRole(requestedRole);
            }
        } catch (SQLException e) {
            // Column might not exist in older schema, ignore
        }
        
        try {
            String enrollmentNumber = rs.getString("enrollment_number");
            if (enrollmentNumber != null) {
                request.setEnrollmentNumber(enrollmentNumber);
            }
        } catch (SQLException e) {
            // Column might not exist in older schema, ignore
        }
        
        return request;
    }
}