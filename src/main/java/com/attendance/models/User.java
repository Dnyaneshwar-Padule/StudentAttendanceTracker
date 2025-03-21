package com.attendance.models;

import java.sql.Timestamp;

/**
 * Model class for User
 */
public class User {
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private String role; // Student, Teacher, HOD, Principal, Admin
    private String department;
    private String classRoom; // FY, SY, TY
    private String rollNo;
    private String profileImagePath;
    private boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    /**
     * Default constructor
     */
    public User() {
    }
    
    /**
     * Constructor with basic details
     * 
     * @param username the username
     * @param password the password
     * @param fullName the full name
     * @param email the email
     * @param role the role
     */
    public User(String username, String password, String fullName, String email, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.isActive = true;
    }
    
    /**
     * Constructor with minimal fields
     * 
     * @param username the username
     * @param password the password
     * @param fullName the full name
     * @param email the email
     */
    public User(String username, String password, String fullName, String email) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.isActive = true;
    }

    /**
     * Full constructor
     * 
     * @param userId the user ID
     * @param username the username
     * @param password the password
     * @param fullName the full name
     * @param email the email
     * @param phoneNumber the phone number
     * @param address the address
     * @param role the role
     * @param department the department
     * @param classRoom the class
     * @param rollNo the roll number
     * @param profileImagePath the profile image path
     * @param isActive whether the user is active
     * @param createdAt when the user was created
     * @param updatedAt when the user was last updated
     */
    public User(int userId, String username, String password, String fullName, String email, 
              String phoneNumber, String address, String role, String department, String classRoom, String rollNo, 
              String profileImagePath, boolean isActive, Timestamp createdAt, Timestamp updatedAt) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.department = department;
        this.classRoom = classRoom;
        this.rollNo = rollNo;
        this.profileImagePath = profileImagePath;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * @return the userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }
    
    /**
     * Get user name (alias for getFullName for better semantic meaning)
     * @return the user's full name
     */
    public String getName() {
        return fullName;
    }

    /**
     * @param fullName the fullName to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the phoneNumber
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @param phoneNumber the phoneNumber to set
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    /**
     * Alias for getPhoneNumber() for better compatibility
     * @return the phone number
     */
    public String getPhone() {
        return phoneNumber;
    }
    
    /**
     * Alias for setPhoneNumber() for better compatibility
     * @param phone the phone number to set
     */
    public void setPhone(String phone) {
        this.phoneNumber = phone;
    }
    
    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @return the department
     */
    public String getDepartment() {
        return department;
    }

    /**
     * @param department the department to set
     */
    public void setDepartment(String department) {
        this.department = department;
    }

    /**
     * @return the classRoom
     */
    public String getClassRoom() {
        return classRoom;
    }

    /**
     * @param classRoom the classRoom to set
     */
    public void setClassRoom(String classRoom) {
        this.classRoom = classRoom;
    }

    /**
     * @return the rollNo
     */
    public String getRollNo() {
        return rollNo;
    }

    /**
     * @param rollNo the rollNo to set
     */
    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    /**
     * @return the profileImagePath
     */
    public String getProfileImagePath() {
        return profileImagePath;
    }

    /**
     * @param profileImagePath the profileImagePath to set
     */
    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    /**
     * @return whether the user is active
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * @param isActive set whether the user is active
     */
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    /**
     * Get the user's status as a string
     * @return "Active" if user is active, "Inactive" otherwise
     */
    public String getStatus() {
        return isActive ? "Active" : "Inactive";
    }
    
    /**
     * Set user status from string
     * @param status "Active" or "Inactive"
     */
    public void setStatus(String status) {
        this.isActive = "Active".equalsIgnoreCase(status);
    }

    /**
     * @return the createdAt
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt the createdAt to set
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return the updatedAt
     */
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @param updatedAt the updatedAt to set
     */
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Check if user is a Student
     * 
     * @return true if the user is a Student
     */
    public boolean isStudent() {
        return "Student".equalsIgnoreCase(role);
    }
    
    /**
     * Check if user is a Teacher
     * 
     * @return true if the user is a Teacher
     */
    public boolean isTeacher() {
        return "Teacher".equalsIgnoreCase(role);
    }
    
    /**
     * Check if user is a Head of Department
     * 
     * @return true if the user is a HOD
     */
    public boolean isHOD() {
        return "HOD".equalsIgnoreCase(role);
    }
    
    /**
     * Check if user is a Principal
     * 
     * @return true if the user is a Principal
     */
    public boolean isPrincipal() {
        return "Principal".equalsIgnoreCase(role);
    }
    
    /**
     * Check if user is an Admin
     * 
     * @return true if the user is an Admin
     */
    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "User{" + "userId=" + userId + ", username=" + username + ", fullName=" + fullName + 
               ", email=" + email + ", role=" + role + ", department=" + department + 
               ", classRoom=" + classRoom + ", isActive=" + isActive + '}';
    }
}