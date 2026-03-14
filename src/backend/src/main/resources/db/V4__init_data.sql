-- 注意：密码 123456 的 BCrypt 加密值
-- 默认管理员
INSERT INTO sys_user (username, password, real_name, user_type, status, school_name) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin', 1, '示范学校');

-- 教师用户
INSERT INTO sys_user (username, password, real_name, user_type, status, school_name) VALUES
('teacher1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张老师', 'teacher', 1, '示范学校'),
('teacher2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李老师', 'teacher', 1, '示范学校'),
('teacher3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王老师', 'teacher', 1, '示范学校');

-- 学生用户
INSERT INTO sys_user (username, password, real_name, user_type, status, school_name) VALUES
('student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵同学', 'student', 1, '示范学校'),
('student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '钱同学', 'student', 1, '示范学校');

-- 默认角色
INSERT INTO sys_role (role_name, role_code) VALUES
('系统管理员', 'SYSTEM_ADMIN'),
('角色管理员', 'ROLE_ADMIN'),
('类别管理员', 'CATEGORY_ADMIN');

-- 管理员角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 当前学期
INSERT INTO base_semester (semester_name, start_date, end_date, is_current) VALUES
('2025-2026学年第二学期', '2026-02-01', '2026-07-15', 1);

-- 年级
INSERT INTO base_grade (grade_name, sort_order) VALUES
('高一', 1),
('高二', 2),
('高三', 3);

-- 班级（grade_id: 高一=1, 高二=2, 高三=3）
INSERT INTO base_class (class_name, grade_id, sort_order) VALUES
('高一(1)班', 1, 1),
('高一(2)班', 1, 2),
('高二(1)班', 2, 1);

-- 学生-班级关联（student1=id5 -> 高一1班, student2=id6 -> 高一2班, 学期id=1）
INSERT INTO base_student_class (student_id, class_id, semester_id) VALUES
(5, 1, 1),
(6, 2, 1);

-- 教师-班级任课关联（teacher1=id2, teacher2=id3, teacher3=id4, 学期id=1）
INSERT INTO base_teacher_class (teacher_id, class_id, semester_id, subject) VALUES
(2, 1, 1, '语文'),
(3, 1, 1, '数学'),
(4, 1, 1, '英语'),
(2, 2, 1, '语文'),
(3, 2, 1, '数学');

-- 反馈类别
INSERT INTO fb_category (name, is_teaching_related, sort_order, status, remark) VALUES
('教学质量', 1, 1, 1, '与教学相关的反馈'),
('校园管理', 0, 2, 1, '校园设施和管理相关'),
('后勤服务', 0, 3, 1, '食堂、宿舍等后勤相关');
