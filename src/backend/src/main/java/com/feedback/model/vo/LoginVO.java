package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginVO {

    /** JWT令牌 */
    private String token;

    /** 用户信息 */
    private UserInfoVO userInfo;
}
