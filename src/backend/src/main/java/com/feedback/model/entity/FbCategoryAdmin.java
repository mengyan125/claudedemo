package com.feedback.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 类别管理员关联实体
 * 记录哪些管理员可以管理哪些反馈类别
 */
@Data
@TableName("fb_category_admin")
public class FbCategoryAdmin {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 类别ID */
    private Long categoryId;

    /** 管理员用户ID */
    private Long userId;
}
