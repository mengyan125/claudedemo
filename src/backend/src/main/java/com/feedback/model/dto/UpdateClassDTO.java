package com.feedback.model.dto;

import lombok.Data;

/**
 * 更新班级请求DTO
 */
@Data
public class UpdateClassDTO {

    /** 班级名称 */
    private String className;

    /** 年级ID */
    private Long gradeId;

    /** 排序序号 */
    private Integer sortOrder;
}
