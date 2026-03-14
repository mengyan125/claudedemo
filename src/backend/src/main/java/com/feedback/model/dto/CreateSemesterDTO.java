package com.feedback.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建学期请求DTO
 */
@Data
public class CreateSemesterDTO {

    /** 学期名称 */
    @NotBlank(message = "学期名称不能为空")
    private String semesterName;

    /** 开始日期 */
    @NotBlank(message = "开始日期不能为空")
    private String startDate;

    /** 结束日期 */
    @NotBlank(message = "结束日期不能为空")
    private String endDate;
}
