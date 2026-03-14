package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 类别权限VO
 * 展示每个反馈类别对应的管理员列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPermissionVO {

    /** 类别ID */
    private Long categoryId;

    /** 类别名称 */
    private String categoryName;

    /** 被授权的管理员用户ID列表 */
    private List<Long> adminIds;
}
