package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 快捷回复列表项VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickReplyItemVO {

    /** 回复ID */
    private Long id;

    /** 回复内容 */
    private String content;

    /** 排序序号 */
    private Integer sortOrder;

    /** 创建人姓名（联查sys_user表获取） */
    private String createUserName;

    /** 创建时间（格式化后的时间字符串） */
    private String createTime;
}
