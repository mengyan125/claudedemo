-- 管理员反馈已读记录表（按用户隔离已读状态）
CREATE TABLE IF NOT EXISTS fb_feedback_admin_read (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feedback_id BIGINT NOT NULL COMMENT '反馈ID',
    user_id BIGINT NOT NULL COMMENT '管理员用户ID',
    read_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '已读时间',
    UNIQUE KEY uk_far_feedback_user (feedback_id, user_id),
    INDEX idx_far_user_id (user_id),
    INDEX idx_far_feedback_id (feedback_id)
);
