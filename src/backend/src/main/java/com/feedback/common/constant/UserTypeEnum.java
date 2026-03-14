package com.feedback.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型枚举
 */
@Getter
@AllArgsConstructor
public enum UserTypeEnum {

    /** 学生 */
    STUDENT("student", "学生"),

    /** 教师 */
    TEACHER("teacher", "教师"),

    /** 管理员 */
    ADMIN("admin", "管理员");

    /** 类型编码 */
    private final String code;

    /** 类型描述 */
    private final String desc;
}
