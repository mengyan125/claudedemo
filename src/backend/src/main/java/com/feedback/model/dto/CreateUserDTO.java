package com.feedback.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建用户请求DTO
 */
@Data
public class CreateUserDTO {

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 真实姓名 */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 用户类型(student/teacher/role_admin) */
    @NotBlank(message = "用户类型不能为空")
    private String userType;
}
