package com.feedback.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建快捷回复请求DTO
 */
@Data
public class CreateQuickReplyDTO {

    /** 回复内容 */
    @NotBlank(message = "回复内容不能为空")
    private String content;

    /** 排序序号 */
    private Integer sortOrder = 0;
}
