package com.feedback.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 提交反馈请求DTO
 */
@Data
public class FeedbackSubmitDTO {

    /** 反馈ID（更新时传入，新建时为null） */
    private Long id;

    /** 类别ID */
    private Long categoryId;

    /** 被反馈教师ID（可为null） */
    private Long teacherId;

    /** 反馈标题 */
    private String title;

    /** 反馈内容 */
    private String content;

    /** 是否匿名 */
    private Boolean isAnonymous;

    /** 状态：draft=草稿 submitted=已提交 */
    private String status;

    /** 附件ID列表 */
    private List<Long> attachmentIds;
}
