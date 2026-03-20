package com.feedback.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 管理员反馈已读记录实体
 */
@Data
@TableName("fb_feedback_admin_read")
public class FbFeedbackAdminRead {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 反馈ID */
    private Long feedbackId;

    /** 管理员用户ID */
    private Long userId;

    /** 已读时间 */
    private Date readTime;
}
