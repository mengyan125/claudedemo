package com.feedback.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 提醒接收人实体
 */
@Data
@TableName("fb_reminder_receiver")
public class FbReminderReceiver {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提醒ID */
    private Long reminderId;

    /** 接收人ID */
    private Long receiverId;

    /** 是否已读：0=未读 1=已读 */
    private Integer isRead;
}
