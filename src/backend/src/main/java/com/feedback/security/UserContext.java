package com.feedback.security;

import com.feedback.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 当前用户上下文
 * 使用 ThreadLocal 存储当前请求的用户信息，保证线程安全
 */
public class UserContext {

    /** 线程本地变量，存储当前用户信息 */
    private static final ThreadLocal<UserInfo> CURRENT_USER = new ThreadLocal<>();

    /**
     * 设置当前用户信息
     *
     * @param user 用户信息
     */
    public static void setCurrentUser(UserInfo user) {
        CURRENT_USER.set(user);
    }

    /**
     * 获取当前用户信息
     * 未登录时抛出业务异常
     *
     * @return 当前用户信息
     * @throws BusinessException 未登录时抛出
     */
    public static UserInfo getCurrentUser() {
        UserInfo user = CURRENT_USER.get();
        if (user == null) {
            throw new BusinessException(401, "未登录");
        }
        return user;
    }

    /**
     * 获取当前用户ID
     *
     * @return 当前用户ID
     * @throws BusinessException 未登录时抛出
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * 清理 ThreadLocal，防止内存泄漏
     * 应在请求结束时调用
     */
    public static void clear() {
        CURRENT_USER.remove();
    }

    /**
     * 用户信息内部类
     * 存储当前登录用户的基本信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {

        /** 用户ID */
        private Long id;

        /** 用户名 */
        private String username;

        /** 真实姓名 */
        private String realName;

        /** 用户类型 */
        private String userType;

        /** 角色列表 */
        private List<String> roles;
    }
}
