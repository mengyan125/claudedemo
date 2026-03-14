package com.feedback.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色编码枚举
 */
@Getter
@AllArgsConstructor
public enum RoleCodeEnum {

    /** 系统管理员 */
    SYSTEM_ADMIN("SYSTEM_ADMIN", "系统管理员"),

    /** 角色管理员 */
    ROLE_ADMIN("ROLE_ADMIN", "角色管理员"),

    /** 类别管理员 */
    CATEGORY_ADMIN("CATEGORY_ADMIN", "类别管理员");

    /** 角色编码 */
    private final String code;

    /** 角色描述 */
    private final String desc;
}
