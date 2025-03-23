package com.attendance.models;

/**
 * User model class representing a user in the system
 */
public class User {
    private int userId;
    private String name;
    private String username; // Added for username
    private String fullName; // Added for fullName
    private String phoneNo;
    private String phoneNumber; // Alternative name for phoneNo
    private String email;
    private String password;
    private String role;
    private Integer departmentId;
    private String address; // Added for address
    private String status; // Added for status
    private boolean active; // Added for active status
    
    // Constructors
    public User() {}
    
    public User(int userId, String name, String phoneNo, String email, 
                String password, String role, Integer departmentId) {
        this.userId = userId;
        this.name = name;
        this.phoneNo = phoneNo;
        this.phoneNumber = phoneNo; // Set both phone fields
        this.email = email;
        this.password = password;
        this.role = role;
        this.departmentId = departmentId;
        this.active = true; // Default to active
        this.fullName = name; // Default fullName to name
        this.username = email; // Default username to email
    }
    
    // Constructor for registration with 4 arguments (name, email, password, role)
    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = true; // Default to active
        this.fullName = name; // Default fullName to name
        this.username = email; // Default username to email
    }
    
    // Constructor for registration with 5 arguments (name, phone, email, password, role)
    public User(String name, String phoneNo, String email, String password, String role) {
        this.name = name;
        this.phoneNo = phoneNo;
        this.phoneNumber = phoneNo; // Set both phone fields
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = true; // Default to active
        this.fullName = name; // Default fullName to name
        this.username = email; // Default username to email
    }
    
    // Getters and Setters
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
        this.fullName = name; // Keep fullName in sync with name
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFullName() {
        return fullName != null ? fullName : name;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPhoneNo() {
        return phoneNo;
    }
    
    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
        this.phoneNumber = phoneNo; // Keep phoneNumber in sync
    }
    
    public String getPhoneNumber() {
        return phoneNumber != null ? phoneNumber : phoneNo;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.phoneNo = phoneNumber; // Keep phoneNo in sync
    }
    
    // Shorthand method to match controller usage
    public void setPhone(String phone) {
        setPhoneNo(phone);
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
    
    public Integer getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Check if this user is a student
     * @return true if the user's role is "Student"
     */
    public boolean isStudent() {
        return "Student".equalsIgnoreCase(this.role);
    }
    
    /**
     * Check if this user is a teacher
     * @return true if the user's role is "Teacher"
     */
    public boolean isTeacher() {
        return "Teacher".equalsIgnoreCase(this.role);
    }
    
    /**
     * Check if this user is a HOD
     * @return true if the user's role is "HOD"
     */
    public boolean isHOD() {
        return "HOD".equalsIgnoreCase(this.role);
    }
    
    /**
     * Check if this user is a Principal
     * @return true if the user's role is "Principal"
     */
    public boolean isPrincipal() {
        return "Principal".equalsIgnoreCase(this.role);
    }
    
    /**
     * Check if this user is an Admin
     * @return true if the user's role is "Admin"
     */
    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(this.role);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phoneNo='" + phoneNo + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", departmentId=" + departmentId +
                ", address='" + address + '\'' +
                ", status='" + status + '\'' +
                ", active=" + active +
                '}';
    }
}