package com.feedback.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 更新反馈类别状态请求DTO
 */
@Data
public class UpdateCategoryStatusDTO {

    /** 状态：0-禁用，1-启用 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
