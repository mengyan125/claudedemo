package com.feedback.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 快捷回复实体
 */
@Data
@TableName("fb_quick_reply")
public class FbQuickReply {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 回复内容 */
    private String content;

    /** 创建人ID */
    private Long createUserId;

    /** 排序序号 */
    private Integer sortOrder;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
