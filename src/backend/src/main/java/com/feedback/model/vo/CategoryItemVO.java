package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 反馈类别列表项VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryItemVO {

    /** 类别ID */
    private Long id;

    /** 类别名称 */
    private String name;

    /** 是否教学相关 */
    private Boolean isTeachingRelated;

    /** 排序序号 */
    private Integer sortOrder;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 反馈数量（联查fb_feedback表计算） */
    private Long feedbackCount;
}
