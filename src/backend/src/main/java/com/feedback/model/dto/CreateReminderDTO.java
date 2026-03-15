package com.feedback.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 发送备注提醒DTO
 */
@Data
public class CreateReminderDTO {

    /** 备注内容 */
    @NotBlank(message = "备注内容不能为空")
    @Size(max = 500, message = "备注内容不能超过500字")
    private String content;

    /** 接收人ID列表（可选，不选则关联人员都可见） */
    private List<Long> receiverIds;
}
