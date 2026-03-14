package com.feedback.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 班级实体
 */
@Data
@TableName("base_class")
public class BaseClass {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 班级名称 */
    private String className;

    /** 年级ID */
    private Long gradeId;

    /** 排序序号 */
    private Integer sortOrder;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
