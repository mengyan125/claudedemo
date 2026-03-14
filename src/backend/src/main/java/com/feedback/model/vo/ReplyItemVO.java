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

    /** 回复内容 */
    private String content;

    /** 回复人姓名 */
    private String replyUserName;

    /** 回复人类型：admin/student */
    private String replyUserType;

    /** 创建时间（格式：yyyy-MM-dd HH:mm:ss） */
    private String createTime;
}
