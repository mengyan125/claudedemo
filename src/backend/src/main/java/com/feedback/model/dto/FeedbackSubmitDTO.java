package com.feedback.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 反馈提交请求DTO
 */
@Data
public class FeedbackSubmitDTO {

    /** 反馈ID（有id时更新暂存，无id时新建） */
    private Long id;

    /** 反馈类别ID */
    @NotNull(message = "反馈类别不能为空")
    private Long categoryId;

    /** 被反馈教师ID（可选） */
    private Long teacherId;

    /** 反馈主题 */
    @NotBlank(message = "反馈主题不能为空")
    private String title;

    /** 反馈内容 */
    @NotBlank(message = "反馈内容不能为空")
    private String content;

    /** 是否匿名 */
    private Boolean isAnonymous;

    /** 状态：draft=草稿 submitted=已提交 */
    @NotBlank(message = "状态不能为空")
    private String status;

    /** 附件ID列表 */
    private List<Long> attachmentIds;
}
