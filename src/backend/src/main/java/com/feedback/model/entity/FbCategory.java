package com.feedback.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 反馈类别实体
 */
@Data
@TableName("fb_category")
public class FbCategory {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 类别名称 */
    private String name;

    /** 是否教学相关：0-否，1-是 */
    private Integer isTeachingRelated;

    /** 排序序号 */
    private Integer sortOrder;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
