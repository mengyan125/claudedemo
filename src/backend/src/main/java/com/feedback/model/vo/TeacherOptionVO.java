package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教师选项VO（学生端选择被反馈教师）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherOptionVO {

    /** 教师ID */
    private Long id;

    /** 教师姓名 */
    private String name;

    /** 任教科目 */
    private String subject;
}
