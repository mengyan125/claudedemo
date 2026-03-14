package com.feedback.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 教师班级关联实体
 */
@Data
@TableName("base_teacher_class")
public class BaseTeacherClass {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 教师ID */
    private Long teacherId;

    /** 班级ID */
    private Long classId;

    /** 学期ID */
    private Long semesterId;

    /** 科目 */
    private String subject;

    /** 创建时间 */
    private Date createTime;
}
