package com.attendance.models;

import java.time.LocalDateTime;

/**
 * Represents a user in the attendance management system
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private String role; // Admin, Principal, HOD, ClassTeacher, Teacher, Student
    private boolean active;
    private String status; // Active, Inactive, Suspended, etc.
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String profilePicture;
    private String department; // Associated department for faculty/staff
    private Integer departmentId; // Foreign key to departments table
    
    /**
     * Default constructor
     */
    public User() {
        this.active = true;
        this.status = "Active";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with essential fields
     */
    public User(String username, String password, String firstName, String lastName, 
                String email, String role) {
        this();
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }
    
    /**
     * Full constructor
     */
    public User(int id, String username, String password, String firstName, String lastName, 
                String email, String phoneNumber, String role, boolean active, String status,
                LocalDateTime createdAt, LocalDateTime updatedAt, String profilePicture, 
                String department, Integer departmentId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.active = active;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.profilePicture = profilePicture;
        this.department = department;
        this.departmentId = departmentId;
    }

    // Getters and Setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    /**
     * Get the full name (first name + last name)
     * 
     * @return The full name
     */
    public String getName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Set the full name by splitting it into first and last name
     * 
     * @param fullName The full name in format "First Last"
     */
    public void setFullName(String fullName) {
        if (fullName != null && fullName.contains(" ")) {
            String[] parts = fullName.trim().split("\\s+", 2);
            this.firstName = parts[0];
            this.lastName = parts[1];
        } else if (fullName != null) {
            this.firstName = fullName;
            this.lastName = "";
        }
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    /**
     * Alias for setPhoneNumber() to maintain compatibility with existing code
     */
    public void setPhone(String phone) {
        this.phoneNumber = phone;
    }
    
    /**
     * Alias for getPhoneNumber() to maintain compatibility with existing code
     */
    public String getPhone() {
        return phoneNumber;
    }
    
    /**
     * Alias for getPhoneNumber() to maintain compatibility with existing code
     */
    public String getPhoneNo() {
        return phoneNumber;
    }
    
    /**
     * Alias for setPhoneNumber() to maintain compatibility with existing code
     */
    public void setPhoneNo(String phoneNo) {
        this.phoneNumber = phoneNo;
    }
    
    /**
     * Alias for getName() to maintain compatibility with existing code
     */
    public void setName(String name) {
        setFullName(name);
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
    
    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }
    
    /**
     * Alias for getId() to maintain compatibility with existing code
     * 
     * @return The user ID
     */
    public int getUserId() {
        return id;
    }
    
    /**
     * Alias for setId() to maintain compatibility with existing code
     * 
     * @param userId The user ID
     */
    public void setUserId(int userId) {
        this.id = userId;
    }
    
    /**
     * Check if this user has a specific role
     * 
     * @param roleName The role to check
     * @return true if the user has the specified role, false otherwise
     */
    public boolean hasRole(String roleName) {
        return role != null && role.equalsIgnoreCase(roleName);
    }
    
    /**
     * Check if this user has admin privileges
     * 
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return hasRole("Admin");
    }
    
    /**
     * Check if this user has principal privileges
     * 
     * @return true if the user is a principal, false otherwise
     */
    public boolean isPrincipal() {
        return hasRole("Principal");
    }
    
    /**
     * Check if this user has HOD privileges
     * 
     * @return true if the user is an HOD, false otherwise
     */
    public boolean isHOD() {
        return hasRole("HOD");
    }
    
    /**
     * Check if this user has class teacher privileges
     * 
     * @return true if the user is a class teacher, false otherwise
     */
    public boolean isClassTeacher() {
        return hasRole("ClassTeacher");
    }
    
    /**
     * Check if this user has teacher privileges
     * 
     * @return true if the user is a teacher, false otherwise
     */
    public boolean isTeacher() {
        return hasRole("Teacher");
    }
    
    /**
     * Check if this user has student privileges
     * 
     * @return true if the user is a student, false otherwise
     */
    public boolean isStudent() {
        return hasRole("Student");
    }
    
    /**
     * Check if this user has faculty privileges (Principal, HOD, ClassTeacher, or Teacher)
     * 
     * @return true if the user is faculty, false otherwise
     */
    public boolean isFaculty() {
        return isPrincipal() || isHOD() || isClassTeacher() || isTeacher();
    }
    
    /**
     * Update the updatedAt timestamp to the current time
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "User [id=" + id + ", username=" + username + ", firstName=" + firstName + ", lastName=" + lastName
                + ", email=" + email + ", role=" + role + "]";
    }
}