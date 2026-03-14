package com.feedback.model.dto;

import lombok.Data;

/**
 * 更新快捷回复请求DTO
 * 所有字段均为可选，仅更新非null字段
 */
@Data
public class UpdateQuickReplyDTO {

    /** 回复内容 */
    private String content;

    /** 排序序号 */
    private Integer sortOrder;
}
