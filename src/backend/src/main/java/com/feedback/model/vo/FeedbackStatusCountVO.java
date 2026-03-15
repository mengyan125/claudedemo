package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 反馈状态数量统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackStatusCountVO {

    /** 全部数量 */
    private Long totalCount;

    /** 已回复数量 */
    private Long repliedCount;

    /** 未回复数量 */
    private Long unrepliedCount;
}
