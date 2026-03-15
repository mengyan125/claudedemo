package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员端-反馈列表项VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFeedbackItemVO {

    /** 反馈ID */
    private Long id;

    /** 反馈标题 */
    private String title;

    /** 反馈内容（截断为前30字） */
    private String content;

    /** 类别名称 */
    private String categoryName;

    /** 年级名称 */
    private String gradeName;

    /** 班级名称 */
    private String className;

    /** 被反馈教师姓名（可null） */
    private String teacherName;

    /** 学生姓名（匿名时返回"匿名"） */
    private String studentName;

    /** 是否匿名 */
    private Boolean isAnonymous;

    /** 状态：submitted/replied */
    private String status;

    /** 是否有未读回复 */
    private Boolean hasUnread;

    /** 是否有未读备注提醒 */
    private Boolean hasUnreadReminder;

    /** 当前管理员是否已收藏 */
    private Boolean isFavorited;

    /** 创建时间 */
    private String createTime;
}
