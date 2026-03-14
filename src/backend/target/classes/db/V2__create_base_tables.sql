-- 学期表
CREATE TABLE IF NOT EXISTS base_semester (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    semester_name VARCHAR(50) NOT NULL COMMENT '学期名称',
    start_date DATE COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    is_current INT DEFAULT 0 COMMENT '是否当前学期：0=否 1=是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 年级表
CREATE TABLE IF NOT EXISTS base_grade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    grade_name VARCHAR(50) NOT NULL COMMENT '年级名称',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 班级表
CREATE TABLE IF NOT EXISTS base_class (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_name VARCHAR(50) NOT NULL COMMENT '班级名称',
    grade_id BIGINT NOT NULL COMMENT '年级ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_grade_id (grade_id)
);

-- 学生班级关联表
CREATE TABLE IF NOT EXISTS base_student_class (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '学生用户ID',
    class_id BIGINT NOT NULL COMMENT '班级ID',
    semester_id BIGINT NOT NULL COMMENT '学期ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sc_student_id (student_id),
    INDEX idx_sc_class_id (class_id),
    INDEX idx_sc_semester_id (semester_id)
);

-- 教师任课关联表
CREATE TABLE IF NOT EXISTS base_teacher_class (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT NOT NULL COMMENT '教师用户ID',
    class_id BIGINT NOT NULL COMMENT '班级ID',
    semester_id BIGINT NOT NULL COMMENT '学期ID',
    subject VARCHAR(50) DEFAULT '' COMMENT '科目',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tc_teacher_id (teacher_id),
    INDEX idx_tc_class_id (class_id),
    INDEX idx_tc_semester_id (semester_id)
);
