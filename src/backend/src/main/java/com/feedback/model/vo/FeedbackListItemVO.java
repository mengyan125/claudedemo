package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生端-反馈列表项VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackListItemVO {

    /** 反馈ID */
    private Long id;

    /** 类别ID */
    private Long categoryId;

    /** 类别名称 */
    private String categoryName;

    /** 被反馈教师ID */
    private Long teacherId;

    /** 教师姓名 */
    private String teacherName;

    /** 反馈标题 */
    private String title;

    /** 是否匿名 */
    private Boolean isAnonymous;

    /** 状态 */
    private String status;

    /** 回复状态 */
    private String replyStatus;

    /** 是否有未读回复 */
    private Boolean hasUnread;

    /** 创建时间 */
    private String createTime;
}
