package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户列表项VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserItemVO {

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

    /** 状态 */
    private Integer status;

    /** 创建时间(格式化后的时间字符串) */
    private String createTime;
}
