package com.feedback.security;

import java.lang.annotation.*;

/**
 * 角色权限注解
 * 标注在 Controller 类或方法上，用于接口级别的权限控制
 * 方法级注解优先于类级注解
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRole {

    /**
     * 允许访问的角色编码列表，满足其一即可
     * 如 {"SYSTEM_ADMIN", "ROLE_ADMIN"}
     */
    String[] value() default {};

    /**
     * 允许访问的用户类型
     * 如 "student"，匹配则直接放行
     */
    String userType() default "";
}
