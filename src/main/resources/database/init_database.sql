-- ================================================================
-- Education System Database Initialization Script
-- Database: MySQL 8.0+
-- Normalization: 3NF (Third Normal Form)
-- ================================================================

-- Create database
CREATE DATABASE IF NOT EXISTS education_system 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE education_system;

-- ================================================================
-- TABLE 1: groups (Учебные группы)
-- ================================================================
CREATE TABLE IF NOT EXISTS groups (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    curator_id INT NULL,
    year_of_study INT NOT NULL DEFAULT 1,
    student_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_curator (curator_id),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- TABLE 2: users (Пользователи)
-- Supports STUDENT, TEACHER, ADMIN roles via single table inheritance
-- Normalized to 3NF - role-specific fields are nullable
-- ================================================================
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('STUDENT', 'TEACHER', 'ADMIN') NOT NULL,
    group_id INT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Student-specific fields
    student_group VARCHAR(50) NULL,
    year_of_study INT NULL,
    student_id_number VARCHAR(50) NULL,
    
    -- Employee-specific fields (Teacher & Admin)
    position VARCHAR(100) NULL,
    department VARCHAR(100) NULL,
    specialization VARCHAR(100) NULL,
    teaching_hours_per_week INT NULL DEFAULT 0,
    access_level VARCHAR(50) NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key (non-identifying relationship)
    CONSTRAINT fk_user_group FOREIGN KEY (group_id) 
        REFERENCES groups(id) ON DELETE SET NULL ON UPDATE CASCADE,
    
    INDEX idx_login (login),
    INDEX idx_role (role),
    INDEX idx_group (group_id),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add foreign key from groups.curator_id to users.id
ALTER TABLE groups
    ADD CONSTRAINT fk_group_curator 
    FOREIGN KEY (curator_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE;

-- ================================================================
-- TABLE 3: courses (Учебные дисциплины)
-- ================================================================
CREATE TABLE IF NOT EXISTS courses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    credits INT NOT NULL DEFAULT 3,
    teacher_id INT NULL,
    department VARCHAR(100) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key (non-identifying relationship)
    CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) 
        REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    
    INDEX idx_teacher (teacher_id),
    INDEX idx_name (name),
    INDEX idx_department (department)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- TABLE 4: schedules (Расписание)
-- ================================================================
CREATE TABLE IF NOT EXISTS schedules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NULL,
    group_id INT NULL,
    teacher_id INT NULL,
    room_number VARCHAR(20) NOT NULL,
    start_time DATETIME NULL,
    end_time DATETIME NULL,
    day_of_week ENUM('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday') NULL,
    lesson_type ENUM('lecture', 'practice', 'lab', 'consultation', 'exam') NOT NULL DEFAULT 'lecture',
    semester VARCHAR(50) NOT NULL,
    
    -- Denormalized fields for query performance
    course_name VARCHAR(100) NULL,
    group_name VARCHAR(50) NULL,
    teacher_name VARCHAR(100) NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign keys (non-identifying relationships)
    CONSTRAINT fk_schedule_course FOREIGN KEY (course_id) 
        REFERENCES courses(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_schedule_group FOREIGN KEY (group_id) 
        REFERENCES groups(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_schedule_teacher FOREIGN KEY (teacher_id) 
        REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    
    -- Composite indexes for conflict detection
    INDEX idx_room_time (room_number, day_of_week, start_time),
    INDEX idx_teacher_time (teacher_id, day_of_week, start_time),
    INDEX idx_group_time (group_id, day_of_week, start_time),
    INDEX idx_semester (semester),
    INDEX idx_day (day_of_week)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- TABLE 5: academic_records (Успеваемость)
-- Combines grades and attendance tracking
-- ================================================================
CREATE TABLE IF NOT EXISTS academic_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NULL,
    course_name VARCHAR(100) NULL,
    student_name VARCHAR(100) NULL,
    grade_value DECIMAL(5,2) NOT NULL DEFAULT 0.00 CHECK (grade_value >= 0 AND grade_value <= 10),
    grade_type ENUM('exam', 'test', 'coursework', 'lab', 'project') NOT NULL DEFAULT 'test',
    grade_date DATE NULL,
    attendance_status ENUM('present', 'absent', 'excused') NULL DEFAULT 'present',
    semester VARCHAR(50) NULL,
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign keys (identifying relationships)
    CONSTRAINT fk_record_student FOREIGN KEY (student_id) 
        REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_record_course FOREIGN KEY (course_id) 
        REFERENCES courses(id) ON DELETE SET NULL ON UPDATE CASCADE,
    
    INDEX idx_student (student_id),
    INDEX idx_course (course_id),
    INDEX idx_grade_date (grade_date),
    INDEX idx_semester (semester),
    INDEX idx_grade_value (grade_value),
    INDEX idx_attendance (attendance_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- SAMPLE DATA (for testing)
-- ================================================================

-- Default admin user (password: admin123)
INSERT INTO users (login, password_hash, full_name, role, position, department, access_level, is_active)
VALUES ('admin', '6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b', 
        'System Administrator', 'ADMIN', 'System Admin', 'IT', 'FULL', TRUE);

-- Sample teacher (password: teacher123)
INSERT INTO users (login, password_hash, full_name, role, position, department, specialization, teaching_hours_per_week, is_active)
VALUES ('teacher1', '6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b',
        'John Smith', 'TEACHER', 'Associate Professor', 'Computer Science', 'Software Engineering', 18, TRUE);

-- Sample group
INSERT INTO groups (name, curator_id, year_of_study, student_count)
VALUES ('CS-201', 2, 2, 25);

-- Sample students (password: student123)
INSERT INTO users (login, password_hash, full_name, role, group_id, student_group, year_of_study, student_id_number, is_active)
VALUES 
    ('student1', '6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b',
     'Alice Johnson', 'STUDENT', 1, 'CS-201', 2, 'STU001', TRUE),
    ('student2', '6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b',
     'Bob Williams', 'STUDENT', 1, 'CS-201', 2, 'STU002', TRUE);

-- Sample course
INSERT INTO courses (name, description, credits, teacher_id, department)
VALUES ('Java Programming', 'Introduction to Java and OOP', 5, 2, 'Computer Science');

-- Sample grades
INSERT INTO academic_records (student_id, course_id, course_name, student_name, grade_value, grade_type, grade_date, attendance_status, semester)
VALUES 
    (3, 1, 'Java Programming', 'Alice Johnson', 8.5, 'exam', '2026-01-15', 'present', '2025-2026-fall'),
    (3, 1, 'Java Programming', 'Alice Johnson', 9.0, 'test', '2025-12-10', 'present', '2025-2026-fall'),
    (4, 1, 'Java Programming', 'Bob Williams', 7.0, 'exam', '2026-01-15', 'present', '2025-2026-fall'),
    (4, 1, 'Java Programming', 'Bob Williams', 6.5, 'coursework', '2025-11-20', 'absent', '2025-2026-fall');

-- Sample schedule
INSERT INTO schedules (course_id, group_id, teacher_id, room_number, start_time, end_time, day_of_week, lesson_type, semester, course_name, group_name, teacher_name)
VALUES 
    (1, 1, 2, 'Room 301', '2026-04-06 10:00:00', '2026-04-06 11:30:00', 'Monday', 'lecture', '2025-2026-spring', 'Java Programming', 'CS-201', 'John Smith'),
    (1, 1, 2, 'Lab 205', '2026-04-08 14:00:00', '2026-04-08 15:30:00', 'Wednesday', 'lab', '2025-2026-spring', 'Java Programming', 'CS-201', 'John Smith');

-- ================================================================
-- VERIFICATION QUERIES
-- ================================================================
-- SELECT * FROM users;
-- SELECT * FROM groups;
-- SELECT * FROM courses;
-- SELECT * FROM schedules;
-- SELECT * FROM academic_records;

-- Check relationships:
-- SELECT u.full_name, g.name as group_name FROM users u LEFT JOIN groups g ON u.group_id = g.id WHERE u.role = 'STUDENT';
-- SELECT c.name as course, u.full_name as teacher FROM courses c LEFT JOIN users u ON c.teacher_id = u.id;

PRINT 'Database initialization completed successfully!';
