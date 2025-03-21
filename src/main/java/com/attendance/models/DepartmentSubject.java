package com.attendance.models;

/**
 * DepartmentSubject model class representing Department_Subject mapping table
 */
public class DepartmentSubject {
    private int id;
    private int departmentId;
    private int classId;
    private String subjectCode;
    private String semester;
    private String yearOfStudy;
    
    // Additional fields for joining
    private Department department;
    private Class classObj;
    private Subject subject;
    
    // Constructors
    public DepartmentSubject() {
    }
    
    public DepartmentSubject(int id, int departmentId, int classId, String subjectCode) {
        this.id = id;
        this.departmentId = departmentId;
        this.classId = classId;
        this.subjectCode = subjectCode;
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }
    
    public int getClassId() {
        return classId;
    }
    
    public void setClassId(int classId) {
        this.classId = classId;
    }
    
    public String getSubjectCode() {
        return subjectCode;
    }
    
    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }
    
    public String getSemester() {
        return semester;
    }
    
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    public String getYearOfStudy() {
        return yearOfStudy;
    }
    
    public void setYearOfStudy(String yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }
    
    public Department getDepartment() {
        return department;
    }
    
    public void setDepartment(Department department) {
        this.department = department;
        if (department != null) {
            this.departmentId = department.getDepartmentId();
        }
    }
    
    public Class getClassObj() {
        return classObj;
    }
    
    public void setClassObj(Class classObj) {
        this.classObj = classObj;
        if (classObj != null) {
            this.classId = classObj.getClassId();
        }
    }
    
    public Subject getSubject() {
        return subject;
    }
    
    public void setSubject(Subject subject) {
        this.subject = subject;
        if (subject != null) {
            this.subjectCode = subject.getSubjectCode();
        }
    }
    
    @Override
    public String toString() {
        return "DepartmentSubject{" +
                "id=" + id +
                ", departmentId=" + departmentId +
                ", classId=" + classId +
                ", subjectCode='" + subjectCode + '\'' +
                ", semester='" + semester + '\'' +
                ", yearOfStudy='" + yearOfStudy + '\'' +
                '}';
    }
}