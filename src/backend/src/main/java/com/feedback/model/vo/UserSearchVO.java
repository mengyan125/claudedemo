package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户搜索结果VO（备注提醒选择人员用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchVO {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 用户类型 */
    private String userType;
}
