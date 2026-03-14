package com.feedback.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 反馈收藏实体
 */
@Data
@TableName("fb_collection")
public class FbCollection {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 反馈ID */
    private Long feedbackId;

    /** 创建时间 */
    private Date createTime;
}
