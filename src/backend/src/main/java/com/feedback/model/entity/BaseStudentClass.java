package com.feedback.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 学生班级关联实体
 */
@Data
@TableName("base_student_class")
public class BaseStudentClass {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学生ID */
    private Long studentId;

    /** 班级ID */
    private Long classId;

    /** 学期ID */
    private Long semesterId;

    /** 创建时间 */
    private Date createTime;
}
