package com.feedback.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建班级请求DTO
 */
@Data
public class CreateClassDTO {

    /** 班级名称 */
    @NotBlank(message = "班级名称不能为空")
    private String className;

    /** 年级ID */
    @NotNull(message = "年级ID不能为空")
    private Long gradeId;

    /** 排序序号 */
    private Integer sortOrder = 0;
}
