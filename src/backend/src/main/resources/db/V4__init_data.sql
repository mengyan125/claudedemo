-- 注意：密码 123456 的 BCrypt 加密值
-- 默认管理员
INSERT INTO sys_user (username, password, real_name, user_type, status, school_name) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin', 1, '示范学校');

-- 默认角色
INSERT INTO sys_role (role_name, role_code) VALUES
('系统管理员', 'SYSTEM_ADMIN'),
('角色管理员', 'ROLE_ADMIN'),
('类别管理员', 'CATEGORY_ADMIN');

-- 管理员角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
