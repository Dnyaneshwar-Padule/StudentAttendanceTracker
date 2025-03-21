package com.attendance.models;

/**
 * User model class representing Users table
 */
public class User {
    private int userId;
    private String name;
    private String phoneNo;
    private String email;
    private String password;
    private String role;
    private int departmentId;
    
    // Constructors
    public User() {
    }
    
    public User(int userId, String name, String phoneNo, String email, String password, String role, int departmentId) {
        this.userId = userId;
        this.name = name;
        this.phoneNo = phoneNo;
        this.email = email;
        this.password = password;
        this.role = role;
        this.departmentId = departmentId;
    }
    
    // Getters and setters
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhoneNo() {
        return phoneNo;
    }
    
    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public int getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}