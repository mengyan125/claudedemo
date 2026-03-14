package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生端-教师选项VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherOptionVO {

    /** 教师用户ID */
    private Long id;

    /** 教师姓名 */
    private String name;

    /** 任教科目 */
    private String subject;
}
