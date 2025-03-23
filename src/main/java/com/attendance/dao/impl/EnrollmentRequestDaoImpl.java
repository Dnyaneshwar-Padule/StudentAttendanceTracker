package com.attendance.dao.impl;

import com.attendance.dao.EnrollmentRequestDao;
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
public class EnrollmentRequestDaoImpl implements EnrollmentRequestDao {
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
    
    /**
     * Maps a database result set to an EnrollmentRequest object
     * @param rs The result set positioned at the current row
     * @return A populated EnrollmentRequest object
     * @throws SQLException If a database error occurs
     */
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
        
        return request;
    }
}