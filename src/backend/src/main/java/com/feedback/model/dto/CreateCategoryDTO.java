package com.feedback.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建反馈类别请求DTO
 */
@Data
public class CreateCategoryDTO {

    /** 类别名称 */
    @NotBlank(message = "类别名称不能为空")
    private String name;

    /** 是否教学相关 */
    @NotNull(message = "是否教学相关不能为空")
    private Boolean isTeachingRelated;

    /** 排序序号 */
    private Integer sortOrder = 0;

    /** 状态：0-禁用，1-启用 */
    private Integer status = 1;
}
