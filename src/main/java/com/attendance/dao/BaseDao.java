package com.attendance.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Generic DAO interface defining common CRUD operations
 * @param <T> Type of entity
 * @param <K> Type of primary key
 */
public interface BaseDao<T, K> {
    
    /**
     * Find an entity by its primary key
     * @param id The primary key
     * @return The entity or null if not found
     * @throws SQLException If a database error occurs
     */
    T findById(K id) throws SQLException;
    
    /**
     * Find all entities
     * @return List of all entities
     * @throws SQLException If a database error occurs
     */
    List<T> findAll() throws SQLException;
    
    /**
     * Save a new entity
     * @param entity The entity to save
     * @return The saved entity with any generated IDs
     * @throws SQLException If a database error occurs
     */
    T save(T entity) throws SQLException;
    
    /**
     * Update an existing entity
     * @param entity The entity to update
     * @return The updated entity
     * @throws SQLException If a database error occurs
     */
    T update(T entity) throws SQLException;
    
    /**
     * Delete an entity by its primary key
     * @param id The primary key
     * @return true if deleted, false if not found
     * @throws SQLException If a database error occurs
     */
    boolean delete(K id) throws SQLException;
}