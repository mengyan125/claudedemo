package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生分配信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAssignmentVO {

    /** 分配记录ID */
    private Long id;

    /** 学生ID */
    private Long studentId;

    /** 学生姓名 */
    private String studentName;

    /** 用户名 */
    private String username;

    /** 班级ID */
    private Long classId;

    /** 班级名称 */
    private String className;

    /** 分配时间 */
    private String assignTime;
}
