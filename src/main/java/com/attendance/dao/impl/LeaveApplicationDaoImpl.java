package com.attendance.dao.impl;

import com.attendance.dao.LeaveApplicationDao;
import com.attendance.models.LeaveApplication;
import com.attendance.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of LeaveApplicationDao interface for database operations
 */
public class LeaveApplicationDaoImpl implements LeaveApplicationDao {
    private static final Logger LOGGER = Logger.getLogger(LeaveApplicationDaoImpl.class.getName());

    @Override
    public LeaveApplication findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM LeaveApplications WHERE application_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLeaveApplication(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding leave application by ID: " + id, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public List<LeaveApplication> findAll() throws SQLException {
        List<LeaveApplication> applications = new ArrayList<>();
        String sql = "SELECT * FROM LeaveApplications";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                applications.add(mapResultSetToLeaveApplication(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all leave applications", e);
            throw e;
        }
        
        return applications;
    }

    @Override
    public LeaveApplication save(LeaveApplication application) throws SQLException {
        String sql = "INSERT INTO LeaveApplications (student_id, from_date, to_date, reason, status, " +
                     "teacher_id, application_date, review_date, teacher_comments) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING application_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, application.getStudentId());
            stmt.setDate(2, application.getFromDate());
            stmt.setDate(3, application.getToDate());
            stmt.setString(4, application.getReason());
            stmt.setString(5, application.getStatus());
            
            if (application.getTeacherId() != 0) {
                stmt.setInt(6, application.getTeacherId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            stmt.setDate(7, application.getApplicationDate());
            
            if (application.getReviewDate() != null) {
                stmt.setDate(8, application.getReviewDate());
            } else {
                stmt.setNull(8, java.sql.Types.DATE);
            }
            
            stmt.setString(9, application.getTeacherComments());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    application.setApplicationId(rs.getInt(1));
                    return application;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving leave application: " + application, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public LeaveApplication update(LeaveApplication application) throws SQLException {
        String sql = "UPDATE LeaveApplications SET student_id = ?, from_date = ?, to_date = ?, " +
                     "reason = ?, status = ?, teacher_id = ?, application_date = ?, " +
                     "review_date = ?, teacher_comments = ? WHERE application_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, application.getStudentId());
            stmt.setDate(2, application.getFromDate());
            stmt.setDate(3, application.getToDate());
            stmt.setString(4, application.getReason());
            stmt.setString(5, application.getStatus());
            
            if (application.getTeacherId() != 0) {
                stmt.setInt(6, application.getTeacherId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            stmt.setDate(7, application.getApplicationDate());
            
            if (application.getReviewDate() != null) {
                stmt.setDate(8, application.getReviewDate());
            } else {
                stmt.setNull(8, java.sql.Types.DATE);
            }
            
            stmt.setString(9, application.getTeacherComments());
            stmt.setInt(10, application.getApplicationId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return application;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating leave application: " + application, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM LeaveApplications WHERE application_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting leave application with ID: " + id, e);
            throw e;
        }
    }

    @Override
    public List<LeaveApplication> findByStudent(int studentId) throws SQLException {
        List<LeaveApplication> applications = new ArrayList<>();
        String sql = "SELECT * FROM LeaveApplications WHERE student_id = ? ORDER BY application_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(mapResultSetToLeaveApplication(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding leave applications by student ID: " + studentId, e);
            throw e;
        }
        
        return applications;
    }

    @Override
    public List<LeaveApplication> findByTeacher(int teacherId) throws SQLException {
        List<LeaveApplication> applications = new ArrayList<>();
        String sql = "SELECT * FROM LeaveApplications WHERE teacher_id = ? ORDER BY application_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, teacherId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(mapResultSetToLeaveApplication(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding leave applications by teacher ID: " + teacherId, e);
            throw e;
        }
        
        return applications;
    }

    @Override
    public List<LeaveApplication> findByStatus(String status) throws SQLException {
        List<LeaveApplication> applications = new ArrayList<>();
        String sql = "SELECT * FROM LeaveApplications WHERE status = ? ORDER BY application_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(mapResultSetToLeaveApplication(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding leave applications by status: " + status, e);
            throw e;
        }
        
        return applications;
    }

    @Override
    public List<LeaveApplication> findActiveByDate(Date date) throws SQLException {
        List<LeaveApplication> applications = new ArrayList<>();
        String sql = "SELECT * FROM LeaveApplications WHERE status = 'APPROVED' " +
                     "AND ? BETWEEN from_date AND to_date";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, date);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(mapResultSetToLeaveApplication(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding active leave applications by date: " + date, e);
            throw e;
        }
        
        return applications;
    }

    @Override
    public List<LeaveApplication> findActiveByStudentAndDate(int studentId, Date date) throws SQLException {
        List<LeaveApplication> applications = new ArrayList<>();
        String sql = "SELECT * FROM LeaveApplications WHERE student_id = ? AND status = 'APPROVED' " +
                     "AND ? BETWEEN from_date AND to_date";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setDate(2, date);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(mapResultSetToLeaveApplication(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding active leave applications by student and date", e);
            throw e;
        }
        
        return applications;
    }

    @Override
    public boolean updateStatus(int applicationId, String status, int teacherId, String teacherComments) throws SQLException {
        String sql = "UPDATE LeaveApplications SET status = ?, teacher_id = ?, review_date = CURRENT_DATE, " +
                     "teacher_comments = ? WHERE application_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, teacherId);
            stmt.setString(3, teacherComments);
            stmt.setInt(4, applicationId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating leave application status: " + applicationId, e);
            throw e;
        }
    }

    @Override
    public boolean hasActiveLeave(int studentId, Date date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM LeaveApplications WHERE student_id = ? " +
                     "AND status = 'APPROVED' AND ? BETWEEN from_date AND to_date";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setDate(2, date);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking active leave for student: " + studentId, e);
            throw e;
        }
        
        return false;
    }
    
    /**
     * Maps a database result set to a LeaveApplication object
     * @param rs The result set positioned at the current row
     * @return A populated LeaveApplication object
     * @throws SQLException If a database error occurs
     */
    private LeaveApplication mapResultSetToLeaveApplication(ResultSet rs) throws SQLException {
        LeaveApplication application = new LeaveApplication();
        application.setApplicationId(rs.getInt("application_id"));
        application.setStudentId(rs.getInt("student_id"));
        application.setFromDate(rs.getDate("from_date"));
        application.setToDate(rs.getDate("to_date"));
        application.setReason(rs.getString("reason"));
        application.setStatus(rs.getString("status"));
        
        // Handle nullable columns
        int teacherId = rs.getInt("teacher_id");
        if (!rs.wasNull()) {
            application.setTeacherId(teacherId);
        }
        
        application.setApplicationDate(rs.getDate("application_date"));
        
        Date reviewDate = rs.getDate("review_date");
        if (reviewDate != null) {
            application.setReviewDate(reviewDate);
        }
        
        application.setTeacherComments(rs.getString("teacher_comments"));
        
        return application;
    }
}