package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 反馈详情VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDetailVO {

    /** 反馈ID */
    private Long id;

    /** 反馈主题 */
    private String title;

    /** 反馈内容 */
    private String content;

    /** 类别ID */
    private Long categoryId;

    /** 类别名称 */
    private String categoryName;

    /** 被反馈教师ID */
    private Long teacherId;

    /** 被反馈教师姓名 */
    private String teacherName;

    /** 是否匿名 */
    private Boolean isAnonymous;

    /** 状态：draft/submitted/replied */
    private String status;

    /** 附件列表 */
    private List<AttachmentItemVO> attachments;

    /** 回复列表 */
    private List<ReplyItemVO> replies;

    /** 创建时间（格式：yyyy-MM-dd HH:mm:ss） */
    private String createTime;
}
