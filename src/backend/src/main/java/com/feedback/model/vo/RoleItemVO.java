package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色列表项VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleItemVO {

    /** 角色ID */
    private Long id;

    /** 角色编码 */
    private String roleCode;

    /** 角色名称 */
    private String roleName;
}
