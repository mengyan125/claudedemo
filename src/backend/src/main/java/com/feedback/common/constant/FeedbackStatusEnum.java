package com.feedback.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 反馈状态枚举
 */
@Getter
@AllArgsConstructor
public enum FeedbackStatusEnum {

    /** 暂存 */
    DRAFT("draft", "暂存"),

    /** 已提交 */
    SUBMITTED("submitted", "已提交"),

    /** 已回复 */
    REPLIED("replied", "已回复");

    /** 状态编码 */
    private final String code;

    /** 状态描述 */
    private final String desc;
}
