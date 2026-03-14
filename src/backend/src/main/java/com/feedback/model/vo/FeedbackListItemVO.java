package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 反馈列表项VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackListItemVO {

    /** 反馈ID */
    private Long id;

    /** 反馈主题 */
    private String title;

    /** 类别名称 */
    private String categoryName;

    /** 被反馈教师姓名 */
    private String teacherName;

    /** 状态：draft/submitted/replied */
    private String status;

    /** 是否有未读回复 */
    private Boolean hasUnread;

    /** 创建时间（格式：yyyy-MM-dd HH:mm:ss） */
    private String createTime;
}
