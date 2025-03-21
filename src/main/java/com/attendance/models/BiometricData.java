package com.attendance.models;

import java.sql.Timestamp;

/**
 * Model class for BiometricData
 */
public class BiometricData {
    private int biometricId;
    private int studentId;
    private boolean faceRegistered;
    private Timestamp lastRegistrationDate;
    private String registrationStatus;  // Pending, Registered, Failed
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Extra fields for reporting (not in database)
    private String studentName;
    
    // Constructor with required fields
    public BiometricData(int studentId) {
        this.studentId = studentId;
        this.faceRegistered = false;
        this.registrationStatus = "Pending";
    }
    
    // Full constructor
    public BiometricData(int biometricId, int studentId, boolean faceRegistered, 
                       Timestamp lastRegistrationDate, String registrationStatus,
                       Timestamp createdAt, Timestamp updatedAt) {
        this.biometricId = biometricId;
        this.studentId = studentId;
        this.faceRegistered = faceRegistered;
        this.lastRegistrationDate = lastRegistrationDate;
        this.registrationStatus = registrationStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Default constructor
    public BiometricData() {
    }
    
    // Getters and Setters
    public int getBiometricId() {
        return biometricId;
    }
    
    public void setBiometricId(int biometricId) {
        this.biometricId = biometricId;
    }
    
    public int getStudentId() {
        return studentId;
    }
    
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
    
    public boolean isFaceRegistered() {
        return faceRegistered;
    }
    
    public void setFaceRegistered(boolean faceRegistered) {
        this.faceRegistered = faceRegistered;
    }
    
    public Timestamp getLastRegistrationDate() {
        return lastRegistrationDate;
    }
    
    public void setLastRegistrationDate(Timestamp lastRegistrationDate) {
        this.lastRegistrationDate = lastRegistrationDate;
    }
    
    public String getRegistrationStatus() {
        return registrationStatus;
    }
    
    public void setRegistrationStatus(String registrationStatus) {
        this.registrationStatus = registrationStatus;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    /**
     * Check if the face has been registered successfully
     * 
     * @return true if registration status is "Registered"
     */
    public boolean isRegistered() {
        return "Registered".equals(registrationStatus);
    }
    
    /**
     * Check if the face registration is pending
     * 
     * @return true if registration status is "Pending"
     */
    public boolean isPending() {
        return "Pending".equals(registrationStatus);
    }
    
    /**
     * Check if the face registration has failed
     * 
     * @return true if registration status is "Failed"
     */
    public boolean isFailed() {
        return "Failed".equals(registrationStatus);
    }
    
    @Override
    public String toString() {
        return "BiometricData{" +
                "biometricId=" + biometricId +
                ", studentId=" + studentId +
                ", faceRegistered=" + faceRegistered +
                ", registrationStatus='" + registrationStatus + '\'' +
                '}';
    }
}