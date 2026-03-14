package com.feedback.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 反馈实体
 */
@Data
@TableName("fb_feedback")
public class FbFeedback {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 反馈类别ID */
    private Long categoryId;

    /** 学生用户ID */
    private Long studentId;

    /** 被反馈教师ID */
    private Long teacherId;

    /** 反馈主题 */
    private String title;

    /** 反馈内容 */
    private String content;

    /** 是否匿名：0=否 1=是 */
    private Integer isAnonymous;

    /** 状态：draft=草稿 submitted=已提交 replied=已回复 */
    private String status;

    /** 回复状态：unreplied=未回复 replied=已回复 */
    private String replyStatus;

    /** 是否有未读回复：0=否 1=是 */
    private Integer hasUnreadReply;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
