package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生端-反馈类别VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCategoryVO {

    /** 类别ID */
    private Long id;

    /** 类别名称 */
    private String name;

    /** 是否教学相关 */
    private Boolean isTeachingRelated;
}
