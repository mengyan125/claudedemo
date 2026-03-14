package com.feedback.model.dto;

import lombok.Data;

/**
 * 更新年级请求DTO
 */
@Data
public class UpdateGradeDTO {

    /** 年级名称 */
    private String gradeName;

    /** 排序序号 */
    private Integer sortOrder;
}
