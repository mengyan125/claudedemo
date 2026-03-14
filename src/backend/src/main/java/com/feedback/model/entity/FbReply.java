package com.feedback.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 反馈回复实体
 */
@Data
@TableName("fb_reply")
public class FbReply {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 反馈ID */
    private Long feedbackId;

    /** 回复人ID */
    private Long replyUserId;

    /** 回复内容 */
    private String content;

    /** 创建时间 */
    private Date createTime;
}
