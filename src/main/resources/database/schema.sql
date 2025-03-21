-- Database Schema for Student Attendance Management System

-- Users table
CREATE TABLE IF NOT EXISTS Users (
    user_id SERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    role VARCHAR(20) NOT NULL CHECK (role IN ('Admin', 'Principal', 'HOD', 'Teacher', 'Student')),
    status VARCHAR(20) NOT NULL DEFAULT 'Active' CHECK (status IN ('Active', 'Inactive', 'Suspended')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Departments table
CREATE TABLE IF NOT EXISTS Departments (
    department_id SERIAL PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL,
    department_code VARCHAR(20) UNIQUE NOT NULL,
    hod_id INTEGER REFERENCES Users(user_id),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Classes table
CREATE TABLE IF NOT EXISTS Classes (
    class_id SERIAL PRIMARY KEY,
    class_name VARCHAR(100) NOT NULL,
    department_id INTEGER NOT NULL REFERENCES Departments(department_id),
    class_teacher_id INTEGER REFERENCES Users(user_id),
    max_students INTEGER,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Subjects table
CREATE TABLE IF NOT EXISTS Subjects (
    subject_code VARCHAR(20) PRIMARY KEY,
    subject_name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- DepartmentSubjects table (Mapping subjects to departments and semesters)
CREATE TABLE IF NOT EXISTS DepartmentSubjects (
    department_subject_id SERIAL PRIMARY KEY,
    department_id INTEGER NOT NULL REFERENCES Departments(department_id),
    subject_code VARCHAR(20) NOT NULL REFERENCES Subjects(subject_code),
    semester VARCHAR(10) NOT NULL,
    credits INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE (department_id, subject_code, semester)
);

-- StudentEnrollments table
CREATE TABLE IF NOT EXISTS StudentEnrollments (
    enrollment_id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES Users(user_id),
    class_id INTEGER NOT NULL REFERENCES Classes(class_id),
    semester VARCHAR(10) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    enrollment_date DATE NOT NULL DEFAULT CURRENT_DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'Active' CHECK (status IN ('Active', 'Inactive', 'Completed', 'Dropped')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE (student_id, academic_year)
);

-- TeacherAssignments table
CREATE TABLE IF NOT EXISTS TeacherAssignments (
    assignment_id SERIAL PRIMARY KEY,
    teacher_id INTEGER NOT NULL REFERENCES Users(user_id),
    subject_code VARCHAR(20) NOT NULL REFERENCES Subjects(subject_code),
    class_id INTEGER NOT NULL REFERENCES Classes(class_id),
    academic_year VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Active' CHECK (status IN ('Active', 'Inactive')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE (teacher_id, subject_code, class_id, academic_year)
);

-- Attendance table
CREATE TABLE IF NOT EXISTS Attendance (
    attendance_id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES Users(user_id),
    subject_code VARCHAR(20) NOT NULL REFERENCES Subjects(subject_code),
    attendance_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('Present', 'Absent', 'Leave')),
    semester VARCHAR(10) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    marked_by VARCHAR(100),
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE (student_id, subject_code, attendance_date)
);

-- EnrollmentRequests table (for student registration workflow)
CREATE TABLE IF NOT EXISTS EnrollmentRequests (
    request_id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES Users(user_id),
    class_id INTEGER NOT NULL REFERENCES Classes(class_id),
    semester VARCHAR(10) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending' CHECK (status IN ('Pending', 'Approved', 'Rejected')),
    requested_date DATE NOT NULL DEFAULT CURRENT_DATE,
    approved_by INTEGER REFERENCES Users(user_id),
    approved_date DATE,
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- BiometricData table (for storing information about registered faces)
CREATE TABLE IF NOT EXISTS BiometricData (
    biometric_id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES Users(user_id),
    face_registered BOOLEAN NOT NULL DEFAULT FALSE,
    last_registration_date TIMESTAMP,
    registration_status VARCHAR(20) DEFAULT 'Pending' CHECK (registration_status IN ('Pending', 'Registered', 'Failed')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE (student_id)
);

-- Add audit triggers here if needed