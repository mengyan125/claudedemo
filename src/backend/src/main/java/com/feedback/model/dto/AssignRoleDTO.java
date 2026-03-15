package com.feedback.model.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 分配/撤销角色DTO
 */
@Data
public class AssignRoleDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}
