package com.feedback.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建年级请求DTO
 */
@Data
public class CreateGradeDTO {

    /** 年级名称 */
    @NotBlank(message = "年级名称不能为空")
    private String gradeName;

    /** 排序序号 */
    private Integer sortOrder = 0;
}
