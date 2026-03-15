package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色下的用户项VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleItemVO {
    private Long userId;
    private String username;
    private String realName;
    private String userType;
}
