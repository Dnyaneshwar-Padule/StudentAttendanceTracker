package com.attendance.dao.impl;

import com.attendance.dao.ClassDAO;
import com.attendance.models.Class;
import com.attendance.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of ClassDAO interface
 */
public class ClassDAOImpl implements ClassDAO {
    
    private static final Logger LOGGER = Logger.getLogger(ClassDAOImpl.class.getName());
    
    /**
     * Create a new class in the database
     */
    @Override
    public int createClass(Class classObj) {
        String sql = "INSERT INTO classes (name, year, semester, department_id, class_teacher_id, " +
                     "academic_year, start_date, end_date, description, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, classObj.getName());
            pstmt.setString(2, classObj.getYear());
            pstmt.setInt(3, classObj.getSemester());
            pstmt.setInt(4, classObj.getDepartmentId());
            pstmt.setInt(5, classObj.getClassTeacherId());
            pstmt.setString(6, classObj.getAcademicYear());
            pstmt.setDate(7, classObj.getStartDate() != null ? 
                    java.sql.Date.valueOf(classObj.getStartDate()) : null);
            pstmt.setDate(8, classObj.getEndDate() != null ? 
                    java.sql.Date.valueOf(classObj.getEndDate()) : null);
            pstmt.setString(9, classObj.getDescription());
            pstmt.setTimestamp(10, Timestamp.valueOf(classObj.getCreatedAt() != null ? 
                    classObj.getCreatedAt() : LocalDateTime.now()));
            pstmt.setTimestamp(11, Timestamp.valueOf(classObj.getUpdatedAt() != null ? 
                    classObj.getUpdatedAt() : LocalDateTime.now()));
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating class failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating class failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating class", e);
        }
        return -1;
    }
    
    /**
     * Get a class by ID
     */
    @Override
    public Class getClassById(int classId) {
        String sql = "SELECT * FROM classes WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, classId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToClass(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving class by ID", e);
        }
        return null;
    }
    
    /**
     * Get classes by department ID
     */
    @Override
    public List<Class> getClassesByDepartmentId(int departmentId) {
        return getClassesByDepartment(departmentId); // Use the alias method
    }
    
    /**
     * Alias for getClassesByDepartmentId()
     */
    @Override
    public List<Class> getClassesByDepartment(int departmentId) {
        List<Class> classes = new ArrayList<>();
        String sql = "SELECT * FROM classes WHERE department_id = ? ORDER BY year, semester";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, departmentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                classes.add(mapResultSetToClass(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving classes by department ID", e);
        }
        return classes;
    }
    
    /**
     * Get classes by class teacher ID
     */
    @Override
    public List<Class> getClassesByClassTeacherId(int classTeacherId) {
        List<Class> classes = new ArrayList<>();
        String sql = "SELECT * FROM classes WHERE class_teacher_id = ? ORDER BY academic_year DESC, year, semester";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, classTeacherId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                classes.add(mapResultSetToClass(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving classes by class teacher ID", e);
        }
        return classes;
    }
    
    /**
     * Get classes by academic year
     */
    @Override
    public List<Class> getClassesByAcademicYear(String academicYear) {
        List<Class> classes = new ArrayList<>();
        String sql = "SELECT * FROM classes WHERE academic_year = ? ORDER BY department_id, year, semester";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, academicYear);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                classes.add(mapResultSetToClass(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving classes by academic year", e);
        }
        return classes;
    }
    
    /**
     * Get classes by year
     */
    @Override
    public List<Class> getClassesByYear(String year) {
        List<Class> classes = new ArrayList<>();
        String sql = "SELECT * FROM classes WHERE year = ? ORDER BY department_id, semester";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, year);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                classes.add(mapResultSetToClass(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving classes by year", e);
        }
        return classes;
    }
    
    /**
     * Get classes by semester
     */
    @Override
    public List<Class> getClassesBySemester(int semester) {
        List<Class> classes = new ArrayList<>();
        String sql = "SELECT * FROM classes WHERE semester = ? ORDER BY department_id, year";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, semester);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                classes.add(mapResultSetToClass(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving classes by semester", e);
        }
        return classes;
    }
    
    /**
     * Update an existing class
     */
    @Override
    public boolean updateClass(Class classObj) {
        String sql = "UPDATE classes SET name = ?, year = ?, semester = ?, department_id = ?, " +
                     "class_teacher_id = ?, academic_year = ?, start_date = ?, end_date = ?, " +
                     "description = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, classObj.getName());
            pstmt.setString(2, classObj.getYear());
            pstmt.setInt(3, classObj.getSemester());
            pstmt.setInt(4, classObj.getDepartmentId());
            pstmt.setInt(5, classObj.getClassTeacherId());
            pstmt.setString(6, classObj.getAcademicYear());
            pstmt.setDate(7, classObj.getStartDate() != null ? 
                    java.sql.Date.valueOf(classObj.getStartDate()) : null);
            pstmt.setDate(8, classObj.getEndDate() != null ? 
                    java.sql.Date.valueOf(classObj.getEndDate()) : null);
            pstmt.setString(9, classObj.getDescription());
            pstmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(11, classObj.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating class", e);
            return false;
        }
    }
    
    /**
     * Delete a class by ID
     */
    @Override
    public boolean deleteClass(int classId) {
        String sql = "DELETE FROM classes WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, classId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting class", e);
            return false;
        }
    }
    
    /**
     * Get all classes
     */
    @Override
    public List<Class> getAllClasses() {
        List<Class> classes = new ArrayList<>();
        String sql = "SELECT * FROM classes ORDER BY department_id, year, semester";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                classes.add(mapResultSetToClass(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all classes", e);
        }
        return classes;
    }
    
    /**
     * Get classes by department ID and academic year
     */
    @Override
    public List<Class> getClassesByDepartmentIdAndAcademicYear(int departmentId, String academicYear) {
        List<Class> classes = new ArrayList<>();
        String sql = "SELECT * FROM classes WHERE department_id = ? AND academic_year = ? ORDER BY year, semester";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, departmentId);
            pstmt.setString(2, academicYear);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                classes.add(mapResultSetToClass(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving classes by department ID and academic year", e);
        }
        return classes;
    }
    
    /**
     * Helper method to map a ResultSet to a Class object
     */
    private Class mapResultSetToClass(ResultSet rs) throws SQLException {
        Class classObj = new Class();
        classObj.setId(rs.getInt("id"));
        classObj.setName(rs.getString("name"));
        classObj.setYear(rs.getString("year"));
        classObj.setSemester(rs.getInt("semester"));
        classObj.setDepartmentId(rs.getInt("department_id"));
        classObj.setClassTeacherId(rs.getInt("class_teacher_id"));
        classObj.setAcademicYear(rs.getString("academic_year"));
        
        java.sql.Date startDate = rs.getDate("start_date");
        java.sql.Date endDate = rs.getDate("end_date");
        
        if (startDate != null) {
            classObj.setStartDate(startDate.toLocalDate());
        }
        
        if (endDate != null) {
            classObj.setEndDate(endDate.toLocalDate());
        }
        
        classObj.setDescription(rs.getString("description"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        
        if (createdAt != null) {
            classObj.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        if (updatedAt != null) {
            classObj.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return classObj;
    }
}