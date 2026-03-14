-- 反馈类别表
CREATE TABLE IF NOT EXISTS fb_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL COMMENT '类别名称',
    is_teaching_related INT DEFAULT 0 COMMENT '是否教学相关：0=否 1=是',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status INT DEFAULT 1 COMMENT '状态：1=启用 0=停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 类别管理员权限表
CREATE TABLE IF NOT EXISTS fb_category_admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL COMMENT '类别ID',
    user_id BIGINT NOT NULL COMMENT '管理员用户ID',
    INDEX idx_ca_category_id (category_id),
    INDEX idx_ca_user_id (user_id)
);

-- 反馈主表
CREATE TABLE IF NOT EXISTS fb_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL COMMENT '反馈类别ID',
    student_id BIGINT NOT NULL COMMENT '学生用户ID',
    teacher_id BIGINT COMMENT '被反馈教师ID',
    title VARCHAR(50) NOT NULL COMMENT '反馈主题',
    content VARCHAR(1000) NOT NULL COMMENT '反馈内容',
    is_anonymous INT DEFAULT 0 COMMENT '是否匿名：0=实名 1=匿名',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft/submitted/replied',
    reply_status VARCHAR(20) DEFAULT 'unreplied' COMMENT '回复状态：unreplied/replied',
    has_unread_reply INT DEFAULT 0 COMMENT '是否有未读回复：0=否 1=是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_fb_category_id (category_id),
    INDEX idx_fb_student_id (student_id),
    INDEX idx_fb_teacher_id (teacher_id),
    INDEX idx_fb_status (status)
);

-- 反馈附件表
CREATE TABLE IF NOT EXISTS fb_feedback_attachment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feedback_id BIGINT COMMENT '反馈ID（上传时可为null）',
    file_name VARCHAR(200) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_type VARCHAR(50) COMMENT '文件类型',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_fa_feedback_id (feedback_id)
);

-- 反馈回复表
CREATE TABLE IF NOT EXISTS fb_reply (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feedback_id BIGINT NOT NULL COMMENT '反馈ID',
    reply_user_id BIGINT NOT NULL COMMENT '回复用户ID',
    content VARCHAR(1000) NOT NULL COMMENT '回复内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_rp_feedback_id (feedback_id)
);

-- 收藏表
CREATE TABLE IF NOT EXISTS fb_collection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    feedback_id BIGINT NOT NULL COMMENT '反馈ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cl_user_id (user_id),
    INDEX idx_cl_feedback_id (feedback_id)
);

-- 备注提醒表
CREATE TABLE IF NOT EXISTS fb_reminder (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feedback_id BIGINT NOT NULL COMMENT '反馈ID',
    sender_id BIGINT NOT NULL COMMENT '发送人ID',
    content VARCHAR(1000) NOT NULL COMMENT '备注内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_rm_feedback_id (feedback_id)
);

-- 提醒接收人表
CREATE TABLE IF NOT EXISTS fb_reminder_receiver (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reminder_id BIGINT NOT NULL COMMENT '提醒ID',
    receiver_id BIGINT NOT NULL COMMENT '接收人ID',
    is_read INT DEFAULT 0 COMMENT '是否已读：0=未读 1=已读',
    INDEX idx_rr_reminder_id (reminder_id),
    INDEX idx_rr_receiver_id (receiver_id)
);

-- 快捷回复模板表
CREATE TABLE IF NOT EXISTS fb_quick_reply (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(500) NOT NULL COMMENT '回复内容',
    create_user_id BIGINT NOT NULL COMMENT '创建人ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
