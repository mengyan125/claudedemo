package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 班级列表项VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassItemVO {

    /** 班级ID */
    private Long id;

    /** 班级名称 */
    private String className;

    /** 年级ID */
    private Long gradeId;

    /** 年级名称 */
    private String gradeName;

    /** 排序序号 */
    private Integer sortOrder;
}
