package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理员端-反馈详情VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFeedbackDetailVO {

    /** 反馈ID */
    private Long id;

    /** 反馈标题 */
    private String title;

    /** 反馈内容（完整） */
    private String content;

    /** 类别ID */
    private Long categoryId;

    /** 类别名称 */
    private String categoryName;

    /** 是否教学相关 */
    private Boolean isTeachingRelated;

    /** 被反馈教师ID */
    private Long teacherId;

    /** 被反馈教师姓名 */
    private String teacherName;

    /** 学生姓名（匿名时返回"匿名"） */
    private String studentName;

    /** 是否匿名 */
    private Boolean isAnonymous;

    /** 状态 */
    private String status;

    /** 年级名称 */
    private String gradeName;

    /** 班级名称 */
    private String className;

    /** 当前管理员是否已收藏 */
    private Boolean isFavorited;

    /** 附件列表 */
    private List<AttachmentItemVO> attachments;

    /** 回复列表 */
    private List<ReplyItemVO> replies;

    /** 创建时间 */
    private String createTime;
}
