package com.attendance.models;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * EnrollmentRequest model class representing EnrollmentRequest table
 */
public class EnrollmentRequest {
    private int requestId;
    private int userId;
    private String requestedRole;
    private Integer classId; // Can be null for non-student roles
    private String enrollmentNumber;
    private Timestamp submittedOn;
    private String status;
    private Integer verifiedBy;
    private Timestamp verifiedOn;
    
    // Additional required fields
    private String academicYear;
    private int departmentId;
    private Date requestDate;
    private Integer approverId;
    private Date approvalDate;
    private String comments;
    
    // Additional fields for joining
    private User user;
    private User verifier;
    private com.attendance.models.Class classObj;
    
    // Constructors
    public EnrollmentRequest() {
    }
    
    public EnrollmentRequest(int requestId, int userId, String requestedRole, Integer classId, 
                            String enrollmentNumber, Timestamp submittedOn, String status, 
                            Integer verifiedBy, Timestamp verifiedOn) {
        this.requestId = requestId;
        this.userId = userId;
        this.requestedRole = requestedRole;
        this.classId = classId;
        this.enrollmentNumber = enrollmentNumber;
        this.submittedOn = submittedOn;
        this.status = status;
        this.verifiedBy = verifiedBy;
        this.verifiedOn = verifiedOn;
    }
    
    // Getters and setters
    public int getRequestId() {
        return requestId;
    }
    
    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getRequestedRole() {
        return requestedRole;
    }
    
    public void setRequestedRole(String requestedRole) {
        this.requestedRole = requestedRole;
    }
    
    public Integer getClassId() {
        return classId;
    }
    
    public void setClassId(Integer classId) {
        this.classId = classId;
    }
    
    public String getEnrollmentNumber() {
        return enrollmentNumber;
    }
    
    public void setEnrollmentNumber(String enrollmentNumber) {
        this.enrollmentNumber = enrollmentNumber;
    }
    
    public Timestamp getSubmittedOn() {
        return submittedOn;
    }
    
    public void setSubmittedOn(Timestamp submittedOn) {
        this.submittedOn = submittedOn;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getVerifiedBy() {
        return verifiedBy;
    }
    
    public void setVerifiedBy(Integer verifiedBy) {
        this.verifiedBy = verifiedBy;
    }
    
    public Timestamp getVerifiedOn() {
        return verifiedOn;
    }
    
    public void setVerifiedOn(Timestamp verifiedOn) {
        this.verifiedOn = verifiedOn;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getUserId();
        }
    }
    
    public String getAcademicYear() {
        return academicYear;
    }
    
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
    
    public int getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }
    
    public Date getRequestDate() {
        return requestDate;
    }
    
    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }
    
    public Integer getApproverId() {
        return approverId;
    }
    
    public void setApproverId(Integer approverId) {
        this.approverId = approverId;
    }
    
    public Date getApprovalDate() {
        return approvalDate;
    }
    
    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public User getVerifier() {
        return verifier;
    }
    
    public void setVerifier(User verifier) {
        this.verifier = verifier;
        if (verifier != null) {
            this.verifiedBy = verifier.getUserId();
        }
    }
    
    public com.attendance.models.Class getClassObj() {
        return classObj;
    }
    
    public void setClassObj(com.attendance.models.Class classObj) {
        this.classObj = classObj;
        if (classObj != null) {
            this.classId = classObj.getClassId();
        }
    }
    
    @Override
    public String toString() {
        return "EnrollmentRequest{" +
                "requestId=" + requestId +
                ", userId=" + userId +
                ", requestedRole='" + requestedRole + '\'' +
                ", status='" + status + '\'' +
                ", submittedOn=" + submittedOn +
                ", academicYear='" + academicYear + '\'' +
                ", departmentId=" + departmentId +
                '}';
    }
}