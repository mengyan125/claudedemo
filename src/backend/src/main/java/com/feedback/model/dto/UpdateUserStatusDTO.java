package com.feedback.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 更新用户状态请求DTO
 */
@Data
public class UpdateUserStatusDTO {

    /** 状态 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
