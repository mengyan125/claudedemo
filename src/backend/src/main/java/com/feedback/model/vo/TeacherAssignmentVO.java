package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教师分配信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssignmentVO {

    /** 分配记录ID */
    private Long id;

    /** 教师ID */
    private Long teacherId;

    /** 教师姓名 */
    private String teacherName;

    /** 用户名 */
    private String username;

    /** 班级ID */
    private Long classId;

    /** 班级名称 */
    private String className;

    /** 科目 */
    private String subject;

    /** 分配时间 */
    private String assignTime;
}
