package com.feedback.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 反馈附件实体
 */
@Data
@TableName("fb_feedback_attachment")
public class FbFeedbackAttachment {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 反馈ID（上传时可为null） */
    private Long feedbackId;

    /** 文件名 */
    private String fileName;

    /** 文件路径 */
    private String filePath;

    /** 文件类型 */
    private String fileType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 创建时间 */
    private Date createTime;
}
