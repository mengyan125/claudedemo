package com.feedback.model.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 分配学生请求DTO
 */
@Data
public class AssignStudentsDTO {

    /** 学生ID列表 */
    @NotEmpty(message = "学生ID列表不能为空")
    private List<Long> studentIds;
}
