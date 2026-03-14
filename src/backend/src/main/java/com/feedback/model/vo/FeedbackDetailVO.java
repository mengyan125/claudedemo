package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 学生端-反馈详情VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDetailVO {

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

    /** 是否教学相关类别 */
    private Boolean isTeachingRelated;

    /** 反馈标题 */
    private String title;

    /** 反馈内容 */
    private String content;

    /** 是否匿名 */
    private Boolean isAnonymous;

    /** 状态 */
    private String status;

    /** 回复状态 */
    private String replyStatus;

    /** 创建时间 */
    private String createTime;

    /** 附件列表 */
    private List<AttachmentItemVO> attachments;

    /** 回复列表 */
    private List<ReplyItemVO> replies;
}
