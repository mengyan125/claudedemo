package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 备注提醒项VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderItemVO {

    /** 提醒ID */
    private Long id;

    /** 反馈ID */
    private Long feedbackId;

    /** 反馈标题 */
    private String feedbackTitle;

    /** 发送人ID */
    private Long senderId;

    /** 发送人姓名 */
    private String senderName;

    /** 备注内容 */
    private String content;

    /** 是否已读 */
    private Boolean isRead;

    /** 创建时间 */
    private String createTime;
}
