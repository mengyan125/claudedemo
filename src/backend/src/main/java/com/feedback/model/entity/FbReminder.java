package com.feedback.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 备注提醒实体
 */
@Data
@TableName("fb_reminder")
public class FbReminder {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 反馈ID */
    private Long feedbackId;

    /** 发送人ID */
    private Long senderId;

    /** 备注内容 */
    private String content;

    /** 创建时间 */
    private Date createTime;
}
