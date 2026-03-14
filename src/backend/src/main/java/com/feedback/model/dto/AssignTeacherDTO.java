package com.feedback.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 分配教师请求DTO
 */
@Data
public class AssignTeacherDTO {

    /** 教师ID */
    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    /** 科目 */
    @NotBlank(message = "科目不能为空")
    private String subject;
}
