package com.feedback.model.dto;

import lombok.Data;

/**
 * 更新反馈类别请求DTO
 * 所有字段均为可选，仅更新非null字段
 */
@Data
public class UpdateCategoryDTO {

    /** 类别名称 */
    private String name;

    /** 是否教学相关 */
    private Boolean isTeachingRelated;

    /** 排序序号 */
    private Integer sortOrder;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
