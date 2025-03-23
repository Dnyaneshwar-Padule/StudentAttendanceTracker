package com.attendance.dao.impl;

import com.attendance.dao.ClassDao;
import com.attendance.dao.DepartmentDao;
import com.attendance.dao.impl.DepartmentDaoImpl;
import com.attendance.models.Class;
import com.attendance.models.Department;
import com.attendance.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of ClassDao interface for database operations
 */
public class ClassDaoImpl implements ClassDao {
    private static final Logger LOGGER = Logger.getLogger(ClassDaoImpl.class.getName());
    
    // DepartmentDao for loading department details
    private DepartmentDao departmentDao;
    
    /**
     * Default constructor
     */
    public ClassDaoImpl() {
        this.departmentDao = new DepartmentDaoImpl();
    }
    
    /**
     * Constructor with DepartmentDao
     * 
     * @param departmentDao The DepartmentDao implementation to use
     */
    public ClassDaoImpl(DepartmentDao departmentDao) {
        this.departmentDao = departmentDao;
    }

    @Override
    public Class findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM Classes WHERE class_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToClass(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding class by ID: " + id, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public List<Class> findAll() throws SQLException {
        List<Class> classes = new ArrayList<>();
        String sql = "SELECT * FROM Classes";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                classes.add(mapResultSetToClass(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all classes", e);
            throw e;
        }
        
        return classes;
    }

    @Override
    public Class save(Class classObj) throws SQLException {
        String sql = "INSERT INTO Classes (course, year, department_id, class_teacher_id) " +
                     "VALUES (?, ?, ?, ?) RETURNING class_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, classObj.getCourse());
            stmt.setString(2, classObj.getYear());
            stmt.setInt(3, classObj.getDepartmentId());
            
            if (classObj.getClassTeacherId() > 0) {
                stmt.setInt(4, classObj.getClassTeacherId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    classObj.setClassId(rs.getInt(1));
                    return classObj;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving class: " + classObj, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public Class update(Class classObj) throws SQLException {
        String sql = "UPDATE Classes SET course = ?, year = ?, department_id = ?, class_teacher_id = ? " +
                     "WHERE class_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, classObj.getCourse());
            stmt.setString(2, classObj.getYear());
            stmt.setInt(3, classObj.getDepartmentId());
            
            if (classObj.getClassTeacherId() > 0) {
                stmt.setInt(4, classObj.getClassTeacherId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            
            stmt.setInt(5, classObj.getClassId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return classObj;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating class: " + classObj, e);
            throw e;
        }
        
        return null;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM Classes WHERE class_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting class with ID: " + id, e);
            throw e;
        }
    }

    @Override
    public List<Class> findByDepartment(int departmentId) throws SQLException {
        List<Class> classes = new ArrayList<>();
        String sql = "SELECT * FROM Classes WHERE department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, departmentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    classes.add(mapResultSetToClass(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding classes by department ID: " + departmentId, e);
            throw e;
        }
        
        return classes;
    }

    @Override
    public List<Class> findByClassTeacher(int teacherId) throws SQLException {
        List<Class> classes = new ArrayList<>();
        String sql = "SELECT * FROM Classes WHERE class_teacher_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, teacherId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    classes.add(mapResultSetToClass(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding classes by teacher ID: " + teacherId, e);
            throw e;
        }
        
        return classes;
    }

    @Override
    public Class findByCourseYearAndDepartment(String course, String year, int departmentId) throws SQLException {
        String sql = "SELECT * FROM Classes WHERE course = ? AND year = ? AND department_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, course);
            stmt.setString(2, year);
            stmt.setInt(3, departmentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToClass(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding class by course, year, and department", e);
            throw e;
        }
        
        return null;
    }
    
    /**
     * Maps a database result set to a Class object
     * @param rs The result set positioned at the current row
     * @return A populated Class object
     * @throws SQLException If a database error occurs
     */
    private Class mapResultSetToClass(ResultSet rs) throws SQLException {
        Class classObj = new Class();
        classObj.setClassId(rs.getInt("class_id"));
        classObj.setCourse(rs.getString("course"));
        classObj.setYear(rs.getString("year"));
        classObj.setDepartmentId(rs.getInt("department_id"));
        
        // Handle null class_teacher_id
        int classTeacherId = rs.getInt("class_teacher_id");
        if (!rs.wasNull()) {
            classObj.setClassTeacherId(classTeacherId);
        }
        
        // Load the Department object to avoid JSP errors when accessing department properties
        try {
            if (departmentDao != null) {
                Department department = departmentDao.findById(classObj.getDepartmentId());
                if (department != null) {
                    classObj.setDepartment(department);
                } else {
                    LOGGER.log(Level.WARNING, "Department not found for ID: " + classObj.getDepartmentId());
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error loading department for class: " + classObj.getClassId(), e);
            // Don't throw this exception as it's not critical for class functionality
        }
        
        return classObj;
    }
}