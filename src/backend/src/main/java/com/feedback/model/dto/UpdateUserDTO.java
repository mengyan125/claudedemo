package com.feedback.model.dto;

import lombok.Data;

/**
 * 更新用户请求DTO
 */
@Data
public class UpdateUserDTO {

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 密码 */
    private String password;

    /** 用户类型(student/teacher) */
    private String userType;
}
