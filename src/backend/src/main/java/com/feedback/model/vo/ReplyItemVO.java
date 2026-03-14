package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 回复项VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyItemVO {

    /** 回复ID */
    private Long id;

    /** 回复人ID */
    private Long replyUserId;

    /** 回复人姓名 */
    private String replyUserName;

    /** 回复人类型：student/teacher/admin */
    private String userType;

    /** 回复内容 */
    private String content;

    /** 创建时间 */
    private String createTime;
}
