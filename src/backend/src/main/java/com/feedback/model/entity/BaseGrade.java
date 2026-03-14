package com.feedback.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 年级实体
 */
@Data
@TableName("base_grade")
public class BaseGrade {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 年级名称 */
    private String gradeName;

    /** 排序序号 */
    private Integer sortOrder;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
